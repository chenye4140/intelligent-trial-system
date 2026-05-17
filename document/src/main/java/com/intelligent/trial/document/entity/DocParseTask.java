package com.intelligent.trial.document.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 文档解析任务实体类
 * 对应数据库表: doc_parse_task
 *
 * @author intelligent-trial
 */
@Data
@TableName("doc_parse_task")
public class DocParseTask implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 文件名
     */
    @TableField("file_name")
    private String fileName;

    /**
     * 文件存储路径（MinIO中的object key）
     */
    @TableField("file_path")
    private String filePath;

    /**
     * 文件类型（pdf/docx/doc/png/jpg等）
     */
    @TableField("file_type")
    private String fileType;

    /**
     * 解析状态：0=待处理, 1=处理中, 2=已完成, 3=失败
     */
    @TableField("status")
    private Integer status;

    /**
     * 解析进度（0-100）
     */
    @TableField("progress")
    private Integer progress;

    /**
     * 解析结果（JSON格式）
     * 结构: { "paragraphs": [...], "vectors": [...], "metadata": {...} }
     */
    @TableField("result_json")
    private String resultJson;

    /**
     * 错误信息
     */
    @TableField("error_msg")
    private String errorMsg;

    /**
     * 解析完成时间
     */
    @TableField("parse_time")
    private Date parseTime;

    /**
     * 生成的向量片段数量
     */
    @TableField("vector_count")
    private Integer vectorCount;

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

    /**
     * 关联的库文档ID（解析完成后自动创建的 repo_document 记录）
     */
    @TableField("document_id")
    private Long documentId;
}
