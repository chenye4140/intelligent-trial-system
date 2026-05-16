package com.intelligent.trial.workflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;

/**
 * 流程引擎模块启动类
 * 基于Flowable 6.8.0
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.intelligent.trial"})
@EntityScan(basePackages = {"com.intelligent.trial"})
public class WorkflowApplication {

    public static void main(String[] args) {
        SpringApplication.run(WorkflowApplication.class, args);
    }
}
