package cn.edu.ccst.sims.controller;

import cn.edu.ccst.sims.common.Result;
import cn.edu.ccst.sims.dto.BookingDTO;
import cn.edu.ccst.sims.entity.SysUser;
import cn.edu.ccst.sims.mapper.SysUserMapper;
import cn.edu.ccst.sims.service.TbBookingService;
import cn.edu.ccst.sims.vo.BookingVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 场馆预约控制器
 * 作用：提供预约提交、修改、取消、审核、查询等完整接口
 * 用户认证：通过 @AuthenticationPrincipal 从 JWT Filter 获取当前用户
 */
@Tag(name = "预约管理")
@RestController
@RequestMapping("/api/booking")
@CrossOrigin
public class BookingController {

    @Autowired
    private TbBookingService bookingService;
    @Autowired
    private SysUserMapper userMapper;
    /**
     * 提交场馆预约
     */
    @Operation(summary = "提交场馆预约")
    @PostMapping("/submit")
    public Result<Long> submitBooking(@Valid @RequestBody BookingDTO bookingDTO,
                                      @AuthenticationPrincipal Long userId) {
        try {
            if (userId == null) {
                return Result.error(401, "未登录");
            }
            Long bookingId = bookingService.submitBooking(bookingDTO, userId);
            return Result.success(bookingId);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 修改场馆预约
     */
    @Operation(summary = "修改场馆预约")
    @PutMapping("/update")
    public Result<Void> updateBooking(@Valid @RequestBody BookingDTO bookingDTO,
                                      @AuthenticationPrincipal Long userId) {
        try {
            if (userId == null) {
                return Result.error(401, "未登录");
            }
            bookingService.updateBooking(bookingDTO, userId);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 取消预约
     */
    @Operation(summary = "取消预约")
    @PutMapping("/cancel/{id}")
    public Result<Void> cancelBooking(@Parameter(description = "预约ID") @PathVariable Long id,
                                      @AuthenticationPrincipal Long userId) {
        try {
            if (userId == null) {
                return Result.error(401, "未登录");
            }
            bookingService.cancelBooking(id, userId);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 管理员审核预约
     */
    @Operation(summary = "管理员审核预约")
    @PutMapping("/audit/{id}")
    public Result<Void> auditBooking(@PathVariable Long id,
                                     @RequestParam Integer status,
                                     @RequestParam(required = false) String remark,
                                     @AuthenticationPrincipal Long userId) {
        try {
            if (userId == null) {
                return Result.error(401, "未登录");
            }

            // 校验是否管理员
            SysUser admin = userMapper.selectById(userId);
            if (admin == null || admin.getRole() != 2) {
                return Result.error(403, "无管理员权限");
            }

            bookingService.auditBooking(id, status, remark);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 分页查询预约记录（管理员用）
     */
    @Operation(summary = "分页查询预约记录")
    @GetMapping("/page")
    public Result<IPage<BookingVO>> getBookingPage(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "场馆名称") @RequestParam(required = false) String venueName) {
        try {
            Page<cn.edu.ccst.sims.entity.TbBooking> page = new Page<>(current, size);
            IPage<BookingVO> voPage = bookingService.getBookingPage(page, userId, status, venueName);
            return Result.success(voPage);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 查询我的预约记录
     */
    @Operation(summary = "查询我的预约记录")
    @GetMapping("/my")
    public Result<List<BookingVO>> getMyBookings(@AuthenticationPrincipal Long userId) {
        try {
            if (userId == null) {
                return Result.error(401, "未登录");
            }
            List<BookingVO> bookings = bookingService.getBookingsByUserId(userId);
            return Result.success(bookings);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 查询预约详情
     */
    @Operation(summary = "查询预约详情")
    @GetMapping("/{id}")
    public Result<BookingVO> getBookingById(@Parameter(description = "预约ID") @PathVariable Long id) {
        try {
            BookingVO booking = bookingService.getBookingById(id);
            if (booking == null) {
                return Result.error("预约记录不存在");
            }
            return Result.success(booking);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 检查时间冲突
     */
    @Operation(summary = "检查时间冲突")
    @GetMapping("/check-conflict")
    public Result<Boolean> checkTimeConflict(
            @Parameter(description = "场馆ID") @RequestParam Long venueId,
            @Parameter(description = "预约日期") @RequestParam String date,
            @Parameter(description = "开始时间") @RequestParam String startTime,
            @Parameter(description = "结束时间") @RequestParam String endTime,
            @Parameter(description = "排除的预约ID（修改时用）") @RequestParam(required = false) Long excludeId) {
        try {
            LocalDate bookingDate = LocalDate.parse(date);
            boolean conflict = bookingService.checkTimeConflict(venueId, bookingDate, startTime, endTime, excludeId);
            return Result.success(conflict);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取场馆某日期已预约时间段
     */
    @Operation(summary = "获取场馆某日期已预约时间段")
    @GetMapping("/booked-slots")
    public Result<List<String>> getBookedTimeSlots(
            @Parameter(description = "场馆ID") @RequestParam Long venueId,
            @Parameter(description = "日期") @RequestParam String date) {
        try {
            LocalDate bookingDate = LocalDate.parse(date);
            List<String> timeSlots = bookingService.getBookedTimeSlots(venueId, bookingDate);
            return Result.success(timeSlots);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 计算预约费用
     */
    @Operation(summary = "计算预约费用")
    @GetMapping("/calculate-price")
    public Result<BigDecimal> calculatePrice(
            @Parameter(description = "场馆ID") @RequestParam Long venueId,
            @Parameter(description = "开始时间") @RequestParam String startTime,
            @Parameter(description = "结束时间") @RequestParam String endTime) {
        try {
            BigDecimal totalPrice = bookingService.calculateTotalPrice(venueId, startTime, endTime);
            return Result.success(totalPrice);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}