package com.intelligent.trial.promotion.vo;

import com.intelligent.trial.promotion.entity.CasePromotion;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 促改记录视图对象
 * 扩展 CasePromotion，增加关联查询的附加字段
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "促改分析结果")
public class CasePromotionVO extends CasePromotion implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 案件标题（从case_info关联查询）
     */
    private String caseTitle;

    /**
     * 创建人姓名（从sys_user关联查询）
     */
    private String userName;
}
