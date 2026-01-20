package cn.edu.ccst.sims.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS配置 - 使用WebMvcConfigurer方式
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                        // 开发环境
                        "http://localhost:3000",
                        "http://127.0.0.1:3000",
                        // 生产环境 - 添加你的前端部署地址
                        "http://wxun.asia:8080",
                        "http://8.137.17.215:8080", // 如果有HTTPS
                        // OSS
                        "https://sims-images.oss-cn-beijing.aliyuncs.com")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}