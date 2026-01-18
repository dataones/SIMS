package cn.edu.ccst.sims.controller;

import cn.edu.ccst.sims.common.Result;
import cn.edu.ccst.sims.entity.SysUser;
import cn.edu.ccst.sims.mapper.SysUserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试控制器 - 用于验证系统基本功能
 */
@Tag(name = "测试接口")
@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private SysUserMapper userMapper;

    @Operation(summary = "测试数据库连接")
    @GetMapping("/db")
    public Result<Map<String, Object>> testDatabase() {
        try {
            Map<String, Object> result = new HashMap<>();

            // 测试数据库连接
            SysUser user = userMapper.selectById(1L);
            result.put("databaseConnected", user != null);
            result.put("user", user);
            result.put("message", "数据库连接测试成功");

            return Result.success(result);
        } catch (Exception e) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("databaseConnected", false);
            errorResult.put("error", e.getMessage());
            errorResult.put("message", "数据库连接失败");

            return Result.error("数据库连接测试失败: " + e.getMessage());
        }
    }

    @Operation(summary = "测试接口响应")
    @GetMapping("/hello")
    public Result<String> hello() {
        return Result.success("Hello, SIMS System is running!");
    }

    @Operation(summary = "获取系统信息")
    @GetMapping("/info")
    public Result<Map<String, Object>> getSystemInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("system", "SIMS - 体育场馆综合管理系统");
        info.put("version", "1.0.0");
        info.put("status", "running");
        info.put("timestamp", System.currentTimeMillis());

        return Result.success(info);
    }
}
