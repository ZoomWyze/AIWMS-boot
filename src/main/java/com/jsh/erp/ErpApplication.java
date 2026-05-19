package com.jsh.erp;

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
