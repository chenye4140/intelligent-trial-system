package com.intelligent.trial.punishment.dto;

import lombok.Data;

import java.io.Serializable;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 处分执行搜索 DTO
 */
@Data
@Schema(description = "处分执行搜索请求")
public class PunishmentSearchDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 关联案件ID
     */
    @Schema(description = "案件ID")
        private String caseId;

    /**
     * 处分类型
     */
    @Schema(description = "处分类型")
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
