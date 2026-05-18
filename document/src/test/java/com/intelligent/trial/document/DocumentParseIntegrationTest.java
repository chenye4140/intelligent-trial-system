package com.intelligent.trial.document;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intelligent.trial.document.util.MinioUtil;
import com.intelligent.trial.repository.service.DocumentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 文档解析模块集成测试 — 使用 H2 内存数据库，
 * 对 DocumentParseController 核心 REST API 进行端到端 HTTP 级别测试。
 */
@SpringBootTest(classes = DocumentTestApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("文档解析模块集成测试")
class DocumentParseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Mock 跨模块依赖
    @MockBean
    private DocumentService documentService;

    // Mock MinIO 工具类
    @MockBean
    private MinioUtil minioUtil;

    private String toJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    private Long extractId(MvcResult result) throws Exception {
        String json = result.getResponse().getContentAsString();
        return objectMapper.readTree(json).get("data").asLong();
    }

    // ==================== 任务状态查询 ====================

    @Test
    @DisplayName("GET /api/document/parse/task/{taskId} - 查询已完成任务成功")
    void getTaskStatus_completed_success() throws Exception {
        mockMvc.perform(get("/api/document/parse/task/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.fileName").value("test_case_001.pdf"))
                .andExpect(jsonPath("$.data.status").value(2))
                .andExpect(jsonPath("$.data.progress").value(100))
                .andExpect(jsonPath("$.data.fileType").value("pdf"));
    }

    @Test
    @DisplayName("GET /api/document/parse/task/{taskId} - 查询处理中任务")
    void getTaskStatus_processing() throws Exception {
        mockMvc.perform(get("/api/document/parse/task/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value(1))
                .andExpect(jsonPath("$.data.progress").value(60));
    }

    @Test
    @DisplayName("GET /api/document/parse/task/{taskId} - 查询失败任务")
    void getTaskStatus_failed() throws Exception {
        mockMvc.perform(get("/api/document/parse/task/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value(3))
                .andExpect(jsonPath("$.data.errorMsg").value("OCR service call timeout"));
    }

    @Test
    @DisplayName("GET /api/document/parse/task/{taskId} - 不存在的任务返回404")
    void getTaskStatus_notFound() throws Exception {
        mockMvc.perform(get("/api/document/parse/task/9999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }

    // ==================== 分页查询任务列表 ====================

    @Test
    @DisplayName("GET /api/document/parse/tasks - 分页查询默认返回全部任务")
    void listTasks_default_returnsAll() throws Exception {
        mockMvc.perform(get("/api/document/parse/tasks")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(3))
                .andExpect(jsonPath("$.data.list").isArray())
                .andExpect(jsonPath("$.data.list.length()").value(3));
    }

    @Test
    @DisplayName("GET /api/document/parse/tasks - 按状态过滤（已完成）")
    void listTasks_filterByStatus() throws Exception {
        mockMvc.perform(get("/api/document/parse/tasks")
                        .param("pageNum", "1")
                        .param("pageSize", "10")
                        .param("status", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].status").value(2));
    }

    @Test
    @DisplayName("GET /api/document/parse/tasks - 按文件类型过滤（pdf）")
    void listTasks_filterByFileType() throws Exception {
        mockMvc.perform(get("/api/document/parse/tasks")
                        .param("pageNum", "1")
                        .param("pageSize", "10")
                        .param("fileType", "pdf"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].fileType").value("pdf"));
    }

    @Test
    @DisplayName("GET /api/document/parse/tasks - 按状态过滤（处理中）")
    void listTasks_filterByProcessing() throws Exception {
        mockMvc.perform(get("/api/document/parse/tasks")
                        .param("pageNum", "1")
                        .param("pageSize", "10")
                        .param("status", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    @DisplayName("GET /api/document/parse/tasks - 按不存在的状态过滤返回空列表")
    void listTasks_filterByNonExistentStatus() throws Exception {
        mockMvc.perform(get("/api/document/parse/tasks")
                        .param("pageNum", "1")
                        .param("pageSize", "10")
                        .param("status", "99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(0))
                .andExpect(jsonPath("$.data.list.length()").value(0));
    }

    @Test
    @DisplayName("GET /api/document/parse/tasks - 分页限制条数")
    void listTasks_pagination_limit() throws Exception {
        mockMvc.perform(get("/api/document/parse/tasks")
                        .param("pageNum", "1")
                        .param("pageSize", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(3))
                .andExpect(jsonPath("$.data.list.length()").value(2));
    }

    // ==================== 获取解析结果 ====================

    @Test
    @DisplayName("GET /api/document/parse/task/{taskId}/result - 获取已完成任务的解析结果")
    void getTaskResult_completed_success() throws Exception {
        mockMvc.perform(get("/api/document/parse/task/1/result"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isString());
    }

    @Test
    @DisplayName("GET /api/document/parse/task/{taskId}/result - 处理中的任务返回错误")
    void getTaskResult_processing_returnsError() throws Exception {
        mockMvc.perform(get("/api/document/parse/task/2/result"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("GET /api/document/parse/task/{taskId}/result - 失败的任务返回错误")
    void getTaskResult_failed_returnsError() throws Exception {
        mockMvc.perform(get("/api/document/parse/task/3/result"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("GET /api/document/parse/task/{taskId}/result - 不存在的任务返回404")
    void getTaskResult_notFound() throws Exception {
        mockMvc.perform(get("/api/document/parse/task/9999/result"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }

    // ==================== 删除任务 ====================

    @Test
    @DisplayName("DELETE /api/document/parse/task/{taskId} - 删除任务成功")
    void deleteTask_success() throws Exception {
        mockMvc.perform(delete("/api/document/parse/task/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 验证删除后查询返回404
        mockMvc.perform(get("/api/document/parse/task/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    @DisplayName("DELETE /api/document/parse/task/{taskId} - 删除不存在的任务返回404")
    void deleteTask_notFound() throws Exception {
        mockMvc.perform(delete("/api/document/parse/task/9999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }

    // ==================== 完整生命周期 ====================

    @Test
    @DisplayName("完整生命周期：查询→获取结果→删除→验证删除")
    void fullLifecycle() throws Exception {
        // 1. Query task status
        mockMvc.perform(get("/api/document/parse/task/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value(2));

        // 2. Get parse result
        mockMvc.perform(get("/api/document/parse/task/1/result"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 3. Delete task
        mockMvc.perform(delete("/api/document/parse/task/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 4. Verify deletion
        mockMvc.perform(get("/api/document/parse/task/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }
}
