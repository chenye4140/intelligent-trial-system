package com.intelligent.trial.workflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * 工作流模块集成测试启动类
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.intelligent.trial"})
public class WorkflowTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(WorkflowTestApplication.class, args);
    }
}
