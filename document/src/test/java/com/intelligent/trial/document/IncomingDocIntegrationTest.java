package com.intelligent.trial.document;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intelligent.trial.document.client.LlmClient;
import com.intelligent.trial.document.entity.IncomingDoc;
import com.intelligent.trial.document.service.VectorStorageService;
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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 来文登记模块集成测试 — 使用 H2 内存数据库，
 * 对 IncomingDocController 核心 REST API 进行端到端 HTTP 级别测试。
 */
@SpringBootTest(classes = DocumentTestApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("来文登记模块集成测试")
class IncomingDocIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Mock 外部依赖
    @MockBean
    private MinioUtil minioUtil;

    @MockBean
    private LlmClient llmClient;

    @MockBean
    private VectorStorageService vectorStorageService;

    @MockBean
    private DocumentService documentService;

    private String toJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    private Long extractId(MvcResult result) throws Exception {
        String json = result.getResponse().getContentAsString();
        return objectMapper.readTree(json).get("data").get("id").asLong();
    }

    // ==================== 分页查询 ====================

    @Test
    @DisplayName("GET /api/incoming-doc/page - 分页查询默认返回全部记录")
    void page_default_returnsAll() throws Exception {
        mockMvc.perform(get("/api/incoming-doc/page")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(4))
                .andExpect(jsonPath("$.data.list").isArray());
    }

    @Test
    @DisplayName("GET /api/incoming-doc/page - 按标题过滤")
    void page_filterByTitle() throws Exception {
        mockMvc.perform(get("/api/incoming-doc/page")
                        .param("pageNum", "1")
                        .param("pageSize", "10")
                        .param("title", "安全生产"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    @DisplayName("GET /api/incoming-doc/page - 按来文单位过滤")
    void page_filterByFromUnit() throws Exception {
        mockMvc.perform(get("/api/incoming-doc/page")
                        .param("pageNum", "1")
                        .param("pageSize", "10")
                        .param("fromUnit", "某市政府"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    @DisplayName("GET /api/incoming-doc/page - 按状态过滤")
    void page_filterByStatus() throws Exception {
        mockMvc.perform(get("/api/incoming-doc/page")
                        .param("pageNum", "1")
                        .param("pageSize", "10")
                        .param("status", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    @DisplayName("GET /api/incoming-doc/page - 按日期范围过滤")
    void page_filterByDateRange() throws Exception {
        mockMvc.perform(get("/api/incoming-doc/page")
                        .param("pageNum", "1")
                        .param("pageSize", "10")
                        .param("startDate", "2026-05-01")
                        .param("endDate", "2026-05-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(4));
    }

    @Test
    @DisplayName("GET /api/incoming-doc/page - 空条件返回全部分页")
    void page_emptyConditions() throws Exception {
        mockMvc.perform(get("/api/incoming-doc/page")
                        .param("pageNum", "1")
                        .param("pageSize", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(4))
                .andExpect(jsonPath("$.data.list.length()").value(2));
    }

    @Test
    @DisplayName("GET /api/incoming-doc/page - pageNum=0返回第一页")
    void page_pageNumZero() throws Exception {
        mockMvc.perform(get("/api/incoming-doc/page")
                        .param("pageNum", "0")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(4));
    }

    // ==================== 详情查询 ====================

    @Test
    @DisplayName("GET /api/incoming-doc/{id} - 获取详情成功")
    void detail_success() throws Exception {
        mockMvc.perform(get("/api/incoming-doc/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.title").isNotEmpty())
                .andExpect(jsonPath("$.data.status").isNumber());
    }

    @Test
    @DisplayName("GET /api/incoming-doc/{id} - 不存在的ID返回错误")
    void detail_notFound() throws Exception {
        mockMvc.perform(get("/api/incoming-doc/9999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(2010));
    }

    // ==================== 新增来文 ====================

    @Test
    @DisplayName("POST /api/incoming-doc - 创建成功")
    void create_success() throws Exception {
        IncomingDoc doc = new IncomingDoc();
        doc.setDocNo("DOC-TEST-001");
        doc.setFromUnit("测试单位");
        doc.setTitle("测试来文标题");
        doc.setReceiveDate(new java.util.Date());
        doc.setSubject("测试事由");
        doc.setStatus(0);

        mockMvc.perform(post("/api/incoming-doc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(doc)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("POST /api/incoming-doc - 标题为空创建失败")
    void create_emptyTitle() throws Exception {
        IncomingDoc doc = new IncomingDoc();
        doc.setTitle("");
        doc.setReceiveDate(new java.util.Date());

        mockMvc.perform(post("/api/incoming-doc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(doc)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("POST /api/incoming-doc - 收到日期为null创建失败")
    void create_nullReceiveDate() throws Exception {
        IncomingDoc doc = new IncomingDoc();
        doc.setTitle("有效标题");
        doc.setReceiveDate(null);

        mockMvc.perform(post("/api/incoming-doc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(doc)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    // ==================== 更新来文 ====================

    @Test
    @DisplayName("PUT /api/incoming-doc - 更新成功")
    void update_success() throws Exception {
        // Create first
        IncomingDoc doc = new IncomingDoc();
        doc.setTitle("原始标题");
        doc.setReceiveDate(new java.util.Date());

        String createResponse = mockMvc.perform(post("/api/incoming-doc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(doc)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(createResponse).get("data").get("id").asLong();

        // Update
        IncomingDoc updateDoc = new IncomingDoc();
        updateDoc.setId(id);
        updateDoc.setTitle("更新后的标题");
        updateDoc.setFromUnit("更新后的单位");

        mockMvc.perform(put("/api/incoming-doc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(updateDoc)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // Verify
        mockMvc.perform(get("/api/incoming-doc/" + id))
                .andExpect(jsonPath("$.data.title").value("更新后的标题"));
    }

    @Test
    @DisplayName("PUT /api/incoming-doc - ID为null更新失败")
    void update_nullId() throws Exception {
        IncomingDoc updateDoc = new IncomingDoc();
        updateDoc.setId(null);
        updateDoc.setTitle("无ID更新");
        updateDoc.setReceiveDate(new java.util.Date());

        mockMvc.perform(put("/api/incoming-doc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(updateDoc)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    // ==================== 删除来文 ====================

    @Test
    @DisplayName("DELETE /api/incoming-doc/{id} - 删除成功")
    void delete_success() throws Exception {
        // Create first
        IncomingDoc doc = new IncomingDoc();
        doc.setTitle("待删除来文");
        doc.setReceiveDate(new java.util.Date());

        String createResponse = mockMvc.perform(post("/api/incoming-doc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(doc)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(createResponse).get("data").get("id").asLong();

        // Delete
        mockMvc.perform(delete("/api/incoming-doc/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // Verify deletion
        mockMvc.perform(get("/api/incoming-doc/" + id))
                .andExpect(jsonPath("$.code").value(2010));
    }

    @Test
    @DisplayName("DELETE /api/incoming-doc/{id} - 删除不存在的记录")
    void delete_notFound() throws Exception {
        mockMvc.perform(delete("/api/incoming-doc/9999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(2010));
    }

    // ==================== 状态变更 ====================

    @Test
    @DisplayName("PUT /api/incoming-doc/status/{id} - 状态变更：待处理→处理中")
    void changeStatus_pendingToProcessing() throws Exception {
        // Create a new doc with status 0
        IncomingDoc doc = new IncomingDoc();
        doc.setTitle("状态变更测试");
        doc.setReceiveDate(new java.util.Date());
        doc.setStatus(0);

        String createResponse = mockMvc.perform(post("/api/incoming-doc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(doc)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(createResponse).get("data").get("id").asLong();

        // Change to processing
        mockMvc.perform(put("/api/incoming-doc/status/" + id)
                        .param("status", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // Verify
        mockMvc.perform(get("/api/incoming-doc/" + id))
                .andExpect(jsonPath("$.data.status").value(1));
    }

    @Test
    @DisplayName("PUT /api/incoming-doc/status/{id} - 状态变更：处理中→已办结")
    void changeStatus_processingToCompleted() throws Exception {
        // Create a new doc
        IncomingDoc doc = new IncomingDoc();
        doc.setTitle("状态流转测试");
        doc.setReceiveDate(new java.util.Date());
        doc.setStatus(0);

        String createResponse = mockMvc.perform(post("/api/incoming-doc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(doc)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(createResponse).get("data").get("id").asLong();

        // Change to processing
        mockMvc.perform(put("/api/incoming-doc/status/" + id)
                        .param("status", "1"))
                .andExpect(status().isOk());

        // Change to completed
        mockMvc.perform(put("/api/incoming-doc/status/" + id)
                        .param("status", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // Verify
        mockMvc.perform(get("/api/incoming-doc/" + id))
                .andExpect(jsonPath("$.data.status").value(2));
    }

    @Test
    @DisplayName("PUT /api/incoming-doc/status/{id} - 不存在的记录")
    void changeStatus_notFound() throws Exception {
        mockMvc.perform(put("/api/incoming-doc/status/9999")
                        .param("status", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(2010));
    }

    // ==================== 完整生命周期 ====================

    @Test
    @DisplayName("完整生命周期：创建→更新→状态变更→删除→验证")
    void fullLifecycle() throws Exception {
        // 1. Create
        IncomingDoc doc = new IncomingDoc();
        doc.setDocNo("DOC-LIFECYCLE-001");
        doc.setFromUnit("生命周期测试单位");
        doc.setTitle("生命周期测试来文");
        doc.setReceiveDate(new java.util.Date());
        doc.setSubject("完整生命周期测试");
        doc.setStatus(0);

        String createResponse = mockMvc.perform(post("/api/incoming-doc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(doc)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(createResponse).get("data").get("id").asLong();
        assertNotNull(id);

        // 2. Update
        IncomingDoc updateDoc = new IncomingDoc();
        updateDoc.setId(id);
        updateDoc.setTitle("生命周期更新后的标题");

        mockMvc.perform(put("/api/incoming-doc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(updateDoc)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // Verify update
        mockMvc.perform(get("/api/incoming-doc/" + id))
                .andExpect(jsonPath("$.data.title").value("生命周期更新后的标题"));

        // 3. Change status: pending -> processing
        mockMvc.perform(put("/api/incoming-doc/status/" + id)
                        .param("status", "1"))
                .andExpect(status().isOk());

        // 4. Change status: processing -> completed
        mockMvc.perform(put("/api/incoming-doc/status/" + id)
                        .param("status", "2"))
                .andExpect(status().isOk());

        // Verify status
        mockMvc.perform(get("/api/incoming-doc/" + id))
                .andExpect(jsonPath("$.data.status").value(2));

        // 5. Delete
        mockMvc.perform(delete("/api/incoming-doc/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 6. Verify deletion
        mockMvc.perform(get("/api/incoming-doc/" + id))
                .andExpect(jsonPath("$.code").value(2010));
    }
}
