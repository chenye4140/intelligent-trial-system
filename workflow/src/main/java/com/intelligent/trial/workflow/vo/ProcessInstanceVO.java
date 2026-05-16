package com.intelligent.trial.workflow.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 流程实例视图对象
 * 用于展示流程实例的运行状态信息
 *
 * @author intelligent-trial
 */
@Data
public class ProcessInstanceVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 流程实例ID
     */
    private String processInstanceId;

    /**
     * 流程定义ID
     */
    private String processDefinitionId;

    /**
     * 流程定义Key
     */
    private String processDefinitionKey;

    /**
     * 流程定义名称
     */
    private String processDefinitionName;

    /**
     * 流程实例名称
     */
    private String name;

    /**
     * 业务Key（通常对应案件ID）
     */
    private String businessKey;

    /**
     * 发起人ID
     */
    private String initiatorId;

    /**
     * 流程是否已结束
     */
    private Boolean ended;

    /**
     * 流程是否已挂起
     */
    private Boolean suspended;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 持续时间（毫秒）
     */
    private Long durationInMillis;

    /**
     * 当前活动节点名称
     */
    private String currentActivityName;

    /**
     * 当前活动节点ID
     */
    private String currentActivityId;
}
