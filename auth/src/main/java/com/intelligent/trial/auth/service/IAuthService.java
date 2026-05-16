package com.intelligent.trial.auth.service;

import com.intelligent.trial.auth.dto.LoginDTO;
import com.intelligent.trial.auth.vo.LoginVO;

/**
 * 认证服务接口
 */
public interface IAuthService {

    /**
     * 用户登录
     *
     * @param loginDTO 登录信息
     * @return 登录结果（包含 Token 和用户信息）
     */
    LoginVO login(LoginDTO loginDTO);

    /**
     * 刷新 Token
     *
     * @param refreshToken 刷新 Token
     * @return 新的登录结果
     */
    LoginVO refreshToken(String refreshToken);

    /**
     * 用户退出登录
     *
     * @param token 当前 Token
     */
    void logout(String token);

    /**
     * 获取当前用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    LoginVO.UserInfo getUserInfo(Long userId);
}
