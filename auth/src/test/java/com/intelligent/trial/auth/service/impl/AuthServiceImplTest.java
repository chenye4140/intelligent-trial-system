package com.intelligent.trial.auth.service.impl;

import com.intelligent.trial.auth.config.JwtConfig;
import com.intelligent.trial.auth.dto.LoginDTO;
import com.intelligent.trial.auth.entity.SysUser;
import com.intelligent.trial.auth.mapper.SysRoleMapper;
import com.intelligent.trial.auth.mapper.SysUserMapper;
import com.intelligent.trial.auth.util.JwtUtil;
import com.intelligent.trial.auth.util.PasswordEncoderUtil;
import com.intelligent.trial.auth.vo.LoginVO;
import com.intelligent.trial.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AuthServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @InjectMocks
    private AuthServiceImpl authService;

    @Mock
    private SysUserMapper userMapper;

    @Mock
    private SysRoleMapper roleMapper;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    private String encodedPassword;

    @BeforeEach
    void setUp() {
        encodedPassword = PasswordEncoderUtil.encode("Test@123");
    }

    @Test
    void login_shouldSucceedWithValidCredentials() {
        // Arrange
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("zhangsan");
        loginDTO.setPassword("Test@123");

        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("zhangsan");
        user.setPassword(encodedPassword);
        user.setRealName("张三");
        user.setPhone("13800138000");
        user.setEmail("zhangsan@test.com");
        user.setStatus(1);

        when(userMapper.selectByUsername("zhangsan")).thenReturn(user);
        when(jwtUtil.generateToken(1L, "zhangsan")).thenReturn("access-token-123");
        when(jwtUtil.generateRefreshToken(1L, "zhangsan")).thenReturn("refresh-token-456");

        // Act
        LoginVO result = authService.login(loginDTO);

        // Assert
        assertNotNull(result);
        assertEquals("access-token-123", result.getAccessToken());
        assertEquals("refresh-token-456", result.getRefreshToken());
        assertNotNull(result.getUserInfo());
        assertEquals("zhangsan", result.getUserInfo().getUsername());
        assertEquals("张三", result.getUserInfo().getRealName());
        verify(userMapper).updateById(any(SysUser.class));
    }

    @Test
    void login_shouldFailWithWrongUsername() {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("nonexistent");
        loginDTO.setPassword("Test@123");

        when(userMapper.selectByUsername("nonexistent")).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.login(loginDTO));
        assertTrue(ex.getMessage().contains("用户名或密码错误"));
    }

    @Test
    void login_shouldFailWithWrongPassword() {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("zhangsan");
        loginDTO.setPassword("WrongPassword");

        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("zhangsan");
        user.setPassword(encodedPassword);
        user.setStatus(1);

        when(userMapper.selectByUsername("zhangsan")).thenReturn(user);

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.login(loginDTO));
        assertTrue(ex.getMessage().contains("用户名或密码错误"));
    }

    @Test
    void login_shouldFailWithDisabledUser() {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("disabled");
        loginDTO.setPassword("Test@123");

        SysUser user = new SysUser();
        user.setId(2L);
        user.setUsername("disabled");
        user.setPassword(encodedPassword);
        user.setStatus(0);  // disabled

        when(userMapper.selectByUsername("disabled")).thenReturn(user);

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.login(loginDTO));
        assertTrue(ex.getMessage().contains("用户已被禁用"));
    }

    @Test
    void refreshToken_shouldSucceedWithValidToken() {
        String refreshToken = "valid-refresh-token";

        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("zhangsan");
        user.setRealName("张三");
        user.setPhone("13800138000");
        user.setEmail("zhangsan@test.com");
        user.setStatus(1);

        when(jwtUtil.validateToken(refreshToken)).thenReturn(true);
        when(jwtUtil.getUserId(refreshToken)).thenReturn(1L);
        when(jwtUtil.getUsername(refreshToken)).thenReturn("zhangsan");
        when(userMapper.selectById(1L)).thenReturn(user);
        when(jwtUtil.generateToken(1L, "zhangsan")).thenReturn("new-access-token");
        when(jwtUtil.generateRefreshToken(1L, "zhangsan")).thenReturn("new-refresh-token");

        LoginVO result = authService.refreshToken(refreshToken);

        assertNotNull(result);
        assertEquals("new-access-token", result.getAccessToken());
        assertEquals("new-refresh-token", result.getRefreshToken());
        assertEquals("zhangsan", result.getUserInfo().getUsername());
    }

    @Test
    void refreshToken_shouldFailWithInvalidToken() {
        String invalidToken = "invalid-token";
        when(jwtUtil.validateToken(invalidToken)).thenReturn(false);

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.refreshToken(invalidToken));
        assertTrue(ex.getMessage().contains("刷新令牌无效或已过期"));
    }

    @Test
    void refreshToken_shouldFailWithNonExistentUser() {
        String refreshToken = "valid-refresh-token";
        when(jwtUtil.validateToken(refreshToken)).thenReturn(true);
        when(jwtUtil.getUserId(refreshToken)).thenReturn(999L);
        when(jwtUtil.getUsername(refreshToken)).thenReturn("ghost");
        when(userMapper.selectById(999L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.refreshToken(refreshToken));
        assertTrue(ex.getMessage().contains("用户不存在"));
    }

    @Test
    void refreshToken_shouldFailWithDisabledUser() {
        String refreshToken = "valid-refresh-token";

        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("zhangsan");
        user.setStatus(0);  // disabled

        when(jwtUtil.validateToken(refreshToken)).thenReturn(true);
        when(jwtUtil.getUserId(refreshToken)).thenReturn(1L);
        when(jwtUtil.getUsername(refreshToken)).thenReturn("zhangsan");
        when(userMapper.selectById(1L)).thenReturn(user);

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.refreshToken(refreshToken));
        assertTrue(ex.getMessage().contains("用户已被禁用"));
    }

    @Test
    void logout_shouldAddTokenToBlacklist() {
        String token = "some-access-token";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        authService.logout(token);

        verify(valueOperations).set(anyString(), eq("1"), anyLong(), any());
    }

    @Test
    void logout_shouldHandleNullToken() {
        authService.logout(null);
        verify(redisTemplate, never()).opsForValue();
    }

    @Test
    void logout_shouldHandleEmptyToken() {
        authService.logout("");
        verify(redisTemplate, never()).opsForValue();
    }

    @Test
    void logout_shouldContinueOnRedisException() {
        String token = "some-token";
        when(redisTemplate.opsForValue()).thenThrow(new RuntimeException("Redis down"));

        // Should not throw - Redis failure doesn't affect logout flow
        assertDoesNotThrow(() -> authService.logout(token));
    }

    @Test
    void getUserInfo_shouldReturnUserInfo() {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("zhangsan");
        user.setRealName("张三");
        user.setPhone("13800138000");
        user.setEmail("zhangsan@test.com");

        when(userMapper.selectById(1L)).thenReturn(user);

        LoginVO.UserInfo result = authService.getUserInfo(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("zhangsan", result.getUsername());
        assertEquals("张三", result.getRealName());
    }

    @Test
    void getUserInfo_shouldFailWithNonExistentUser() {
        when(userMapper.selectById(999L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.getUserInfo(999L));
        assertTrue(ex.getMessage().contains("用户不存在"));
    }
}
