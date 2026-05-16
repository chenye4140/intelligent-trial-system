package com.intelligent.trial.workflow.service.impl;

import com.intelligent.trial.common.exception.BusinessException;
import com.intelligent.trial.workflow.dto.StartProcessDTO;
import com.intelligent.trial.workflow.vo.ProcessInstanceVO;
import org.flowable.common.engine.impl.identity.Authentication;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.history.HistoricProcessInstanceQuery;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.runtime.ProcessInstanceQuery;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ProcessInstanceServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
class ProcessInstanceServiceImplTest {

    @InjectMocks
    private ProcessInstanceServiceImpl processInstanceService;

    @Mock
    private RuntimeService runtimeService;

    @Mock
    private HistoryService historyService;

    @Mock
    private RepositoryService repositoryService;

    @Mock
    private ProcessInstance mockProcessInstance;

    @Mock
    private ProcessInstanceQuery mockProcessInstanceQuery;

    @Mock
    private HistoricProcessInstanceQuery mockHistoricQuery;

    @Mock
    private HistoricProcessInstance mockHistoricInstance;

    @BeforeEach
    void setUp() {
        // Clear any leftover authentication state
        Authentication.setAuthenticatedUserId(null);
    }

    @AfterEach
    void tearDown() {
        Authentication.setAuthenticatedUserId(null);
    }

    // ==================== startProcess Tests ====================

    @Test
    void startProcess_shouldThrow_whenProcessDefinitionKeyEmpty() {
        StartProcessDTO dto = new StartProcessDTO();
        dto.setProcessDefinitionKey(null);
        dto.setCaseId("100");

        assertThrows(IllegalArgumentException.class, () -> processInstanceService.startProcess(dto));
    }

    @Test
    void startProcess_shouldThrow_whenCaseIdEmpty() {
        StartProcessDTO dto = new StartProcessDTO();
        dto.setProcessDefinitionKey("case-review");
        dto.setCaseId(null);

        assertThrows(IllegalArgumentException.class, () -> processInstanceService.startProcess(dto));
    }

    @Test
    void startProcess_shouldThrow_whenCaseIdBlank() {
        StartProcessDTO dto = new StartProcessDTO();
        dto.setProcessDefinitionKey("case-review");
        dto.setCaseId("");

        assertThrows(IllegalArgumentException.class, () -> processInstanceService.startProcess(dto));
    }

    @Test
    void startProcess_shouldSetAuthenticationAndStartProcess() {
        StartProcessDTO dto = new StartProcessDTO();
        dto.setProcessDefinitionKey("case-review");
        dto.setCaseId("CASE-001");
        dto.setInitiatorId("user-1");
        dto.setInitiatorName("张三");
        dto.setCaseTitle("张三违纪案");
        dto.setCaseDescription("涉嫌违反廉洁纪律");

        when(runtimeService.startProcessInstanceByKey(eq("case-review"), eq("CASE-001"), anyMap()))
                .thenReturn(mockProcessInstance);
        when(mockProcessInstance.getId()).thenReturn("proc-inst-001");

        String result = processInstanceService.startProcess(dto);

        assertEquals("proc-inst-001", result);
        verify(runtimeService).startProcessInstanceByKey(eq("case-review"), eq("CASE-001"), argThat(vars -> {
            @SuppressWarnings("unchecked")
            Map<String, Object> v = (Map<String, Object>) vars;
            return "CASE-001".equals(v.get("caseId"))
                    && "user-1".equals(v.get("initiatorId"))
                    && "张三".equals(v.get("initiatorName"));
        }));
        // Verify authentication was cleared after start
        assertNull(Authentication.getAuthenticatedUserId());
    }

