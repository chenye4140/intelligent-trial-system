package com.intelligent.trial.repository;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;

/**
 * 多库管理模块启动类
 * 法规库/资料库/案例库/裁判文书库
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.intelligent.trial"})
@EntityScan(basePackages = {"com.intelligent.trial"})
public class RepositoryApplication {

    public static void main(String[] args) {
        SpringApplication.run(RepositoryApplication.class, args);
    }
}
