package com.intelligent.trial.auth.client;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.intelligent.trial.auth.config.DashScopeConfig;
import com.intelligent.trial.common.exception.BusinessException;
import com.intelligent.trial.common.exception.ErrorCode;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * DeepSeek AI 客户端
 * 用于调用 DeepSeek API 进行定密建议分析
 */
@Component
public class DeepSeekClient {

    private static final Logger log = LoggerFactory.getLogger(DeepSeekClient.class);

    @Autowired
    private DashScopeConfig dashScopeConfig;

    private OkHttpClient okHttpClient;

    private String chatApiUrl;
    private boolean deepseekEnabled;

    @PostConstruct
    public void init() {
        okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(180, TimeUnit.SECONDS)
                .readTimeout(300, TimeUnit.SECONDS)
                .writeTimeout(180, TimeUnit.SECONDS)
                .build();

        // DeepSeek 端点
        String deepseekBaseUrl = dashScopeConfig.getDeepseekBaseUrl();
        String deepseekApiKey = dashScopeConfig.getDeepseekApiKey();
        if (deepseekBaseUrl != null && !deepseekBaseUrl.isEmpty()
                && deepseekApiKey != null && !deepseekApiKey.isEmpty()) {
            if (deepseekBaseUrl.endsWith("/")) {
                deepseekBaseUrl = deepseekBaseUrl.substring(0, deepseekBaseUrl.length() - 1);
            }
            this.chatApiUrl = deepseekBaseUrl + "/chat/completions";
            this.deepseekEnabled = true;
            log.info("DeepSeek 客户端已初始化: baseUrl={}, model={}",
                    deepseekBaseUrl, dashScopeConfig.getDeepseekModel());
        } else {
            this.deepseekEnabled = false;
            log.warn("DeepSeek 未配置，定密建议功能不可用");
        }
    }

    /**
     * 调用 DeepSeek Chat API 生成分析结果
     *
     * @param systemPrompt 系统提示词
     * @param userPrompt   用户提示词
     * @return AI 生成的文本内容
     */
    public String generateContent(String systemPrompt, String userPrompt) {
        if (!deepseekEnabled) {
            throw new BusinessException(ErrorCode.AI_SERVICE_UNAVAILABLE);
        }

        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", dashScopeConfig.getDeepseekModel());

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
            requestBody.put("temperature", 0.3);
            requestBody.put("max_tokens", 2000);

            RequestBody body = RequestBody.create(
                    requestBody.toJSONString(), MediaType.parse("application/json; charset=utf-8"));

            Request request = new Request.Builder()
                    .url(chatApiUrl)
                    .post(body)
                    .header("Authorization", "Bearer " + dashScopeConfig.getDeepseekApiKey())
                    .header("Content-Type", "application/json")
                    .build();

            log.info("调用 DeepSeek API 进行定密分析...");
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
                    log.info("定密分析成功，内容长度: {} 字符", content != null ? content.length() : 0);
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
