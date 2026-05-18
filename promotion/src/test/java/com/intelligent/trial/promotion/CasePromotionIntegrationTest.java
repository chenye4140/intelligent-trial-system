package com.intelligent.trial.promotion;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intelligent.trial.promotion.client.DeepSeekClient;
import com.intelligent.trial.promotion.dto.CasePromotionGenerateDTO;
import com.intelligent.trial.promotion.entity.CasePromotion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 以案促改模块集成测试 — 使用 H2 内存数据库，
 * 对 CasePromotionController 核心 REST API 进行端到端 HTTP 级别测试。
 */
@SpringBootTest(classes = PromotionTestApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("以案促改模块集成测试")
class CasePromotionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DeepSeekClient deepSeekClient;

    @MockBean
    private RedisTemplate<String, String> redisTemplate;

    @MockBean
    private ValueOperations<String, String> valueOperations;

    private String toJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    private JsonNode parseResponse(String json) throws Exception {
        return objectMapper.readTree(json);
    }

    // ==================== 生成促改分析（异步） ====================

    @Test
    @DisplayName("POST /api/promotion/generate — 生成促改分析，返回 taskId")
    void generateAnalysis_success() throws Exception {
        // Mock Redis
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        doNothing().when(valueOperations).set(anyString(), eq("running"), eq(24L), any());

        CasePromotionGenerateDTO dto = new CasePromotionGenerateDTO();
        dto.setCaseId("AJ20260501001");
        dto.setAnalysisType("comprehensive");
        dto.setUserId(10L);
        dto.setTemplateId(100L);

        String response = mockMvc.perform(post("/api/promotion/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.taskId").isNotEmpty())
                .andExpect(jsonPath("$.data.message").isNotEmpty())
                .andReturn().getResponse().getContentAsString();

        JsonNode node = parseResponse(response);
        String taskId = node.get("data").get("taskId").asText();
        // Verify taskId format (32-char UUID without dashes)
        assert taskId.length() == 32 : "taskId should be 32 chars, got: " + taskId.length();
    }

    @Test
    @DisplayName("POST /api/promotion/generate — 缺少 caseId 返回验证错误")
    void generateAnalysis_missingCaseId() throws Exception {
        CasePromotionGenerateDTO dto = new CasePromotionGenerateDTO();
        dto.setAnalysisType("comprehensive");
        dto.setUserId(10L);

        mockMvc.perform(post("/api/promotion/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/promotion/generate — 不同分析类型")
    void generateAnalysis_differentTypes() throws Exception {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        doNothing().when(valueOperations).set(anyString(), eq("running"), eq(24L), any());

        String[] types = {"discipline", "management", "system", "comprehensive"};
        for (String type : types) {
            CasePromotionGenerateDTO dto = new CasePromotionGenerateDTO();
            dto.setCaseId("AJ20260501001");
            dto.setAnalysisType(type);
            dto.setUserId(10L);

            mockMvc.perform(post("/api/promotion/generate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    // ==================== 任务状态查询 ====================

    @Test
    @DisplayName("GET /api/promotion/status/{taskId} — 查询运行中的任务")
    void getAnalysisStatus_running() throws Exception {
        String taskId = "test-task-running";
        String redisKey = "promotion:task:" + taskId;

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(redisKey)).thenReturn("running");

        mockMvc.perform(get("/api/promotion/status/" + taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.taskId").value(taskId))
                .andExpect(jsonPath("$.data.status").value("running"))
                .andExpect(jsonPath("$.data.statusDesc").value("生成中"));
    }

    @Test
    @DisplayName("GET /api/promotion/status/{taskId} — 查询已完成的任务")
    void getAnalysisStatus_completed() throws Exception {
        String taskId = "test-task-completed";
        String redisKey = "promotion:task:" + taskId;

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(redisKey)).thenReturn("completed");

        mockMvc.perform(get("/api/promotion/status/" + taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("completed"))
                .andExpect(jsonPath("$.data.statusDesc").value("已完成"));
    }

    @Test
    @DisplayName("GET /api/promotion/status/{taskId} — 不存在的任务返回 404")
    void getAnalysisStatus_notFound() throws Exception {
        String taskId = "nonexistent-task";
        String redisKey = "promotion:task:" + taskId;

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(redisKey)).thenReturn(null);

        mockMvc.perform(get("/api/promotion/status/" + taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("not_found"))
                .andExpect(jsonPath("$.data.statusDesc").value("未知(not_found)"));
    }

    @Test
    @DisplayName("GET /api/promotion/status/{taskId} — 查询失败的任务")
    void getAnalysisStatus_failed() throws Exception {
        String taskId = "test-task-failed";
        String redisKey = "promotion:task:" + taskId;

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(redisKey)).thenReturn("failed:some error");

        mockMvc.perform(get("/api/promotion/status/" + taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("failed:some error"));
    }

    // ==================== 按 ID 查询 ====================

    @Test
    @DisplayName("GET /api/promotion/{id} — 按 ID 查询成功")
    void getById_success() throws Exception {
        mockMvc.perform(get("/api/promotion/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.caseId").value("AJ20260501001"))
                .andExpect(jsonPath("$.data.status").value(0));
    }

    @Test
    @DisplayName("GET /api/promotion/{id} — 不存在的 ID 返回 404")
    void getById_notFound() throws Exception {
        mockMvc.perform(get("/api/promotion/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    // ==================== 按案件 ID 查询 ====================

    @Test
    @DisplayName("GET /api/promotion/case/{caseId} — 按案件查询成功")
    void getByCaseId_success() throws Exception {
        mockMvc.perform(get("/api/promotion/case/AJ20260501001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.caseId").value("AJ20260501001"))
                .andExpect(jsonPath("$.data.status").value(0));
    }

    @Test
    @DisplayName("GET /api/promotion/case/{caseId} — 无记录返回 404")
    void getByCaseId_noRecord() throws Exception {
        mockMvc.perform(get("/api/promotion/case/NONEXISTENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    // ==================== 分页搜索 ====================

    @Test
    @DisplayName("GET /api/promotion/list — 默认分页返回全部记录")
    void list_default() throws Exception {
        mockMvc.perform(get("/api/promotion/list")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(3))
                .andExpect(jsonPath("$.data.pageNum").value(1))
                .andExpect(jsonPath("$.data.pageSize").value(10))
                .andExpect(jsonPath("$.data.list").isArray())
                .andExpect(jsonPath("$.data.list.length()").value(3));
    }

    @Test
    @DisplayName("GET /api/promotion/list — 按案件 ID 过滤")
    void list_filterByCaseId() throws Exception {
        mockMvc.perform(get("/api/promotion/list")
                        .param("pageNum", "1")
                        .param("pageSize", "10")
                        .param("caseId", "AJ20260501001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].caseId").value("AJ20260501001"));
    }

    @Test
    @DisplayName("GET /api/promotion/list — 按状态过滤")
    void list_filterByStatus() throws Exception {
        mockMvc.perform(get("/api/promotion/list")
                        .param("pageNum", "1")
                        .param("pageSize", "10")
                        .param("status", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].status").value(1));
    }

    @Test
    @DisplayName("GET /api/promotion/list — 按用户 ID 过滤")
    void list_filterByUserId() throws Exception {
        mockMvc.perform(get("/api/promotion/list")
                        .param("pageNum", "1")
                        .param("pageSize", "10")
                        .param("userId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(2));
    }

    @Test
    @DisplayName("GET /api/promotion/list — 分页大小限制")
    void list_pagination() throws Exception {
        mockMvc.perform(get("/api/promotion/list")
                        .param("pageNum", "1")
                        .param("pageSize", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(3))
                .andExpect(jsonPath("$.data.list.length()").value(2))
                .andExpect(jsonPath("$.data.pages").value(2));
    }

    @Test
    @DisplayName("GET /api/promotion/list — 无匹配返回空列表")
    void list_noMatch() throws Exception {
        mockMvc.perform(get("/api/promotion/list")
                        .param("pageNum", "1")
                        .param("pageSize", "10")
                        .param("caseId", "NONEXISTENT_CASE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(0))
                .andExpect(jsonPath("$.data.list.length()").value(0));
    }

    // ==================== 创建记录 ====================

    @Test
    @DisplayName("POST /api/promotion — 创建成功")
    void create_success() throws Exception {
        CasePromotion entity = new CasePromotion();
        entity.setCaseId("AJ20260501999");
        entity.setTemplateId(200L);
        entity.setContent("New promotion content");
        entity.setUserId(10L);

        String response = mockMvc.perform(post("/api/promotion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(entity)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.caseId").value("AJ20260501999"))
                .andExpect(jsonPath("$.data.content").value("New promotion content"))
                .andExpect(jsonPath("$.data.status").value(0))
                .andReturn().getResponse().getContentAsString();

        JsonNode node = parseResponse(response);
        Long id = node.get("data").get("id").asLong();
        assert id != null && id > 0 : "Created record should have a positive ID";
    }

    @Test
    @DisplayName("POST /api/promotion — 创建带指定状态的记录")
    void create_withStatus() throws Exception {
        CasePromotion entity = new CasePromotion();
        entity.setCaseId("AJ20260501998");
        entity.setTemplateId(201L);
        entity.setContent("Promotion with custom status");
        entity.setStatus(1);
        entity.setUserId(11L);

        mockMvc.perform(post("/api/promotion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(entity)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value(1));
    }

    // ==================== 更新记录 ====================

    @Test
    @DisplayName("PUT /api/promotion — 更新成功")
    void update_success() throws Exception {
        CasePromotion entity = new CasePromotion();
        entity.setId(1L);
        entity.setCaseId("AJ20260501001");
        entity.setContent("Updated content for test");

        mockMvc.perform(put("/api/promotion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(entity)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // Verify the update
        mockMvc.perform(get("/api/promotion/1"))
                .andExpect(jsonPath("$.data.content").value("Updated content for test"));
    }

    @Test
    @DisplayName("PUT /api/promotion — ID 为空更新失败")
    void update_nullId() throws Exception {
        CasePromotion entity = new CasePromotion();
        entity.setCaseId("AJ20260501001");
        entity.setContent("Content without ID");

        mockMvc.perform(put("/api/promotion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(entity)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    // ==================== 状态变更 ====================

    @Test
    @DisplayName("PUT /api/promotion/status/{id} — 变更为待审核")
    void updateStatus_toPending() throws Exception {
        mockMvc.perform(put("/api/promotion/status/1")
                        .param("status", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/api/promotion/1"))
                .andExpect(jsonPath("$.data.status").value(1));
    }

    @Test
    @DisplayName("PUT /api/promotion/status/{id} — 变更为已通过")
    void updateStatus_toApproved() throws Exception {
        mockMvc.perform(put("/api/promotion/status/1")
                        .param("status", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/api/promotion/1"))
                .andExpect(jsonPath("$.data.status").value(2));
    }

    @Test
    @DisplayName("PUT /api/promotion/status/{id} — 变更为已驳回")
    void updateStatus_toRejected() throws Exception {
        mockMvc.perform(put("/api/promotion/status/2")
                        .param("status", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("PUT /api/promotion/status/{id} — 不存在的记录")
    void updateStatus_notFound() throws Exception {
        mockMvc.perform(put("/api/promotion/status/999")
                        .param("status", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    // ==================== 删除记录 ====================

    @Test
    @DisplayName("DELETE /api/promotion/{id} — 删除成功")
    void delete_success() throws Exception {
        mockMvc.perform(delete("/api/promotion/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // Verify deletion
        mockMvc.perform(get("/api/promotion/1"))
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    @DisplayName("DELETE /api/promotion/{id} — 删除不存在的记录")
    void delete_notFound() throws Exception {
        mockMvc.perform(delete("/api/promotion/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    // ==================== 完整生命周期 ====================

    @Test
    @DisplayName("完整生命周期：创建 → 查询 → 更新 → 状态变更 → 删除")
    void fullLifecycle() throws Exception {
        // 1. Create
        CasePromotion entity = new CasePromotion();
        entity.setCaseId("AJ20260501997");
        entity.setTemplateId(300L);
        entity.setContent("Lifecycle test content");
        entity.setUserId(10L);

        String createResponse = mockMvc.perform(post("/api/promotion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(entity)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn().getResponse().getContentAsString();

        JsonNode node = parseResponse(createResponse);
        Long id = node.get("data").get("id").asLong();

        // 2. Query by ID
        mockMvc.perform(get("/api/promotion/" + id))
                .andExpect(jsonPath("$.data.caseId").value("AJ20260501997"))
                .andExpect(jsonPath("$.data.status").value(0));

        // 3. Update content
        CasePromotion updateEntity = new CasePromotion();
        updateEntity.setId(id);
        updateEntity.setCaseId("AJ20260501997");
        updateEntity.setContent("Updated lifecycle content");

        mockMvc.perform(put("/api/promotion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(updateEntity)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/promotion/" + id))
                .andExpect(jsonPath("$.data.content").value("Updated lifecycle content"));

        // 4. Change status to approved
        mockMvc.perform(put("/api/promotion/status/" + id)
                        .param("status", "2"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/promotion/" + id))
                .andExpect(jsonPath("$.data.status").value(2));

        // 5. Query by case ID
        mockMvc.perform(get("/api/promotion/case/AJ20260501997"))
                .andExpect(jsonPath("$.data.id").value(id));

        // 6. Delete
        mockMvc.perform(delete("/api/promotion/" + id))
                .andExpect(status().isOk());

        // 7. Verify deletion
        mockMvc.perform(get("/api/promotion/" + id))
                .andExpect(jsonPath("$.code").value(404));
    }

    // ==================== 生成分析 + 状态查询联合 ====================

    @Test
    @DisplayName("联合测试：生成分析 → 查询状态")
    void generateThenCheckStatus() throws Exception {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        doNothing().when(valueOperations).set(anyString(), eq("running"), eq(24L), any());

        CasePromotionGenerateDTO dto = new CasePromotionGenerateDTO();
        dto.setCaseId("AJ20260501001");
        dto.setAnalysisType("discipline");
        dto.setUserId(10L);

        // Generate
        String genResponse = mockMvc.perform(post("/api/promotion/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(dto)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode node = parseResponse(genResponse);
        String taskId = node.get("data").get("taskId").asText();

        // Mock the status for the subsequent check
        String redisKey = "promotion:task:" + taskId;
        when(valueOperations.get(redisKey)).thenReturn("running");

        // Check status
        mockMvc.perform(get("/api/promotion/status/" + taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.taskId").value(taskId))
                .andExpect(jsonPath("$.data.status").value("running"));
    }
}
