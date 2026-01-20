package cn.edu.ccst.sims.config;

import cn.edu.ccst.sims.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        @Autowired
        private JwtAuthenticationFilter jwtAuthenticationFilter;

        /* ==================== PasswordEncoder ==================== */
        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        /* ==================== CORS ==================== */
        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration config = new CorsConfiguration();
                config.setAllowedOrigins(Arrays.asList(
                                "http://wxun.asia",
                                "http://wxun.asia:8080",
                                "http://localhost:3000",
                                "http://localhost:8080",
                                "http://8.137.17.215",
                                "http://localhost:5173", // Vite 开发服务器
                                "http://127.0.0.1:5173" // Vite 开发服务器
                ));
                config.setAllowedMethods(Arrays.asList(
                                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
                config.setAllowedHeaders(Arrays.asList(
                                "Authorization",
                                "Content-Type",
                                "X-Requested-With",
                                "Accept",
                                "Origin",
                                "Access-Control-Request-Method",
                                "Access-Control-Request-Headers",
                                "X-XSRF-TOKEN"));
                config.setExposedHeaders(Arrays.asList("Authorization"));
                config.setAllowCredentials(true);
                config.setMaxAge(3600L);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", config);
                return source;
        }

        /* ==================== Security ==================== */
        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                // ========== 1. 启用CORS配置 ==========
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                                // ========== 2. 禁用CSRF（JWT无状态API不需要） ==========
                                .csrf(csrf -> csrf.disable())

                                // ========== 3. 配置异常处理 ==========
                                .exceptionHandling(exception -> exception
                                                // 未认证时返回401 JSON（而不是重定向到登录页）
                                                .authenticationEntryPoint((request, response, authException) -> {
                                                        response.setContentType("application/json;charset=UTF-8");
                                                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                                        response.getWriter().write("{\"code\":401,\"msg\":\"请先登录\"}");
                                                })
                                                // 权限不足时返回403 JSON
                                                .accessDeniedHandler((request, response, accessDeniedException) -> {
                                                        response.setContentType("application/json;charset=UTF-8");
                                                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                                                        response.getWriter().write("{\"code\":403,\"msg\":\"权限不足\"}");
                                                }))

                                // ========== 4. 配置请求授权规则 ==========
                                .authorizeHttpRequests(auth -> auth

                                                /* ---------- 【第一优先级】静态资源和主页 ---------- */
                                                .requestMatchers(
                                                                "/", // 根路径
                                                                "/index.html", // 主页
                                                                "/favicon.ico", // 网站图标
                                                                "/error", // 错误页面
                                                                "/static/**", // 静态资源目录
                                                                "/css/**", // CSS文件
                                                                "/js/**", // JavaScript文件
                                                                "/images/**", // 图片文件
                                                                "/assets/**", // 其他资源
                                                                "/public/**", // 公开资源
                                                                "/uploads/**" // 上传文件（如果有）
                                                ).permitAll()

                                                /* ---------- 【第二优先级】CORS预检请求 ---------- */
                                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                                                /* ---------- 【第三优先级】API文档和Swagger ---------- */
                                                .requestMatchers(
                                                                "/swagger-ui.html",
                                                                "/swagger-ui/**",
                                                                "/v3/api-docs/**",
                                                                "/v3/api-docs",
                                                                "/webjars/**",
                                                                "/swagger-resources/**",
                                                                "/swagger-resources")
                                                .permitAll()

                                                /* ---------- 【第四优先级】认证接口（完全公开） ---------- */
                                                .requestMatchers(
                                                                "/api/auth/login", // POST 登录
                                                                "/api/auth/register", // POST 注册
                                                                "/api/auth/logout" // POST 登出（可选）
                                                ).permitAll()

                                                /* ---------- 【第五优先级】公共业务接口（无需登录） ---------- */

                                                // 5.1 场馆相关公开接口
                                                .requestMatchers(
                                                                "/api/venue/list", // 场馆列表
                                                                "/api/venue/recommended", // 推荐场馆
                                                                "/api/venue/{id}" // 场馆详情（动态路径）
                                                ).permitAll()

                                                // 5.2 Banner和公告公开接口
                                                .requestMatchers(
                                                                "/api/banner/active", // 活动Banner
                                                                "/api/notice/list", // 公告列表
                                                                "/api/notice/latest", // 最新公告
                                                                "/api/notice/{id}", // 公告详情
                                                                "/api/notice/{id}/read" // 增加阅读量
                                                ).permitAll()

                                                // 5.2.1 公告详情管理员接口（单独放行，必须在通用admin规则之前）
                                                .requestMatchers("/api/admin/notices/{id}").permitAll()

                                                // 5.3 评价只读接口（GET请求）
                                                .requestMatchers(HttpMethod.GET, "/api/review/**").permitAll()

                                                // 5.4 预约查询公开接口
                                                .requestMatchers(
                                                                "/api/booking/check-conflict", // 检查时间冲突
                                                                "/api/booking/booked-slots", // 已预约时段
                                                                "/api/booking/calculate-price" // 计算价格
                                                ).permitAll()

                                                /* ---------- 【第六优先级】用户信息接口 ---------- */
                                                // ⭐ 注意：前端调用的是 /api/auth/user-info，这个接口需要认证
                                                .requestMatchers("/api/auth/user-info").authenticated()

                                                // ⭐ 注意：你配置了 /api/user/info 和 /api/user/current，但前端可能没调用
                                                // 这里保持放行，以防前端调用这些接口
                                                .requestMatchers("/api/user/info", "/api/user/current").permitAll()

                                                /* ---------- 【第七优先级】需要登录的业务接口 ---------- */

                                                // 7.1 场馆预约操作接口
                                                .requestMatchers(
                                                                "/api/booking/submit", // 提交预约
                                                                "/api/booking/my", // 我的预约
                                                                "/api/booking/{id}" // 预约详情
                                                ).authenticated()

                                                // 7.2 设备租赁接口
                                                .requestMatchers("/api/equipment-rentals/**").authenticated()

                                                // 7.3 评价写操作（POST请求需要认证）
                                                .requestMatchers(HttpMethod.POST, "/api/review/**").authenticated()

                                                // 7.4 退款接口
                                                .requestMatchers("/api/refund/**").authenticated()

                                                // 7.5 故障报修接口
                                                .requestMatchers("/api/facility/fault/**").authenticated()

                                                // 7.6 OSS上传接口
                                                .requestMatchers("/api/oss/**").authenticated()

                                                /* ---------- 【第八优先级】用户管理接口 ---------- */
                                                // ⭐ 注意：除了info和current外，其他用户接口需要认证
                                                .requestMatchers(
                                                                "/api/user/update",
                                                                "/api/user/change-password",
                                                                "/api/user/profile",
                                                                "/api/user/avatar")
                                                .authenticated()

                                                /* ---------- 【第九优先级】管理员接口 ---------- */
                                                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                                                /* ---------- 【第十优先级】兜底规则 ---------- */
                                                // ⭐ 关键修改：将 .anyRequest().authenticated() 改为 .anyRequest().permitAll()
                                                // 这样确保主页、前端路由等不被拦截
                                                .anyRequest().permitAll())

                                // ========== 5. 添加JWT过滤器 ==========
                                // JWT过滤器在UsernamePasswordAuthenticationFilter之前执行
                                // 这样JWT过滤器可以先处理token，设置认证信息
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }
}