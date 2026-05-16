package com.intelligent.trial.workflow.service.impl;

import com.intelligent.trial.common.dto.R;
import com.intelligent.trial.common.exception.BusinessException;
import com.intelligent.trial.common.exception.ErrorCode;
import com.intelligent.trial.workflow.dto.CompleteTaskDTO;
import com.intelligent.trial.workflow.service.ITaskService;
import com.intelligent.trial.workflow.vo.TaskVO;
import org.flowable.common.engine.impl.identity.Authentication;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.task.Comment;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskQuery;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 任务服务实现类
 * 基于Flowable TaskService和HistoryService实现任务管理
 *
 * @author intelligent-trial
 */
@Service
public class TaskServiceImpl implements ITaskService {

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private RepositoryService repositoryService;

    /**
     * 查询指定用户的已分配任务
     *
     * @param assignee 任务处理人ID
     * @return 任务列表
     */
    @Override
    public List<TaskVO> getTasksByAssignee(String assignee) {
        List<TaskVO> result = new ArrayList<TaskVO>();
        if (!StringUtils.hasText(assignee)) {
            return result;
        }

        TaskQuery query = taskService.createTaskQuery()
                .taskAssignee(assignee)
                .orderByTaskCreateTime()
                .desc();

        List<Task> tasks = query.list();
        for (Task task : tasks) {
            result.add(convertTaskToVO(task));
        }
        return result;
    }

    /**
     * 查询指定候选组的待办任务
     *
     * @param candidateGroup 候选组名称
     * @return 任务列表
     */
    @Override
    public List<TaskVO> getTasksByCandidateGroup(String candidateGroup) {
        List<TaskVO> result = new ArrayList<TaskVO>();
        if (!StringUtils.hasText(candidateGroup)) {
            return result;
        }

        TaskQuery query = taskService.createTaskQuery()
                .taskCandidateGroup(candidateGroup)
                .orderByTaskCreateTime()
                .desc();

        List<Task> tasks = query.list();
        for (Task task : tasks) {
            result.add(convertTaskToVO(task));
        }
        return result;
    }

    /**
     * 查询指定流程实例的所有活跃任务
     *
     * @param processInstanceId 流程实例ID
     * @return 任务列表
     */
    @Override
    public List<TaskVO> getActiveTasksByProcessInstanceId(String processInstanceId) {
        List<TaskVO> result = new ArrayList<TaskVO>();
        if (!StringUtils.hasText(processInstanceId)) {
            return result;
        }

        TaskQuery query = taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .orderByTaskCreateTime()
                .desc();

        List<Task> tasks = query.list();
        for (Task task : tasks) {
            result.add(convertTaskToVO(task));
        }
        return result;
    }

    /**
     * 完成任务
     *
     * @param taskId 任务ID
     * @param dto    完成任务参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeTask(String taskId, CompleteTaskDTO dto) {
        if (!StringUtils.hasText(taskId)) {
            throw new IllegalArgumentException("任务ID不能为空");
        }

        Task task = taskService.createTaskQuery()
                .taskId(taskId)
                .singleResult();

        if (task == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "任务不存在: " + taskId);
        }

        // 设置处理人（如果尚未分配）
        if (task.getAssignee() == null && StringUtils.hasText(dto.getAssignee())) {
            taskService.setAssignee(taskId, dto.getAssignee());
        }

        // 构建完成任务的变量
        Map<String, Object> variables = new java.util.HashMap<String, Object>();
        if (dto.getVariables() != null) {
            variables.putAll(dto.getVariables());
        }

        // 设置审批结果
        if (StringUtils.hasText(dto.getApprovalResult())) {
            variables.put("approvalResult", dto.getApprovalResult());
        }

        // 添加审批意见作为Comment
        if (StringUtils.hasText(dto.getComment())) {
            String userId = StringUtils.hasText(dto.getAssignee()) ? dto.getAssignee() : "system";
            Authentication.setAuthenticatedUserId(userId);
            taskService.addComment(taskId, task.getProcessInstanceId(), dto.getComment());
            Authentication.setAuthenticatedUserId(null);
        }

        // 完成任务
        taskService.complete(taskId, variables);
    }

    /**
     * 签收任务（将任务认领到指定处理人）
     *
     * @param taskId   任务ID
     * @param assignee 签收人ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void claimTask(String taskId, String assignee) {
        if (!StringUtils.hasText(taskId)) {
            throw new IllegalArgumentException("任务ID不能为空");
        }
        if (!StringUtils.hasText(assignee)) {
            throw new IllegalArgumentException("签收人ID不能为空");
        }

        Task task = taskService.createTaskQuery()
                .taskId(taskId)
                .singleResult();

        if (task == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "任务不存在: " + taskId);
        }

        taskService.claim(taskId, assignee);
    }

    /**
     * 委派任务给其他用户处理
     *
     * @param taskId     任务ID
     * @param delegateTo 被委派人ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delegateTask(String taskId, String delegateTo) {
        if (!StringUtils.hasText(taskId)) {
            throw new IllegalArgumentException("任务ID不能为空");
        }
        if (!StringUtils.hasText(delegateTo)) {
            throw new IllegalArgumentException("被委派人ID不能为空");
        }

        Task task = taskService.createTaskQuery()
                .taskId(taskId)
                .singleResult();

        if (task == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "任务不存在: " + taskId);
        }

        taskService.delegateTask(taskId, delegateTo);
    }

    /**
     * 查询指定流程实例的已完成任务历史记录
     *
     * @param processInstanceId 流程实例ID
     * @return 历史任务列表
     */
    @Override
    public List<TaskVO> getTaskHistory(String processInstanceId) {
        List<TaskVO> result = new ArrayList<TaskVO>();
        if (!StringUtils.hasText(processInstanceId)) {
            return result;
        }

        List<HistoricTaskInstance> historyTasks = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(processInstanceId)
                .orderByHistoricTaskInstanceEndTime()
                .desc()
                .list();

        for (HistoricTaskInstance historyTask : historyTasks) {
            result.add(convertHistoricTaskToVO(historyTask));
        }
        return result;
    }

