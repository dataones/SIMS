package cn.edu.ccst.sims.service;

import cn.edu.ccst.sims.entity.TbRefund;
import cn.edu.ccst.sims.vo.RefundVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;
import java.util.List;

/**
 * 退款申请服务接口
 */
public interface TbRefundService extends IService<TbRefund> {

    /**
     * 用户申请退款
     */
    boolean requestRefund(String orderNo, Long userId, BigDecimal refundAmount, String reason, String remark);

    /**
     * 管理员审核退款
     */
    boolean auditRefund(Long refundId, Long auditUserId, boolean approved, String auditRemark);

    /**
     * 获取待审核退款列表
     */
    List<RefundVO> getPendingRefunds();

    /**
     * 获取用户退款记录
     */
    Page<RefundVO> getUserRefunds(Long userId, Page<RefundVO> page);

    /**
     * 完成退款（更新用户余额和订单状态）
     */
    boolean completeRefund(Long refundId);
}
