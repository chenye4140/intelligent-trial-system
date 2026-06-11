package com.intelligent.trial.report.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 文书生成请求 DTO
 */
@Data
@Schema(description = "文书生成请求")
public class ReportGenerateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 案件ID（必填）
     */
    @NotNull(message = "案件ID不能为空")
    @Schema(description = "案件ID")
        private Long caseId;

    /**
     * 模板ID（可选，为空时由AI自动选择模板）
     */
    @Schema(description = "模板ID")
        private Long templateId;

    /**
     * 自定义提示词（可选，附加的生成指令）
     */
    @Schema(description = "自定义提示词")
        private String customPrompt;
}
