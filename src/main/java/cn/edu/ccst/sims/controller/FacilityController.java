package cn.edu.ccst.sims.controller;

import cn.edu.ccst.sims.common.Result;
import cn.edu.ccst.sims.dto.FaultReportDTO;
import cn.edu.ccst.sims.entity.SysUser;
import cn.edu.ccst.sims.mapper.SysUserMapper;
import cn.edu.ccst.sims.service.FacilityService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "设施故障管理")
@RestController
@RequestMapping("/api/facility")
public class FacilityController {

    @Autowired
    private FacilityService facilityService;

    @Autowired
    private SysUserMapper userMapper;

    @Operation(summary = "申报故障")
    @PostMapping("/fault/report")
    public Result<String> reportFault(@AuthenticationPrincipal Long userId,
            @Valid @RequestBody FaultReportDTO faultReportDTO) {
        try {
            if (userId == null) {
                return Result.error(401, "未登录");
            }
            facilityService.reportFault(userId, faultReportDTO);
            return Result.success("故障申报成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "获取故障列表")
    @GetMapping("/fault/list")
    public Result<Page<Map<String, Object>>> getFaultList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "故障状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "场馆ID") @RequestParam(required = false) Long venueId,
            @Parameter(description = "故障类型") @RequestParam(required = false) String faultType) {
        try {
            Page<Map<String, Object>> page = facilityService.getFaultList(pageNum, pageSize, status, venueId,
                    faultType);
            return Result.success(page);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "获取故障详情")
    @GetMapping("/fault/{id}")
    public Result<Map<String, Object>> getFaultDetail(@PathVariable Long id) {
        try {
            Map<String, Object> fault = facilityService.getFaultDetail(id);
            if (fault == null) {
                return Result.error("故障记录不存在");
            }
            return Result.success(fault);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "管理员处理故障")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/fault/{id}/process")
    public Result<String> processFault(@AuthenticationPrincipal Long userId,
            @PathVariable Long id,
            @RequestParam Integer status,
            @RequestParam(required = false) String processRemark,
            @RequestParam(required = false) String estimatedTime) {
        try {
            if (userId == null) {
                return Result.error(401, "未登录");
            }

            // 校验管理员权限
            SysUser admin = userMapper.selectById(userId);
            if (admin == null || admin.getRole() != 2) {
                return Result.error(403, "无管理员权限");
            }

            facilityService.processFault(id, status, processRemark, estimatedTime);
            return Result.success("故障处理成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "更新故障进度")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/fault/{id}/progress")
    public Result<String> updateFaultProgress(@AuthenticationPrincipal Long userId,
            @PathVariable Long id,
            @RequestParam String progress) {
        try {
            if (userId == null) {
                return Result.error(401, "未登录");
            }

            // 校验管理员权限
            SysUser admin = userMapper.selectById(userId);
            if (admin == null || admin.getRole() != 2) {
                return Result.error(403, "无管理员权限");
            }

            facilityService.updateFaultProgress(id, progress);
            return Result.success("进度更新成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "用户取消故障申报")
    @PutMapping("/fault/{id}/cancel")
    public Result<String> cancelFault(@AuthenticationPrincipal Long userId,
            @PathVariable Long id,
            @RequestParam String reason) {
        try {
            if (userId == null) {
                return Result.error(401, "未登录");
            }
            facilityService.cancelFault(userId, id, reason);
            return Result.success("取消成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "获取我的故障申报")
    @GetMapping("/fault/my")
    public Result<Page<Map<String, Object>>> getMyFaults(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "故障状态") @RequestParam(required = false) Integer status) {
        try {
            if (userId == null) {
                return Result.error(401, "未登录");
            }
            Page<Map<String, Object>> page = facilityService.getMyFaults(userId, pageNum, pageSize, status);
            return Result.success(page);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "获取故障统计")
    @GetMapping("/fault/statistics")
    public Result<Map<String, Object>> getFaultStatistics(
            @Parameter(description = "统计类型：day, week, month") @RequestParam(defaultValue = "month") String type,
            @Parameter(description = "场馆ID") @RequestParam(required = false) Long venueId) {
        try {
            Map<String, Object> statistics = facilityService.getFaultStatistics(type, venueId);
            return Result.success(statistics);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "获取设施列表")
    @GetMapping("/list")
    public Result<List<Map<String, Object>>> getFacilityList(
            @Parameter(description = "场馆ID") @RequestParam(required = false) Long venueId,
            @Parameter(description = "设施类型") @RequestParam(required = false) String facilityType) {
        try {
            List<Map<String, Object>> facilities = facilityService.getFacilityList(venueId, facilityType);
            return Result.success(facilities);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "管理员添加设施")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin")
    public Result<String> addFacility(@AuthenticationPrincipal Long userId,
            @RequestBody Map<String, Object> facilityData) {
        try {
            if (userId == null) {
                return Result.error(401, "未登录");
            }

            // 校验管理员权限
            SysUser admin = userMapper.selectById(userId);
            if (admin == null || admin.getRole() != 2) {
                return Result.error(403, "无管理员权限");
            }

            facilityService.addFacility(facilityData);
            return Result.success("设施添加成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "管理员更新设施")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/{id}")
    public Result<String> updateFacility(@AuthenticationPrincipal Long userId,
            @PathVariable Long id,
            @RequestBody Map<String, Object> facilityData) {
        try {
            if (userId == null) {
                return Result.error(401, "未登录");
            }

            // 校验管理员权限
            SysUser admin = userMapper.selectById(userId);
            if (admin == null || admin.getRole() != 2) {
                return Result.error(403, "无管理员权限");
            }

            facilityService.updateFacility(id, facilityData);
            return Result.success("设施更新成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "管理员删除设施")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/admin/{id}")
    public Result<String> deleteFacility(@AuthenticationPrincipal Long userId,
            @PathVariable Long id) {
        try {
            if (userId == null) {
                return Result.error(401, "未登录");
            }

            // 校验管理员权限
            SysUser admin = userMapper.selectById(userId);
            if (admin == null || admin.getRole() != 2) {
                return Result.error(403, "无管理员权限");
            }

            facilityService.deleteFacility(id);
            return Result.success("设施删除成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
