package cn.edu.ccst.sims.service;

import cn.edu.ccst.sims.common.Result;
import cn.edu.ccst.sims.dto.OrderSettlementDTO;
import cn.edu.ccst.sims.entity.TbOrder;
import cn.edu.ccst.sims.vo.OrderVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface TbOrderSettlementService {

    // 原有方法
    // 创建订单并计算费用
    Result<Long> createOrder(Long userId,
            Integer type,
            Long relatedId,
            BigDecimal baseAmount);

    // 支付订单
    Result<Void> payOrder(Long userId, String orderNo);

    // 订单完成（用于评价）
    Result<Void> finishOrder(String orderNo);

    // 退款
    Result<Void> refundOrder(String orderNo, BigDecimal refundAmount);

    // 订单详情
    Result<TbOrder> getOrderDetail(Long userId, String orderNo);

    // 订单列表
    Result<List<OrderVO>> listOrders(Long userId, Integer type);

    // 新增方法
    /**
     * 计算订单费用
     * 
     * @param settlementDTO 结算信息
     * @return 费用详情
     */
    Map<String, Object> calculateSettlement(OrderSettlementDTO settlementDTO);

    /**
     * 应用优惠
     * 
     * @param orderNo      订单号
     * @param discountCode 优惠码
     * @return 优惠后费用
     */
    Map<String, Object> applyDiscount(String orderNo, String discountCode);

    /**
     * 申请退款
     * 
     * @param userId  用户ID
     * @param orderNo 订单号
     * @param reason  退款原因
     */
    void requestRefund(Long userId, String orderNo, String reason);

    /**
     * 管理员处理退款
     * 
     * @param orderNo     订单号
     * @param amount      退款金额
     * @param adminRemark 管理员备注
     */
    void processRefund(String orderNo, BigDecimal amount, String adminRemark);

    /**
     * 获取订单列表（分页）
     * 
     * @param userId   用户ID
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @param type     订单类型
     * @param status   订单状态
     * @return 订单列表
     */
    Page<OrderVO> listOrders(Long userId, Integer pageNum, Integer pageSize, Integer type, Integer status);

    /**
     * 管理员获取所有订单
     * 
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @param type     订单类型
     * @param status   订单状态
     * @param username 用户名
     * @return 订单列表
     */
    Page<OrderVO> adminListOrders(Integer pageNum, Integer pageSize, Integer type, Integer status, String username);

    /**
     * 获取结算详情
     * 
     * @param userId  用户ID
     * @param orderNo 订单号
     * @return 结算详情
     */
    Map<String, Object> getSettlementDetail(Long userId, String orderNo);

    /**
     * 根据订单号删除订单
     * 
     * @param orderNo 订单号
     * @param userId  用户ID
     */
    void deleteOrderByOrderNo(String orderNo, Long userId);

    /**
     * 自动取消未支付订单
     * 
     * @param minutes 超时分钟数
     * @return 取消的订单数量
     */
    int cancelUnpaidOrders(int minutes);
}
