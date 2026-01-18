package cn.edu.ccst.sims.controller;

import cn.edu.ccst.sims.common.Result;
import cn.edu.ccst.sims.dto.ReviewDTO;
import cn.edu.ccst.sims.entity.SysUser;
import cn.edu.ccst.sims.mapper.SysUserMapper;
import cn.edu.ccst.sims.service.TbReviewService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "评价管理")
@RestController
@RequestMapping("/api/review")
public class TbReviewController {

    @Autowired
    private TbReviewService reviewService;

    @Autowired
    private SysUserMapper userMapper;

    @Operation(summary = "发表评价")
    @PostMapping("/create")
    public Result<String> createReview(@AuthenticationPrincipal Long userId,
            @Valid @RequestBody ReviewDTO reviewDTO) {
        try {
            if (userId == null) {
                return Result.error(401, "未登录");
            }
            reviewService.createReview(userId, reviewDTO);
            return Result.success("评价发表成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "获取场馆评价列表")
    @GetMapping("/venue/{venueId}")
    public Result<Map<String, Object>> getVenueReviews(
            @Parameter(description = "场馆ID") @PathVariable Long venueId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "排序方式") @RequestParam(defaultValue = "latest") String sortBy) {
        try {
            Map<String, Object> result = reviewService.getVenueReviews(venueId, pageNum, pageSize, sortBy);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "获取场馆评价统计")
    @GetMapping("/venue/{venueId}/stats")
    public Result<Map<String, Object>> getVenueReviewStats(@PathVariable Long venueId) {
        try {
            return reviewService.getVenueReviewStats(venueId);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "获取我的评价")
    @GetMapping("/my")
    public Result<Map<String, Object>> getMyReviews(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            if (userId == null) {
                return Result.error(401, "未登录");
            }
            Map<String, Object> result = reviewService.getUserReviews(userId, pageNum, pageSize);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "管理员获取所有评价")
    @GetMapping("/admin/list")
    public Result<Map<String, Object>> getAllReviews(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "审核状态") @RequestParam(required = false) Integer auditStatus,
            @Parameter(description = "场馆名称") @RequestParam(required = false) String venueName) {
        try {
            Map<String, Object> result = reviewService.getAllReviews(pageNum, pageSize, auditStatus, venueName);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "管理员审核评价")
    @PutMapping("/admin/audit/{id}")
    public Result<String> auditReview(
            @Parameter(description = "评价ID") @PathVariable Long id,
            @Parameter(description = "审核状态") @RequestParam Integer auditStatus,
            @AuthenticationPrincipal Long userId) {
        try {
            if (userId == null) {
                return Result.error(401, "未登录");
            }

            boolean result = reviewService.auditReview(id, auditStatus, userId);
            if (result) {
                return Result.success("审核完成");
            } else {
                return Result.error("审核失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "删除评价")
    @DeleteMapping("/{id}")
    public Result<String> deleteReview(
            @Parameter(description = "评价ID") @PathVariable Long id,
            @AuthenticationPrincipal Long userId) {
        try {
            if (userId == null) {
                return Result.error(401, "未登录");
            }
            boolean result = reviewService.deleteReview(id, userId);
            if (result) {
                return Result.success("删除成功");
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // 兼容旧接口
    @PostMapping("/reviews")
    public Result<Void> submitReview(@AuthenticationPrincipal Long user,
            @RequestParam String orderNo,
            @RequestParam String content,
            @RequestParam Integer rating) {
        return reviewService.submitReview(user, orderNo, content, rating);
    }

    @GetMapping("/reviews/{orderNo}")
    public Result<List<String>> getReviewsByOrderNo(@PathVariable String orderNo) {
        return reviewService.getReviewsByOrderNo(orderNo);
    }
}