    @Test
    void startProcess_shouldMergeExtraVariables() {
        StartProcessDTO dto = new StartProcessDTO();
        dto.setProcessDefinitionKey("case-review");
        dto.setCaseId("CASE-001");
        dto.setInitiatorId("user-1");

        Map<String, Object> extraVars = new HashMap<>();
        extraVars.put("customKey", "customValue");
        dto.setVariables(extraVars);

        when(runtimeService.startProcessInstanceByKey(eq("case-review"), eq("CASE-001"), anyMap()))
                .thenReturn(mockProcessInstance);
        when(mockProcessInstance.getId()).thenReturn("proc-001");

        processInstanceService.startProcess(dto);

        verify(runtimeService).startProcessInstanceByKey(eq("case-review"), eq("CASE-001"), argThat(vars -> {
            @SuppressWarnings("unchecked")
            Map<String, Object> v = (Map<String, Object>) vars;
            return "customValue".equals(v.get("customKey"));
        }));
    }

    // ==================== getProcessInstancesByCaseId Tests ====================

    @Test
    void getProcessInstancesByCaseId_shouldReturnEmpty_whenCaseIdNull() {
        List<ProcessInstanceVO> result = processInstanceService.getProcessInstancesByCaseId(null);
        assertTrue(result.isEmpty());
    }

    @Test
    void getProcessInstancesByCaseId_shouldReturnEmpty_whenCaseIdBlank() {
        List<ProcessInstanceVO> result = processInstanceService.getProcessInstancesByCaseId("");
        assertTrue(result.isEmpty());
    }

    @Test
    void getProcessInstancesByCaseId_shouldReturnRunningInstances() {
        when(runtimeService.createProcessInstanceQuery()).thenReturn(mockProcessInstanceQuery);
        when(mockProcessInstanceQuery.processInstanceBusinessKey("CASE-001")).thenReturn(mockProcessInstanceQuery);
        when(mockProcessInstanceQuery.list()).thenReturn(Collections.singletonList(mockProcessInstance));
        when(mockProcessInstance.getId()).thenReturn("proc-001");
        when(mockProcessInstance.getProcessDefinitionId()).thenReturn("def-001");
        when(mockProcessInstance.getProcessDefinitionKey()).thenReturn("case-review");
        when(mockProcessInstance.getProcessDefinitionName()).thenReturn("案件审理审批流程");
        when(mockProcessInstance.getName()).thenReturn("审批流程");
        when(mockProcessInstance.getBusinessKey()).thenReturn("CASE-001");
        when(mockProcessInstance.isSuspended()).thenReturn(false);
        when(mockProcessInstance.getStartTime()).thenReturn(new Date());

        when(historyService.createHistoricProcessInstanceQuery()).thenReturn(mockHistoricQuery);
        when(mockHistoricQuery.processInstanceBusinessKey("CASE-001")).thenReturn(mockHistoricQuery);
        when(mockHistoricQuery.finished()).thenReturn(mockHistoricQuery);
        when(mockHistoricQuery.list()).thenReturn(Collections.emptyList());

        List<ProcessInstanceVO> result = processInstanceService.getProcessInstancesByCaseId("CASE-001");

        assertEquals(1, result.size());
        ProcessInstanceVO vo = result.get(0);
        assertEquals("proc-001", vo.getProcessInstanceId());
        assertEquals("case-review", vo.getProcessDefinitionKey());
        assertFalse(vo.getEnded());
    }

