package com.intelligent.trial.auth.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 定密建议请求 DTO
 */
@Data
public class ClassificationSuggestionDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 案件ID */
    @NotNull(message = "案件ID不能为空")
    private Long caseId;

    /** 是否强制重新生成（忽略已有建议） */
    private Boolean forceRefresh = false;
}
