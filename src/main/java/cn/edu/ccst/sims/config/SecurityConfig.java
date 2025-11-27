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

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // 公开接口
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/venue/list",
                                "/api/venue/detail/**","/api/equipment-rentals",
                                "/api/equipment-rentals/my",
                                "/api/reviews/**",
                                "/api/venues/**").permitAll()

                        // 管理员专用接口（明确列出）
                        .requestMatchers("/api/booking/audit/**").hasRole("ADMIN")  // 审核预约
                        .requestMatchers(HttpMethod.POST,
                                "/api/venue/**").hasRole("ADMIN")  // 添加场馆
                        .requestMatchers(HttpMethod.PUT,
                                "/api/venue/**",
                                "/api/admin/equipment-rentals/**").hasRole("ADMIN")   // 编辑场馆
                        .requestMatchers(HttpMethod.DELETE,
                                "/api/venue/**").hasRole("ADMIN") // 删除场馆
                        // 可以继续添加其他管理员接口，如用户管理、统计等

                        // 其他所有接口需要登录（authenticated）
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
