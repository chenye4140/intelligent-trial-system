package com.intelligent.trial.report.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 文书段落向量存储实体类
 * 用于基于向量的模板匹配
 * 对应数据库表: report_paragraph_vector
 */
@Data
@TableName("report_paragraph_vector")
public class ReportParagraphVector implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联文书记录ID
     */
    @TableField("report_id")
    private Long reportId;

    /**
     * 段落序号
     */
    @TableField("paragraph_index")
    private Integer paragraphIndex;

    /**
     * 段落内容
     */
    @TableField("paragraph_content")
    private String paragraphContent;

    /**
     * 向量数据（JSON数组格式）
     */
    @TableField("embedding")
    private String embedding;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private Date createTime;
}
