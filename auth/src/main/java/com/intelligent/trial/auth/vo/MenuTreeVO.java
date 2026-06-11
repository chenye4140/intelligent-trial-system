package com.intelligent.trial.auth.vo;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "菜单树节点")
public class MenuTreeVO implements Serializable {
    private static final long serialVersionUID = 1L;
    @Schema(description = "菜单ID")
        private Long id;
    @Schema(description = "父菜单ID")
        private Long parentId;
    @Schema(description = "菜单名称")
        private String name;
    @Schema(description = "路由路径")
        private String path;
    @Schema(description = "组件路径")
        private String component;
    @Schema(description = "权限标识")
        private String perms;
    @Schema(description = "类型")
        private Integer type;
    @Schema(description = "图标")
        private String icon;
    @Schema(description = "排序")
        private Integer sort;
    @Schema(description = "是否可见")
        private Integer visible;
    private Integer status;
    private Date createTime;
    @Schema(description = "子菜单列表")
        private List<MenuTreeVO> children;
}
