package com.intelligent.trial.auth.vo;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;

/**
 * 定密建议响应 VO
 */
@Data
public class ClassificationSuggestionVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long caseId;
    private String caseName;
    private Long suggestedLevelId;
    private String suggestedLevelName;
    private Integer confidence;
    private String reason;
    private String referencedRegulations;
    private Integer adopted;
    private Date createTime;
}
