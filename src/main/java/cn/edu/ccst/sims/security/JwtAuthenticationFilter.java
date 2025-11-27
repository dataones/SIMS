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
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT 认证过滤器
 * 作用：拦截所有请求，解析 Authorization Header 中的 Bearer Token
 *       如果 Token 有效，则提取 userId、username、role
 *       将 userId (Long) 作为 principal 放入 SecurityContext
 *       同时设置对应角色权限（ROLE_ADMIN / ROLE_MEMBER / ROLE_USER）
 *       便于后续使用 @AuthenticationPrincipal Long userId 获取当前用户ID
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        // 如果没有 Authorization 头或不是 Bearer 开头，直接放行
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        // 验证 Token 是否有效
        if (!jwtUtil.validateToken(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 解析 Token 获取信息
        Long userId = jwtUtil.getUserId(token);
        String username = jwtUtil.getUsername(token);
        Integer role = jwtUtil.getRole(token);

        // 根据 role 设置权限
        SimpleGrantedAuthority authority;
        if (role != null && role == 2) {
            authority = new SimpleGrantedAuthority("ROLE_ADMIN");
        } else if (role != null && role == 1) {
            authority = new SimpleGrantedAuthority("ROLE_MEMBER");
        } else {
            authority = new SimpleGrantedAuthority("ROLE_USER");
        }

        // 关键：principal 设为 userId (Long 类型)
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userId,           // ← principal 是 userId (Long)
                        null,             // credentials (无需密码)
                        Collections.singletonList(authority)
                );

        // 设置请求详情（IP、session 等，用于审计）
        authentication.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request)
        );

        // 放入 Spring Security 上下文
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 继续执行过滤链
        filterChain.doFilter(request, response);
    }
}