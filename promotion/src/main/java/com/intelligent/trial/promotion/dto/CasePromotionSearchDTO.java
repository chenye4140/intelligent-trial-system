package com.intelligent.trial.promotion.dto;

import lombok.Data;

import java.io.Serializable;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 促改记录搜索请求DTO
 */
@Data
@Schema(description = "促改分析搜索请求")
public class CasePromotionSearchDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 案件ID
     */
    @Schema(description = "案件ID")
        private String caseId;

    /**
     * 状态：0=草稿, 1=待审核, 2=已通过, 3=已驳回
     */
    @Schema(description = "状态")
        private Integer status;

    /**
     * 创建人ID
     */
    private Long userId;

    /**
     * 页码，默认1
     */
    private int pageNum = 1;

    /**
     * 每页条数，默认10
     */
    private int pageSize = 10;
}
