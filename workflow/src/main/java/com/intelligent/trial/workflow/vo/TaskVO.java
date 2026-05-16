package com.intelligent.trial.workflow.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 任务视图对象
 * 用于展示待办/已办任务的信息
 *
 * @author intelligent-trial
 */
@Data
public class TaskVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 任务定义Key
     */
    private String taskDefinitionKey;

    /**
     * 任务描述
     */
    private String description;

    /**
     * 任务处理人
     */
    private String assignee;

    /**
     * 任务候选人（逗号分隔）
     */
    private String candidateUsers;

    /**
     * 候选组（逗号分隔）
     */
    private String candidateGroups;

    /**
     * 流程实例ID
     */
    private String processInstanceId;

    /**
     * 流程定义Key
     */
    private String processDefinitionKey;

    /**
     * 流程定义名称
     */
    private String processDefinitionName;

    /**
     * 业务Key（案件ID）
     */
    private String businessKey;

    /**
     * 任务创建时间
     */
    private Date createTime;

    /**
     * 任务完成时间
     */
    private Date endTime;

    /**
     * 任务持续时间（毫秒）
     */
    private Long durationInMillis;

    /**
     * 审批意见
     */
    private String comment;

    /**
     * 审批结果：approved / rejected / pending
     */
    private String approvalResult;

    /**
     * 任务状态：active / completed / deleted
     */
    private String taskStatus;

    /**
     * 父任务ID
     */
    private String parentTaskId;

    /**
     * 任务优先级
     */
    private Integer priority;

    /**
     * 案件标题（从流程变量中获取）
     */
    private String caseTitle;
}