    @Test
    void getProcessInstancesByCaseId_shouldNotDuplicateRunningAndHistoric() {
        // Both running and historic return the same instance
        when(runtimeService.createProcessInstanceQuery()).thenReturn(mockProcessInstanceQuery);
        when(mockProcessInstanceQuery.processInstanceBusinessKey("CASE-001")).thenReturn(mockProcessInstanceQuery);
        when(mockProcessInstanceQuery.list()).thenReturn(Collections.singletonList(mockProcessInstance));
        when(mockProcessInstance.getId()).thenReturn("proc-001");
        when(mockProcessInstance.getProcessDefinitionId()).thenReturn("def-001");
        when(mockProcessInstance.getProcessDefinitionKey()).thenReturn("case-review");
        when(mockProcessInstance.getProcessDefinitionName()).thenReturn(null);
        when(mockProcessInstance.getName()).thenReturn(null);
        when(mockProcessInstance.getBusinessKey()).thenReturn("CASE-001");
        when(mockProcessInstance.isSuspended()).thenReturn(false);
        when(mockProcessInstance.getStartTime()).thenReturn(new Date());

        when(historyService.createHistoricProcessInstanceQuery()).thenReturn(mockHistoricQuery);
        when(mockHistoricQuery.processInstanceBusinessKey("CASE-001")).thenReturn(mockHistoricQuery);
        when(mockHistoricQuery.finished()).thenReturn(mockHistoricQuery);
        when(mockHistoricQuery.list()).thenReturn(Collections.singletonList(mockHistoricInstance));
        when(mockHistoricInstance.getId()).thenReturn("proc-001");

        List<ProcessInstanceVO> result = processInstanceService.getProcessInstancesByCaseId("CASE-001");

        // Should only have 1 result (no duplicate)
        assertEquals(1, result.size());
    }

    // ==================== getProcessInstanceById Tests ====================

    @Test
    void getProcessInstanceById_shouldThrow_whenIdEmpty() {
        assertThrows(IllegalArgumentException.class, () -> processInstanceService.getProcessInstanceById(null));
        assertThrows(IllegalArgumentException.class, () -> processInstanceService.getProcessInstanceById(""));
    }

    @Test
    void getProcessInstanceById_shouldFindRunningInstance() {
        when(runtimeService.createProcessInstanceQuery()).thenReturn(mockProcessInstanceQuery);
        when(mockProcessInstanceQuery.processInstanceId("proc-001")).thenReturn(mockProcessInstanceQuery);
        when(mockProcessInstanceQuery.singleResult()).thenReturn(mockProcessInstance);
        when(mockProcessInstance.getId()).thenReturn("proc-001");
        when(mockProcessInstance.getProcessDefinitionId()).thenReturn("def-001");
        when(mockProcessInstance.getProcessDefinitionKey()).thenReturn("case-review");
        when(mockProcessInstance.getProcessDefinitionName()).thenReturn("审批流程");
        when(mockProcessInstance.getName()).thenReturn(null);
        when(mockProcessInstance.getBusinessKey()).thenReturn("CASE-001");
        when(mockProcessInstance.isSuspended()).thenReturn(false);
        when(mockProcessInstance.getStartTime()).thenReturn(new Date());

        ProcessInstanceVO result = processInstanceService.getProcessInstanceById("proc-001");

        assertNotNull(result);
        assertEquals("proc-001", result.getProcessInstanceId());
        assertFalse(result.getEnded());
    }

    @Test
    void getProcessInstanceById_shouldFallbackToHistoric_whenRunningNotFound() {
        when(runtimeService.createProcessInstanceQuery()).thenReturn(mockProcessInstanceQuery);
        when(mockProcessInstanceQuery.processInstanceId("proc-001")).thenReturn(mockProcessInstanceQuery);
        when(mockProcessInstanceQuery.singleResult()).thenReturn(null);

        when(historyService.createHistoricProcessInstanceQuery()).thenReturn(mockHistoricQuery);
        when(mockHistoricQuery.processInstanceId("proc-001")).thenReturn(mockHistoricQuery);
        when(mockHistoricQuery.singleResult()).thenReturn(mockHistoricInstance);
        when(mockHistoricInstance.getId()).thenReturn("proc-001");
        when(mockHistoricInstance.getProcessDefinitionId()).thenReturn("def-001");
        when(mockHistoricInstance.getProcessDefinitionKey()).thenReturn("case-review");
        when(mockHistoricInstance.getProcessDefinitionName()).thenReturn("审批流程");
        when(mockHistoricInstance.getName()).thenReturn(null);
        when(mockHistoricInstance.getBusinessKey()).thenReturn("CASE-001");
        when(mockHistoricInstance.getStartTime()).thenReturn(new Date());
        when(mockHistoricInstance.getEndTime()).thenReturn(new Date());
        when(mockHistoricInstance.getDurationInMillis()).thenReturn(3600000L);

        ProcessInstanceVO result = processInstanceService.getProcessInstanceById("proc-001");

        assertNotNull(result);
        assertTrue(result.getEnded());
        assertEquals(3600000L, result.getDurationInMillis());
    }

