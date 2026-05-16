package com.intelligent.trial.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * API网关启动类
 * 基于 Spring Cloud Gateway（WebFlux）
 *
 * 功能：
 * 1. 统一入口：前端所有请求通过 8080 端口
 * 2. 路由转发：按路径前缀分发到各后端服务（静态路由，直连各服务地址）
 * 3. JWT 认证：网关层统一 Token 校验
 * 4. 请求日志：记录每个请求的 URL、IP、耗时
 * 5. 全局异常：统一错误响应格式
 *
 * 注意：当前使用静态路由（hardcoded localhost），未启用服务注册中心。
 * 未来如需接入 Nacos/Eureka，可添加 @EnableDiscoveryClient 并修改 routes 使用 lb:// 前缀。
 */
@SpringBootApplication
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
