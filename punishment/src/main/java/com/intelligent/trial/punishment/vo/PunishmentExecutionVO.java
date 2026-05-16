package com.intelligent.trial.punishment.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.intelligent.trial.punishment.entity.PunishmentMaterial;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 处分执行 VO（含关联材料）
 */
@Data
public class PunishmentExecutionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String caseId;

    /**
     * 案件名称（关联查询填充）
     */
    private String caseName;

    /**
     * 被处分人姓名（关联查询填充）
     */
    private String respondentName;

    private String punishmentType;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date decisionDate;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date startDate;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
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

    private Date createTime;

    private Date updateTime;
}
