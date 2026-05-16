package com.intelligent.trial.auth.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class ResetPasswordDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    @NotNull(message = "用户ID不能为空")
    private Long userId;
    @NotBlank(message = "新密码不能为空")
    private String newPassword;
}
