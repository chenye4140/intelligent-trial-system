package com.intelligent.trial.casemanage.management.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 案件违纪事实实体
 */
@Data
@TableName("case_violation_fact")
public class CaseViolationFact implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    /** 关联案件ID */
    private Long caseId;

    /** 事实标题 */
    private String factTitle;

    /** 事实内容 */
    private String factContent;

    /** 违纪类型 */
    private String violationType;

    /** 发生时间 */
    private Date occurredDate;

    /** 涉及金额 */
    private BigDecimal amount;

    /** 证据材料 */
    private String evidence;

    /** 排序 */
    private Integer sort;

    private Date createTime;

    private Date updateTime;
}
