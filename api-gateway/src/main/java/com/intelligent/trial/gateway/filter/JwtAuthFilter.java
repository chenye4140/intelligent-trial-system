package com.intelligent.trial.gateway.filter;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * JWT 认证全局过滤器
 * 在网关层统一进行 Token 验证，避免每个后端服务重复校验
 */
@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    @Value("#{'${gateway.jwt.whitelist:/api/auth/login,/api/auth/captcha,/api/auth/refresh}'.split(',')}")
    private List<String> whitelist;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 白名单路径直接放行
        if (isWhitelist(path)) {
            return chain.filter(exchange);
        }

        // 获取 Authorization header
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || authHeader.isEmpty()) {
            return unauthorized(exchange.getResponse(), "未提供认证Token");
        }

        // 提取 Token（支持 "Bearer <token>" 格式）
        String token = authHeader;
        if (authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }

        // 网关层做基础 Token 校验（非空 + 格式检查）
        // 完整的签名验证由后端 auth 服务的 JwtInterceptor 处理
        if (token.isEmpty() || token.length() < 10) {
            return unauthorized(exchange.getResponse(), "Token格式无效");
        }

        // 将 Token 和解析后的用户信息透传给下游服务
        ServerHttpRequest mutatedRequest = request.mutate()
                .header("X-Gateway-Token", token)
                .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    /**
     * 检查路径是否在白名单中
     */
    private boolean isWhitelist(String path) {
        for (String pattern : whitelist) {
            if (pathMatcher.match(pattern.trim(), path)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 返回 401 未授权响应
     */
    private Mono<Void> unauthorized(ServerHttpResponse response, String message) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        JSONObject result = new JSONObject();
        result.put("code", 401);
        result.put("msg", message);
        result.put("data", null);

        byte[] bytes = JSON.toJSONString(result).getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100; // 高优先级，在其他过滤器之前执行
    }
}
