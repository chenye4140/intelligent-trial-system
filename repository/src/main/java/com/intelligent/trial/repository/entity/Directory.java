package com.intelligent.trial.repository.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.intelligent.trial.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 库目录实体
 * 支持最多10级嵌套目录结构
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("repo_directory")
public class Directory extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 库类型：1=法规库, 2=资料库, 3=裁判文书库, 4=案例库
     */
    @TableField("repo_type")
    private Integer repoType;

    /**
     * 父目录ID，根目录为0
     */
    @TableField("parent_id")
    private Long parentId;

    /**
     * 目录名称
     */
    @TableField("name")
    private String name;

    /**
     * 排序值，数值越小越靠前
     */
    @TableField("sort")
    private Integer sort;

    /**
     * 定密级别ID
     */
    @TableField("classification_level_id")
    private Long classificationLevelId;

    /**
     * 权限范围：public=公开, internal=内部, restricted=受限
     */
    @TableField("permission_scope")
    private String permissionScope;

    /**
     * 目录路径（如 /1/2/3/），用于快速查询子树
     */
    @TableField("path")
    private String path;

    /**
     * 状态：0=禁用, 1=启用
     */
    @TableField("status")
    private Integer status;
}
