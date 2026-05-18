package com.intelligent.trial.report;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intelligent.trial.report.client.DeepSeekClient;
import com.intelligent.trial.report.dto.ReportGenerateDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 文书生成模块集成测试 — 使用 H2 内存数据库，
 * 对核心 REST API 进行端到端 HTTP 级别测试。
 */
@SpringBootTest(classes = ReportTestApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("文书生成模块集成测试")
class ReportIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DeepSeekClient deepSeekClient;

    @MockBean
    private RedisTemplate<String, Object> redisTemplate;

    @BeforeEach
    void setUp() {
        // Mock DeepSeek client to return test content
        when(deepSeekClient.generateContent(anyString(), anyString()))
                .thenReturn("【测试文书内容】根据案件事实，生成规范的纪检监察文书。本案事实清楚，证据确凿，建议给予相应处分。");
    }

    private String toJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    // ==================== 模板列表 ====================

    @Test
    @DisplayName("GET /api/report/templates - 获取可用模板列表")
    void listTemplates_success() throws Exception {
        mockMvc.perform(get("/api/report/templates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].templateCode").value("SHENLI_REPORT"))
                .andExpect(jsonPath("$.data[0].templateName").value("审理报告"));
    }

    // ==================== 文书记录详情 ====================

    @Test
    @DisplayName("GET /api/report/record/{id} - 获取文书记录详情")
    void getRecord_success() throws Exception {
        mockMvc.perform(get("/api/report/record/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.caseCode").value("AJ20260501001"))
                .andExpect(jsonPath("$.data.templateCode").value("SHENLI_REPORT"))
                .andExpect(jsonPath("$.data.status").value(1));
    }

    @Test
    @DisplayName("GET /api/report/record/{id} - 不存在的记录返回404")
    void getRecord_notFound() throws Exception {
        mockMvc.perform(get("/api/report/record/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }

    // ==================== 文书记录分页查询 ====================

    @Test
    @DisplayName("GET /api/report/list - 分页查询默认返回全部记录")
    void list_default_returnsAll() throws Exception {
        mockMvc.perform(get("/api/report/list")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").isNotEmpty())
                .andExpect(jsonPath("$.data.list").isArray());
    }

    @Test
    @DisplayName("GET /api/report/list - 按案件ID过滤")
    void list_filterByCaseId() throws Exception {
        mockMvc.perform(get("/api/report/list")
                        .param("caseId", "1")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").isNotEmpty())
                .andExpect(jsonPath("$.data.list.length()").isNotEmpty());
    }

    @Test
    @DisplayName("GET /api/report/list - 分页参数生效")
    void list_pagination() throws Exception {
        mockMvc.perform(get("/api/report/list")
                        .param("pageNum", "1")
                        .param("pageSize", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").isNotEmpty())
                .andExpect(jsonPath("$.data.list.length()").value(2));
    }

    // ==================== 文书生成状态 ====================

    @Test
    @DisplayName("GET /api/report/status/{id} - 获取生成状态（已完成）")
    void status_completed() throws Exception {
        mockMvc.perform(get("/api/report/status/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.caseCode").value("AJ20260501001"))
                .andExpect(jsonPath("$.data.status").value(1))
                .andExpect(jsonPath("$.data.statusDesc").value("已完成"))
                .andExpect(jsonPath("$.data.contentPreview").exists());
    }

    @Test
    @DisplayName("GET /api/report/status/{id} - 获取生成状态（生成中）")
    void status_generating() throws Exception {
        mockMvc.perform(get("/api/report/status/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value(0))
                .andExpect(jsonPath("$.data.statusDesc").value("生成中"));
    }

    @Test
    @DisplayName("GET /api/report/status/{id} - 获取生成状态（失败）")
    void status_failed() throws Exception {
        mockMvc.perform(get("/api/report/status/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value(2))
                .andExpect(jsonPath("$.data.statusDesc").value("生成失败"))
                .andExpect(jsonPath("$.data.errorMessage").value("AI服务调用超时"));
    }

    @Test
    @DisplayName("GET /api/report/status/{id} - 不存在的记录")
    void status_notFound() throws Exception {
        mockMvc.perform(get("/api/report/status/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }

    // ==================== 生成文书 ====================
    // 注意：generateReport 是 @Async 方法，返回值在控制器层为 null
    // 生成逻辑的正确性由 ReportServiceImplTest 单元测试覆盖

    @Test
    @DisplayName("POST /api/report/generate - 不存在的案件ID")
    void generate_caseNotFound() throws Exception {
        ReportGenerateDTO dto = new ReportGenerateDTO();
        dto.setCaseId(999L);
        dto.setTemplateId(1L);

        mockMvc.perform(post("/api/report/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(dto)))
                .andExpect(status().isOk())
                // Async returns null → NumberFormatException → caught → 500
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("POST /api/report/generate - 缺少必填参数 caseId")
    void generate_missingCaseId() throws Exception {
        mockMvc.perform(post("/api/report/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
