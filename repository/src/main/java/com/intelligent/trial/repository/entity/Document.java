package com.intelligent.trial.repository.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.intelligent.trial.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 库文档实体
 * 支持法规/资料/裁判文书/案例四类文档存储
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("repo_document")
public class Document extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 库类型：1=法规库, 2=资料库, 3=裁判文书库, 4=案例库
     */
    @TableField("repo_type")
    private Integer repoType;

    /**
     * 所属目录ID
     */
    @TableField("directory_id")
    private Long directoryId;

    /**
     * 文档标题
     */
    @TableField("title")
    private String title;

    /**
     * 文号
     */
    @TableField("doc_no")
    private String docNo;

    /**
     * 发布单位
     */
    @TableField("publish_unit")
    private String publishUnit;

    /**
     * 发布日期
     */
    @TableField("publish_date")
    private Date publishDate;

    /**
     * 生效日期
     */
    @TableField("effective_date")
    private Date effectiveDate;

    /**
     * 修订日期
     */
    @TableField("revision_date")
    private Date revisionDate;

    /**
     * 有效性状态：valid=有效, invalid=失效, pending=待生效
     */
    @TableField("validity_status")
    private String validityStatus;

    /**
     * 定密级别ID
     */
    @TableField("classification_level_id")
    private Long classificationLevelId;

    /**
     * 文件存储路径（MinIO对象路径）
     */
    @TableField("file_path")
    private String filePath;

    /**
     * 文件大小（字节）
     */
    @TableField("file_size")
    private Long fileSize;

    /**
     * 文件类型/扩展名
     */
    @TableField("file_type")
    private String fileType;

    /**
     * 文档摘要/简介
     */
    @TableField("summary")
    private String summary;

    /**
     * 向量检索ID（用于智能检索）
     */
    @TableField("vector_id")
    private String vectorId;

    /**
     * 状态：0=草稿, 1=已发布, 2=已下架
     */
    @TableField("status")
    private Integer status;
}
