package com.intelligent.trial.auth.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "重置密码请求")
public class ResetPasswordDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID")
        private Long userId;
    @NotBlank(message = "新密码不能为空")
    @Schema(description = "新密码")
        private String newPassword;
}
