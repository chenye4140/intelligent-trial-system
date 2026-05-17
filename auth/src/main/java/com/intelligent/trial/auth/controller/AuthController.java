package com.intelligent.trial.auth.controller;

import com.intelligent.trial.common.annotation.RequireLog;
import com.intelligent.trial.auth.dto.LoginDTO;
import com.intelligent.trial.auth.service.IAuthService;
import com.intelligent.trial.auth.vo.LoginVO;
import com.intelligent.trial.common.dto.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired private IAuthService authService;

    @PostMapping("/login")
    @RequireLog(module = "认证", action = "登录", description = "用户登录")
    public R<LoginVO> login(@Validated @RequestBody LoginDTO loginDTO) {
        return R.ok(authService.login(loginDTO));
    }

    @PostMapping("/refresh")
    public R<LoginVO> refreshToken(@RequestParam String refreshToken) {
        return R.ok(authService.refreshToken(refreshToken));
    }

    @PostMapping("/logout")
    @RequireLog(module = "认证", action = "退出", description = "用户退出登录")
    public R<Void> logout(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            authService.logout(header.substring(7));
        }
        return R.ok();
    }

    @GetMapping("/info")
    public R<LoginVO.UserInfo> getUserInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return R.fail(401, "未认证");
        }
        return R.ok(authService.getUserInfo(userId));
    }
}
