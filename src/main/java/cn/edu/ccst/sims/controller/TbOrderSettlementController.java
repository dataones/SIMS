package cn.edu.ccst.sims.controller;

import cn.edu.ccst.sims.common.Result;
import cn.edu.ccst.sims.entity.TbOrder;
import cn.edu.ccst.sims.ov.OrderVO;
import cn.edu.ccst.sims.service.TbOrderSettlementService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class TbOrderSettlementController {

    private final TbOrderSettlementService orderService;

    public TbOrderSettlementController(TbOrderSettlementService orderService) {
        this.orderService = orderService;
    }

    /**
     * 用户支付订单
     */
    @PostMapping("/{orderNo}/pay")
    public Result<Void> pay(@AuthenticationPrincipal Long userId,
                            @PathVariable String orderNo) {
        return orderService.payOrder(userId, orderNo);
    }

    /**
     * 管理员确认订单完成（可评价）
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{orderNo}/finish")
    public Result<Void> finish(@PathVariable String orderNo) {
        return orderService.finishOrder(orderNo);
    }

    /**
     * 管理员退款
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{orderNo}/refund")
    public Result<Void> refund(@PathVariable String orderNo,
                               @RequestParam BigDecimal amount) {
        return orderService.refundOrder(orderNo, amount);
    }
    /** 查询订单详情（用户/管理员） */
    @GetMapping("/{orderNo}")
    public Result<TbOrder> detail(@AuthenticationPrincipal Long userId,
                                  @PathVariable String orderNo) {
        return orderService.getOrderDetail(userId, orderNo);
    }
    /**
     * 查询所有订单（管理员/用户）
     */
    /**
     * 查询订单列表（管理员 / 用户）
     * type: 1-场馆预约  2-器材租赁
     */
    @GetMapping
    public Result<List<OrderVO>> listOrders(@AuthenticationPrincipal Long userId,
                                            @RequestParam(required = false) Integer type) {
        return orderService.listOrders(userId, type);
    }


}
