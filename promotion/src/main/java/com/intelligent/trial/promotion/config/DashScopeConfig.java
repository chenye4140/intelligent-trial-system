package com.intelligent.trial.promotion.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * DeepSeek AI 配置类
 * 用于配置 DeepSeek API 连接参数
 */
@Configuration
public class DashScopeConfig {

    /**
     * DeepSeek API Key
     */
    @Value("${ai.deepseek.api-key:}")
    private String apiKey;

    /**
     * DeepSeek API Base URL
     */
    @Value("${ai.deepseek.base-url:https://api.deepseek.com}")
    private String baseUrl;

    /**
     * DeepSeek 模型名称
     */
    @Value("${ai.deepseek.model:deepseek-v4-pro}")
    private String model;

    @Bean
    public String deepseekApiKey() {
        return apiKey;
    }

    @Bean
    public String deepseekBaseUrl() {
        return baseUrl;
    }

    @Bean
    public String deepseekModel() {
        return model;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getModel() {
        return model;
    }
}
