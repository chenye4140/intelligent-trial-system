package com.intelligent.trial.workflow.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Map;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 完成任务请求DTO
 * 用于完成审批任务并传递审批意见和结果
 *
 * @author intelligent-trial
 */
@Data
@Schema(description = "完成任务请求")
public class CompleteTaskDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务处理人ID
     */
    @Schema(description = "处理人")
        private String assignee;

    /**
     * 审批意见/备注
     */
    @Schema(description = "审批意见")
        private String comment;

    /**
     * 审批结果：approved（通过）/ rejected（驳回）
     */
    @NotBlank(message = "审批结果不能为空")
    private String approvalResult;

    /**
     * 流程变量（扩展参数）
     */
    @Schema(description = "流程变量")
        private Map<String, Object> variables;
}
