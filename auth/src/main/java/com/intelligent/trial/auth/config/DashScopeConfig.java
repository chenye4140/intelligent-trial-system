package com.intelligent.trial.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * DashScope / DeepSeek API 配置
 * 用于定密建议 AI 分析
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "dashscope")
public class DashScopeConfig {

    /**
     * DeepSeek API Key
     */
    private String deepseekApiKey;

    /**
     * DeepSeek API Base URL
     */
    private String deepseekBaseUrl;

    /**
     * DeepSeek 模型名称（如 deepseek-chat, deepseek-v4-pro）
     */
    private String deepseekModel;
}
