package cn.edu.ccst.sims.config;

import cn.edu.ccst.sims.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.Customizer;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 🔴 ① 启用 CORS（让 CorsConfig 生效）
                .cors(Customizer.withDefaults())

                // 🔴 ② 关闭 CSRF（前后端分离必须）
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth
                        // 🔴 ③ 放行所有 OPTIONS 预检请求（关键）
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 公开接口
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(
                                "/api/venue/list",
                                "/api/venue/detail/**",
                                "/api/equipment-rentals",
                                "/api/equipment-rentals/my",
                                "/api/reviews/**",
                                "/api/venues/**"
                        ).permitAll()

                        // 🔴 ④ 预约查询接口（给前端用，必须放行）
                        .requestMatchers("/api/booking/booked-slots").permitAll()

                        // 管理员接口
                        .requestMatchers("/api/booking/audit/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/venue/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,
                                "/api/venue/**",
                                "/api/admin/equipment-rentals/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/venue/**").hasRole("ADMIN")

                        // 其他接口需要登录
                        .anyRequest().authenticated()
                )

                // JWT 过滤器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
