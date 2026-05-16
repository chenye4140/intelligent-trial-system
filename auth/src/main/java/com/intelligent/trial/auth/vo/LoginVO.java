package com.intelligent.trial.auth.vo;

import lombok.Data;
import java.io.Serializable;

@Data
public class LoginVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String accessToken;
    private String refreshToken;
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