    @Test
    void getProcessInstanceById_shouldThrow_whenNotFound() {
        when(runtimeService.createProcessInstanceQuery()).thenReturn(mockProcessInstanceQuery);
        when(mockProcessInstanceQuery.processInstanceId("proc-999")).thenReturn(mockProcessInstanceQuery);
        when(mockProcessInstanceQuery.singleResult()).thenReturn(null);

        when(historyService.createHistoricProcessInstanceQuery()).thenReturn(mockHistoricQuery);
        when(mockHistoricQuery.processInstanceId("proc-999")).thenReturn(mockHistoricQuery);
        when(mockHistoricQuery.singleResult()).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> processInstanceService.getProcessInstanceById("proc-999"));
        assertTrue(ex.getMessage().contains("未找到流程实例"));
    }

    // ==================== cancelProcessInstance Tests ====================

    @Test
    void cancelProcessInstance_shouldThrow_whenIdEmpty() {
        assertThrows(IllegalArgumentException.class,
                () -> processInstanceService.cancelProcessInstance(null, "reason"));
    }

    @Test
    void cancelProcessInstance_shouldThrow_whenInstanceNotFound() {
        when(runtimeService.createProcessInstanceQuery()).thenReturn(mockProcessInstanceQuery);
        when(mockProcessInstanceQuery.processInstanceId("proc-001")).thenReturn(mockProcessInstanceQuery);
        when(mockProcessInstanceQuery.singleResult()).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> processInstanceService.cancelProcessInstance("proc-001", "user cancelled"));
        assertTrue(ex.getMessage().contains("流程实例不存在或已结束"));
    }

    @Test
    void cancelProcessInstance_shouldDeleteInstance() {
        when(runtimeService.createProcessInstanceQuery()).thenReturn(mockProcessInstanceQuery);
        when(mockProcessInstanceQuery.processInstanceId("proc-001")).thenReturn(mockProcessInstanceQuery);
        when(mockProcessInstanceQuery.singleResult()).thenReturn(mockProcessInstance);

        processInstanceService.cancelProcessInstance("proc-001", "cancelled by admin");

        verify(runtimeService).deleteProcessInstance("proc-001", "cancelled by admin");
    }

    // ==================== getProcessInstanceStatus Tests ====================

    @Test
    void getProcessInstanceStatus_shouldReturnStatusWithActiveActivity() {
        when(runtimeService.createProcessInstanceQuery()).thenReturn(mockProcessInstanceQuery);
        when(mockProcessInstanceQuery.processInstanceId("proc-001")).thenReturn(mockProcessInstanceQuery);
        when(mockProcessInstanceQuery.singleResult()).thenReturn(mockProcessInstance);
        when(mockProcessInstance.getId()).thenReturn("proc-001");
        when(mockProcessInstance.getProcessDefinitionId()).thenReturn("def-001");
        when(mockProcessInstance.getProcessDefinitionKey()).thenReturn("case-review");
        when(mockProcessInstance.getProcessDefinitionName()).thenReturn("审批流程");
        when(mockProcessInstance.getName()).thenReturn(null);
        when(mockProcessInstance.getBusinessKey()).thenReturn("CASE-001");
        when(mockProcessInstance.isSuspended()).thenReturn(false);
        when(mockProcessInstance.getStartTime()).thenReturn(new Date());

        when(runtimeService.getActiveActivityIds("proc-001")).thenReturn(Collections.singletonList("activity-1"));

        ProcessInstanceVO result = processInstanceService.getProcessInstanceStatus("proc-001");

        assertNotNull(result);
        assertEquals("activity-1", result.getCurrentActivityId());
    }
}
