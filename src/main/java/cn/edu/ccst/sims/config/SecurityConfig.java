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
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 登录注册这些匿名访问
                        .requestMatchers("/api/auth/**").permitAll()

                        // 只放行“查看”类接口
                        .requestMatchers(
                                "/api/venue/list",
                                "/api/venue/detail/**",
                                "/api/equipment-rentals",   // 如果是公共列表
                                "/api/venues/**"
                        ).permitAll()

                        // 我的租赁、提交评价 等需要登录
                        .requestMatchers("/api/equipment-rentals/my").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/reviews/**").authenticated()
                        .requestMatchers(HttpMethod.GET,  "/api/reviews/**").permitAll()

                        .requestMatchers("/api/booking/booked-slots").permitAll()

                        // 管理员接口
                        .requestMatchers("/api/booking/audit/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/venue/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,
                                "/api/venue/**",
                                "/api/admin/equipment-rentals/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/venue/**").hasRole("ADMIN")

                        // 其他都需要登录
                        .anyRequest().authenticated()
                )

                // JWT 过滤器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
