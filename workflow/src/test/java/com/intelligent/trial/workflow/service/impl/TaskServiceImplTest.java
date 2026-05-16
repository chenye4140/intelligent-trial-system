package com.intelligent.trial.workflow.service.impl;

import com.intelligent.trial.common.exception.BusinessException;
import com.intelligent.trial.workflow.dto.CompleteTaskDTO;
import com.intelligent.trial.workflow.vo.TaskVO;
import org.flowable.common.engine.impl.identity.Authentication;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.runtime.ProcessInstanceQuery;
import org.flowable.engine.task.Comment;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskQuery;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.task.api.history.HistoricTaskInstanceQuery;
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
 * TaskServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @InjectMocks
    private TaskServiceImpl taskService;

    @Mock
    private TaskService flowableTaskService;

    @Mock
    private HistoryService historyService;

    @Mock
    private RuntimeService runtimeService;

    @Mock
    private RepositoryService repositoryService;

    @Mock
    private TaskQuery mockTaskQuery;

    @Mock
    private Task mockTask;

    @Mock
    private HistoricTaskInstanceQuery mockHistoricTaskQuery;

    @Mock
    private HistoricTaskInstance mockHistoricTask;

    @Mock
    private ProcessInstanceQuery mockProcessInstanceQuery;

    @Mock
    private ProcessInstance mockProcessInstance;

    @Mock
    private Comment mockComment;

    @BeforeEach
    void setUp() {
        Authentication.setAuthenticatedUserId(null);
    }

    @AfterEach
    void tearDown() {
        Authentication.setAuthenticatedUserId(null);
    }

    // ==================== getTasksByAssignee Tests ====================

    @Test
    void getTasksByAssignee_shouldReturnEmpty_whenAssigneeNull() {
        List<TaskVO> result = taskService.getTasksByAssignee(null);
        assertTrue(result.isEmpty());
    }

    @Test
    void getTasksByAssignee_shouldReturnEmpty_whenAssigneeBlank() {
        List<TaskVO> result = taskService.getTasksByAssignee("");
        assertTrue(result.isEmpty());
    }

    @Test
    void getTasksByAssignee_shouldReturnTasks() {
        when(flowableTaskService.createTaskQuery()).thenReturn(mockTaskQuery);
        when(mockTaskQuery.taskAssignee("user-1")).thenReturn(mockTaskQuery);
        when(mockTaskQuery.orderByTaskCreateTime()).thenReturn(mockTaskQuery);
        when(mockTaskQuery.desc()).thenReturn(mockTaskQuery);
        when(mockTaskQuery.list()).thenReturn(Collections.singletonList(mockTask));
        when(mockTask.getId()).thenReturn("task-001");
        when(mockTask.getName()).thenReturn("部门审核");
        when(mockTask.getTaskDefinitionKey()).thenReturn("dept_review");
        when(mockTask.getDescription()).thenReturn("请审核案件材料");
        when(mockTask.getAssignee()).thenReturn("user-1");
        when(mockTask.getProcessInstanceId()).thenReturn("proc-001");
        when(mockTask.getCreateTime()).thenReturn(new Date());
        when(mockTask.getParentTaskId()).thenReturn(null);
        when(mockTask.getPriority()).thenReturn(50);
        when(mockTask.getProcessDefinitionId()).thenReturn("def-001");

        // Enrichment returns null (no running process found)
        when(runtimeService.createProcessInstanceQuery()).thenReturn(mockProcessInstanceQuery);
        when(mockProcessInstanceQuery.processInstanceId("proc-001")).thenReturn(mockProcessInstanceQuery);
        when(mockProcessInstanceQuery.singleResult()).thenReturn(null);

        List<TaskVO> result = taskService.getTasksByAssignee("user-1");

        assertEquals(1, result.size());
        TaskVO vo = result.get(0);
        assertEquals("task-001", vo.getTaskId());
        assertEquals("部门审核", vo.getTaskName());
        assertEquals("dept_review", vo.getTaskDefinitionKey());
        assertEquals("user-1", vo.getAssignee());
        assertEquals("active", vo.getTaskStatus());
    }

    // ==================== getTasksByCandidateGroup Tests ====================

    @Test
    void getTasksByCandidateGroup_shouldReturnEmpty_whenGroupNull() {
        List<TaskVO> result = taskService.getTasksByCandidateGroup(null);
        assertTrue(result.isEmpty());
    }

    @Test
    void getTasksByCandidateGroup_shouldReturnTasks() {
        when(flowableTaskService.createTaskQuery()).thenReturn(mockTaskQuery);
        when(mockTaskQuery.taskCandidateGroup("discipline")).thenReturn(mockTaskQuery);
        when(mockTaskQuery.orderByTaskCreateTime()).thenReturn(mockTaskQuery);
        when(mockTaskQuery.desc()).thenReturn(mockTaskQuery);
        when(mockTaskQuery.list()).thenReturn(Collections.singletonList(mockTask));
        when(mockTask.getId()).thenReturn("task-002");
        when(mockTask.getName()).thenReturn("纪检审核");
        when(mockTask.getTaskDefinitionKey()).thenReturn("discipline_review");
        when(mockTask.getDescription()).thenReturn(null);
        when(mockTask.getAssignee()).thenReturn(null);
        when(mockTask.getProcessInstanceId()).thenReturn("proc-002");
        when(mockTask.getCreateTime()).thenReturn(new Date());
        when(mockTask.getParentTaskId()).thenReturn(null);
        when(mockTask.getPriority()).thenReturn(50);
        when(mockTask.getProcessDefinitionId()).thenReturn("def-001");

        when(runtimeService.createProcessInstanceQuery()).thenReturn(mockProcessInstanceQuery);
        when(mockProcessInstanceQuery.processInstanceId("proc-002")).thenReturn(mockProcessInstanceQuery);
        when(mockProcessInstanceQuery.singleResult()).thenReturn(null);

        List<TaskVO> result = taskService.getTasksByCandidateGroup("discipline");

        assertEquals(1, result.size());
        assertNull(result.get(0).getAssignee());
    }

    // ==================== getActiveTasksByProcessInstanceId Tests ====================

    @Test
    void getActiveTasksByProcessInstanceId_shouldReturnEmpty_whenIdNull() {
        List<TaskVO> result = taskService.getActiveTasksByProcessInstanceId(null);
        assertTrue(result.isEmpty());
    }

    @Test
    void getActiveTasksByProcessInstanceId_shouldReturnTasks() {
        when(flowableTaskService.createTaskQuery()).thenReturn(mockTaskQuery);
        when(mockTaskQuery.processInstanceId("proc-001")).thenReturn(mockTaskQuery);
        when(mockTaskQuery.orderByTaskCreateTime()).thenReturn(mockTaskQuery);
        when(mockTaskQuery.desc()).thenReturn(mockTaskQuery);
        when(mockTaskQuery.list()).thenReturn(Collections.singletonList(mockTask));
        when(mockTask.getId()).thenReturn("task-003");
        when(mockTask.getName()).thenReturn("领导审批");
        when(mockTask.getTaskDefinitionKey()).thenReturn("leader_approval");
        when(mockTask.getDescription()).thenReturn(null);
        when(mockTask.getAssignee()).thenReturn("leader-1");
        when(mockTask.getProcessInstanceId()).thenReturn("proc-001");
        when(mockTask.getCreateTime()).thenReturn(new Date());
        when(mockTask.getParentTaskId()).thenReturn(null);
        when(mockTask.getPriority()).thenReturn(50);
        when(mockTask.getProcessDefinitionId()).thenReturn("def-001");

        when(runtimeService.createProcessInstanceQuery()).thenReturn(mockProcessInstanceQuery);
        when(mockProcessInstanceQuery.processInstanceId("proc-001")).thenReturn(mockProcessInstanceQuery);
        when(mockProcessInstanceQuery.singleResult()).thenReturn(mockProcessInstance);
        when(mockProcessInstance.getBusinessKey()).thenReturn("CASE-001");
        when(mockProcessInstance.getProcessDefinitionKey()).thenReturn("case-review");
        when(mockProcessInstance.getProcessDefinitionName()).thenReturn("审批流程");

        List<TaskVO> result = taskService.getActiveTasksByProcessInstanceId("proc-001");

        assertEquals(1, result.size());
        assertEquals("CASE-001", result.get(0).getBusinessKey());
    }

    // ==================== completeTask Tests ====================

    @Test
    void completeTask_shouldThrow_whenTaskIdEmpty() {
        CompleteTaskDTO dto = new CompleteTaskDTO();
        assertThrows(IllegalArgumentException.class,
                () -> taskService.completeTask(null, dto));
    }

    @Test
    void completeTask_shouldThrow_whenTaskNotFound() {
        when(flowableTaskService.createTaskQuery()).thenReturn(mockTaskQuery);
        when(mockTaskQuery.taskId("task-999")).thenReturn(mockTaskQuery);
        when(mockTaskQuery.singleResult()).thenReturn(null);

        CompleteTaskDTO dto = new CompleteTaskDTO();
        BusinessException ex = assertThrows(BusinessException.class,
                () -> taskService.completeTask("task-999", dto));
        assertTrue(ex.getMessage().contains("任务不存在"));
    }

    @Test
    void completeTask_shouldSetAssignee_whenUnassigned() {
        when(flowableTaskService.createTaskQuery()).thenReturn(mockTaskQuery);
        when(mockTaskQuery.taskId("task-001")).thenReturn(mockTaskQuery);
        when(mockTaskQuery.singleResult()).thenReturn(mockTask);
        when(mockTask.getAssignee()).thenReturn(null);
        when(mockTask.getProcessInstanceId()).thenReturn("proc-001");

        CompleteTaskDTO dto = new CompleteTaskDTO();
        dto.setAssignee("user-1");
        dto.setApprovalResult("同意");
        dto.setComment("材料齐全，同意通过");

        taskService.completeTask("task-001", dto);

        verify(flowableTaskService).setAssignee("task-001", "user-1");
    }

    @Test
    void completeTask_shouldAddComment_whenProvided() {
        when(flowableTaskService.createTaskQuery()).thenReturn(mockTaskQuery);
        when(mockTaskQuery.taskId("task-001")).thenReturn(mockTaskQuery);
        when(mockTaskQuery.singleResult()).thenReturn(mockTask);
        when(mockTask.getAssignee()).thenReturn("user-1");
        when(mockTask.getProcessInstanceId()).thenReturn("proc-001");

        CompleteTaskDTO dto = new CompleteTaskDTO();
        dto.setAssignee("user-1");
        dto.setComment("审批意见");

        taskService.completeTask("task-001", dto);

        verify(flowableTaskService).addComment("task-001", "proc-001", "审批意见");
        // Verify authentication was cleared
        assertNull(Authentication.getAuthenticatedUserId());
    }

    @Test
    void completeTask_shouldCompleteWithVariables() {
        when(flowableTaskService.createTaskQuery()).thenReturn(mockTaskQuery);
        when(mockTaskQuery.taskId("task-001")).thenReturn(mockTaskQuery);
        when(mockTaskQuery.singleResult()).thenReturn(mockTask);
        when(mockTask.getAssignee()).thenReturn("user-1");
        // No comment provided in DTO, so addComment path is skipped
        // getProcessInstanceId is only called when comment is provided
        CompleteTaskDTO dto = new CompleteTaskDTO();
        Map<String, Object> vars = new HashMap<>();
        vars.put("approved", true);
        dto.setVariables(vars);
        dto.setApprovalResult("同意");

        taskService.completeTask("task-001", dto);

        verify(flowableTaskService).complete(eq("task-001"), argThat(v -> {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) v;
            return Boolean.TRUE.equals(map.get("approved"))
                    && "同意".equals(map.get("approvalResult"));
        }));
    }

    // ==================== claimTask Tests ====================

    @Test
    void claimTask_shouldThrow_whenTaskIdEmpty() {
        assertThrows(IllegalArgumentException.class,
                () -> taskService.claimTask(null, "user-1"));
    }

    @Test
    void claimTask_shouldThrow_whenAssigneeEmpty() {
        assertThrows(IllegalArgumentException.class,
                () -> taskService.claimTask("task-001", null));
    }

    @Test
    void claimTask_shouldThrow_whenTaskNotFound() {
        when(flowableTaskService.createTaskQuery()).thenReturn(mockTaskQuery);
        when(mockTaskQuery.taskId("task-999")).thenReturn(mockTaskQuery);
        when(mockTaskQuery.singleResult()).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> taskService.claimTask("task-999", "user-1"));
        assertTrue(ex.getMessage().contains("任务不存在"));
    }

    @Test
    void claimTask_shouldCallClaim() {
        when(flowableTaskService.createTaskQuery()).thenReturn(mockTaskQuery);
        when(mockTaskQuery.taskId("task-001")).thenReturn(mockTaskQuery);
        when(mockTaskQuery.singleResult()).thenReturn(mockTask);

        taskService.claimTask("task-001", "user-1");

        verify(flowableTaskService).claim("task-001", "user-1");
    }

    // ==================== delegateTask Tests ====================

    @Test
    void delegateTask_shouldThrow_whenTaskIdEmpty() {
        assertThrows(IllegalArgumentException.class,
                () -> taskService.delegateTask(null, "user-2"));
    }

    @Test
    void delegateTask_shouldThrow_whenDelegateToEmpty() {
        assertThrows(IllegalArgumentException.class,
                () -> taskService.delegateTask("task-001", null));
    }

    @Test
    void delegateTask_shouldThrow_whenTaskNotFound() {
        when(flowableTaskService.createTaskQuery()).thenReturn(mockTaskQuery);
        when(mockTaskQuery.taskId("task-999")).thenReturn(mockTaskQuery);
        when(mockTaskQuery.singleResult()).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> taskService.delegateTask("task-999", "user-2"));
        assertTrue(ex.getMessage().contains("任务不存在"));
    }

    @Test
    void delegateTask_shouldCallDelegateTask() {
        when(flowableTaskService.createTaskQuery()).thenReturn(mockTaskQuery);
        when(mockTaskQuery.taskId("task-001")).thenReturn(mockTaskQuery);
        when(mockTaskQuery.singleResult()).thenReturn(mockTask);

        taskService.delegateTask("task-001", "user-2");

        verify(flowableTaskService).delegateTask("task-001", "user-2");
    }

    // ==================== getTaskHistory Tests ====================

    @Test
    void getTaskHistory_shouldReturnEmpty_whenProcessInstanceIdNull() {
        List<TaskVO> result = taskService.getTaskHistory(null);
        assertTrue(result.isEmpty());
    }

    @Test
    void getTaskHistory_shouldReturnHistoricTasks() {
        when(historyService.createHistoricTaskInstanceQuery()).thenReturn(mockHistoricTaskQuery);
        when(mockHistoricTaskQuery.processInstanceId("proc-001")).thenReturn(mockHistoricTaskQuery);
        when(mockHistoricTaskQuery.orderByHistoricTaskInstanceEndTime()).thenReturn(mockHistoricTaskQuery);
        when(mockHistoricTaskQuery.desc()).thenReturn(mockHistoricTaskQuery);
        when(mockHistoricTaskQuery.list()).thenReturn(Collections.singletonList(mockHistoricTask));

        when(mockHistoricTask.getId()).thenReturn("hist-task-001");
        when(mockHistoricTask.getName()).thenReturn("部门审核");
        when(mockHistoricTask.getTaskDefinitionKey()).thenReturn("dept_review");
        when(mockHistoricTask.getDescription()).thenReturn("审核材料");
        when(mockHistoricTask.getAssignee()).thenReturn("user-1");
        when(mockHistoricTask.getProcessInstanceId()).thenReturn("proc-001");
        when(mockHistoricTask.getCreateTime()).thenReturn(new Date());
        when(mockHistoricTask.getEndTime()).thenReturn(new Date());
        when(mockHistoricTask.getDurationInMillis()).thenReturn(7200000L);
        when(mockHistoricTask.getDeleteReason()).thenReturn(null);
        when(mockHistoricTask.getParentTaskId()).thenReturn(null);
        when(mockHistoricTask.getPriority()).thenReturn(50);
        when(mockHistoricTask.getProcessDefinitionId()).thenReturn("def-001");

        // No comments
        when(flowableTaskService.getTaskComments("hist-task-001")).thenReturn(Collections.emptyList());

        List<TaskVO> result = taskService.getTaskHistory("proc-001");

        assertEquals(1, result.size());
        TaskVO vo = result.get(0);
        assertEquals("hist-task-001", vo.getTaskId());
        assertEquals("completed", vo.getTaskStatus());
        assertEquals(7200000L, vo.getDurationInMillis());
    }

    @Test
    void getTaskHistory_shouldIncludeComments() {
        when(historyService.createHistoricTaskInstanceQuery()).thenReturn(mockHistoricTaskQuery);
        when(mockHistoricTaskQuery.processInstanceId("proc-001")).thenReturn(mockHistoricTaskQuery);
        when(mockHistoricTaskQuery.orderByHistoricTaskInstanceEndTime()).thenReturn(mockHistoricTaskQuery);
        when(mockHistoricTaskQuery.desc()).thenReturn(mockHistoricTaskQuery);
        when(mockHistoricTaskQuery.list()).thenReturn(Collections.singletonList(mockHistoricTask));

        when(mockHistoricTask.getId()).thenReturn("hist-task-002");
        when(mockHistoricTask.getName()).thenReturn("领导审批");
        when(mockHistoricTask.getTaskDefinitionKey()).thenReturn("leader_approval");
        when(mockHistoricTask.getDescription()).thenReturn(null);
        when(mockHistoricTask.getAssignee()).thenReturn("leader-1");
        when(mockHistoricTask.getProcessInstanceId()).thenReturn("proc-001");
        when(mockHistoricTask.getCreateTime()).thenReturn(new Date());
        when(mockHistoricTask.getEndTime()).thenReturn(new Date());
        when(mockHistoricTask.getDurationInMillis()).thenReturn(3600000L);
        when(mockHistoricTask.getDeleteReason()).thenReturn(null);
        when(mockHistoricTask.getParentTaskId()).thenReturn(null);
        when(mockHistoricTask.getPriority()).thenReturn(50);
        when(mockHistoricTask.getProcessDefinitionId()).thenReturn("def-001");

        when(flowableTaskService.getTaskComments("hist-task-002")).thenReturn(Collections.singletonList(mockComment));
        when(mockComment.getFullMessage()).thenReturn("审批通过");

        List<TaskVO> result = taskService.getTaskHistory("proc-001");

        assertEquals("审批通过", result.get(0).getComment());
    }

    @Test
    void getTaskHistory_shouldShowDeleted_whenTaskWasDeleted() {
        when(historyService.createHistoricTaskInstanceQuery()).thenReturn(mockHistoricTaskQuery);
        when(mockHistoricTaskQuery.processInstanceId("proc-001")).thenReturn(mockHistoricTaskQuery);
        when(mockHistoricTaskQuery.orderByHistoricTaskInstanceEndTime()).thenReturn(mockHistoricTaskQuery);
        when(mockHistoricTaskQuery.desc()).thenReturn(mockHistoricTaskQuery);
        when(mockHistoricTaskQuery.list()).thenReturn(Collections.singletonList(mockHistoricTask));

        when(mockHistoricTask.getId()).thenReturn("hist-task-003");
        when(mockHistoricTask.getName()).thenReturn("纪检审核");
        when(mockHistoricTask.getTaskDefinitionKey()).thenReturn("discipline_review");
        when(mockHistoricTask.getDescription()).thenReturn(null);
        when(mockHistoricTask.getAssignee()).thenReturn("user-2");
        when(mockHistoricTask.getProcessInstanceId()).thenReturn("proc-001");
        when(mockHistoricTask.getCreateTime()).thenReturn(new Date());
        when(mockHistoricTask.getEndTime()).thenReturn(new Date());
        when(mockHistoricTask.getDurationInMillis()).thenReturn(1800000L);
        when(mockHistoricTask.getDeleteReason()).thenReturn("流程取消");
        when(mockHistoricTask.getParentTaskId()).thenReturn(null);
        when(mockHistoricTask.getPriority()).thenReturn(50);
        when(mockHistoricTask.getProcessDefinitionId()).thenReturn("def-001");

        when(flowableTaskService.getTaskComments("hist-task-003")).thenReturn(Collections.emptyList());

        List<TaskVO> result = taskService.getTaskHistory("proc-001");

        assertEquals("deleted", result.get(0).getTaskStatus());
    }
}
