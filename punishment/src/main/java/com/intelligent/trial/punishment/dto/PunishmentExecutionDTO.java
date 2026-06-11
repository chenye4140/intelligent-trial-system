package com.intelligent.trial.punishment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 处分执行创建/更新 DTO
 */
@Data
@Schema(description = "处分执行创建/更新请求")
public class PunishmentExecutionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
        private Long id;

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
     * 决定日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date decisionDate;

    /**
     * 开始日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @Schema(description = "开始日期")
        private Date startDate;

    /**
     * 结束日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @Schema(description = "结束日期")
        private Date endDate;

    /**
     * 状态：0=待执行, 1=执行中, 2=已完成, 3=已撤销
     */
    private Integer status;
}
