package cn.edu.ccst.sims.service;

import cn.edu.ccst.sims.common.Result;
import cn.edu.ccst.sims.entity.TbOrder;
import cn.edu.ccst.sims.ov.OrderVO;

import java.math.BigDecimal;
import java.util.List;

public interface TbOrderSettlementService {

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
    //订单列表
    Result<List<OrderVO>> listOrders(Long userId, Integer type);
}
