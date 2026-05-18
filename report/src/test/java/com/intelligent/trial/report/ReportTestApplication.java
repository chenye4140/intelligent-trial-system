package com.intelligent.trial.report;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 文书生成模块集成测试启动类
 */
@SpringBootApplication
@EnableAsync
@ComponentScan(basePackages = {"com.intelligent.trial"})
@EntityScan(basePackages = {"com.intelligent.trial"})
@MapperScan(basePackages = {
        "com.intelligent.trial.report.mapper",
        "com.intelligent.trial.repository.mapper"
})
public class ReportTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReportTestApplication.class, args);
    }
}
