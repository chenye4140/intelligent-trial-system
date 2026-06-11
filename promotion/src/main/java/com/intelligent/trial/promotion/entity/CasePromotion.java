package com.intelligent.trial.promotion.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Date;

/**
 * 以案促改实体类
 * 对应数据库表: case_promotion
 */
@Data
@EqualsAndHashCode
@TableName("case_promotion")
public class CasePromotion implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    @TableField("id")
    private Long id;

    /**
     * 关联案件ID
     */
    @NotBlank(message = "案件ID不能为空")
    @TableField("case_id")
    private String caseId;

    /**
     * 使用的模板ID
     */
    @TableField("template_id")
    private Long templateId;

    /**
     * 促改内容
     */
    @NotBlank(message = "促改内容不能为空")
    @TableField("content")
    private String content;

    /**
     * 状态：0=草稿, 1=待审核, 2=已通过, 3=已驳回
     */
    @TableField("status")
    private Integer status;

    /**
     * 创建人ID
     */
    @TableField("user_id")
    private Long userId;

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
