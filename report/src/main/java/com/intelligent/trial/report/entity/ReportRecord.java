package com.intelligent.trial.report.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 文书生成记录实体类
 * 对应数据库表: report_record
 */
@Data
@TableName("report_record")
public class ReportRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联案件ID（FK to case_info）
     */
    @TableField("case_id")
    private Long caseId;

    /**
     * 案件编号
     */
    @TableField("case_code")
    private String caseCode;

    /**
     * 模板ID
     */
    @TableField("template_id")
    private Long templateId;

    /**
     * 模板编码
     */
    @TableField("template_code")
    private String templateCode;

    /**
     * 文书标题
     */
    @TableField("report_title")
    private String reportTitle;

    /**
     * 文书内容（AI生成内容）
     */
    @TableField("report_content")
    private String reportContent;

    /**
     * 生成人（用户ID）
     */
    @TableField("generated_by")
    private Long generatedBy;

    /**
     * 状态：0=生成中, 1=已完成, 2=失败
     */
    @TableField("status")
    private Integer status;

    /**
     * 错误信息
     */
    @TableField("error_message")
    private String errorMessage;

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
