package com.intelligent.trial.punishment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 处分执行实体
 */
@Data
@TableName("punishment_execution")
public class PunishmentExecution implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联案件ID
     */
    private String caseId;

    /**
     * 处分类型（警告/记过/降级/撤职/开除等）
     */
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
    private Date startDate;

    /**
     * 结束日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date endDate;

    /**
     * 状态：0=待执行, 1=执行中, 2=已完成, 3=已撤销
     */
    private Integer status;

    /**
     * 提醒标志：0=未提醒, 1=已提醒
     */
    private Integer reminderFlag;

    /**
     * 是否逾期：0=否, 1=是
     */
    private Integer isOverdue;

    private Date createTime;

    private Date updateTime;
}
