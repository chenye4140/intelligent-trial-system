package com.intelligent.trial.gateway.handler;

import com.intelligent.trial.gateway.config.SwaggerProvider;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

/**
 * Swagger Resources Handler - 为网关提供 swagger-resources 端点
 * 用于 Swagger UI 发现和加载各微服务的 OpenAPI 文档
 */
@Component
public class SwaggerResourcesHandler {

    private final SwaggerProvider swaggerProvider;

    public SwaggerResourcesHandler(SwaggerProvider swaggerProvider) {
        this.swaggerProvider = swaggerProvider;
    }

    public Mono<ServerResponse> swaggerResources(ServerRequest request) {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(swaggerProvider.getSwaggerResources());
    }
}
