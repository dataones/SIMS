package cn.edu.ccst.sims.security;

import cn.edu.ccst.sims.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    // 使用Ant路径匹配器（支持*和**通配符）
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    // ========== 【公开路径列表】必须与SecurityConfig中的.permitAll()路径完全一致 ==========
    private static final List<String> PUBLIC_PATTERNS = Arrays.asList(
            // 1. 静态资源和主页
            "/",
            "/index.html",
            "/favicon.ico",
            "/error",
            "/static/**",
            "/css/**",
            "/js/**",
            "/images/**",
            "/assets/**",
            "/public/**",
            "/uploads/**",

            // 2. API文档和Swagger
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/v3/api-docs",
            "/webjars/**",
            "/swagger-resources/**",
            "/swagger-resources",

            // 3. 认证接口（完全公开）
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/logout",

            // 4. 用户信息接口（根据SecurityConfig，这些路径被放行了）
            "/api/user/info",
            "/api/user/current",

            // 5. 场馆公开接口
            "/api/venue/list",
            "/api/venue/recommended",
            "/api/venue/*",

            // 6. Banner和公告公开接口
            "/api/banner/active",
            "/api/notice/list",
            "/api/notice/latest",
            "/api/notice/*",
            "/api/notice/*/read",
            "/api/admin/notices/*",

            // 7. 预约查询公开接口
            "/api/booking/check-conflict",
            "/api/booking/booked-slots",
            "/api/booking/calculate-price");

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        // ========== 【调试信息】开始 ==========
        String path = request.getRequestURI();
        String method = request.getMethod();
        System.out.println("\n=== JWT过滤器开始处理 ===");
        System.out.println("请求路径: " + path);
        System.out.println("请求方法: " + method);
        System.out.println("Authorization头: " + request.getHeader("Authorization"));

        // ========== 1. 检查是否为公开路径 ==========
        if (isPublicPath(path, method)) {
            System.out.println("✅ 公开路径，直接放行");
            System.out.println("=== JWT过滤器结束（公开路径）===\n");
            filterChain.doFilter(request, response);
            return;
        }

        System.out.println("🔒 需要认证的路径");

        // ========== 2. 检查Authorization头 ==========
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            System.out.println("❌ 未提供有效的Authorization头");
            System.out.println("=== JWT过滤器结束（无Token）===\n");

            // ⭐ 重要：对于需要认证的路径但没有token，直接返回401
            // 不要调用filterChain.doFilter()，因为这会继续执行后续过滤器
            sendUnauthorized(response, "请先登录");
            return;
        }

        // ========== 3. 提取和验证Token ==========
        String token = header.substring(7);
        System.out.println("找到Token，长度: " + token.length() + " 字符");

        try {
            // 3.1 验证Token是否有效
            if (!jwtUtil.validateToken(token)) {
                System.out.println("❌ Token验证失败（可能已过期或无效）");
                System.out.println("=== JWT过滤器结束（Token无效）===\n");
                sendUnauthorized(response, "登录已过期，请重新登录");
                return;
            }

            // 3.2 从Token中提取用户信息
            Long userId = jwtUtil.getUserId(token);
            Integer role = jwtUtil.getRole(token);

            System.out.println("✅ Token验证成功");
            System.out.println("用户ID: " + userId);
            System.out.println("用户角色: " + role);

            // ========== 4. 根据角色创建权限 ==========
            SimpleGrantedAuthority authority;
            if (role != null && role == 2) {
                authority = new SimpleGrantedAuthority("ROLE_ADMIN");
                System.out.println("用户权限: ROLE_ADMIN");
            } else if (role != null && role == 1) {
                authority = new SimpleGrantedAuthority("ROLE_MEMBER");
                System.out.println("用户权限: ROLE_MEMBER");
            } else {
                authority = new SimpleGrantedAuthority("ROLE_USER");
                System.out.println("用户权限: ROLE_USER");
            }

            // ========== 5. 创建Authentication对象 ==========
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userId, // principal设置为userId
                    null, // credentials设为null（不需要密码）
                    Collections.singletonList(authority) // 权限列表
            );

            // 设置请求详情
            authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request));

            // ========== 6. 设置到SecurityContext ==========
            SecurityContextHolder.getContext().setAuthentication(authentication);
            System.out.println("✅ 认证信息已设置到SecurityContext");

            // ========== 7. 继续过滤器链 ==========
            System.out.println("=== JWT过滤器结束（认证成功）===\n");
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            // Token解析异常（如格式错误、签名错误等）
            System.out.println("❌ Token解析异常: " + e.getMessage());
            e.printStackTrace();
            System.out.println("=== JWT过滤器结束（Token解析异常）===\n");

            // 清除安全上下文，防止使用无效的认证信息
            SecurityContextHolder.clearContext();
            sendUnauthorized(response, "令牌无效: " + e.getMessage());
        }
    }

    /**
     * 判断是否为公开路径
     * 规则：与SecurityConfig中.permitAll()的路径完全一致
     *
     * @param path   请求路径
     * @param method 请求方法
     * @return true表示公开路径，false表示需要认证
     */
    private boolean isPublicPath(String path, String method) {
        // 1. CORS预检请求直接放行
        if ("OPTIONS".equalsIgnoreCase(method)) {
            System.out.println("CORS预检请求，直接放行");
            return true;
        }

        // 2. 评价接口的GET请求放行（与SecurityConfig一致）
        if ("GET".equalsIgnoreCase(method) && path.startsWith("/api/review/")) {
            System.out.println("评价GET请求，直接放行");
            return true;
        }

        // 3. 检查路径是否匹配公开路径模式
        for (String pattern : PUBLIC_PATTERNS) {
            if (pathMatcher.match(pattern, path)) {
                System.out.println("路径匹配公开模式: " + pattern);
                return true;
            }
        }

        // 4. 路径未匹配任何公开模式，需要认证
        System.out.println("路径未匹配任何公开模式");
        return false;
    }

    /**
     * 返回401 Unauthorized响应
     * 格式与Spring Security的异常处理器保持一致
     */
    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
                String.format("{\"code\": 401, \"msg\": \"%s\", \"data\": null}", message));
        response.getWriter().flush();
    }
}