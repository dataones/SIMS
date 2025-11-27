package cn.edu.ccst.sims;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("cn.edu.ccst.sims.mapper")  // 使用 org.mybatis.spring.annotation.MapperScan
public class SimsApplication {
    public static void main(String[] args) {
        SpringApplication.run(SimsApplication.class, args);
    }
}