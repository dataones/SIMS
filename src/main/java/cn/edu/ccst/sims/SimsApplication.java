package cn.edu.ccst.sims;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * SIMS - 体育场馆综合管理系统
 * 
 * Spring Boot 主启动类
 * 
 * 功能说明：
 * 1. 启动Spring Boot应用
 * 2. 配置MyBatis Mapper扫描
 * 3. 启用定时任务功能
 * 4. 自动配置组件扫描和依赖注入
 * 
 * 技术栈：
 * - Spring Boot 3.3.5 (主框架)
 * - MyBatis-Plus 3.5.7 (ORM框架)
 * - Spring Security (安全认证)
 * - MySQL 8 (数据库)
 * - JWT (令牌认证)
 * 
 * @author SIMS开发团队
 * @version 1.0.0
 * @since 2024-01-01
 */
@SpringBootApplication
@MapperScan("cn.edu.ccst.sims.mapper") // 扫描Mapper接口，自动创建代理对象
@EnableScheduling // 启用Spring的定时任务功能，用于订单自动取消等
public class SimsApplication {

    /**
     * 应用程序入口点
     * 
     * @param args 命令行参数
     *             --spring.profiles.active: 指定运行环境配置
     *             --server.port: 指定服务端口
     *             --spring.config.location: 指定配置文件位置
     */
    public static void main(String[] args) {
        // 启动Spring Boot应用
        SpringApplication.run(SimsApplication.class, args);
    }
}