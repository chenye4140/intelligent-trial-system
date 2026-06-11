package com.intelligent.trial.punishment.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.intelligent.trial.punishment.entity.PunishmentMaterial;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 处分执行 VO（含关联材料）
 */
@Data
@Schema(description = "处分执行信息")
public class PunishmentExecutionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
        private Long id;

    @Schema(description = "案件ID")
        private String caseId;

    /**
     * 案件名称（关联查询填充）
     */
    @Schema(description = "案件名称")
        private String caseName;

    /**
     * 被处分人姓名（关联查询填充）
     */
    private String respondentName;

    @Schema(description = "处分类型")
        private String punishmentType;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date decisionDate;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @Schema(description = "开始日期")
        private Date startDate;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @Schema(description = "结束日期")
        private Date endDate;

    /**
     * 状态：0=待执行, 1=执行中, 2=已完成, 3=已撤销
     */
    private Integer status;

    /**
     * 状态文本
     */
    private String statusText;

    private Integer reminderFlag;

    private Integer isOverdue;

    /**
     * 关联的处分材料列表
     */
    private List<PunishmentMaterial> materials;

    @Schema(description = "创建时间")
        private Date createTime;

    private Date updateTime;
}
