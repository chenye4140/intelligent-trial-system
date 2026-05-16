package com.intelligent.trial.document.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 段落向量存储实体类
 * 对应数据库表: doc_paragraph_vector
 *
 * @author intelligent-trial
 */
@Data
@TableName("doc_paragraph_vector")
public class DocParagraphVector implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联解析任务ID
     */
    @TableField("task_id")
    private Long taskId;

    /**
     * 段落序号
     */
    @TableField("paragraph_index")
    private Integer paragraphIndex;

    /**
     * 段落文本内容
     */
    @TableField("content")
    private String content;

    /**
     * 分类：总则/分则/附则/法律责任/案件事实/处理意见/法律依据
     */
    @TableField("category")
    private String category;

    /**
     * 法规层级：篇/章/节/条/款/项
     */
    @TableField("law_level")
    private String lawLevel;

    /**
     * 向量数据（JSON数组格式）
     */
    @TableField("vector_data")
    private String vectorData;

    /**
     * 向量维度
     */
    @TableField("vector_dimension")
    private Integer vectorDimension;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private Date createTime;
}
