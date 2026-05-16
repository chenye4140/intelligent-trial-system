package com.intelligent.trial.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

/**
 * 请求日志过滤器
 * 记录每个请求的 URL、方法、IP、耗时等信息
 */
@Component
public class RequestLogFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(RequestLogFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String method = request.getMethodValue();
        String path = request.getURI().getPath();
        String ip = getClientIp(request);

        Instant startTime = Instant.now();

        return chain.filter(exchange).doOnSuccess(aVoid -> {
            Duration duration = Duration.between(startTime, Instant.now());
            int statusCode = exchange.getResponse().getStatusCode() != null
                    ? exchange.getResponse().getStatusCode().value() : 0;
            log.info("[Gateway] {} {} from {} -> {} ({}ms)",
                    method, path, ip, statusCode, duration.toMillis());
        }).doOnError(throwable -> {
            Duration duration = Duration.between(startTime, Instant.now());
            log.error("[Gateway] {} {} from {} -> ERROR ({}ms): {}",
                    method, path, ip, duration.toMillis(), throwable.getMessage());
        });
    }

    /**
     * 获取客户端真实 IP
     */
    private String getClientIp(ServerHttpRequest request) {
        HttpHeaders headers = request.getHeaders();
        String ip = headers.getFirst("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = headers.getFirst("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddress() != null
                    ? request.getRemoteAddress().getAddress().getHostAddress() : "unknown";
        }
        // X-Forwarded-For 可能包含多个 IP，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    @Override
    public int getOrder() {
        return -200; // 最高优先级，最先执行
    }
}
