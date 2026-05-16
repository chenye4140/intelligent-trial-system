package com.intelligent.trial.auth.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

/**
 * 定密建议记录实体
 */
@Data
@TableName("classification_suggestion")
public class ClassificationSuggestion implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    /** 关联案件ID */
    private Long caseId;

    /** 建议密级ID */
    private Long suggestedLevelId;

    /** 建议密级名称（冗余字段） */
    private String suggestedLevelName;

    /** 建议置信度（0-100） */
    private Integer confidence;

    /** AI 分析理由 */
    private String reason;

    /** 参考法规条款 */
    private String referencedRegulations;

    /** 是否已采纳: 0=未采纳, 1=已采纳 */
    private Integer adopted;

    /** 操作人ID */
    private Long operatorId;

    private Date createTime;
    private Date updateTime;
}
