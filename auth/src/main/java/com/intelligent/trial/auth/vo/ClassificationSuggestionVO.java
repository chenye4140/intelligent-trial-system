package com.intelligent.trial.auth.vo;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 定密建议响应 VO
 */
@Data
@Schema(description = "定密建议信息")
public class ClassificationSuggestionVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
        private Long id;
    @Schema(description = "案件ID")
        private Long caseId;
    private String caseName;
    private Long suggestedLevelId;
    private String suggestedLevelName;
    @Schema(description = "置信度")
        private Integer confidence;
    @Schema(description = "定密理由")
        private String reason;
    private String referencedRegulations;
    private Integer adopted;
    private Date createTime;
}
