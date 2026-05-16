package com.intelligent.trial.gateway.handler;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * 网关全局异常处理器
 * 统一处理网关层的异常，返回标准 JSON 格式响应
 */
@Order(-1)
@Component
public class GatewayExceptionHandler implements ErrorWebExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GatewayExceptionHandler.class);

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();

        // 如果响应已经提交，直接返回
        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        // 设置 JSON 响应类型
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // 根据异常类型设置合适的 HTTP 状态码和消息
        HttpStatus status;
        String message;

        if (ex instanceof ResponseStatusException) {
            status = (HttpStatus) ((ResponseStatusException) ex).getStatus();
            message = ex.getLocalizedMessage();
        } else if (ex instanceof org.springframework.web.server.MethodNotAllowedException) {
            status = HttpStatus.METHOD_NOT_ALLOWED;
            message = "请求方法不被允许";
        } else if (ex instanceof java.util.concurrent.TimeoutException) {
            status = HttpStatus.GATEWAY_TIMEOUT;
            message = "服务响应超时";
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            message = "网关内部错误";
            log.error("[Gateway] Unexpected error: ", ex);
        }

        response.setStatusCode(status);

        // 构建统一响应格式
        JSONObject result = new JSONObject();
        result.put("code", status.value());
        result.put("msg", message);
        result.put("data", null);

        byte[] bytes = JSON.toJSONString(result).getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);

        return response.writeWith(Mono.just(buffer));
    }
}
