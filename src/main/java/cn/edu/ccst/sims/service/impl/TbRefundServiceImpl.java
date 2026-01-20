package cn.edu.ccst.sims.service.impl;

import cn.edu.ccst.sims.entity.TbOrder;
import cn.edu.ccst.sims.entity.TbRefund;
import cn.edu.ccst.sims.entity.SysUser;
import cn.edu.ccst.sims.mapper.TbOrderMapper;
import cn.edu.ccst.sims.mapper.TbRefundMapper;
import cn.edu.ccst.sims.mapper.SysUserMapper;
import cn.edu.ccst.sims.service.TbRefundService;
import cn.edu.ccst.sims.vo.RefundVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 退款申请服务实现类
 */
@Slf4j
@Service
public class TbRefundServiceImpl extends ServiceImpl<TbRefundMapper, TbRefund> implements TbRefundService {

    @Resource
    private TbRefundMapper refundMapper;

    @Resource
    private TbOrderMapper orderMapper;

    @Resource
    private SysUserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean requestRefund(String orderNo, Long userId, BigDecimal refundAmount, String reason, String remark) {
        try {
            // 1. 检查订单是否存在且属于该用户
            QueryWrapper<TbOrder> orderQuery = new QueryWrapper<>();
            orderQuery.eq("order_no", orderNo).eq("user_id", userId);
            TbOrder order = orderMapper.selectOne(orderQuery);

            if (order == null) {
                log.error("订单不存在: {}", orderNo);
                return false;
            }

            // 2. 检查订单状态（只有已支付的订单才能申请退款）
            if (order.getStatus() != 1) {
                log.error("订单状态不允许退款: {}, status: {}", orderNo, order.getStatus());
                return false;
            }

            // 3. 检查是否已经申请过退款
            QueryWrapper<TbRefund> refundQuery = new QueryWrapper<>();
            refundQuery.eq("order_no", orderNo).in("status", 0, 1, 3); // 待审核、已批准、退款完成
            TbRefund existingRefund = refundMapper.selectOne(refundQuery);

            if (existingRefund != null) {
                log.error("订单已申请退款: {}", orderNo);
                return false;
            }

            // 4. 检查退款金额是否合理
            if (refundAmount.compareTo(order.getAmount()) > 0) {
                log.error("退款金额超过订单金额: refundAmount={}, orderAmount={}", refundAmount, order.getAmount());
                return false;
            }

            // 5. 创建退款申请
            TbRefund refund = new TbRefund();
            refund.setOrderNo(orderNo);
            refund.setUserId(userId);
            refund.setOrderType(order.getType());
            refund.setRefundAmount(refundAmount);
            refund.setReason(reason);
            refund.setRemark(remark);
            refund.setStatus(0); // 待审核
            refund.setCreateTime(LocalDateTime.now());

            int result = refundMapper.insert(refund);
            log.info("退款申请创建成功: orderNo={}, refundId={}", orderNo, refund.getId());

            return result > 0;

        } catch (Exception e) {
            log.error("申请退款失败: orderNo={}", orderNo, e);
            throw new RuntimeException("申请退款失败", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean auditRefund(Long refundId, Long auditUserId, boolean approved, String auditRemark) {
        try {
            TbRefund refund = refundMapper.selectById(refundId);
            if (refund == null) {
                log.error("退款申请不存在: {}", refundId);
                return false;
            }

            if (refund.getStatus() != 0) {
                log.error("退款申请状态不允许审核: {}, status: {}", refundId, refund.getStatus());
                return false;
            }

            // 更新审核状态
            refund.setStatus(approved ? 1 : 2); // 1-已批准, 2-已驳回
            refund.setAuditUserId(auditUserId);
            refund.setAuditTime(LocalDateTime.now());
            refund.setAuditRemark(auditRemark);
            refund.setUpdateTime(LocalDateTime.now());

            int result = refundMapper.updateById(refund);

            if (approved) {
                // 如果批准，立即完成退款流程
                completeRefund(refundId);
            }

            log.info("退款审核完成: refundId={}, approved={}", refundId, approved);
            return result > 0;

        } catch (Exception e) {
            log.error("审核退款失败: refundId={}", refundId, e);
            throw new RuntimeException("审核退款失败", e);
        }
    }

    @Override
    public List<RefundVO> getPendingRefunds() {
        try {
            QueryWrapper<TbRefund> query = new QueryWrapper<>();
            query.eq("status", 0) // 待审核
                    .orderByDesc("create_time");

            List<TbRefund> refunds = refundMapper.selectList(query);

            return refunds.stream().map(this::convertToVO).collect(Collectors.toList());

        } catch (Exception e) {
            log.error("获取待审核退款列表失败", e);
            return List.of();
        }
    }

    @Override
    public Page<RefundVO> getUserRefunds(Long userId, Page<RefundVO> page) {
        try {
            QueryWrapper<TbRefund> query = new QueryWrapper<>();
            query.eq("user_id", userId)
                    .orderByDesc("create_time");

            Page<TbRefund> refundPage = refundMapper.selectPage(new Page<>(page.getCurrent(), page.getSize()), query);

            Page<RefundVO> voPage = new Page<>();
            voPage.setCurrent(refundPage.getCurrent());
            voPage.setSize(refundPage.getSize());
            voPage.setTotal(refundPage.getTotal());
            voPage.setRecords(refundPage.getRecords().stream().map(this::convertToVO).collect(Collectors.toList()));

            return voPage;

        } catch (Exception e) {
            log.error("获取用户退款记录失败: userId={}", userId, e);
            return new Page<>();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean completeRefund(Long refundId) {
        try {
            TbRefund refund = refundMapper.selectById(refundId);
            if (refund == null || refund.getStatus() != 1) {
                log.error("退款申请状态不允许完成退款: {}, status: {}", refundId, refund != null ? refund.getStatus() : null);
                return false;
            }

            // 1. 获取订单信息
            QueryWrapper<TbOrder> orderQuery = new QueryWrapper<>();
            orderQuery.eq("order_no", refund.getOrderNo());
            TbOrder order = orderMapper.selectOne(orderQuery);

            if (order == null) {
                log.error("订单不存在: {}", refund.getOrderNo());
                return false;
            }

            // 2. 更新用户余额
            QueryWrapper<SysUser> userQuery = new QueryWrapper<>();
            userQuery.eq("id", refund.getUserId());
            SysUser user = userMapper.selectOne(userQuery);

            if (user != null) {
                BigDecimal newBalance = user.getBalance().add(refund.getRefundAmount());
                user.setBalance(newBalance);
                user.setUpdateTime(LocalDateTime.now());
                userMapper.updateById(user);
                log.info("用户余额更新成功: userId={}, oldBalance={}, newBalance={}",
                        user.getId(), user.getBalance().subtract(refund.getRefundAmount()), newBalance);
            }

            // 3. 更新订单状态为已退款
            order.setStatus(2); // 2-已退款
            order.setUpdateTime(LocalDateTime.now());
            orderMapper.updateById(order);

            // 4. 更新退款状态为已完成
            refund.setStatus(3); // 3-退款完成
            refund.setRefundTime(LocalDateTime.now());
            refund.setUpdateTime(LocalDateTime.now());
            refundMapper.updateById(refund);

            log.info("退款完成: refundId={}, orderNo={}, refundAmount={}",
                    refundId, refund.getOrderNo(), refund.getRefundAmount());

            return true;

        } catch (Exception e) {
            log.error("完成退款失败: refundId={}", refundId, e);
            throw new RuntimeException("完成退款失败", e);
        }
    }

    private RefundVO convertToVO(TbRefund refund) {
        RefundVO vo = new RefundVO();
        vo.setId(refund.getId());
        vo.setOrderNo(refund.getOrderNo());
        vo.setOrderType(refund.getOrderType());
        vo.setRefundAmount(refund.getRefundAmount());
        vo.setReason(refund.getReason());
        vo.setRemark(refund.getRemark());
        vo.setStatus(refund.getStatus());
        vo.setCreateTime(refund.getCreateTime());

        // 获取用户信息
        QueryWrapper<SysUser> userQuery = new QueryWrapper<>();
        userQuery.eq("id", refund.getUserId());
        SysUser user = userMapper.selectOne(userQuery);
        if (user != null) {
            vo.setUsername(user.getUsername());
            vo.setNickname(user.getNickname());
        }

        return vo;
    }
}
