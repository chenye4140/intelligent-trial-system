package com.intelligent.trial.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Swagger 聚合配置 - 聚合所有微服务的 OpenAPI 文档
 * 网关通过此配置统一管理各后端服务的 API 文档路由
 */
@Configuration
public class SwaggerProvider {

    /**
     * 各微服务的 Swagger 资源映射
     * Key: 网关路径前缀
     * Value: 后端服务地址
     */
    public static final Map<String, String> SWAGGER_RESOURCES = new HashMap<>();

    static {
        SWAGGER_RESOURCES.put("/auth", "http://localhost:8081");
        SWAGGER_RESOURCES.put("/document", "http://localhost:8082");
        SWAGGER_RESOURCES.put("/repository", "http://localhost:8083");
        SWAGGER_RESOURCES.put("/report", "http://localhost:8084");
        SWAGGER_RESOURCES.put("/case", "http://localhost:8085");
        SWAGGER_RESOURCES.put("/workflow", "http://localhost:8086");
        SWAGGER_RESOURCES.put("/promotion", "http://localhost:8088");
        SWAGGER_RESOURCES.put("/punishment", "http://localhost:8089");
        SWAGGER_RESOURCES.put("/reading-note", "http://localhost:8090");
    }

    /**
     * 获取所有 swagger-resources 条目
     */
    public List<Map<String, Object>> getSwaggerResources() {
        List<Map<String, Object>> resources = new ArrayList<>();
        for (Map.Entry<String, String> entry : SWAGGER_RESOURCES.entrySet()) {
            Map<String, Object> resource = new HashMap<>();
            resource.put("name", entry.getKey() + " service");
            resource.put("url", entry.getKey() + "/v3/api-docs");
            resource.put("swaggerVersion", "3.0");
            resource.put("location", "/v3/api-docs/" + entry.getKey());
            resources.add(resource);
        }
        return resources;
    }
}
