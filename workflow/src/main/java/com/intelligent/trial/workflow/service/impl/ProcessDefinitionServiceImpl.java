package com.intelligent.trial.workflow.service.impl;

import com.intelligent.trial.common.exception.BusinessException;
import com.intelligent.trial.common.exception.ErrorCode;
import com.intelligent.trial.workflow.service.IProcessDefinitionService;
import com.intelligent.trial.workflow.vo.ProcessDefinitionVO;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.DeploymentBuilder;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.repository.ProcessDefinitionQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 流程定义服务实现类
 * 基于Flowable RepositoryService实现流程定义的管理功能
 *
 * @author intelligent-trial
 */
@Service
public class ProcessDefinitionServiceImpl implements IProcessDefinitionService {

    @Autowired
    private RepositoryService repositoryService;

    /**
     * 根据流程定义Key部署流程
     * 自动从classpath的processes目录查找匹配的BPMN文件
     *
     * @param processDefinitionKey 流程定义Key
     * @return 部署ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String deployProcessByKey(String processDefinitionKey) {
        if (!StringUtils.hasText(processDefinitionKey)) {
            throw new IllegalArgumentException("流程定义Key不能为空");
        }
        String bpmnResource = "processes/" + processDefinitionKey + ".bpmn20.xml";
        return deployProcess(bpmnResource);
    }

    /**
     * 部署指定classpath路径的BPMN流程定义
     *
     * @param bpmnResourcePath BPMN文件在classpath中的路径
     * @return 部署ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String deployProcess(String bpmnResourcePath) {
        if (!StringUtils.hasText(bpmnResourcePath)) {
            throw new IllegalArgumentException("BPMN资源路径不能为空");
        }

        DeploymentBuilder builder = repositoryService.createDeployment()
                .name(bpmnResourcePath)
                .addClasspathResource(bpmnResourcePath);

        Deployment deployment = builder.deploy();
        return deployment.getId();
    }

    /**
     * 查询所有已部署的流程定义
     * 按版本号降序排列
     *
     * @return 流程定义列表
     */
    @Override
    public List<ProcessDefinitionVO> listProcessDefinitions() {
        ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery()
                .orderByProcessDefinitionVersion()
                .desc();

        List<ProcessDefinition> definitions = query.list();
        List<ProcessDefinitionVO> voList = new ArrayList<>();
        for (ProcessDefinition def : definitions) {
            voList.add(convertToVO(def));
        }
        return voList;
    }

    /**
     * 根据流程定义Key查询最新版本的流程定义
     *
     * @param processDefinitionKey 流程定义Key
     * @return 最新版本的流程定义VO
     */
    @Override
    public ProcessDefinitionVO getLatestProcessDefinition(String processDefinitionKey) {
        if (!StringUtils.hasText(processDefinitionKey)) {
            throw new IllegalArgumentException("流程定义Key不能为空");
        }

        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(processDefinitionKey)
                .latestVersion()
                .singleResult();

        if (definition == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "未找到流程定义: " + processDefinitionKey);
        }
        return convertToVO(definition);
    }

    /**
     * 根据流程定义ID获取流程定义详情
     *
     * @param processDefinitionId 流程定义ID
     * @return 流程定义VO
     */
    @Override
    public ProcessDefinitionVO getProcessDefinitionById(String processDefinitionId) {
        if (!StringUtils.hasText(processDefinitionId)) {
            throw new IllegalArgumentException("流程定义ID不能为空");
        }

        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(processDefinitionId)
                .singleResult();

        if (definition == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "未找到流程定义: " + processDefinitionId);
        }
        return convertToVO(definition);
    }

    /**
     * 挂起指定的流程定义
     * 挂起后无法基于此定义启动新的流程实例
     *
     * @param processDefinitionId 流程定义ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void suspendProcessDefinition(String processDefinitionId) {
        repositoryService.suspendProcessDefinitionById(processDefinitionId);
    }

    /**
     * 激活已挂起的流程定义
     *
     * @param processDefinitionId 流程定义ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void activateProcessDefinition(String processDefinitionId) {
        repositoryService.activateProcessDefinitionById(processDefinitionId);
    }

    /**
     * 删除流程定义及其部署信息
     *
     * @param deploymentId 部署ID
     * @param cascade      是否级联删除运行中的流程实例
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProcessDefinition(String deploymentId, boolean cascade) {
        repositoryService.deleteDeployment(deploymentId, cascade);
    }

    /**
     * 将Flowable流程定义实体转换为VO对象
     *
     * @param definition Flowable流程定义
     * @return 流程定义VO
     */
    private ProcessDefinitionVO convertToVO(ProcessDefinition definition) {
        ProcessDefinitionVO vo = new ProcessDefinitionVO();
        vo.setId(definition.getId());
        vo.setKey(definition.getKey());
        vo.setName(definition.getName());
        vo.setVersion(definition.getVersion());
        vo.setResourceName(definition.getResourceName());
        vo.setDescription(definition.getDescription());
        vo.setDeploymentId(definition.getDeploymentId());
        vo.setSuspended(definition.isSuspended());
        vo.setDiagramResourceName(definition.getDiagramResourceName());
        return vo;
    }
}
