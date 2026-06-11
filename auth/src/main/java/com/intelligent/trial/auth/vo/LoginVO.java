package com.intelligent.trial.auth.vo;

import lombok.Data;
import java.io.Serializable;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "登录响应")
public class LoginVO implements Serializable {
    private static final long serialVersionUID = 1L;
    @Schema(description = "访问Token")
        private String accessToken;
    @Schema(description = "刷新Token")
        private String refreshToken;
    @Schema(description = "过期时间(秒)")
        private Long expiresIn;
    private UserInfo userInfo;

    @Data
    public static class UserInfo {
        private Long id;
        private String username;
        private String realName;
        private String deptName;
        private String phone;
        private String email;
    }
}
