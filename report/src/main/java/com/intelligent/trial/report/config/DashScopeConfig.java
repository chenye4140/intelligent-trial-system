package com.intelligent.trial.report.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * DashScope（通义千问）API 配置
 * 使用 OpenAI 兼容端点
 * 同时支持 DeepSeek 模型配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "dashscope")
public class DashScopeConfig {

    /**
     * API Key
     */
    private String apiKey;

    /**
     * API Base URL（OpenAI兼容端点）
     */
    private String baseUrl;

    /**
     * 段落分类模型
     */
    private String classifyModel;

    /**
     * OCR模型（用于PDF/图片识别）
     */
    private String ocrModel;

    /**
     * 向量嵌入模型
     */
    private String embeddingModel;

    // ========================= DeepSeek 配置 =========================

    /**
     * DeepSeek API Key
     */
    private String deepseekApiKey;

    /**
     * DeepSeek API Base URL
     */
    private String deepseekBaseUrl;

    /**
     * DeepSeek 模型名称（如 deepseek-chat, deepseek-reasoner）
     */
    private String deepseekModel;
}
