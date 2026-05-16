package com.intelligent.trial.report.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 文书模板实体类
 * 对应数据库表: report_template
 */
@Data
@TableName("report_template")
public class ReportTemplate implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 模板编码（如 SHENLI_REPORT）
     */
    @TableField("template_code")
    private String templateCode;

    /**
     * 模板名称
     */
    @TableField("template_name")
    private String templateName;

    /**
     * 模板类型：1=审理报告, 2=处分决定, 3=谈话笔录, 4=初核报告
     */
    @TableField("template_type")
    private Integer templateType;

    /**
     * 模板内容（包含占位符）
     */
    @TableField("content")
    private String content;

    /**
     * 模板描述
     */
    @TableField("description")
    private String description;

    /**
     * 状态：0=禁用, 1=启用
     */
    @TableField("status")
    private Integer status;

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
