package com.intelligent.trial.workflow.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 启动流程实例请求DTO
 * 用于发起新的案件审理审批流程
 *
 * @author intelligent-trial
 */
@Data
@Schema(description = "启动流程请求")
public class StartProcessDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 案件ID（业务标识）
     */
    @Schema(description = "案件ID")
        private String caseId;

    /**
     * 流程定义Key，例如 "case-review-approval"
     */
    @Schema(description = "流程定义Key")
        private String processDefinitionKey;

    /**
     * 发起人ID
     */
    private String initiatorId;

    /**
     * 发起人姓名
     */
    private String initiatorName;

    /**
     * 部门审核人
     */
    private String departmentAssignee;

    /**
     * 纪检审核人
     */
    private String disciplineAssignee;

    /**
     * 领导审批人
     */
    private String leaderAssignee;

    /**
     * 案件标题
     */
    private String caseTitle;

    /**
     * 案件描述
     */
    private String caseDescription;

    /**
     * 流程变量（扩展参数）
     */
    @Schema(description = "流程变量")
        private Map<String, Object> variables;
}
