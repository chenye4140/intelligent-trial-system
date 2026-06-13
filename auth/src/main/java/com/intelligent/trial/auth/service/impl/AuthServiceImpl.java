package com.intelligent.trial.auth.service.impl;

import com.intelligent.trial.auth.dto.LoginDTO;
import com.intelligent.trial.auth.entity.SysUser;
import com.intelligent.trial.auth.mapper.SysRoleMapper;
import com.intelligent.trial.auth.mapper.SysUserMapper;
import com.intelligent.trial.auth.service.IAuthService;
import com.intelligent.trial.auth.util.JwtUtil;
import com.intelligent.trial.auth.util.PasswordEncoderUtil;
import com.intelligent.trial.auth.vo.LoginVO;
import com.intelligent.trial.auth.vo.RoleVO;
import com.intelligent.trial.common.exception.BusinessException;
import com.intelligent.trial.common.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 认证服务实现类
 */
@Slf4j
@Service
public class AuthServiceImpl implements IAuthService {

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private SysRoleMapper roleMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String TOKEN_BLACKLIST_PREFIX="auth:t...ist:";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO login(LoginDTO loginDTO) {
        // 根据用户名查询用户
        SysUser user = userMapper.selectByUsername(loginDTO.getUsername());
        if (user == null) {
            throw new BusinessException(ErrorCode.AUTH_USER_NOT_FOUND);
        }

        // 验证密码
        if (!PasswordEncoderUtil.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.AUTH_PASSWORD_WRONG);
        }

        // 检查用户状态
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(ErrorCode.AUTH_USER_DISABLED);
        }

        // 生成 Token
        String accessToken = jwtUtil.generateToken(user.getId(), user.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername());

        // 更新最后登录时间
        SysUser updateUser = new SysUser();
        updateUser.setId(user.getId());
        updateUser.setLastLoginTime(new Date());
        userMapper.updateById(updateUser);

        // 构建返回结果
        LoginVO loginVO = new LoginVO();
        loginVO.setAccessToken(accessToken);
        loginVO.setRefreshToken(refreshToken);
        loginVO.setExpiresIn(86400L);

        LoginVO.UserInfo userInfo = new LoginVO.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setRealName(user.getRealName());
        userInfo.setPhone(user.getPhone());
        userInfo.setEmail(user.getEmail());
        loginVO.setUserInfo(userInfo);

        return loginVO;
    }

    @Override
    public LoginVO refreshToken(String refreshToken) {
        // 验证刷新 Token
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.AUTH_TOKEN_INVALID);
        }

        Long userId = jwtUtil.getUserId(refreshToken);
        String username = jwtUtil.getUsername(refreshToken);

        // 查询用户信息
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.AUTH_USER_NOT_FOUND);
        }

        // 检查用户状态
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(ErrorCode.AUTH_USER_DISABLED);
        }

        // 生成新 Token
        String newAccessToken = jwtUtil.generateToken(userId, username);
        String newRefreshToken = jwtUtil.generateRefreshToken(userId, username);

        LoginVO loginVO = new LoginVO();
        loginVO.setAccessToken(newAccessToken);
        loginVO.setRefreshToken(newRefreshToken);
        loginVO.setExpiresIn(86400L);

        LoginVO.UserInfo userInfo = new LoginVO.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setRealName(user.getRealName());
        userInfo.setPhone(user.getPhone());
        userInfo.setEmail(user.getEmail());
        loginVO.setUserInfo(userInfo);

        return loginVO;
    }

    @Override
    public void logout(String token) {
        if (token != null && !token.isEmpty()) {
            // 获取 Token 剩余有效时间
            Long expiration = 86400L; // 默认24小时
            try {
                // 将 Token 加入黑名单
                redisTemplate.opsForValue().set(TOKEN_BLACKLIST_PREFIX + token, "1", expiration, TimeUnit.SECONDS);
            } catch (Exception e) {
                log.warn("Redis 加入黑名单失败（不影响退出主流程）: {}", e.getMessage());
            }
        }
    }

    @Override
    public LoginVO.UserInfo getUserInfo(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.AUTH_USER_NOT_FOUND);
        }

        LoginVO.UserInfo userInfo = new LoginVO.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setRealName(user.getRealName());
        userInfo.setPhone(user.getPhone());
        userInfo.setEmail(user.getEmail());

        // 查询部门名称
        if (user.getDeptId() != null) {
            // 通过 Mapper 查询部门名称，这里简化处理
            // 实际应该关联查询
        }

        return userInfo;
    }
}
