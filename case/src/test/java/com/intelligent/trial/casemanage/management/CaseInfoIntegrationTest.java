package com.intelligent.trial.casemanage.management;

import com.intelligent.trial.casemanage.management.dto.CaseInfoDTO;
import com.intelligent.trial.casemanage.management.dto.CaseSearchDTO;
import com.intelligent.trial.casemanage.management.entity.CaseParty;
import com.intelligent.trial.casemanage.management.entity.CaseViolationFact;
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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 案件管理模块集成测试 — 使用 H2 内存数据库，
 * 对核心 REST API 进行端到端 HTTP 级别测试。
 */
@SpringBootTest(classes = CaseTestApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("案件管理模块集成测试")
class CaseInfoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // Mock DocumentService — 跨模块依赖，集成测试不需要真实的 repository 模块
    @MockBean
    private DocumentService documentService;

    // ==================== 案件分页查询 ====================

    @Test
    @DisplayName("POST /api/case/page - 分页查询默认返回全部案件")
    void pageCase_default_returnsAll() throws Exception {
        CaseSearchDTO search = new CaseSearchDTO();
        search.setPageNum(1);
        search.setPageSize(10);

        mockMvc.perform(post("/api/case/page")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(search)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(3))
                .andExpect(jsonPath("$.data.list").isArray())
                .andExpect(jsonPath("$.data.list.length()").value(3));
    }

    @Test
    @DisplayName("POST /api/case/page - 按被调查人名称搜索")
    void pageCase_searchByRespondentName() throws Exception {
        CaseSearchDTO search = new CaseSearchDTO();
        search.setPageNum(1);
        search.setPageSize(10);
        search.setRespondentName("张三");

        mockMvc.perform(post("/api/case/page")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(search)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].respondentName").value("张三"));
    }

    @Test
    @DisplayName("POST /api/case/page - 按案件类型过滤")
    void pageCase_filterByCaseType() throws Exception {
        CaseSearchDTO search = new CaseSearchDTO();
        search.setPageNum(1);
        search.setPageSize(10);
        search.setCaseType(3);

        mockMvc.perform(post("/api/case/page")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(search)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    @DisplayName("POST /api/case/page - 按状态过滤")
    void pageCase_filterByStatus() throws Exception {
        CaseSearchDTO search = new CaseSearchDTO();
        search.setPageNum(1);
        search.setPageSize(10);
        search.setStatus(1);

        mockMvc.perform(post("/api/case/page")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(search)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(2));
    }

    // ==================== 案件详情 ====================

    @Test
    @DisplayName("GET /api/case/{id} - 案件详情查询成功")
    void detail_success() throws Exception {
        mockMvc.perform(get("/api/case/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.caseName").value("张三违纪案"))
                .andExpect(jsonPath("$.data.respondentName").value("张三"))
                .andExpect(jsonPath("$.data.caseCode").value("AJ20260501001"));
    }

    @Test
    @DisplayName("GET /api/case/{id} - 不存在的案件返回错误")
    void detail_notFound() throws Exception {
        mockMvc.perform(get("/api/case/9999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }

    // ==================== 创建案件 ====================

    @Test
    @DisplayName("POST /api/case - 创建案件成功")
    void addCase_success() throws Exception {
        String body = "{"
                + "\"caseName\":\"测试案件\","
                + "\"caseType\":1,"
                + "\"caseSource\":\"信访举报\","
                + "\"respondentName\":\"测试人\","
                + "\"respondentDept\":\"测试单位\","
                + "\"respondentPosition\":\"科员\","
                + "\"briefDescription\":\"简要案情\""
                + "}";

        mockMvc.perform(post("/api/case")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("POST /api/case - 案件名称为空时返回业务错误")
    void addCase_emptyName_returnsError() throws Exception {
        String body = "{\"caseType\":1}";

        mockMvc.perform(post("/api/case")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    // ==================== 更新案件 ====================

    @Test
    @DisplayName("PUT /api/case - 更新案件成功")
    void updateCase_success() throws Exception {
        String body = "{"
                + "\"id\":1,"
                + "\"caseName\":\"张三违纪案（已更新）\","
                + "\"caseType\":1,"
                + "\"caseSource\":\"信访举报\","
                + "\"respondentName\":\"张三\""
                + "}";

        mockMvc.perform(put("/api/case")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 验证更新生效
        mockMvc.perform(get("/api/case/1"))
                .andExpect(jsonPath("$.data.caseName").value("张三违纪案（已更新）"));
    }

    @Test
    @DisplayName("PUT /api/case - ID 为空时返回业务错误")
    void updateCase_noId_returnsError() throws Exception {
        String body = "{\"caseName\":\"新案件\",\"caseType\":1}";

        mockMvc.perform(put("/api/case")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    // ==================== 删除案件 ====================

    @Test
    @DisplayName("DELETE /api/case/{id} - 删除案件（级联删除当事人和违纪事实）")
    void deleteCase_cascadeDelete() throws Exception {
        mockMvc.perform(delete("/api/case/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 删除后查询应返回 2 条
        CaseSearchDTO search = new CaseSearchDTO();
        search.setPageNum(1);
        search.setPageSize(10);

        mockMvc.perform(post("/api/case/page")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(search)))
                .andExpect(jsonPath("$.data.total").value(2));
    }

    // ==================== 修改案件状态 ====================

    @Test
    @DisplayName("PUT /api/case/status/{id} - 状态变更为完结")
    void changeStatus_toCompleted() throws Exception {
        mockMvc.perform(put("/api/case/status/1")
                        .param("status", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("PUT /api/case/status/{id} - 不存在的案件返回错误")
    void changeStatus_notFound() throws Exception {
        mockMvc.perform(put("/api/case/status/9999")
                        .param("status", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }

    // ==================== 当事人管理 ====================

    @Test
    @DisplayName("GET /api/case/{caseId}/parties - 获取当事人列表")
    void getParties_success() throws Exception {
        mockMvc.perform(get("/api/case/1/parties"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    @DisplayName("POST /api/case/party - 添加当事人成功")
    void addParty_success() throws Exception {
        String body = "{"
                + "\"caseId\":1,"
                + "\"partyName\":\"新证人\","
                + "\"partyType\":2,"
                + "\"gender\":1,"
                + "\"dept\":\"测试单位\","
                + "\"position\":\"职员\""
                + "}";

        mockMvc.perform(post("/api/case/party")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("DELETE /api/case/party/{id} - 删除当事人")
    void deleteParty_success() throws Exception {
        mockMvc.perform(delete("/api/case/party/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    // ==================== 违纪事实管理 ====================

    @Test
    @DisplayName("GET /api/case/{caseId}/violations - 获取违纪事实列表")
    void getViolationFacts_success() throws Exception {
        mockMvc.perform(get("/api/case/1/violations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    @DisplayName("POST /api/case/violation - 添加违纪事实成功")
    void addViolationFact_success() throws Exception {
        String body = "{"
                + "\"caseId\":1,"
                + "\"factTitle\":\"新的违纪事实\","
                + "\"factContent\":\"详细内容\","
                + "\"violationType\":\"违反政治纪律\","
                + "\"sort\":3"
                + "}";

        mockMvc.perform(post("/api/case/violation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("PUT /api/case/violation - 更新违纪事实")
    void updateViolationFact_success() throws Exception {
        String body = "{"
                + "\"id\":1,"
                + "\"caseId\":1,"
                + "\"factTitle\":\"违规收受礼品（已修改）\","
                + "\"factContent\":\"修改后的内容\","
                + "\"violationType\":\"违反廉洁纪律\","
                + "\"amount\":60000,"
                + "\"sort\":1"
                + "}";

        mockMvc.perform(put("/api/case/violation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("DELETE /api/case/violation/{id} - 删除违纪事实")
    void deleteViolationFact_success() throws Exception {
        mockMvc.perform(delete("/api/case/violation/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * 简单对象转 JSON（避免引入额外 JSON 库依赖）
     */
    private String toJson(Object obj) throws Exception {
        com.fasterxml.jackson.databind.ObjectMapper mapper =
                new com.fasterxml.jackson.databind.ObjectMapper();
        return mapper.writeValueAsString(obj);
    }
}
