package cn.edu.ccst.sims.controller;

import cn.edu.ccst.sims.common.Result;
import cn.edu.ccst.sims.entity.TbRefund;
import cn.edu.ccst.sims.service.TbRefundService;
import cn.edu.ccst.sims.vo.RefundVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

/**
 * 退款管理控制器
 */
@Tag(name = "退款管理")
@Slf4j
@RestController
@RequestMapping("/api/refund")
public class TbRefundController {

    @Resource
    private TbRefundService refundService;

    @Operation(summary = "用户申请退款")
    @PostMapping("/request")
    public Result<Boolean> requestRefund(@RequestBody RefundRequest request,
            @AuthenticationPrincipal Long userId) {
        try {
            // 检查用户是否已认证
            if (userId == null) {
                return Result.error("用户未登录，请先登录");
            }

            boolean success = refundService.requestRefund(
                    request.getOrderNo(),
                    userId,
                    request.getRefundAmount(),
                    request.getReason(),
                    request.getRemark());

            if (success) {
                Result<Boolean> result = Result.success(true);
                result.setMsg("退款申请提交成功，请等待审核");
                return result;

            } else {
                return Result.<Boolean>error("退款申请失败，请检查订单状态或是否已申请退款");
            }

        } catch (Exception e) {
            log.error("申请退款失败", e);
            return Result.error("系统异常，请稍后重试");
        }
    }

    @Operation(summary = "管理员审核退款")
    @PutMapping("/audit/{id}")
    public Result<Boolean> auditRefund(@PathVariable Long id,
            @RequestBody RefundAuditRequest request,
            @AuthenticationPrincipal Long auditUserId) {
        try {
            // 检查用户是否已认证
            if (auditUserId == null) {
                return Result.error("用户未登录，请先登录");
            }

            boolean success = refundService.auditRefund(
                    id,
                    auditUserId,
                    request.isApproved(),
                    request.getAuditRemark());

            if (success) {
                Result<Boolean> result = Result.success(true);
                result.setMsg(request.isApproved() ? "退款已批准" : "退款申请已驳回");
                return result;

            } else {
                return Result.<Boolean>error("审核失败，请检查退款申请状态");
            }

        } catch (Exception e) {
            log.error("审核退款失败", e);
            return Result.error("系统异常，请稍后重试");
        }
    }

    @Operation(summary = "获取待审核退款列表")
    @GetMapping("/admin/pending")
    public Result<List<RefundVO>> getPendingRefunds() {
        try {
            List<RefundVO> refunds = refundService.getPendingRefunds();
            return Result.success(refunds);
        } catch (Exception e) {
            log.error("获取待审核退款列表失败", e);
            return Result.error("获取数据失败");
        }
    }

    @Operation(summary = "获取用户退款记录")
    @GetMapping("/user/records")
    public Result<Page<RefundVO>> getUserRefunds(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @AuthenticationPrincipal Long userId) {
        try {
            // 检查用户是否已认证
            if (userId == null) {
                return Result.error("用户未登录，请先登录");
            }

            Page<RefundVO> page = new Page<>(current, size);
            Page<RefundVO> result = refundService.getUserRefunds(userId, page);
            return Result.success(result);

        } catch (Exception e) {
            log.error("获取用户退款记录失败", e);
            return Result.error("获取数据失败");
        }
    }

    // 请求参数类
    public static class RefundRequest {
        private String orderNo;
        private BigDecimal refundAmount;
        private String reason;
        private String remark;

        // getters and setters
        public String getOrderNo() {
            return orderNo;
        }

        public void setOrderNo(String orderNo) {
            this.orderNo = orderNo;
        }

        public BigDecimal getRefundAmount() {
            return refundAmount;
        }

        public void setRefundAmount(BigDecimal refundAmount) {
            this.refundAmount = refundAmount;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }
    }

    public static class RefundAuditRequest {
        private boolean approved;
        private String auditRemark;

        // getters and setters
        public boolean isApproved() {
            return approved;
        }

        public void setApproved(boolean approved) {
            this.approved = approved;
        }

        public String getAuditRemark() {
            return auditRemark;
        }

        public void setAuditRemark(String auditRemark) {
            this.auditRemark = auditRemark;
        }
    }
}
