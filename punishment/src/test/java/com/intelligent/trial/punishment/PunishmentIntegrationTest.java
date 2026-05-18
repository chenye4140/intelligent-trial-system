package com.intelligent.trial.punishment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intelligent.trial.punishment.dto.PunishmentExecutionDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 处分执行模块集成测试 — 使用 H2 内存数据库，
 * 对核心 REST API 进行端到端 HTTP 级别测试。
 */
@SpringBootTest(classes = PunishmentTestApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("处分执行模块集成测试")
class PunishmentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String toJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    // ==================== 分页查询 ====================

    @Test
    @DisplayName("GET /api/punishment/page - 分页查询默认返回全部记录")
    void page_default_returnsAll() throws Exception {
        mockMvc.perform(get("/api/punishment/page")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(3))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    @DisplayName("GET /api/punishment/page - 按状态过滤")
    void page_filterByStatus() throws Exception {
        mockMvc.perform(get("/api/punishment/page")
                        .param("pageNum", "1")
                        .param("pageSize", "10")
                        .param("status", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    @DisplayName("GET /api/punishment/page - 按案件ID过滤")
    void page_filterByCaseId() throws Exception {
        mockMvc.perform(get("/api/punishment/page")
                        .param("pageNum", "1")
                        .param("pageSize", "10")
                        .param("caseId", "AJ20260501001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    @DisplayName("GET /api/punishment/page - 按处分类型过滤")
    void page_filterByPunishmentType() throws Exception {
        mockMvc.perform(get("/api/punishment/page")
                        .param("pageNum", "1")
                        .param("pageSize", "10")
                        .param("punishmentType", "记过"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    @DisplayName("GET /api/punishment/page - 空条件返回全部分页")
    void page_emptyConditions() throws Exception {
        mockMvc.perform(get("/api/punishment/page")
                        .param("pageNum", "1")
                        .param("pageSize", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(3))
                .andExpect(jsonPath("$.data.records.length()").value(2));
    }

    // ==================== 详情查询 ====================

    @Test
    @DisplayName("GET /api/punishment/{id} - 获取详情成功（含材料）")
    void getDetail_success() throws Exception {
        mockMvc.perform(get("/api/punishment/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.caseId").value("AJ20260501001"))
                .andExpect(jsonPath("$.data.punishmentType").value("警告"))
                .andExpect(jsonPath("$.data.status").value(1))
                .andExpect(jsonPath("$.data.materials").isArray())
                .andExpect(jsonPath("$.data.materials.length()").value(2));
    }

    @Test
    @DisplayName("GET /api/punishment/{id} - 不存在的ID返回null")
    void getDetail_notFound() throws Exception {
        mockMvc.perform(get("/api/punishment/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(6001));
    }

    // ==================== 创建处分执行 ====================

    @Test
    @DisplayName("POST /api/punishment - 创建成功")
    void create_success() throws Exception {
        PunishmentExecutionDTO dto = new PunishmentExecutionDTO();
        dto.setCaseId("AJ20260501001");
        dto.setPunishmentType("降级");

        mockMvc.perform(post("/api/punishment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.caseId").value("AJ20260501001"))
                .andExpect(jsonPath("$.data.punishmentType").value("降级"))
                .andExpect(jsonPath("$.data.status").value(0));
    }

    @Test
    @DisplayName("POST /api/punishment - 带完整日期创建")
    void create_withDates() throws Exception {
        PunishmentExecutionDTO dto = new PunishmentExecutionDTO();
        dto.setCaseId("AJ20260501002");
        dto.setPunishmentType("开除");
        dto.setDecisionDate(new SimpleDateFormat("yyyy-MM-dd").parse("2026-06-01"));
        dto.setStartDate(new SimpleDateFormat("yyyy-MM-dd").parse("2026-06-01"));
        dto.setEndDate(new SimpleDateFormat("yyyy-MM-dd").parse("2026-12-01"));
        dto.setStatus(1);

        mockMvc.perform(post("/api/punishment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value(1));
    }

    // ==================== 更新处分执行 ====================

    @Test
    @DisplayName("PUT /api/punishment - 更新成功")
    void update_success() throws Exception {
        PunishmentExecutionDTO dto = new PunishmentExecutionDTO();
        dto.setId(1L);
        dto.setCaseId("AJ20260501001");
        dto.setPunishmentType("记过");

        mockMvc.perform(put("/api/punishment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // Verify the update
        mockMvc.perform(get("/api/punishment/1"))
                .andExpect(jsonPath("$.data.punishmentType").value("记过"));
    }

    @Test
    @DisplayName("PUT /api/punishment - ID为空更新失败")
    void update_nullId() throws Exception {
        PunishmentExecutionDTO dto = new PunishmentExecutionDTO();
        dto.setCaseId("AJ20260501001");
        dto.setPunishmentType("警告");

        mockMvc.perform(put("/api/punishment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(6004));
    }

    // ==================== 删除处分执行 ====================

    @Test
    @DisplayName("DELETE /api/punishment/{id} - 删除成功（级联删除材料）")
    void delete_success() throws Exception {
        mockMvc.perform(delete("/api/punishment/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // Verify deletion
        mockMvc.perform(get("/api/punishment/1"))
                .andExpect(jsonPath("$.code").value(6001));

        // Verify materials were cascade deleted
        mockMvc.perform(get("/api/punishment/material/1"))
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @DisplayName("DELETE /api/punishment/{id} - 删除不存在的记录")
    void delete_notFound() throws Exception {
        mockMvc.perform(delete("/api/punishment/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(6001));
    }

    // ==================== 状态变更 ====================

    @Test
    @DisplayName("PUT /api/punishment/{id}/status - 变更为执行中")
    void changeStatus_toExecuting() throws Exception {
        mockMvc.perform(put("/api/punishment/2/status")
                        .param("status", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/api/punishment/2"))
                .andExpect(jsonPath("$.data.status").value(1));
    }

    @Test
    @DisplayName("PUT /api/punishment/{id}/status - 变更为已完成")
    void changeStatus_toCompleted() throws Exception {
        mockMvc.perform(put("/api/punishment/2/status")
                        .param("status", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("PUT /api/punishment/{id}/status - 变更为已撤销")
    void changeStatus_toRevoked() throws Exception {
        mockMvc.perform(put("/api/punishment/2/status")
                        .param("status", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("PUT /api/punishment/{id}/status - 不存在记录")
    void changeStatus_notFound() throws Exception {
        mockMvc.perform(put("/api/punishment/999/status")
                        .param("status", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(6001));
    }

    // ==================== 按案件ID查询 ====================

    @Test
    @DisplayName("GET /api/punishment/case/{caseId} - 按案件查询")
    void getByCaseId_success() throws Exception {
        mockMvc.perform(get("/api/punishment/case/AJ20260501001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].caseId").value("AJ20260501001"));
    }

    @Test
    @DisplayName("GET /api/punishment/case/{caseId} - 无匹配案件返回空列表")
    void getByCaseId_noMatch() throws Exception {
        mockMvc.perform(get("/api/punishment/case/NONEXISTENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    // ==================== 逾期查询 ====================

    @Test
    @DisplayName("GET /api/punishment/overdue - 查询逾期记录")
    void getOverdue() throws Exception {
        mockMvc.perform(get("/api/punishment/overdue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    // ==================== 统计 ====================

    @Test
    @DisplayName("GET /api/punishment/statistics - 统计各状态数量")
    void statistics() throws Exception {
        mockMvc.perform(get("/api/punishment/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    // ==================== 材料管理 ====================

    @Test
    @DisplayName("POST /api/punishment/material - 上传材料成功")
    void uploadMaterial_success() throws Exception {
        mockMvc.perform(post("/api/punishment/material")
                        .param("executionId", "2")
                        .param("materialType", "决定书")
                        .param("filePath", "/files/test/decision.pdf"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.executionId").value(2))
                .andExpect(jsonPath("$.data.materialType").value("决定书"));
    }

    @Test
    @DisplayName("GET /api/punishment/material/{executionId} - 获取材料列表")
    void getMaterials_success() throws Exception {
        mockMvc.perform(get("/api/punishment/material/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].materialType").isNotEmpty());
    }

    @Test
    @DisplayName("GET /api/punishment/material/{executionId} - 无材料返回空列表")
    void getMaterials_empty() throws Exception {
        mockMvc.perform(get("/api/punishment/material/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @DisplayName("DELETE /api/punishment/material/{materialId} - 删除材料成功")
    void deleteMaterial_success() throws Exception {
        mockMvc.perform(delete("/api/punishment/material/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/api/punishment/material/1"))
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    @DisplayName("DELETE /api/punishment/material/{materialId} - 删除不存在的材料")
    void deleteMaterial_notFound() throws Exception {
        mockMvc.perform(delete("/api/punishment/material/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(6006));
    }

    // ==================== 完整生命周期 ====================

    @Test
    @DisplayName("完整生命周期：创建→上传材料→状态变更→删除")
    void fullLifecycle() throws Exception {
        // 1. Create
        PunishmentExecutionDTO dto = new PunishmentExecutionDTO();
        dto.setCaseId("AJ20260501001");
        dto.setPunishmentType("降级");

        String createResponse = mockMvc.perform(post("/api/punishment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(createResponse).get("data").get("id").asLong();

        // 2. Upload material
        mockMvc.perform(post("/api/punishment/material")
                        .param("executionId", String.valueOf(id))
                        .param("materialType", "执行报告")
                        .param("filePath", "/files/report.pdf"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 3. Change status to executing
        mockMvc.perform(put("/api/punishment/" + id + "/status")
                        .param("status", "1"))
                .andExpect(status().isOk());

        // 4. Change status to completed
        mockMvc.perform(put("/api/punishment/" + id + "/status")
                        .param("status", "2"))
                .andExpect(status().isOk());

        // 5. Verify final state
        mockMvc.perform(get("/api/punishment/" + id))
                .andExpect(jsonPath("$.data.status").value(2))
                .andExpect(jsonPath("$.data.materials.length()").value(1));

        // 6. Delete
        mockMvc.perform(delete("/api/punishment/" + id))
                .andExpect(status().isOk());

        // 7. Verify deletion
        mockMvc.perform(get("/api/punishment/" + id))
                .andExpect(jsonPath("$.code").value(6001));
    }
}
