package com.intelligent.trial.workflow.service.impl;

import com.intelligent.trial.common.exception.BusinessException;
import com.intelligent.trial.common.exception.ErrorCode;
import com.intelligent.trial.workflow.dto.StartProcessDTO;
import com.intelligent.trial.workflow.service.IProcessInstanceService;
import com.intelligent.trial.workflow.vo.ProcessInstanceVO;
import org.flowable.common.engine.impl.identity.Authentication;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.history.HistoricProcessInstanceQuery;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.runtime.ProcessInstanceQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 流程实例服务实现类
 * 基于Flowable RuntimeService和HistoryService实现流程实例管理
 *
 * @author intelligent-trial
 */
@Service
public class ProcessInstanceServiceImpl implements IProcessInstanceService {

    private static final Logger log = LoggerFactory.getLogger(ProcessInstanceServiceImpl.class);

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private RepositoryService repositoryService;

    /**
     * 启动一个新的流程实例
     *
     * @param dto 启动流程请求参数
     * @return 流程实例ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String startProcess(StartProcessDTO dto) {
        if (!StringUtils.hasText(dto.getProcessDefinitionKey())) {
            throw new IllegalArgumentException("流程定义Key不能为空");
        }
        if (!StringUtils.hasText(dto.getCaseId())) {
            throw new IllegalArgumentException("案件ID不能为空");
        }

        // 构建流程变量
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("caseId", dto.getCaseId());
        variables.put("initiatorId", dto.getInitiatorId());
        variables.put("initiatorName", dto.getInitiatorName());
        variables.put("caseTitle", dto.getCaseTitle());
        variables.put("caseDescription", dto.getCaseDescription());
        variables.put("departmentAssignee", dto.getDepartmentAssignee());
        variables.put("disciplineAssignee", dto.getDisciplineAssignee());
        variables.put("leaderAssignee", dto.getLeaderAssignee());

        // 合并扩展变量
        if (dto.getVariables() != null) {
            variables.putAll(dto.getVariables());
        }

        // 设置发起人
        if (StringUtils.hasText(dto.getInitiatorId())) {
            Authentication.setAuthenticatedUserId(dto.getInitiatorId());
        }

        // 启动流程实例，以caseId作为businessKey
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(
                dto.getProcessDefinitionKey(),
                dto.getCaseId(),
                variables
        );

        // 清除认证信息
        Authentication.setAuthenticatedUserId(null);

        return processInstance.getId();
    }

    /**
     * 根据案件ID查询该案件关联的所有流程实例
     *
     * @param caseId 案件ID
     * @return 流程实例列表
     */
    @Override
    public List<ProcessInstanceVO> getProcessInstancesByCaseId(String caseId) {
        List<ProcessInstanceVO> result = new ArrayList<ProcessInstanceVO>();

        if (!StringUtils.hasText(caseId)) {
            return result;
        }

        // 查询运行中的实例
        ProcessInstanceQuery runningQuery = runtimeService.createProcessInstanceQuery()
                .processInstanceBusinessKey(caseId);
        List<ProcessInstance> runningInstances = runningQuery.list();
        for (ProcessInstance instance : runningInstances) {
            result.add(convertRunningInstanceToVO(instance));
        }

        // 查询历史中的已完成的实例
        HistoricProcessInstanceQuery historyQuery = historyService.createHistoricProcessInstanceQuery()
                .processInstanceBusinessKey(caseId)
                .finished();
        List<HistoricProcessInstance> historyInstances = historyQuery.list();
        for (HistoricProcessInstance instance : historyInstances) {
            // 避免与运行中的实例重复
            boolean exists = false;
            for (ProcessInstanceVO vo : result) {
                if (vo.getProcessInstanceId().equals(instance.getId())) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                result.add(convertHistoricInstanceToVO(instance));
            }
        }

        return result;
    }

