package com.intelligent.trial.promotion.client;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.intelligent.trial.common.exception.BusinessException;
import com.intelligent.trial.common.exception.ErrorCode;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * DeepSeek AI HTTP 客户端
 * 用于调用 DeepSeek API 进行促改分析生成
 */
@Component
public class DeepSeekClient {

    private static final Logger log = LoggerFactory.getLogger(DeepSeekClient.class);

    private final String baseUrl;
    private final String apiKey;
    private final String model;
    private final OkHttpClient okHttpClient;
    private final boolean enabled;

    /**
     * 构造器注入配置
     */
    public DeepSeekClient(
            @org.springframework.beans.factory.annotation.Qualifier("deepseekBaseUrl") String baseUrl,
            @org.springframework.beans.factory.annotation.Qualifier("deepseekApiKey") String apiKey,
            @org.springframework.beans.factory.annotation.Qualifier("deepseekModel") String model) {
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.model = model;

        this.okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(180, TimeUnit.SECONDS)
                .readTimeout(300, TimeUnit.SECONDS)
                .writeTimeout(180, TimeUnit.SECONDS)
                .build();

        // 检查配置是否有效
        if (baseUrl != null && !baseUrl.isEmpty()
                && apiKey != null && !apiKey.isEmpty()
                && !apiKey.startsWith("sk-xxxx")) {
            String cleanBaseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
            this.enabled = true;
            log.info("DeepSeek 促改分析客户端已初始化: baseUrl={}, model={}", cleanBaseUrl, model);
        } else {
            this.enabled = false;
            log.warn("DeepSeek 未正确配置，促改分析功能不可用");
        }
    }

    /**
     * 调用 DeepSeek Chat API
     *
     * @param systemPrompt 系统提示词（角色设定、任务说明）
     * @param userPrompt   用户提示词（案件事实、分析要求等）
     * @return AI 生成的分析内容
     */
    public String chat(String systemPrompt, String userPrompt) {
        if (!enabled) {
            throw new BusinessException(ErrorCode.AI_SERVICE_UNAVAILABLE);
        }

        String chatUrl = baseUrl + "/v1/chat/completions";

        try {
            // 构建请求体
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", model);

            JSONArray messages = new JSONArray();

            // 系统消息
            JSONObject systemMessage = new JSONObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", systemPrompt);
            messages.add(systemMessage);

            // 用户消息
            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", userPrompt);
            messages.add(userMessage);

            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.5);
            requestBody.put("max_tokens", 8000);

            RequestBody body = RequestBody.create(
                    requestBody.toJSONString(), MediaType.parse("application/json; charset=utf-8"));

            Request request = new Request.Builder()
                    .url(chatUrl)
                    .post(body)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .build();

            log.info("调用 DeepSeek API 生成促改分析...");
            try (Response response = okHttpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "无响应体";
                    throw new IOException(String.format("DeepSeek API 调用失败: HTTP %d, %s",
                            response.code(), errorBody));
                }

                String responseBody = response.body() != null ? response.body().string() : "";

                // 解析响应
                JSONObject respJson = JSON.parseObject(responseBody);
                JSONArray choices = respJson.getJSONArray("choices");

                if (choices != null && !choices.isEmpty()) {
                    String content = choices.getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content");
                    log.info("促改分析生成成功，内容长度: {} 字符", content != null ? content.length() : 0);
                    return content;
                }

                throw new BusinessException(ErrorCode.AI_EMPTY_RESPONSE);
            }
        } catch (IOException e) {
            log.error("DeepSeek API 调用失败", e);
            throw new BusinessException(ErrorCode.AI_API_CALL_FAILED.getCode(), "AI API 调用失败: " + e.getMessage());
        }
    }
}