    /**
     * 将运行中的任务转换为VO
     *
     * @param task Flowable任务对象
     * @return 任务VO
     */
    private TaskVO convertTaskToVO(Task task) {
        TaskVO vo = new TaskVO();
        vo.setTaskId(task.getId());
        vo.setTaskName(task.getName());
        vo.setTaskDefinitionKey(task.getTaskDefinitionKey());
        vo.setDescription(task.getDescription());
        vo.setAssignee(task.getAssignee());
        vo.setProcessInstanceId(task.getProcessInstanceId());
        vo.setCreateTime(task.getCreateTime());
        vo.setEndTime(null);
        vo.setDurationInMillis(null);
        vo.setTaskStatus("active");
        vo.setParentTaskId(task.getParentTaskId());
        vo.setPriority(task.getPriority());

        // 从流程实例获取补充信息
        enrichTaskVOWithProcessInfo(vo, task.getProcessInstanceId(), task.getProcessDefinitionId());

        return vo;
    }

    /**
     * 将历史任务转换为VO
     *
     * @param historyTask Flowable历史任务对象
     * @return 任务VO
     */
    private TaskVO convertHistoricTaskToVO(HistoricTaskInstance historyTask) {
        TaskVO vo = new TaskVO();
        vo.setTaskId(historyTask.getId());
        vo.setTaskName(historyTask.getName());
        vo.setTaskDefinitionKey(historyTask.getTaskDefinitionKey());
        vo.setDescription(historyTask.getDescription());
        vo.setAssignee(historyTask.getAssignee());
        vo.setProcessInstanceId(historyTask.getProcessInstanceId());
        vo.setCreateTime(historyTask.getCreateTime());
        vo.setEndTime(historyTask.getEndTime());
        vo.setDurationInMillis(historyTask.getDurationInMillis());
        vo.setTaskStatus(historyTask.getDeleteReason() != null ? "deleted" : "completed");
        vo.setParentTaskId(historyTask.getParentTaskId());
        vo.setPriority(historyTask.getPriority());

        // 尝试获取审批意见
        List<Comment> comments = taskService.getTaskComments(historyTask.getId());
        if (comments != null && !comments.isEmpty()) {
            vo.setComment(comments.get(0).getFullMessage());
        }

        // 从历史变量中获取审批结果
        try {
            List<org.flowable.variable.api.history.HistoricVariableInstance> varInstances =
                    historyService.createHistoricVariableInstanceQuery()
                            .taskId(historyTask.getId())
                            .list();
            if (varInstances != null) {
                for (org.flowable.variable.api.history.HistoricVariableInstance var : varInstances) {
                    if ("approvalResult".equals(var.getVariableName())) {
                        vo.setApprovalResult(String.valueOf(var.getValue()));
                        break;
                    }
                }
            }
        } catch (Exception e) {
            // 忽略
        }

        // 从流程变量中获取案件标题和业务Key
        enrichTaskVOWithProcessInfo(vo, historyTask.getProcessInstanceId(), historyTask.getProcessDefinitionId());

        return vo;
    }

    /**
     * 从流程变量中补充任务VO的案件信息
     *
     * @param vo                  任务VO
     * @param processInstanceId   流程实例ID
     * @param processDefinitionId 流程定义ID
     */
    private void enrichTaskVOWithProcessInfo(TaskVO vo, String processInstanceId, String processDefinitionId) {
        try {
            // 获取业务Key
            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .singleResult();
            if (processInstance != null) {
                vo.setBusinessKey(processInstance.getBusinessKey());
                vo.setProcessDefinitionKey(processInstance.getProcessDefinitionKey());
                vo.setProcessDefinitionName(processInstance.getProcessDefinitionName());

                // 获取案件标题
                Map<String, Object> vars = runtimeService.getVariables(processInstanceId);
                if (vars != null && vars.containsKey("caseTitle")) {
                    vo.setCaseTitle(String.valueOf(vars.get("caseTitle")));
                }
            } else {
                // 尝试从历史中获取
                HistoricProcessInstance histInstance =
                        historyService.createHistoricProcessInstanceQuery()
                                .processInstanceId(processInstanceId)
                                .singleResult();
                if (histInstance != null) {
                    vo.setBusinessKey(histInstance.getBusinessKey());
                    vo.setProcessDefinitionKey(histInstance.getProcessDefinitionKey());
                    vo.setProcessDefinitionName(histInstance.getProcessDefinitionName());
                }
            }

            // 获取流程定义名称
            if (processDefinitionId != null && vo.getProcessDefinitionName() == null) {
                org.flowable.engine.repository.ProcessDefinition def =
                        repositoryService.createProcessDefinitionQuery()
                                .processDefinitionId(processDefinitionId)
                                .singleResult();
                if (def != null) {
                    vo.setProcessDefinitionName(def.getName());
                }
            }
        } catch (Exception e) {
            // 忽略获取流程信息时的异常
        }
    }
}
