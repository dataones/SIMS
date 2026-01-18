package cn.edu.ccst.sims.controller;

import cn.edu.ccst.sims.common.Result;
import cn.edu.ccst.sims.dto.OrderSettlementDTO;
import cn.edu.ccst.sims.entity.TbOrder;
import cn.edu.ccst.sims.entity.SysUser;
import cn.edu.ccst.sims.mapper.SysUserMapper;
import cn.edu.ccst.sims.vo.OrderVO;
import cn.edu.ccst.sims.service.TbOrderSettlementService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Tag(name = "订单结算管理")
@RestController
@RequestMapping("/api/settlement")
public class TbOrderSettlementController {

    @Autowired
    private TbOrderSettlementService orderService;

    @Autowired
    private SysUserMapper userMapper;

    @Operation(summary = "计算订单费用")
    @PostMapping("/calculate")
    public Result<Map<String, Object>> calculateSettlement(@Valid @RequestBody OrderSettlementDTO settlementDTO) {
        try {
            Map<String, Object> result = orderService.calculateSettlement(settlementDTO);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "应用优惠")
    @PostMapping("/discount")
    public Result<Map<String, Object>> applyDiscount(
            @RequestParam String orderNo,
            @RequestParam String discountCode) {
        try {
            Map<String, Object> result = orderService.applyDiscount(orderNo, discountCode);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "用户支付订单")
    @PostMapping("/{orderNo}/pay")
    public Result<Void> pay(@AuthenticationPrincipal Long userId,
            @PathVariable String orderNo) {
        try {
            if (userId == null) {
                return Result.error(401, "未登录");
            }
            return orderService.payOrder(userId, orderNo);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "申请退款")
    @PostMapping("/{orderNo}/refund-request")
    public Result<String> requestRefund(
            @AuthenticationPrincipal Long userId,
            @PathVariable String orderNo,
            @RequestParam String reason) {
        try {
            if (userId == null) {
                return Result.error(401, "未登录");
            }
            orderService.requestRefund(userId, orderNo, reason);
            return Result.success("退款申请提交成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "管理员处理退款")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{orderNo}/refund")
    public Result<String> processRefund(
            @PathVariable String orderNo,
            @RequestParam BigDecimal amount,
            @RequestParam String adminRemark) {
        try {
            orderService.processRefund(orderNo, amount, adminRemark);
            return Result.success("退款处理成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "管理员确认订单完成")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{orderNo}/finish")
    public Result<String> finish(@PathVariable String orderNo) {
        try {
            orderService.finishOrder(orderNo);
            return Result.success("订单完成确认成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "查询订单详情")
    @GetMapping("/{orderNo}")
    public Result<TbOrder> detail(@AuthenticationPrincipal Long userId,
            @PathVariable String orderNo) {
        try {
            return orderService.getOrderDetail(userId, orderNo);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "查询订单列表")
    @GetMapping
    public Result<Page<OrderVO>> listOrders(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "订单类型") @RequestParam(required = false) Integer type,
            @Parameter(description = "订单状态") @RequestParam(required = false) Integer status) {
        try {
            Page<OrderVO> page = orderService.listOrders(userId, pageNum, pageSize, type, status);
            return Result.success(page);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "管理员获取所有订单")
    @GetMapping("/admin/list")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Page<OrderVO>> adminListOrders(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "订单类型") @RequestParam(required = false) Integer type,
            @Parameter(description = "订单状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "用户名") @RequestParam(required = false) String username) {
        try {
            Page<OrderVO> page = orderService.adminListOrders(pageNum, pageSize, type, status, username);
            return Result.success(page);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "获取结算详情")
    @GetMapping("/{orderNo}/settlement")
    public Result<Map<String, Object>> getSettlementDetail(
            @AuthenticationPrincipal Long userId,
            @PathVariable String orderNo) {
        try {
            if (userId == null) {
                return Result.error(401, "未登录");
            }
            Map<String, Object> detail = orderService.getSettlementDetail(userId, orderNo);
            return Result.success(detail);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "删除订单")
    @DeleteMapping("/{orderNo}")
    public Result<Void> deleteOrder(@AuthenticationPrincipal Long userId,
            @PathVariable String orderNo) {
        try {
            if (userId == null) {
                return Result.error(401, "未登录");
            }
            orderService.deleteOrderByOrderNo(orderNo, userId);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
