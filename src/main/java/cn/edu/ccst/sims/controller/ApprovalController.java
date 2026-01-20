package cn.edu.ccst.sims.controller;

import cn.edu.ccst.sims.common.Result;
import cn.edu.ccst.sims.vo.BookingApprovalVO;
import cn.edu.ccst.sims.vo.RentalApprovalVO;
import cn.edu.ccst.sims.vo.RefundVO;
import cn.edu.ccst.sims.service.ApprovalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 审批管理控制器
 * 
 * 负责处理系统中的各类审批业务，包括：
 * 1. 场馆预约审批 - 管理员审核用户预约申请
 * 2. 器材租赁审批 - 管理员审核器材借用申请
 * 3. 退款申请审批 - 管理员审核用户退款请求
 * 
 * 审批流程：
 * 1. 用户提交申请 → 状态：待审批
 * 2. 管理员查看列表 → 状态：待审批
 * 3. 管理员审核操作 → 状态：已通过/已驳回
 * 4. 系统通知用户 → 状态更新
 * 
 * 权限控制：
 * - 需要管理员权限（role=2）
 * - 仅管理员可访问此控制器
 * - 审批操作记录日志
 * 
 * @author SIMS开发团队
 * @version 1.0.0
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/admin/approval")
public class ApprovalController {

    @Autowired
    private ApprovalService approvalService;

    /**
     * 获取待审批预约列表
     * 
     * 查询所有状态为"待审批"的场馆预约申请
     * 用于管理员审批界面显示待处理项目
     * 
     * @return 预约审批列表，包含预约详情和用户信息
     * 
     *         请求示例：
     *         GET /api/admin/approval/bookings
     * 
     *         响应示例：
     *         {
     *         "code": 200,
     *         "msg": "成功",
     *         "data": [
     *         {
     *         "id": 1,
     *         "orderNo": "BK20240101001",
     *         "venueName": "篮球场A",
     *         "date": "2024-01-01",
     *         "startTime": "14:00",
     *         "endTime": "16:00",
     *         "totalPrice": 200.00,
     *         "status": 0,
     *         "createTime": "2024-01-01 10:00:00"
     *         }
     *         ]
     *         }
     */
    @GetMapping("/bookings")
    public Result<List<BookingApprovalVO>> getBookings() {
        return Result.success(approvalService.getPendingBookings());
    }

    /**
     * 获取待审批器材列表
     * 
     * 查询所有状态为"申请中"的器材租赁申请
     * 用于管理员审批界面显示待处理的器材借用
     * 
     * @return 器材租赁审批列表，包含租赁详情和用户信息
     * 
     *         请求示例：
     *         GET /api/admin/approval/rentals
     * 
     *         响应示例：
     *         {
     *         "code": 200,
     *         "msg": "成功",
     *         "data": [
     *         {
     *         "id": 1,
     *         "orderNo": "EQ20240101001",
     *         "equipmentName": "篮球",
     *         "count": 2,
     *         "price": 50.00,
     *         "status": 1,
     *         "createTime": "2024-01-01 11:00:00"
     *         }
     *         ]
     *         }
     */
    @GetMapping("/rentals")
    public Result<List<RentalApprovalVO>> getRentals() {
        return Result.success(approvalService.getPendingRentals());
    }

    /**
     * 获取待审批退款列表
     * 
     * 查询所有状态为"待审核"的退款申请
     * 用于管理员审批界面显示待处理的退款请求
     * 
     * @return 退款审批列表，包含退款详情和订单信息
     * 
     *         请求示例：
     *         GET /api/admin/approval/refunds
     * 
     *         响应示例：
     *         {
     *         "code": 200,
     *         "msg": "成功",
     *         "data": [
     *         {
     *         "id": 1,
     *         "orderNo": "BK20240101001",
     *         "refundAmount": 200.00,
     *         "reason": "临时有事",
     *         "status": 0,
     *         "createTime": "2024-01-01 12:00:00"
     *         }
     *         ]
     *         }
     */
    @GetMapping("/refunds")
    public Result<List<RefundVO>> getRefunds() {
        return Result.success(approvalService.getPendingRefunds());
    }

    /**
     * 审核退款申请
     * 
     * 处理用户退款申请的审批操作
     * 管理员可以选择批准或驳回退款请求
     * 
     * @param id        退款申请ID
     * @param request   审核请求数据
     * @param {Boolean} request.approved - 审核结果：true-批准，false-驳回
     * @param {String}  request.auditRemark - 审核备注，说明审核原因
     * @return 审核结果
     * 
     *         请求示例：
     *         PUT /api/admin/approval/refund/1
     *         {
     *         "approved": true,
     *         "auditRemark": "用户原因合理，同意退款"
     *         }
     * 
     *         响应示例（批准）：
     *         {
     *         "code": 200,
     *         "msg": "退款已批准",
     *         "data": true
     *         }
     * 
     *         响应示例（驳回）：
     *         {
     *         "code": 200,
     *         "msg": "退款申请已驳回",
     *         "data": false
     *         }
     */
    @PutMapping("/refund/{id}")
    public Result<Boolean> auditRefund(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        try {
            // 解析审核参数
            Boolean approved = (Boolean) request.get("approved");
            String auditRemark = (String) request.get("auditRemark");

            // 调用服务层执行审核操作
            approvalService.auditRefund(id, approved != null && approved);

            // 构建响应结果
            Result<Boolean> result = Result.success(approved != null && approved);
            result.setMsg(approved != null && approved ? "退款已批准" : "退款申请已驳回");
            return result;

        } catch (Exception e) {
            // 记录审核失败日志
            System.err.println("退款审核失败，ID: " + id + ", 错误: " + e.getMessage());
            return Result.error("审核失败：" + e.getMessage());
        }
    }
}