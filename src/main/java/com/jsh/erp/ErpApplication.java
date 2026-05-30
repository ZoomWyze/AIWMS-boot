package com.jsh.erp;

/**
 * AIWMS（智能仓储管理系统）Spring Boot 启动类
 *
 * 功能：
 *   1. @SpringBootApplication  — 标识 Spring Boot 应用入口
 *   2. @MapperScan             — 扫描 MyBatis Mapper 接口（datasource.mappers 包）
 *   3. @ServletComponentScan   — 扫描 Servlet/Filter/Listener 注解
 *   4. @EnableScheduling       — 启用定时任务
 *   5. @EnableAsync            — 启用异步方法
 *
 * 启动后默认监听端口 9999，上下文路径为 /AIWMS-boot
 *
 * @author jishenghua
 */
import com.jsh.erp.utils.ComputerInfo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.IOException;

@SpringBootApplication
@MapperScan("com.jsh.erp.datasource.mappers")
@ServletComponentScan
@EnableScheduling
@EnableAsync
public class ErpApplication{
    public static void main(String[] args) throws IOException {
        ConfigurableApplicationContext context = SpringApplication.run(ErpApplication.class, args);
        Environment environment = context.getBean(Environment.class);
        String port = environment.getProperty("server.port", "9999");
        String contextPath = environment.getProperty("server.servlet.context-path", "");
        System.out.println("如需联调前端，请在前端工程目录执行：yarn run serve 或 npm run serve");
    }
}
