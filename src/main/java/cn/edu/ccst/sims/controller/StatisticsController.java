package cn.edu.ccst.sims.controller;

import cn.edu.ccst.sims.common.Result; // 假设你的统一返回结果类在这里
import cn.edu.ccst.sims.vo.AdminStatsVO;
import cn.edu.ccst.sims.vo.UserStatsVO;
import cn.edu.ccst.sims.service.StatisticsService;
import cn.edu.ccst.sims.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest; // 如果是 Spring Boot 3+
// import javax.servlet.http.HttpServletRequest; // 如果是 Spring Boot 2.x
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取管理员统计数据
     * 路径: GET /api/admin/statistics
     */
    @GetMapping("/admin/statistics")
    public Result<AdminStatsVO> getAdminStats() {
        // 可以在这里加上权限校验，确保当前用户是管理员(role=2)
        // 省略校验逻辑，直接返回数据
        AdminStatsVO stats = statisticsService.getAdminStats();
        return Result.success(stats);
    }

    /**
     * 获取用户个人统计数据
     * 路径: GET /api/user/statistics
     */
    @GetMapping("/user/statistics")
    public Result<UserStatsVO> getUserStats(HttpServletRequest request) {
        // 1. 从请求头获取 Token
        String token = getTokenFromRequest(request);

        if (!StringUtils.hasText(token)) {
            return Result.error("未登录"); // 假设 Result 有 error 方法
        }

        // 2. 解析 Token 获取 UserId
        Long userId;
        try {
            userId = jwtUtil.getUserId(token);
        } catch (Exception e) {
            return Result.error("Token无效");
        }

        // 3. 查询数据
        UserStatsVO stats = statisticsService.getUserStats(userId);
        return Result.success(stats);
    }

    /**
     * 辅助方法：从 Authorization Header 中提取 Token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}