package com.intelligent.trial.punishment.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 处分执行搜索 DTO
 */
@Data
public class PunishmentSearchDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 关联案件ID
     */
    private String caseId;

    /**
     * 处分类型
     */
    private String punishmentType;

    /**
     * 状态：0=待执行, 1=执行中, 2=已完成, 3=已撤销
     */
    private Integer status;

    /**
     * 是否逾期
     */
    private Integer isOverdue;
}
