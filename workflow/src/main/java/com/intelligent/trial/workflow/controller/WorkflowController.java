package com.intelligent.trial.workflow.controller;

import com.intelligent.trial.common.annotation.RequireLog;
import com.intelligent.trial.common.dto.R;
import com.intelligent.trial.workflow.dto.CompleteTaskDTO;
import com.intelligent.trial.workflow.dto.StartProcessDTO;
import com.intelligent.trial.workflow.service.IProcessDefinitionService;
import com.intelligent.trial.workflow.service.IProcessInstanceService;
import com.intelligent.trial.workflow.service.ITaskService;
import com.intelligent.trial.workflow.vo.ProcessDefinitionVO;
import com.intelligent.trial.workflow.vo.ProcessInstanceVO;
import com.intelligent.trial.workflow.vo.TaskVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import javax.validation.Valid;

/**
 * 工作流REST控制器
 * 提供案件审理审批流程的完整REST API
 * 包括流程定义管理、流程实例管理、任务管理等
 *
 * @author intelligent-trial
 */
@RestController
@RequestMapping("/api/workflow")
public class WorkflowController {

    @Autowired
    private IProcessDefinitionService processDefinitionService;

    @Autowired
    private IProcessInstanceService processInstanceService;

    @Autowired
    private ITaskService taskService;

    // ==================== 流程定义管理 ====================

    /**
     * 查询所有已部署的流程定义
     *
     * @return 流程定义列表
     */
    @GetMapping("/process/definitions")
    public R<List<ProcessDefinitionVO>> listProcessDefinitions() {
        List<ProcessDefinitionVO> definitions = processDefinitionService.listProcessDefinitions();
        return R.ok(definitions);
    }

    /**
     * 根据流程定义Key部署流程
     *
     * @param key 流程定义Key，例如 "case-review-approval"
     * @return 部署ID
     */
    @PostMapping("/process/definitions/deploy/{key}")
    @RequireLog(module = "工作流", action = "部署")
    public R<String> deployProcess(@PathVariable("key") String key) {
        String deploymentId = processDefinitionService.deployProcessByKey(key);
        return R.ok("流程部署成功", deploymentId);
    }

    // ==================== 流程实例管理 ====================

    /**
     * 启动一个流程实例
     *
     * @param dto 启动流程请求参数
     * @return 新创建的流程实例ID
     */
    @PostMapping("/process/start")
    @RequireLog(module = "工作流", action = "启动")
    public R<String> startProcess(@Valid @RequestBody StartProcessDTO dto) {
        String processInstanceId = processInstanceService.startProcess(dto);
        return R.ok("流程启动成功", processInstanceId);
    }

    /**
     * 根据案件ID查询该案件关联的所有流程实例
     *
     * @param caseId 案件ID
     * @return 流程实例列表
     */
    @GetMapping("/process/instances/{caseId}")
    public R<List<ProcessInstanceVO>> getProcessInstancesByCaseId(@PathVariable("caseId") String caseId) {
        List<ProcessInstanceVO> instances = processInstanceService.getProcessInstancesByCaseId(caseId);
        return R.ok(instances);
    }

    /**
     * 获取流程实例的状态信息
     * 包含当前活动节点、运行状态等
     *
     * @param processInstanceId 流程实例ID
     * @return 流程实例状态
     */
    @GetMapping("/process/instances/{processInstanceId}/status")
    public R<ProcessInstanceVO> getProcessInstanceStatus(@PathVariable("processInstanceId") String processInstanceId) {
        ProcessInstanceVO status = processInstanceService.getProcessInstanceStatus(processInstanceId);
        return R.ok(status);
    }

    /**
     * 取消/终止流程实例
     *
     * @param processInstanceId 流程实例ID
     * @param reason            取消原因（可选）
     * @return 操作结果
     */
    @DeleteMapping("/process/instances/{processInstanceId}")
    @RequireLog(module = "工作流", action = "取消")
    public R<Void> cancelProcessInstance(
            @PathVariable("processInstanceId") String processInstanceId,
            @RequestParam(value = "reason", required = false, defaultValue = "用户主动取消") String reason) {
        processInstanceService.cancelProcessInstance(processInstanceId, reason);
        return R.ok("流程实例已终止", null);
    }

    // ==================== 任务管理 ====================

    /**
     * 查询指定用户的已分配任务
     *
     * @param assignee 任务处理人ID
     * @return 任务列表
     */
    @GetMapping("/task/my-tasks")
    public R<List<TaskVO>> getMyTasks(@RequestParam("assignee") String assignee) {
        List<TaskVO> tasks = taskService.getTasksByAssignee(assignee);
        return R.ok(tasks);
    }

    /**
     * 查询指定候选组的待办任务
     *
     * @param candidateGroup 候选组名称，如 "department", "discipline", "leader"
     * @return 任务列表
     */
    @GetMapping("/task/pending-tasks")
    public R<List<TaskVO>> getPendingTasks(@RequestParam("candidateGroup") String candidateGroup) {
        List<TaskVO> tasks = taskService.getTasksByCandidateGroup(candidateGroup);
        return R.ok(tasks);
    }

    /**
     * 完成任务
     *
     * @param taskId 任务ID
     * @param dto    完成任务参数
     * @return 操作结果
     */
    @PutMapping("/task/complete/{taskId}")
    @RequireLog(module = "工作流", action = "完成任务")
    public R<Void> completeTask(
            @PathVariable("taskId") String taskId,
            @RequestBody CompleteTaskDTO dto) {
        taskService.completeTask(taskId, dto);
        return R.ok("任务已完成", null);
    }

    /**
     * 签收任务
     *
     * @param taskId   任务ID
     * @param assignee 签收人ID
     * @return 操作结果
     */
    @PutMapping("/task/claim/{taskId}")
    @RequireLog(module = "工作流", action = "签收")
    public R<Void> claimTask(
            @PathVariable("taskId") String taskId,
            @RequestParam("assignee") String assignee) {
        taskService.claimTask(taskId, assignee);
        return R.ok("任务已签收", null);
    }

    /**
     * 委派任务给其他用户
     *
     * @param taskId     任务ID
     * @param delegateTo 被委派人ID
     * @return 操作结果
     */
    @PutMapping("/task/delegate/{taskId}")
    @RequireLog(module = "工作流", action = "委派")
    public R<Void> delegateTask(
            @PathVariable("taskId") String taskId,
            @RequestParam("delegateTo") String delegateTo) {
        taskService.delegateTask(taskId, delegateTo);
        return R.ok("任务已委派", null);
    }

    /**
     * 查询指定流程实例的任务历史记录
     *
     * @param processInstanceId 流程实例ID
     * @return 历史任务列表
     */
    @GetMapping("/task/history/{processInstanceId}")
    public R<List<TaskVO>> getTaskHistory(@PathVariable("processInstanceId") String processInstanceId) {
        List<TaskVO> history = taskService.getTaskHistory(processInstanceId);
        return R.ok(history);
    }
}
