package cn.edu.ccst.sims.controller;

import cn.edu.ccst.sims.common.Result;
import cn.edu.ccst.sims.vo.BookingApprovalVO;
import cn.edu.ccst.sims.vo.RentalApprovalVO;
import cn.edu.ccst.sims.service.ApprovalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/approval")
public class ApprovalController {

    @Autowired
    private ApprovalService approvalService;

    // 获取待审批预约列表
    @GetMapping("/bookings")
    public Result<List<BookingApprovalVO>> getBookings() {
        return Result.success(approvalService.getPendingBookings());
    }

    // 获取待审批器材列表
    @GetMapping("/rentals")
    public Result<List<RentalApprovalVO>> getRentals() {
        return Result.success(approvalService.getPendingRentals());
    }

    // 审核操作
    // POST /api/admin/approval/audit
    // Body: { "type": "booking"|"rental", "id": 1, "pass": true|false }
    @PostMapping("/audit")
    public Result<String> audit(@RequestBody Map<String, Object> params) {
        String type = (String) params.get("type");
        Long id = Long.valueOf(params.get("id").toString());
        Boolean pass = (Boolean) params.get("pass");

        if ("booking".equals(type)) {
            approvalService.auditBooking(id, pass);
        } else if ("rental".equals(type)) {
            approvalService.auditRental(id, pass);
        } else {
            return Result.error("未知类型");
        }
        return Result.success("操作成功");
    }
}