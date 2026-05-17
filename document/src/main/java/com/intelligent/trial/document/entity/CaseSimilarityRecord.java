package com.intelligent.trial.document.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 类案相似度记录实体类
 * 对应数据库表: case_similarity_record
 *
 * @author intelligent-trial
 */
@Data
@TableName("case_similarity_record")
public class CaseSimilarityRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 源案件ID
     */
    @TableField("source_case_id")
    private String sourceCaseId;

    /**
     * 相似案件ID
     */
    @TableField("similar_case_id")
    private String similarCaseId;

    /**
     * 综合相似度得分（0.0000-1.0000）
     */
    @TableField("similarity_score")
    private BigDecimal similarityScore;

    /**
     * 内容相似度得分
     */
    @TableField("content_score")
    private BigDecimal contentScore;

    /**
     * 金额相似度得分
     */
    @TableField("amount_score")
    private BigDecimal amountScore;

    /**
     * 类型相似度得分
     */
    @TableField("type_score")
    private BigDecimal typeScore;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private Date updateTime;
}
