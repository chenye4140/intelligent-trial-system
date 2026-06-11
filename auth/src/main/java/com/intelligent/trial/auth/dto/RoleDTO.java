package com.intelligent.trial.auth.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "角色创建/更新请求")
public class RoleDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    @NotBlank(message = "角色名称不能为空")
    @Schema(description = "角色名称")
        private String roleName;
    @NotBlank(message = "角色编码不能为空")
    @Schema(description = "角色编码")
        private String roleCode;
    @Schema(description = "描述")
        private String description;
    @Schema(description = "状态")
        private Integer status;
    @Schema(description = "菜单ID列表")
        private List<Long> menuIds;
}
