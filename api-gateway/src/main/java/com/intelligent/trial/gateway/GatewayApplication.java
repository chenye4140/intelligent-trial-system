package com.intelligent.trial.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * API网关启动类
 * 基于 Spring Cloud Gateway（WebFlux）
 *
 * 功能：
 * 1. 统一入口：前端所有请求通过 8080 端口
 * 2. 路由转发：按路径前缀分发到各后端服务
 * 3. JWT 认证：网关层统一 Token 校验
 * 4. 请求日志：记录每个请求的 URL、IP、耗时
 * 5. 全局异常：统一错误响应格式
 */
@SpringBootApplication
@EnableDiscoveryClient
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
