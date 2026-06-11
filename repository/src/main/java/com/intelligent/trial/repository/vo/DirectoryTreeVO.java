package com.intelligent.trial.repository.vo;

import lombok.Data;

import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 目录树形结构 VO
 * 用于返回嵌套的目录层级结构
 */
@Data
@Schema(description = "目录树节点")
public class DirectoryTreeVO {

    /**
     * 目录ID
     */
    @Schema(description = "目录ID")
        private Long id;

    /**
     * 库类型：1=法规库, 2=资料库, 3=裁判文书库, 4=案例库
     */
    private Integer repoType;

    /**
     * 父目录ID
     */
    @Schema(description = "父目录ID")
        private Long parentId;

    /**
     * 目录名称
     */
    @Schema(description = "目录名称")
        private String name;

    /**
     * 排序值
     */
    @Schema(description = "排序")
        private Integer sort;

    /**
     * 定密级别ID
     */
    private Long classificationLevelId;

    /**
     * 权限范围
     */
    private String permissionScope;

    /**
     * 目录路径
     */
    private String path;

    /**
     * 状态：0=禁用, 1=启用
     */
    private Integer status;

    /**
     * 子目录列表
     */
    @Schema(description = "子目录列表")
        private List<DirectoryTreeVO> children;
}
