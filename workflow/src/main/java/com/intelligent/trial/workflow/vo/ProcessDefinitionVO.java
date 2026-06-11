package com.intelligent.trial.workflow.vo;

import lombok.Data;

import java.io.Serializable;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 流程定义视图对象
 * 用于展示流程定义的基本信息
 *
 * @author intelligent-trial
 */
@Data
@Schema(description = "流程定义信息")
public class ProcessDefinitionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 流程定义ID
     */
    @Schema(description = "定义ID")
        private String id;

    /**
     * 流程定义Key
     */
    @Schema(description = "流程Key")
        private String key;

    /**
     * 流程定义名称
     */
    @Schema(description = "流程名称")
        private String name;

    /**
     * 流程定义版本
     */
    @Schema(description = "版本")
        private Integer version;

    /**
     * 资源名称（BPMN文件名）
     */
    private String resourceName;

    /**
     * 流程描述
     */
    private String description;

    /**
     * 部署ID
     */
    @Schema(description = "部署ID")
        private String deploymentId;

    /**
     * 是否挂起：true-挂起，false-激活
     */
    @Schema(description = "是否挂起")
        private Boolean suspended;

    /**
     * 流程定义图资源名称
     */
    private String diagramResourceName;
}
