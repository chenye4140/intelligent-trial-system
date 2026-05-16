package com.intelligent.trial.auth.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;

@Data
public class RoleDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    @NotBlank(message = "角色名称不能为空")
    private String roleName;
    @NotBlank(message = "角色编码不能为空")
    private String roleCode;
    private String description;
    private Integer status;
    private List<Long> menuIds;
}
