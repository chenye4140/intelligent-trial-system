package com.intelligent.trial.workflow.service;

import com.intelligent.trial.workflow.dto.CompleteTaskDTO;
import com.intelligent.trial.workflow.vo.TaskVO;

import java.util.List;

/**
 * 任务服务接口
 * 提供任务查询、完成、委托、审批等操作
 *
 * @author intelligent-trial
 */
public interface ITaskService {

    /**
     * 查询指定用户的已分配任务
     *
     * @param assignee 任务处理人ID
     * @return 任务列表
     */
    List<TaskVO> getTasksByAssignee(String assignee);

    /**
     * 查询指定候选组的待办任务
     *
     * @param candidateGroup 候选组名称
     * @return 任务列表
     */
    List<TaskVO> getTasksByCandidateGroup(String candidateGroup);

    /**
     * 查询指定流程实例的所有活跃任务
     *
     * @param processInstanceId 流程实例ID
     * @return 任务列表
     */
    List<TaskVO> getActiveTasksByProcessInstanceId(String processInstanceId);

    /**
     * 完成任务
     *
     * @param taskId 任务ID
     * @param dto    完成任务参数，包含处理人、审批意见、审批结果等
     */
    void completeTask(String taskId, CompleteTaskDTO dto);

    /**
     * 签收任务（将任务认领到指定处理人）
     *
     * @param taskId   任务ID
     * @param assignee 签收人ID
     */
    void claimTask(String taskId, String assignee);

    /**
     * 委派任务给其他用户处理
     * 委派后原处理人仍保留owner身份
     *
     * @param taskId    任务ID
     * @param delegateTo 被委派人ID
     */
    void delegateTask(String taskId, String delegateTo);

    /**
     * 查询指定流程实例的已完成任务历史记录
     *
     * @param processInstanceId 流程实例ID
     * @return 历史任务列表
     */
    List<TaskVO> getTaskHistory(String processInstanceId);
}
