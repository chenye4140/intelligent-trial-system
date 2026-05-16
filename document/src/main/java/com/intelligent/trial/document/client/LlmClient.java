package com.intelligent.trial.document.client;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.intelligent.trial.common.exception.BusinessException;
import com.intelligent.trial.common.exception.ErrorCode;
import com.intelligent.trial.document.config.DashScopeConfig;
import com.intelligent.trial.document.dto.ParseResultDTO;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * 大模型 API 客户端
 * 支持 DashScope（通义千问）和 DeepSeek 两种后端
 * 提供：段落分类、OCR识别、向量生成等功能
 *
 * @author intelligent-trial
 */
@Component
public class LlmClient {

    private static final Logger log = LoggerFactory.getLogger(LlmClient.class);

    @Autowired
    private DashScopeConfig dashScopeConfig;

    private OkHttpClient okHttpClient;

    // DashScope 端点
    private String chatApiUrl;
    private String embeddingApiUrl;

    // DeepSeek 端点
    private String deepseekChatApiUrl;
    private boolean deepseekEnabled;

    @PostConstruct
    public void init() {
        okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
                .build();

        // DashScope 端点
        String baseUrl = dashScopeConfig.getBaseUrl();
        if (baseUrl != null && baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        this.chatApiUrl = baseUrl + "/chat/completions";
        this.embeddingApiUrl = baseUrl + "/embeddings";

        // DeepSeek 端点
        String deepseekBaseUrl = dashScopeConfig.getDeepseekBaseUrl();
        String deepseekApiKey = dashScopeConfig.getDeepseekApiKey();
        if (deepseekBaseUrl != null && !deepseekBaseUrl.isEmpty()
                && deepseekApiKey != null && !deepseekApiKey.isEmpty()) {
            if (deepseekBaseUrl.endsWith("/")) {
                deepseekBaseUrl = deepseekBaseUrl.substring(0, deepseekBaseUrl.length() - 1);
            }
            this.deepseekChatApiUrl = deepseekBaseUrl + "/chat/completions";
            this.deepseekEnabled = true;
            log.info("DeepSeek 配置已启用: baseUrl={}, model={}",
                    deepseekBaseUrl, dashScopeConfig.getDeepseekModel());
        } else {
            this.deepseekEnabled = false;
            log.info("DeepSeek 未配置，使用 DashScope 默认配置");
        }

        log.info("LlmClient 初始化完成: baseUrl={}, classifyModel={}, ocrModel={}, embeddingModel={}, deepseekEnabled={}",
                baseUrl, dashScopeConfig.getClassifyModel(),
                dashScopeConfig.getOcrModel(), dashScopeConfig.getEmbeddingModel(),
                deepseekEnabled);
    }

    // ========================= 段落分类 =========================

    /**
     * 对段落列表进行分类
     * 优先使用 DeepSeek 模型
     *
     * @param paragraphs 待分类的段落列表
     * @return 分类后的段落列表
     */
    public List<ParseResultDTO.ParagraphDTO> classifyParagraphs(List<ParseResultDTO.ParagraphDTO> paragraphs) {
        if (paragraphs == null || paragraphs.isEmpty()) {
            return paragraphs;
        }

        // 分批处理，避免单次请求过长
        int batchSize = 20;
        List<ParseResultDTO.ParagraphDTO> result = new ArrayList<>();

        for (int i = 0; i < paragraphs.size(); i += batchSize) {
            int end = Math.min(i + batchSize, paragraphs.size());
            List<ParseResultDTO.ParagraphDTO> batch = paragraphs.subList(i, end);
            classifyBatch(batch);
            result.addAll(batch);
        }

        return result;
    }

    /**
     * 批量分类段落（优先使用 DeepSeek，降级使用通义千问）
     */
    private void classifyBatch(List<ParseResultDTO.ParagraphDTO> batch) {
        // 构建分类 prompt
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个专业的法律文书分析助手。请对以下段落进行分类和层级识别。\n\n");
        prompt.append("## 分类类型（category，只能选一个）：\n");
        prompt.append("- 总则：法规的基本原则、适用范围、定义等\n");
        prompt.append("- 分则：法规的具体规定、行为规范等\n");
        prompt.append("- 附则：法规的生效时间、解释权、废止说明等\n");
        prompt.append("- 法律责任：违法处罚、处分、强制措施等\n");
        prompt.append("- 案件事实：案件的具体事实描述\n");
        prompt.append("- 处理意见：处理决定、意见、结论等\n");
        prompt.append("- 法律依据：引用的法律法规条文\n");
        prompt.append("\n## 法规层级（law_level）：\n");
        prompt.append("篇、章、节、条、款、项，如果不属于上述层级则为 null\n\n");
        prompt.append("## 输出格式：\n");
        prompt.append("JSON数组，每个元素包含：index(对应输入段落序号从0开始), category, law_level\n\n");
        prompt.append("## 待分类段落：\n");

        for (int i = 0; i < batch.size(); i++) {
            ParseResultDTO.ParagraphDTO p = batch.get(i);
            prompt.append(String.format("[%d][%s] %s\n", i, p.getStyle(), p.getContent()));
        }

        try {
            String response;
            // 优先使用 DeepSeek
            if (deepseekEnabled) {
                response = callDeepseekChatApi(prompt.toString());
            } else {
                response = callChatApi(prompt.toString());
            }

            JSONObject respJson = JSON.parseObject(response);
            JSONArray choices = respJson.getJSONArray("choices");

            if (choices != null && !choices.isEmpty()) {
                String content = choices.getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content");

                // 提取 JSON 数组
                content = extractJsonArray(content);
                JSONArray results = JSON.parseArray(content);

                for (int i = 0; i < results.size(); i++) {
                    JSONObject item = results.getJSONObject(i);
                    int index = item.getIntValue("index");
                    if (index >= 0 && index < batch.size()) {
                        ParseResultDTO.ParagraphDTO p = batch.get(index);
                        p.setCategory(item.getString("category"));
                        p.setLawLevel(item.getString("law_level"));
                    }
                }
                log.debug("分类成功，本批处理 {} 个段落", batch.size());
            }
        } catch (Exception e) {
            log.error("段落分类失败，本批 {} 个段落", batch.size(), e);
            // 分类失败不影响主流程，段落保留但无分类信息
        }
    }

    /**
     * 从 LLM 回复中提取 JSON 数组字符串
     */
    private String extractJsonArray(String content) {
        if (content == null) return "[]";

        // 尝试直接解析
        if (content.trim().startsWith("[")) {
            // 移除可能的 markdown 标记
            content = content.replaceAll("```json\\s*", "").replaceAll("```\\s*$", "").trim();
            return content;
        }

        // 在文本中查找 JSON 数组
        int start = content.indexOf('[');
        int end = content.lastIndexOf(']');
        if (start >= 0 && end > start) {
            return content.substring(start, end + 1);
        }

        return "[]";
    }

    // ========================= OCR 识别 =========================

    /**
     * 调用 qwen-vl-max 进行图片 OCR 识别
     *
     * @param imageUrl    图片的公开访问URL
     * @param description 识别描述提示
     * @return 识别出的文本
     */
    public String recognizeImage(String imageUrl, String description) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", dashScopeConfig.getOcrModel());

            // 构建消息
            JSONArray messages = new JSONArray();
            JSONObject message = new JSONObject();
            message.put("role", "user");

            JSONArray contentArray = new JSONArray();

            // 图片内容
            JSONObject imageContent = new JSONObject();
            imageContent.put("type", "image_url");
            JSONObject imageUrlObj = new JSONObject();
            imageUrlObj.put("url", imageUrl);
            imageContent.put("image_url", imageUrlObj);
            contentArray.add(imageContent);

            // 文本提示
            JSONObject textContent = new JSONObject();
            textContent.put("type", "text");
            textContent.put("text", description != null ? description : "请识别并提取图片中的所有文字内容，按原文排版输出。");
            contentArray.add(textContent);

            message.put("content", contentArray);
            messages.add(message);
            requestBody.put("messages", messages);

            String response = callChatApiJson(requestBody.toJSONString());
            JSONObject respJson = JSON.parseObject(response);
            JSONArray choices = respJson.getJSONArray("choices");

            if (choices != null && !choices.isEmpty()) {
                return choices.getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content");
            }
            return "";
        } catch (Exception e) {
            log.error("OCR识别失败: {}", imageUrl, e);
            throw new BusinessException(ErrorCode.DOC_PARSE_FAILED.getCode(), "OCR识别失败: " + e.getMessage());
        }
    }

    /**
     * 调用 qwen-vl-max 进行图片 OCR 识别（使用 Base64 编码图片）
     *
     * @param imageBase64 Base64编码的图片数据
     * @param mimeType    图片 MIME 类型
     * @param description 识别描述提示
     * @return 识别出的文本
     */
    public String recognizeImageBase64(String imageBase64, String mimeType, String description) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", dashScopeConfig.getOcrModel());

            JSONArray messages = new JSONArray();
            JSONObject message = new JSONObject();
            message.put("role", "user");

            JSONArray contentArray = new JSONArray();

            // Base64 图片
            JSONObject imageContent = new JSONObject();
            imageContent.put("type", "image_url");
            JSONObject imageUrlObj = new JSONObject();
            imageUrlObj.put("url", "data:" + mimeType + ";base64," + imageBase64);
            imageContent.put("image_url", imageUrlObj);
            contentArray.add(imageContent);

            // 文本提示
            JSONObject textContent = new JSONObject();
            textContent.put("type", "text");
            textContent.put("text", description != null ? description : "请识别并提取图片中的所有文字内容，按原文排版输出。");
            contentArray.add(textContent);

            message.put("content", contentArray);
            messages.add(message);
            requestBody.put("messages", messages);

            String response = callChatApiJson(requestBody.toJSONString());
            JSONObject respJson = JSON.parseObject(response);
            JSONArray choices = respJson.getJSONArray("choices");

            if (choices != null && !choices.isEmpty()) {
                return choices.getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content");
            }
            return "";
        } catch (Exception e) {
            log.error("Base64 OCR识别失败", e);
            throw new BusinessException(ErrorCode.DOC_PARSE_FAILED.getCode(), "Base64 OCR识别失败: " + e.getMessage());
        }
    }

    // ========================= 向量生成 =========================

    /**
     * 调用 text-embedding-v3 生成向量
     *
     * @param texts 待生成向量的文本列表
     * @return 向量列表（每个向量是一个 float 数组）
     */
    public List<float[]> generateEmbeddings(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            return new ArrayList<>();
        }

        List<float[]> embeddings = new ArrayList<>();
        // 分批处理（API 通常限制单次最多 25 条）
        int batchSize = 25;

        for (int i = 0; i < texts.size(); i += batchSize) {
            int end = Math.min(i + batchSize, texts.size());
            List<String> batch = texts.subList(i, end);
            List<float[]> batchEmbeddings = generateEmbeddingBatch(batch);
            embeddings.addAll(batchEmbeddings);
        }

        return embeddings;
    }

    /**
     * 批量生成向量
     */
    public List<float[]> generateEmbeddingBatch(List<String> texts) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", dashScopeConfig.getEmbeddingModel());
            requestBody.put("input", new JSONArray(texts));

            String response = callEmbeddingApi(JSON.toJSONString(requestBody));
            JSONObject respJson = JSON.parseObject(response);
            JSONArray data = respJson.getJSONArray("data");

            List<float[]> result = new ArrayList<>();
            if (data != null) {
                for (int i = 0; i < data.size(); i++) {
                    JSONArray embedding = data.getJSONObject(i).getJSONArray("embedding");
                    float[] vector = new float[embedding.size()];
                    for (int j = 0; j < embedding.size(); j++) {
                        vector[j] = embedding.getFloatValue(j);
                    }
                    result.add(vector);
                }
            }
            log.debug("向量生成成功，本批 {} 条，维度: {}", texts.size(),
                    result.isEmpty() ? 0 : result.get(0).length);
            return result;
        } catch (Exception e) {
            log.error("向量生成失败", e);
            throw new BusinessException(ErrorCode.DOC_VECTOR_FAILED.getCode(), "向量生成失败: " + e.getMessage());
        }
    }

    /**
     * 对单个文本生成向量
     */
    public float[] generateEmbedding(String text) {
        List<float[]> results = generateEmbeddingBatch(java.util.Collections.singletonList(text));
        return results.isEmpty() ? new float[0] : results.get(0);
    }

    // ========================= HTTP 请求方法 =========================

    /**
     * 调用 DashScope Chat API（简化版）
     */
    private String callChatApi(String prompt) throws IOException {
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", dashScopeConfig.getClassifyModel());

        JSONArray messages = new JSONArray();
        JSONObject message = new JSONObject();
        message.put("role", "user");
        message.put("content", prompt);
        messages.add(message);
        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.1);

        return callChatApiJson(requestBody.toJSONString());
    }

    /**
     * 调用 DeepSeek Chat API（简化版，优先使用 DeepSeek 模型）
     */
    private String callDeepseekChatApi(String prompt) throws IOException {
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", dashScopeConfig.getDeepseekModel());

        JSONArray messages = new JSONArray();
        JSONObject message = new JSONObject();
        message.put("role", "user");
        message.put("content", prompt);
        messages.add(message);
        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.1);

        RequestBody body = RequestBody.create(
                requestBody.toJSONString(), MediaType.parse("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(deepseekChatApiUrl)
                .post(body)
                .header("Authorization", "Bearer " + dashScopeConfig.getDeepseekApiKey())
                .header("Content-Type", "application/json")
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "无响应体";
                throw new IOException(String.format("DeepSeek Chat API 调用失败: HTTP %d, %s",
                        response.code(), errorBody));
            }
            return response.body() != null ? response.body().string() : "";
        }
    }

    /**
     * 调用 DashScope Chat API（自定义请求体）
     */
    private String callChatApiJson(String jsonBody) throws IOException {
        RequestBody body = RequestBody.create(
                jsonBody, MediaType.parse("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(chatApiUrl)
                .post(body)
                .header("Authorization", "Bearer " + dashScopeConfig.getApiKey())
                .header("Content-Type", "application/json")
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "无响应体";
                throw new IOException(String.format("Chat API 调用失败: HTTP %d, %s",
                        response.code(), errorBody));
            }
            return response.body() != null ? response.body().string() : "";
        }
    }

    /**
     * 调用 Embedding API
     */
    private String callEmbeddingApi(String jsonBody) throws IOException {
        RequestBody body = RequestBody.create(
                jsonBody, MediaType.parse("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(embeddingApiUrl)
                .post(body)
                .header("Authorization", "Bearer " + dashScopeConfig.getApiKey())
                .header("Content-Type", "application/json")
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "无响应体";
                throw new IOException(String.format("Embedding API 调用失败: HTTP %d, %s",
                        response.code(), errorBody));
            }
            return response.body() != null ? response.body().string() : "";
        }
    }
}
