package com.intelligent.trial.document;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intelligent.trial.document.client.LlmClient;
import com.intelligent.trial.document.service.VectorStorageService;
import com.intelligent.trial.document.util.MinioUtil;
import com.intelligent.trial.repository.service.DocumentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 类案搜索模块集成测试 — 使用 H2 内存数据库，
 * 对 CaseSimilarityController 核心 REST API 进行端到端 HTTP 级别测试。
 */
@SpringBootTest(classes = DocumentTestApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("类案搜索模块集成测试")
class CaseSimilarityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Mock 跨模块依赖
    @MockBean
    private DocumentService documentService;

    @MockBean
    private MinioUtil minioUtil;

    // Mock LLM 客户端 — 类案搜索基于文本时需要生成向量
    @MockBean
    private LlmClient llmClient;

    // Mock 向量存储服务 — 相似度搜索依赖此服务
    @MockBean
    private VectorStorageService vectorStorageService;

    private String toJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    /**
     * 构造搜索请求 JSON
     */
    private String searchByCaseId(Long caseId, Integer limit) throws Exception {
        return objectMapper.createObjectNode()
                .put("caseId", caseId)
                .put("limit", limit != null ? limit : 10)
                .toString();
    }

    private String searchByText(String text, Integer limit) throws Exception {
        return objectMapper.createObjectNode()
                .put("text", text)
                .put("limit", limit != null ? limit : 10)
                .toString();
    }

    // ==================== 基于 caseId 搜索 ====================

    @Test
    @DisplayName("POST /api/document/similarity/search - 基于 caseId 搜索相似段落")
    void searchSimilar_byCaseId_success() throws Exception {
        // Mock vectorStorageService.getByTaskId — 返回任务1的向量
        List<com.intelligent.trial.document.entity.DocParagraphVector> vectors = new ArrayList<>();
        com.intelligent.trial.document.entity.DocParagraphVector v = 
                new com.intelligent.trial.document.entity.DocParagraphVector();
        v.setId(1L);
        v.setTaskId(1L);
        v.setParagraphIndex(0);
        v.setContent("Defendant Zhang San committed theft");
        v.setVectorData("[0.12,0.34,0.56,0.78,0.23,0.45,0.67,0.89,0.11,0.33]");
        v.setVectorDimension(10);
        vectors.add(v);
        when(vectorStorageService.getByTaskId(1L)).thenReturn(vectors);

        // Mock vectorStorageService.searchSimilar — 返回相似结果
        List<VectorStorageService.SimilarParagraphResult> results = new ArrayList<>();
        VectorStorageService.SimilarParagraphResult r1 = new VectorStorageService.SimilarParagraphResult();
        r1.setId(2L);
        r1.setTaskId(1L);
        r1.setParagraphIndex(1);
        r1.setContent("Similar theft case paragraph");
        r1.setCategory("案件事实");
        r1.setLawLevel("条");
        r1.setSimilarity(0.95);
        results.add(r1);

        VectorStorageService.SimilarParagraphResult r2 = new VectorStorageService.SimilarParagraphResult();
        r2.setId(3L);
        r2.setTaskId(1L);
        r2.setParagraphIndex(2);
        r2.setContent("Another similar paragraph about legal basis");
        r2.setCategory("法律依据");
        r2.setLawLevel("条");
        r2.setSimilarity(0.85);
        results.add(r2);

        float[] queryVector = new float[]{0.12f, 0.34f, 0.56f, 0.78f, 0.23f, 0.45f, 0.67f, 0.89f, 0.11f, 0.33f};
        when(vectorStorageService.searchSimilar(ArgumentMatchers.eq(queryVector), anyInt(), isNull()))
                .thenReturn(results);

        mockMvc.perform(post("/api/document/similarity/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(searchByCaseId(1L, 5)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].similarity").value(0.95))
                .andExpect(jsonPath("$.data[0].caseId").value(1));
    }

    @Test
    @DisplayName("POST /api/document/similarity/search - 基于不存在的 caseId 返回错误")
    void searchSimilar_byCaseId_notFound() throws Exception {
        mockMvc.perform(post("/api/document/similarity/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(searchByCaseId(9999L, 10)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("POST /api/document/similarity/search - 基于未完成解析的 caseId 返回错误")
    void searchSimilar_byCaseId_notCompleted() throws Exception {
        // Task 2 is in processing state (status=1)
        mockMvc.perform(post("/api/document/similarity/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(searchByCaseId(2L, 10)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("POST /api/document/similarity/search - 基于无向量的 caseId 返回错误")
    void searchSimilar_byCaseId_noVector() throws Exception {
        // Task 3 is failed, so it has no vectors
        mockMvc.perform(post("/api/document/similarity/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(searchByCaseId(3L, 10)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    // ==================== 基于 text 搜索 ====================

    @Test
    @DisplayName("POST /api/document/similarity/search - 基于文本搜索相似段落")
    void searchSimilar_byText_success() throws Exception {
        // Mock LlmClient.generateEmbeddingBatch — 返回查询向量
        float[] queryVector = new float[]{0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f, 0.9f, 1.0f};
        when(llmClient.generateEmbeddingBatch(ArgumentMatchers.anyList()))
                .thenReturn(Collections.singletonList(queryVector));

        // Mock vectorStorageService.searchSimilar — 返回相似结果
        List<VectorStorageService.SimilarParagraphResult> results = new ArrayList<>();
        VectorStorageService.SimilarParagraphResult r = new VectorStorageService.SimilarParagraphResult();
        r.setId(1L);
        r.setTaskId(1L);
        r.setParagraphIndex(0);
        r.setContent("Defendant Zhang San committed theft on March 15 2025");
        r.setCategory("案件事实");
        r.setSimilarity(0.88);
        results.add(r);

        when(vectorStorageService.searchSimilar(any(float[].class), anyInt(), isNull()))
                .thenReturn(results);

        mockMvc.perform(post("/api/document/similarity/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(searchByText("被告人盗窃他人财物", 10)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].similarity").value(0.88));
    }

    @Test
    @DisplayName("POST /api/document/similarity/search - 基于文本搜索但向量生成失败")
    void searchSimilar_byText_embeddingFailed() throws Exception {
        // Mock LlmClient to return empty vector list
        when(llmClient.generateEmbeddingBatch(ArgumentMatchers.anyList()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(post("/api/document/similarity/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(searchByText("test text", 10)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("POST /api/document/similarity/search - 既无 caseId 也无 text 返回错误")
    void searchSimilar_noParams() throws Exception {
        String body = objectMapper.createObjectNode()
                .put("limit", 10)
                .toString();

        mockMvc.perform(post("/api/document/similarity/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("POST /api/document/similarity/search - 基于文本搜索带分类过滤")
    void searchSimilar_byText_withCategory() throws Exception {
        // Mock LlmClient
        float[] queryVector = new float[10];
        for (int i = 0; i < 10; i++) queryVector[i] = 0.1f * i;
        when(llmClient.generateEmbeddingBatch(ArgumentMatchers.anyList()))
                .thenReturn(Collections.singletonList(queryVector));

        // Mock with category filter
        List<VectorStorageService.SimilarParagraphResult> results = new ArrayList<>();
        VectorStorageService.SimilarParagraphResult r = new VectorStorageService.SimilarParagraphResult();
        r.setId(1L);
        r.setTaskId(1L);
        r.setParagraphIndex(0);
        r.setContent("Legal basis paragraph");
        r.setCategory("法律依据");
        r.setSimilarity(0.92);
        results.add(r);

        when(vectorStorageService.searchSimilar(any(float[].class), anyInt(), eq("法律依据")))
                .thenReturn(results);

        String body = objectMapper.createObjectNode()
                .put("text", "刑法规定")
                .put("limit", 5)
                .put("category", "法律依据")
                .toString();

        mockMvc.perform(post("/api/document/similarity/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].category").value("法律依据"));
    }

    // ==================== 带 limit 的搜索 ====================

    @Test
    @DisplayName("POST /api/document/similarity/search - 基于文本搜索限制返回数量")
    void searchSimilar_byText_limitResults() throws Exception {
        // Mock LlmClient
        float[] queryVector = new float[10];
        for (int i = 0; i < 10; i++) queryVector[i] = 0.1f * (i + 1);
        when(llmClient.generateEmbeddingBatch(ArgumentMatchers.anyList()))
                .thenReturn(Collections.singletonList(queryVector));

        // Return only 2 results
        List<VectorStorageService.SimilarParagraphResult> results = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            VectorStorageService.SimilarParagraphResult r = new VectorStorageService.SimilarParagraphResult();
            r.setId((long) (i + 1));
            r.setTaskId(1L);
            r.setParagraphIndex(i);
            r.setContent("Result " + i);
            r.setSimilarity(0.9 - i * 0.05);
            results.add(r);
        }

        when(vectorStorageService.searchSimilar(any(float[].class), eq(2), isNull()))
                .thenReturn(results);

        mockMvc.perform(post("/api/document/similarity/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(searchByText("test query", 2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(2));
    }
}
