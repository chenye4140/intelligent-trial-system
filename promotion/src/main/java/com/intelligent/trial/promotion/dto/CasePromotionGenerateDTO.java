package com.intelligent.trial.promotion.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 促改分析生成请求DTO
 */
@Data
public class CasePromotionGenerateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 关联案件ID
     */
    @NotBlank(message = "案件ID不能为空")
    private String caseId;

    /**
     * 使用的模板ID
     */
    private Long templateId;

    /**
     * 分析类型: comprehensive(综合), discipline(纪律), management(管理), system(制度)
     */
    private String analysisType = "comprehensive";

    /**
     * 创建人ID
     */
    private Long userId;
}
