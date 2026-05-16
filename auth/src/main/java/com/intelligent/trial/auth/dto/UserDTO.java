package com.intelligent.trial.auth.dto;

import lombok.Data;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;

@Data
public class UserDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    @NotBlank(message = "用户名不能为空")
    private String username;
    private String password;
    @NotBlank(message = "真实姓名不能为空")
    private String realName;
    private Long deptId;
    private String phone;
    @Email(message = "邮箱格式不正确")
    private String email;
    private Integer status;
    private List<Long> roleIds;
}
