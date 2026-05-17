package com.intelligent.trial.gateway.config;

import com.intelligent.trial.gateway.handler.SwaggerResourcesHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;

/**
 * Swagger 路由配置 - 定义网关级别的 Swagger 端点
 * 聚合所有微服务的 OpenAPI 文档供 Swagger UI 使用
 */
@Configuration
public class SwaggerRouteConfig {

    @Bean
    public RouterFunction<ServerResponse> swaggerResourcesRouter(SwaggerResourcesHandler handler) {
        return RouterFunctions.route(
                GET("/swagger-resources")
                        .and(request -> request.headers().accept().stream()
                                .anyMatch(mediaType -> mediaType.isCompatibleWith(MediaType.APPLICATION_JSON))),
                handler::swaggerResources
        );
    }
}
