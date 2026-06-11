package com.intelligent.trial.auth.vo;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "角色信息")
public class RoleVO implements Serializable {
    private static final long serialVersionUID = 1L;
    @Schema(description = "角色ID")
        private Long id;
    @Schema(description = "角色名称")
        private String roleName;
    @Schema(description = "角色编码")
        private String roleCode;
    @Schema(description = "描述")
        private String description;
    @Schema(description = "状态")
        private Integer status;
    @Schema(description = "创建时间")
        private Date createTime;
    @Schema(description = "菜单ID列表")
        private List<Long> menuIds;
}
