package com.intelligent.trial.auth.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "登录请求")
public class LoginDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户名")
        private String username;
    @NotBlank(message = "密码不能为空")
    @Schema(description = "密码")
        private String password;
}
