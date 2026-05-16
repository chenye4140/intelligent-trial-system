package com.intelligent.trial.workflow;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;

/**
 * 流程引擎模块启动类
 * 基于Flowable 6.8.0实现案件审理审批工作流
 *
 * @author intelligent-trial
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.intelligent.trial"})
@EntityScan(basePackages = {"com.intelligent.trial"})
@MapperScan(basePackages = {"com.intelligent.trial.**.mapper"})
public class WorkflowApplication {

    /**
     * 主入口方法
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(WorkflowApplication.class, args);
    }
}
