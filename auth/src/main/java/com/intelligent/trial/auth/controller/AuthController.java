package com.intelligent.trial.auth.controller;

import com.intelligent.trial.common.annotation.RequireLog;
import com.intelligent.trial.auth.dto.LoginDTO;
import com.intelligent.trial.auth.service.IAuthService;
import com.intelligent.trial.auth.vo.LoginVO;
import com.intelligent.trial.common.dto.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Tag(name = "认证管理", description = "用户登录、Token刷新、退出登录等认证相关接口")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired private IAuthService authService;

    @Operation(summary = "用户登录", description = "用户名密码登录，返回 accessToken 和 refreshToken")
    @PostMapping("/login")
    @RequireLog(module = "认证", action = "登录", description = "用户登录")
    public R<LoginVO> login(@Validated @RequestBody LoginDTO loginDTO) {
        return R.ok(authService.login(loginDTO));
    }

    @Operation(summary = "刷新Token", description = "使用 refreshToken 刷新 accessToken")
    @PostMapping("/refresh")
    public R<LoginVO> refreshToken(@Parameter(description = "刷新令牌") @RequestParam String refreshToken) {
        return R.ok(authService.refreshToken(refreshToken));
    }

    @Operation(summary = "退出登录", description = "使当前 Token 失效")
    @PostMapping("/logout")
    @RequireLog(module = "认证", action = "退出", description = "用户退出登录")
    public R<Void> logout(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            authService.logout(header.substring(7));
        }
        return R.ok();
    }

    @Operation(summary = "获取当前用户信息", description = "根据 Token 获取当前登录用户的详细信息")
    @GetMapping("/info")
    public R<LoginVO.UserInfo> getUserInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return R.fail(401, "未认证");
        }
        return R.ok(authService.getUserInfo(userId));
    }
}
