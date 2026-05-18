package com.intelligent.trial.readingnote;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intelligent.trial.readingnote.entity.ReadingNote;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 阅卷笔记模块集成测试 — 使用 H2 内存数据库，
 * 对核心 REST API 进行端到端 HTTP 级别测试。
 */
@SpringBootTest(classes = ReadingNoteTestApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("阅卷笔记模块集成测试")
class ReadingNoteIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String toJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    // ==================== 分页查询 ====================

    @Test
    @DisplayName("GET /api/reading-note/page - 分页查询默认返回全部记录")
    void page_default_returnsAll() throws Exception {
        mockMvc.perform(get("/api/reading-note/page")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(4))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    @DisplayName("GET /api/reading-note/page - 按案件ID过滤")
    void page_filterByCaseId() throws Exception {
        mockMvc.perform(get("/api/reading-note/page")
                        .param("pageNum", "1")
                        .param("pageSize", "10")
                        .param("caseId", "AJ20260501001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(2));
    }

    @Test
    @DisplayName("GET /api/reading-note/page - 按笔记类型过滤")
    void page_filterByNoteType() throws Exception {
        mockMvc.perform(get("/api/reading-note/page")
                        .param("pageNum", "1")
                        .param("pageSize", "10")
                        .param("noteType", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(2));
    }

    @Test
    @DisplayName("GET /api/reading-note/page - 分页参数生效")
    void page_pagination() throws Exception {
        mockMvc.perform(get("/api/reading-note/page")
                        .param("pageNum", "1")
                        .param("pageSize", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(4))
                .andExpect(jsonPath("$.data.records.length()").value(2));
    }

    // ==================== 详情查询 ====================

    @Test
    @DisplayName("GET /api/reading-note/{id} - 获取详情成功")
    void getDetail_success() throws Exception {
        mockMvc.perform(get("/api/reading-note/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.caseId").value("AJ20260501001"))
                .andExpect(jsonPath("$.data.title").value("案件事实梳理"))
                .andExpect(jsonPath("$.data.noteType").value(1));
    }

    @Test
    @DisplayName("GET /api/reading-note/{id} - 不存在的ID返回错误")
    void getDetail_notFound() throws Exception {
        mockMvc.perform(get("/api/reading-note/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    // ==================== 创建笔记 ====================

    @Test
    @DisplayName("POST /api/reading-note - 创建成功")
    void create_success() throws Exception {
        ReadingNote note = new ReadingNote();
        note.setCaseId("AJ20260501001");
        note.setTitle("新笔记");
        note.setContent("测试内容");

        mockMvc.perform(post("/api/reading-note")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(note)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.caseId").value("AJ20260501001"))
                .andExpect(jsonPath("$.data.title").value("新笔记"))
                .andExpect(jsonPath("$.data.isShared").value(0))
                .andExpect(jsonPath("$.data.noteType").value(1));
    }

    @Test
    @DisplayName("POST /api/reading-note - 案件ID为空创建失败")
    void create_missingCaseId() throws Exception {
        ReadingNote note = new ReadingNote();
        note.setTitle("新笔记");
        note.setContent("测试内容");

        mockMvc.perform(post("/api/reading-note")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(note)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("POST /api/reading-note - 标题为空创建失败")
    void create_missingTitle() throws Exception {
        ReadingNote note = new ReadingNote();
        note.setCaseId("AJ20260501001");

        mockMvc.perform(post("/api/reading-note")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(note)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    // ==================== 更新笔记 ====================

    @Test
    @DisplayName("PUT /api/reading-note - 更新成功")
    void update_success() throws Exception {
        ReadingNote note = new ReadingNote();
        note.setId(1L);
        note.setTitle("更新后的标题");
        note.setContent("更新后的内容");

        mockMvc.perform(put("/api/reading-note")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(note)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // Verify the update
        mockMvc.perform(get("/api/reading-note/1"))
                .andExpect(jsonPath("$.data.title").value("更新后的标题"));
    }

    @Test
    @DisplayName("PUT /api/reading-note - ID为空更新失败")
    void update_nullId() throws Exception {
        ReadingNote note = new ReadingNote();
        note.setTitle("更新后的标题");

        mockMvc.perform(put("/api/reading-note")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(note)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    // ==================== 删除笔记 ====================

    @Test
    @DisplayName("DELETE /api/reading-note/{id} - 删除成功")
    void delete_success() throws Exception {
        mockMvc.perform(delete("/api/reading-note/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // Verify deletion
        mockMvc.perform(get("/api/reading-note/1"))
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("DELETE /api/reading-note/{id} - 删除不存在的笔记")
    void delete_notFound() throws Exception {
        mockMvc.perform(delete("/api/reading-note/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    // ==================== 按案件ID查询 ====================

    @Test
    @DisplayName("GET /api/reading-note/case/{caseId} - 按案件查询")
    void getByCaseId_success() throws Exception {
        mockMvc.perform(get("/api/reading-note/case/AJ20260501001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].caseId").value("AJ20260501001"));
    }

    @Test
    @DisplayName("GET /api/reading-note/case/{caseId} - 无匹配案件返回空列表")
    void getByCaseId_noMatch() throws Exception {
        mockMvc.perform(get("/api/reading-note/case/NONEXISTENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    // ==================== 共享笔记 ====================

    @Test
    @DisplayName("GET /api/reading-note/shared/{caseId} - 获取共享笔记")
    void getSharedNotes_success() throws Exception {
        mockMvc.perform(get("/api/reading-note/shared/AJ20260501001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].isShared").value(1));
    }

    @Test
    @DisplayName("GET /api/reading-note/shared/{caseId} - 无共享笔记返回空列表")
    void getSharedNotes_empty() throws Exception {
        mockMvc.perform(get("/api/reading-note/shared/AJ20260501002"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    // ==================== 共享切换 ====================

    @Test
    @DisplayName("PUT /api/reading-note/{id}/share - 设置共享")
    void toggleShared_toShared() throws Exception {
        mockMvc.perform(put("/api/reading-note/1/share")
                        .param("isShared", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/api/reading-note/1"))
                .andExpect(jsonPath("$.data.isShared").value(1));
    }

    @Test
    @DisplayName("PUT /api/reading-note/{id}/share - 取消共享")
    void toggleShared_toPrivate() throws Exception {
        mockMvc.perform(put("/api/reading-note/2/share")
                        .param("isShared", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/api/reading-note/2"))
                .andExpect(jsonPath("$.data.isShared").value(0));
    }

    @Test
    @DisplayName("PUT /api/reading-note/{id}/share - 不存在的笔记")
    void toggleShared_notFound() throws Exception {
        mockMvc.perform(put("/api/reading-note/999/share")
                        .param("isShared", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    // ==================== 完整生命周期 ====================

    @Test
    @DisplayName("完整生命周期：创建→查询→更新→共享→删除")
    void fullLifecycle() throws Exception {
        // 1. Create
        ReadingNote note = new ReadingNote();
        note.setCaseId("AJ20260501001");
        note.setTitle("生命周期测试笔记");
        note.setContent("完整测试流程");
        note.setTags("测试,生命周期");

        String createResponse = mockMvc.perform(post("/api/reading-note")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(note)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(createResponse).get("data").get("id").asLong();

        // 2. Verify creation
        mockMvc.perform(get("/api/reading-note/" + id))
                .andExpect(jsonPath("$.data.title").value("生命周期测试笔记"));

        // 3. Update
        ReadingNote updated = new ReadingNote();
        updated.setId(id);
        updated.setTitle("更新后的生命周期笔记");
        mockMvc.perform(put("/api/reading-note")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(updated)))
                .andExpect(status().isOk());

        // 4. Toggle shared
        mockMvc.perform(put("/api/reading-note/" + id + "/share")
                        .param("isShared", "1"))
                .andExpect(status().isOk());

        // 5. Verify shared (note 2 + lifecycle note = 2 shared for this case)
        mockMvc.perform(get("/api/reading-note/shared/AJ20260501001"))
                .andExpect(jsonPath("$.data.length()").value(2));

        // 6. Delete
        mockMvc.perform(delete("/api/reading-note/" + id))
                .andExpect(status().isOk());

        // 7. Verify deletion
        mockMvc.perform(get("/api/reading-note/" + id))
                .andExpect(jsonPath("$.code").value(500));
    }
}
