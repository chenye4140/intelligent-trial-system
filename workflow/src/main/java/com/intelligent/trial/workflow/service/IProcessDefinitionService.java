package com.intelligent.trial.workflow.service;

import com.intelligent.trial.workflow.vo.ProcessDefinitionVO;

import java.util.List;

/**
 * 流程定义服务接口
 * 提供流程定义的部署、查询、删除等操作
 *
 * @author intelligent-trial
 */
public interface IProcessDefinitionService {

    /**
     * 根据流程定义Key部署流程
     * 从classpath的processes目录自动部署匹配的BPMN文件
     *
     * @param processDefinitionKey 流程定义Key
     * @return 部署ID
     */
    String deployProcessByKey(String processDefinitionKey);

    /**
     * 部署指定classpath路径的BPMN流程定义
     *
     * @param bpmnResourcePath BPMN文件在classpath中的路径
     * @return 部署ID
     */
    String deployProcess(String bpmnResourcePath);

    /**
     * 查询所有已部署的流程定义
     *
     * @return 流程定义列表
     */
    List<ProcessDefinitionVO> listProcessDefinitions();

    /**
     * 根据流程定义Key查询最新版本的流程定义
     *
     * @param processDefinitionKey 流程定义Key
     * @return 流程定义VO
     */
    ProcessDefinitionVO getLatestProcessDefinition(String processDefinitionKey);

    /**
     * 根据流程定义ID获取流程定义详情
     *
     * @param processDefinitionId 流程定义ID
     * @return 流程定义VO
     */
    ProcessDefinitionVO getProcessDefinitionById(String processDefinitionId);

    /**
     * 挂起指定的流程定义
     * 挂起后无法启动新的流程实例
     *
     * @param processDefinitionId 流程定义ID
     */
    void suspendProcessDefinition(String processDefinitionId);

    /**
     * 激活指定的流程定义
     *
     * @param processDefinitionId 流程定义ID
     */
    void activateProcessDefinition(String processDefinitionId);

    /**
     * 删除流程定义及其部署信息
     * 注意：如果存在运行中的流程实例，将同时删除这些实例
     *
     * @param deploymentId 部署ID
     * @param cascade      是否级联删除运行中的流程实例
     */
    void deleteProcessDefinition(String deploymentId, boolean cascade);
}
