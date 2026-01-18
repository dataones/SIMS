package cn.edu.ccst.sims.controller;

import cn.edu.ccst.sims.common.Result;
import cn.edu.ccst.sims.entity.SysUser;
import cn.edu.ccst.sims.service.UserManageService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "用户管理")
@RestController
@RequestMapping("/api/admin/users")
public class UserManageController {

    @Autowired
    private UserManageService userManageService;

    @Operation(summary = "获取用户列表")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public Result<Page<SysUser>> getUserList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "用户角色") @RequestParam(required = false) Integer role,
            @Parameter(description = "用户状态") @RequestParam(required = false) Integer status,
            @AuthenticationPrincipal Long userId) {
        try {
            if (userId == null) {
                return Result.error(401, "未登录");
            }

            Page<SysUser> page = new Page<>(pageNum, pageSize);
            LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();

            // 搜索条件
            if (keyword != null && !keyword.trim().isEmpty()) {
                queryWrapper.and(wrapper -> wrapper
                        .like(SysUser::getUsername, keyword)
                        .or()
                        .like(SysUser::getNickname, keyword)
                        .or()
                        .like(SysUser::getEmail, keyword)
                        .or()
                        .like(SysUser::getPhone, keyword));
            }

            if (role != null) {
                queryWrapper.eq(SysUser::getRole, role);
            }

            if (status != null) {
                queryWrapper.eq(SysUser::getStatus, status);
            }

            queryWrapper.orderByDesc(SysUser::getCreateTime);

            Page<SysUser> result = userManageService.page(page, queryWrapper);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "更新用户")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public Result<String> updateUser(@PathVariable Long id, @RequestBody SysUser user,
            @AuthenticationPrincipal Long userId) {
        try {
            if (userId == null) {
                return Result.error(401, "未登录");
            }

            user.setId(id);
            userManageService.updateUser(user);
            return Result.success("用户更新成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "删除用户")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public Result<String> deleteUser(@PathVariable Long id, @AuthenticationPrincipal Long userId) {
        try {
            if (userId == null) {
                return Result.error(401, "未登录");
            }

            // 防止删除自己
            if (userId.equals(id)) {
                return Result.error("不能删除自己");
            }

            userManageService.deleteUser(id);
            return Result.success("用户删除成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "切换用户状态")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/status")
    public Result<String> toggleUserStatus(@PathVariable Long id, @RequestBody Map<String, Integer> statusMap,
            @AuthenticationPrincipal Long userId) {
        try {
            if (userId == null) {
                return Result.error(401, "未登录");
            }

            // 防止禁用自己
            if (userId.equals(id) && statusMap.get("status") == 0) {
                return Result.error("不能禁用自己");
            }

            Integer status = statusMap.get("status");
            userManageService.toggleUserStatus(id, status);
            return Result.success("状态切换成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "重置用户密码")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/password")
    public Result<String> resetUserPassword(@PathVariable Long id, @RequestBody Map<String, String> passwordMap,
            @AuthenticationPrincipal Long userId) {
        try {
            if (userId == null) {
                return Result.error(401, "未登录");
            }

            String password = passwordMap.get("password");
            userManageService.resetPassword(id, password);
            return Result.success("密码重置成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "获取用户详情")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public Result<SysUser> getUserDetail(@PathVariable Long id, @AuthenticationPrincipal Long userId) {
        try {
            if (userId == null) {
                return Result.error(401, "未登录");
            }

            SysUser user = userManageService.getById(id);
            if (user == null) {
                return Result.error("用户不存在");
            }

            // 清除密码信息
            user.setPassword(null);
            return Result.success(user);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
