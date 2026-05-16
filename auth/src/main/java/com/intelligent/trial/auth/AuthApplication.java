package com.intelligent.trial.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;

/**
 * 认证授权模块启动类
 * RBAC权限管理
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.intelligent.trial"})
@EntityScan(basePackages = {"com.intelligent.trial"})
public class AuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }
}
