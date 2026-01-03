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
}