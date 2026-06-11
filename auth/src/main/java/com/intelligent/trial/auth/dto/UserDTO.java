package com.intelligent.trial.auth.dto;

import lombok.Data;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "用户创建/更新请求")
public class UserDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户名")
        private String username;
    @Schema(description = "密码")
        private String password;
    @NotBlank(message = "真实姓名不能为空")
    @Schema(description = "真实姓名")
        private String realName;
    @Schema(description = "部门ID")
        private Long deptId;
    @Schema(description = "手机号")
        private String phone;
    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱")
        private String email;
    @Schema(description = "状态")
        private Integer status;
    @Schema(description = "角色ID列表")
        private List<Long> roleIds;
}
