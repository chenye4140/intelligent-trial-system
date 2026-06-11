package com.intelligent.trial.auth.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "菜单创建/更新请求")
public class MenuDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    @Schema(description = "父菜单ID")
        private Long parentId;
    @NotBlank(message = "菜单名称不能为空")
    @Schema(description = "菜单名称")
        private String name;
    @Schema(description = "路由路径")
        private String path;
    @Schema(description = "组件路径")
        private String component;
    @Schema(description = "权限标识")
        private String perms;
    @Schema(description = "类型(0目录1菜单2按钮)")
        private Integer type;
    @Schema(description = "图标")
        private String icon;
    @Schema(description = "排序")
        private Integer sort;
    @Schema(description = "是否可见")
        private Integer visible;
    private Integer status;
}