    /**
     * 根据流程实例ID获取流程实例详情
     *
     * @param processInstanceId 流程实例ID
     * @return 流程实例VO
     */
    @Override
    public ProcessInstanceVO getProcessInstanceById(String processInstanceId) {
        if (!StringUtils.hasText(processInstanceId)) {
            throw new IllegalArgumentException("流程实例ID不能为空");
        }

        // 先查询运行中的实例
        ProcessInstance runningInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();

        if (runningInstance != null) {
            return convertRunningInstanceToVO(runningInstance);
        }

        // 再查询历史实例
        HistoricProcessInstance historyInstance = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();

        if (historyInstance != null) {
            return convertHistoricInstanceToVO(historyInstance);
        }

        throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "未找到流程实例: " + processInstanceId);
    }

    /**
     * 获取流程实例的当前状态信息
     *
     * @param processInstanceId 流程实例ID
     * @return 包含当前状态的流程实例VO
     */
    @Override
    public ProcessInstanceVO getProcessInstanceStatus(String processInstanceId) {
        ProcessInstanceVO vo = getProcessInstanceById(processInstanceId);

        // 如果是运行中的实例，获取当前活动节点信息
        if (!vo.getEnded()) {
            List<String> activeActivityIds = runtimeService.getActiveActivityIds(processInstanceId);
            if (activeActivityIds != null && !activeActivityIds.isEmpty()) {
                vo.setCurrentActivityId(activeActivityIds.get(0));
                // 尝试获取活动名称
                String activityName = getActiveActivityName(processInstanceId, activeActivityIds.get(0));
                vo.setCurrentActivityName(activityName);
            }
        }

        return vo;
    }

    /**
     * 取消/终止正在运行的流程实例
     *
     * @param processInstanceId 流程实例ID
     * @param reason            取消原因
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelProcessInstance(String processInstanceId, String reason) {
        if (!StringUtils.hasText(processInstanceId)) {
            throw new IllegalArgumentException("流程实例ID不能为空");
        }

        ProcessInstance instance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();

        if (instance == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "流程实例不存在或已结束: " + processInstanceId);
        }

        // 设置删除原因并终止流程
        runtimeService.deleteProcessInstance(processInstanceId, reason);
    }

    /**
     * 挂起指定的流程实例
     *
     * @param processInstanceId 流程实例ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void suspendProcessInstance(String processInstanceId) {
        runtimeService.suspendProcessInstanceById(processInstanceId);
    }

    /**
     * 激活已挂起的流程实例
     *
     * @param processInstanceId 流程实例ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void activateProcessInstance(String processInstanceId) {
        runtimeService.activateProcessInstanceById(processInstanceId);
    }

    /**
     * 将运行中的流程实例转换为VO
     *
     * @param instance 运行中的流程实例
     * @return 流程实例VO
     */
    private ProcessInstanceVO convertRunningInstanceToVO(ProcessInstance instance) {
        ProcessInstanceVO vo = new ProcessInstanceVO();
        vo.setProcessInstanceId(instance.getId());
        vo.setProcessDefinitionId(instance.getProcessDefinitionId());
        vo.setProcessDefinitionKey(instance.getProcessDefinitionKey());
        vo.setProcessDefinitionName(instance.getProcessDefinitionName());
        vo.setName(instance.getName());
        vo.setBusinessKey(instance.getBusinessKey());
        vo.setEnded(false);
        vo.setSuspended(instance.isSuspended());
        vo.setStartTime(instance.getStartTime());
        vo.setEndTime(null);
        vo.setDurationInMillis(null);
        return vo;
    }

    /**
     * 将历史流程实例转换为VO
     *
     * @param instance 历史流程实例
     * @return 流程实例VO
     */
    private ProcessInstanceVO convertHistoricInstanceToVO(HistoricProcessInstance instance) {
        ProcessInstanceVO vo = new ProcessInstanceVO();
        vo.setProcessInstanceId(instance.getId());
        vo.setProcessDefinitionId(instance.getProcessDefinitionId());
        vo.setProcessDefinitionKey(instance.getProcessDefinitionKey());
        vo.setProcessDefinitionName(instance.getProcessDefinitionName());
        vo.setName(instance.getName());
        vo.setBusinessKey(instance.getBusinessKey());
        vo.setEnded(instance.getEndTime() != null);
        vo.setSuspended(false);
        vo.setStartTime(instance.getStartTime());
        vo.setEndTime(instance.getEndTime());
        vo.setDurationInMillis(instance.getDurationInMillis());
        return vo;
    }

    /**
     * 获取当前活动节点名称
     *
     * @param processInstanceId 流程实例ID
     * @param activityId        活动ID
     * @return 活动名称
     */
    private String getActiveActivityName(String processInstanceId, String activityId) {
        try {
            ProcessInstance instance = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .singleResult();
            if (instance != null) {
                org.flowable.bpmn.model.BpmnModel bpmnModel = repositoryService.getBpmnModel(instance.getProcessDefinitionId());
                if (bpmnModel != null) {
                    org.flowable.bpmn.model.FlowElement flowElement = bpmnModel.getFlowElement(activityId);
                    if (flowElement != null) {
                        return flowElement.getName();
                    }
                }
            }
        } catch (Exception e) {
            log.debug("获取活动名称失败，返回原始 activityId: {}", e.getMessage());
        }
        return activityId;
    }
}
