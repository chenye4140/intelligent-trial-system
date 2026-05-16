package com.intelligent.trial.workflow.service;

import com.intelligent.trial.workflow.dto.StartProcessDTO;
import com.intelligent.trial.workflow.vo.ProcessInstanceVO;

import java.util.List;

/**
 * 流程实例服务接口
 * 提供流程实例的启动、查询、取消等操作
 *
 * @author intelligent-trial
 */
public interface IProcessInstanceService {

    /**
     * 启动一个新的流程实例
     *
     * @param dto 启动流程请求参数，包含案件ID、流程定义Key、发起人等信息
     * @return 新创建的流程实例ID
     */
    String startProcess(StartProcessDTO dto);

    /**
     * 根据案件ID查询该案件关联的所有流程实例
     *
     * @param caseId 案件ID
     * @return 流程实例列表
     */
    List<ProcessInstanceVO> getProcessInstancesByCaseId(String caseId);

    /**
     * 根据流程实例ID获取流程实例详情
     *
     * @param processInstanceId 流程实例ID
     * @return 流程实例VO
     */
    ProcessInstanceVO getProcessInstanceById(String processInstanceId);

    /**
     * 获取流程实例的当前状态信息
     * 包含当前活动节点、运行状态等
     *
     * @param processInstanceId 流程实例ID
     * @return 流程实例VO（含当前状态）
     */
    ProcessInstanceVO getProcessInstanceStatus(String processInstanceId);

    /**
     * 取消/终止正在运行的流程实例
     *
     * @param processInstanceId 流程实例ID
     * @param reason            取消原因
     */
    void cancelProcessInstance(String processInstanceId, String reason);

    /**
     * 挂起指定的流程实例
     * 挂起后流程实例暂停执行
     *
     * @param processInstanceId 流程实例ID
     */
    void suspendProcessInstance(String processInstanceId);

    /**
     * 激活已挂起的流程实例
     *
     * @param processInstanceId 流程实例ID
     */
    void activateProcessInstance(String processInstanceId);
}
