package com.intelligent.trial.workflow;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intelligent.trial.workflow.dto.StartProcessDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 工作流模块集成测试 — 使用 H2 内存数据库 + Flowable 引擎，
 * 对核心 REST API 进行端到端 HTTP 级别测试。
 */
@SpringBootTest(classes = WorkflowTestApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("工作流模块集成测试")
class WorkflowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String uniqueCaseId() {
        return "AJ-" + UUID.randomUUID().toString().substring(0, 8);
    }

    // ==================== 流程定义管理 ====================

    @Test
    @DisplayName("GET /api/workflow/process/definitions - 查询流程定义列表（自动部署）")
    void listProcessDefinitions_returnsDeployed() throws Exception {
        mockMvc.perform(get("/api/workflow/process/definitions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].key").value("case-review-approval"));
    }

    // ==================== 流程实例管理 ====================

    @Test
    @DisplayName("POST /api/workflow/process/start - 启动流程成功")
    void startProcess_success() throws Exception {
        String caseId = uniqueCaseId();
        StartProcessDTO dto = new StartProcessDTO();
        dto.setCaseId(caseId);
        dto.setProcessDefinitionKey("case-review-approval");
        dto.setInitiatorId("user001");
        dto.setInitiatorName("测试发起人");
        dto.setDepartmentAssignee("dept_user");
        dto.setDisciplineAssignee("discipline_user");
        dto.setLeaderAssignee("leader_user");
        dto.setCaseTitle("测试案件");

        mockMvc.perform(post("/api/workflow/process/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isNotEmpty());
    }

    @Test
    @DisplayName("POST /api/workflow/process/start - 缺少案件ID返回400")
    void startProcess_missingCaseId() throws Exception {
        StartProcessDTO dto = new StartProcessDTO();
        dto.setProcessDefinitionKey("case-review-approval");
        dto.setInitiatorId("user001");

        mockMvc.perform(post("/api/workflow/process/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/workflow/process/start - 缺少流程定义Key返回400")
    void startProcess_missingProcessKey() throws Exception {
        StartProcessDTO dto = new StartProcessDTO();
        dto.setCaseId(uniqueCaseId());
        dto.setInitiatorId("user001");

        mockMvc.perform(post("/api/workflow/process/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/workflow/process/instances/{caseId} - 按案件查询流程实例")
    void getInstancesByCaseId_found() throws Exception {
        String caseId = uniqueCaseId();
        startAndGetProcessId(caseId);

        mockMvc.perform(get("/api/workflow/process/instances/" + caseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].businessKey").value(caseId))
                .andExpect(jsonPath("$.data[0].ended").value(false));
    }

    @Test
    @DisplayName("GET /api/workflow/process/instances/{caseId} - 不存在的案件返回空数组")
    void getInstancesByCaseId_notFound_returnsEmpty() throws Exception {
        mockMvc.perform(get("/api/workflow/process/instances/NON-EXISTENT-" + UUID.randomUUID()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @DisplayName("GET /api/workflow/process/instances/{id}/status - 获取流程实例状态")
    void getProcessInstanceStatus_running() throws Exception {
        String caseId = uniqueCaseId();
        String pid = startAndGetProcessId(caseId);

        mockMvc.perform(get("/api/workflow/process/instances/" + pid + "/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.processInstanceId").value(pid))
                .andExpect(jsonPath("$.data.ended").value(false))
                .andExpect(jsonPath("$.data.suspended").value(false));
    }

    @Test
    @DisplayName("DELETE /api/workflow/process/instances/{id} - 取消流程实例")
    void cancelProcessInstance_success() throws Exception {
        String caseId = uniqueCaseId();
        String pid = startAndGetProcessId(caseId);

        mockMvc.perform(delete("/api/workflow/process/instances/" + pid)
                        .param("reason", "测试取消"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 取消后状态为已结束
        mockMvc.perform(get("/api/workflow/process/instances/" + pid + "/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.ended").value(true));
    }

    // ==================== 任务管理 ====================

    @Test
    @DisplayName("GET /api/workflow/task/my-tasks - 查询部门审核人任务")
    void getMyTasks_departmentAssignee() throws Exception {
        String caseId = uniqueCaseId();
        String assignee = "dept_" + UUID.randomUUID().toString().substring(0, 6);
        startAndGetProcessId(caseId, assignee);

        mockMvc.perform(get("/api/workflow/task/my-tasks")
                        .param("assignee", assignee))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].assignee").value(assignee))
                .andExpect(jsonPath("$.data[0].taskName").value("部门审核"));
    }

    @Test
    @DisplayName("GET /api/workflow/task/my-tasks - 查询非审核人返回空")
    void getMyTasks_noTasks() throws Exception {
        String caseId = uniqueCaseId();
        startAndGetProcessId(caseId);

        mockMvc.perform(get("/api/workflow/task/my-tasks")
                        .param("assignee", "random_user_" + UUID.randomUUID()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @DisplayName("GET /api/workflow/task/pending-tasks - 查询候选组待办")
    void getPendingTasks_byCandidateGroup() throws Exception {
        String caseId = uniqueCaseId();
        startAndGetProcessId(caseId);

        mockMvc.perform(get("/api/workflow/task/pending-tasks")
                        .param("candidateGroup", "department"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("GET /api/workflow/task/history/{processId} - 查询空流程的任务历史")
    void getTaskHistory_emptyProcess() throws Exception {
        String caseId = uniqueCaseId();
        String pid = startAndGetProcessId(caseId);

        // 刚启动的流程还没有完成的任务
        mockMvc.perform(get("/api/workflow/task/history/" + pid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    // ==================== 辅助方法 ====================

    private String startAndGetProcessId(String caseId) throws Exception {
        return startAndGetProcessId(caseId, "dept_user");
    }

    private String startAndGetProcessId(String caseId, String deptAssignee) throws Exception {
        StartProcessDTO dto = new StartProcessDTO();
        dto.setCaseId(caseId);
        dto.setProcessDefinitionKey("case-review-approval");
        dto.setInitiatorId("user001");
        dto.setInitiatorName("测试发起人");
        dto.setDepartmentAssignee(deptAssignee);
        dto.setDisciplineAssignee("discipline_user");
        dto.setLeaderAssignee("leader_user");
        dto.setCaseTitle("测试案件");

        byte[] content = mockMvc.perform(post("/api/workflow/process/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn().getResponse().getContentAsByteArray();

        JsonNode json = objectMapper.readTree(content);
        return json.path("data").asText();
    }
}
