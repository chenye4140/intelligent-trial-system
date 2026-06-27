package com.intelligent.trial.auth.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.intelligent.trial.auth.dto.UserDTO;
import com.intelligent.trial.auth.entity.SysUser;
import com.intelligent.trial.auth.entity.SysUserRole;
import com.intelligent.trial.auth.interceptor.PermissionInterceptor;
import com.intelligent.trial.auth.mapper.SysUserMapper;
import com.intelligent.trial.auth.mapper.SysUserRoleMapper;
import com.intelligent.trial.auth.util.PasswordEncoderUtil;
import com.intelligent.trial.auth.vo.UserVO;
import com.intelligent.trial.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * UserServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private SysUserMapper baseMapper;

    @Mock
    private SysUserRoleMapper userRoleMapper;

    @Mock
    private PermissionInterceptor permissionInterceptor;

    @Test
    void getUserDetail_shouldReturnUserWithRoles() {
        UserVO vo = new UserVO();
        vo.setId(1L);
        vo.setUsername("zhangsan");
        vo.setRealName("张三");

        when(baseMapper.selectUserDetailById(1L)).thenReturn(vo);
        when(userRoleMapper.selectRoleIdsByUserId(1L)).thenReturn(Arrays.asList(1L, 2L));

        UserVO result = userService.getUserDetail(1L);

        assertNotNull(result);
        assertEquals("zhangsan", result.getUsername());
        assertEquals(2, result.getRoleIds().size());
    }

    @Test
    void getUserDetail_shouldThrowWhenUserNotFound() {
        when(baseMapper.selectUserDetailById(999L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> userService.getUserDetail(999L));
        assertTrue(ex.getMessage().contains("用户不存在"));
    }

    @Test
    void addUser_shouldCreateUserWithEncryptedPassword() {
        UserDTO dto = new UserDTO();
        dto.setUsername("newuser");
        dto.setPassword("PlainPassword123");
        dto.setRealName("新用户");
        dto.setStatus(1);

        when(baseMapper.selectByUsername("newuser")).thenReturn(null);
        when(baseMapper.insert(any(SysUser.class))).thenReturn(1);

        userService.addUser(dto);

        // Verify insert was called with encrypted password
        verify(baseMapper).insert(argThat((SysUser user) -> {
            if (!"newuser".equals(user.getUsername())) return false;
            if ("PlainPassword123".equals(user.getPassword())) return false;
            if (user.getPassword() == null || !user.getPassword().startsWith("$2")) return false;
            return true;
        }));
    }

    @Test
    void addUser_shouldThrowWhenUsernameExists() {
        UserDTO dto = new UserDTO();
        dto.setUsername("existing");
        dto.setPassword("pass123");
        dto.setRealName("Existing User");

        SysUser existingUser = new SysUser();
        existingUser.setId(1L);
        existingUser.setUsername("existing");

        when(baseMapper.selectByUsername("existing")).thenReturn(existingUser);

        BusinessException ex = assertThrows(BusinessException.class, () -> userService.addUser(dto));
        assertTrue(ex.getMessage().contains("用户名已存在"));
    }

    @Test
    void addUser_shouldAssignRolesWhenProvided() {
        UserDTO dto = new UserDTO();
        dto.setUsername("roleuser");
        dto.setPassword("pass123");
        dto.setRealName("Role User");
        dto.setRoleIds(Arrays.asList(1L, 3L));

        when(baseMapper.selectByUsername("roleuser")).thenReturn(null);
        when(baseMapper.insert(any(SysUser.class))).thenAnswer(invocation -> {
            SysUser user = invocation.getArgument(0);
            user.setId(100L);
            return 1;
        });

        userService.addUser(dto);

        verify(userRoleMapper, times(2)).insert(any(SysUserRole.class));
    }

    @Test
    void addUser_shouldNotAssignRolesWhenEmpty() {
        UserDTO dto = new UserDTO();
        dto.setUsername("noroleuser");
        dto.setPassword("pass123");
        dto.setRealName("No Role User");
        dto.setRoleIds(Collections.emptyList());

        when(baseMapper.selectByUsername("noroleuser")).thenReturn(null);
        when(baseMapper.insert(any(SysUser.class))).thenReturn(1);

        userService.addUser(dto);

        verify(userRoleMapper, never()).insert(any());
    }

    @Test
    void updateUser_shouldUpdateUser() {
        UserDTO dto = new UserDTO();
        dto.setId(1L);
        dto.setUsername("updated");
        dto.setRealName("Updated User");
        dto.setPassword("NewPass123");

        SysUser existing = new SysUser();
        existing.setId(1L);
        existing.setUsername("oldname");
        existing.setPassword(PasswordEncoderUtil.encode("oldpass"));

        when(baseMapper.selectById(1L)).thenReturn(existing);
        when(baseMapper.selectByUsername("updated")).thenReturn(null);
        when(baseMapper.updateById(any(SysUser.class))).thenReturn(1);

        userService.updateUser(dto);

        verify(baseMapper).updateById(argThat((SysUser user) -> {
            if (!Long.valueOf(1L).equals(user.getId())) return false;
            if ("NewPass123".equals(user.getPassword())) return false;
            if (user.getPassword() == null || !user.getPassword().startsWith("$2")) return false;
            return true;
        }));
    }

    @Test
    void updateUser_shouldNotUpdatePasswordWhenNotProvided() {
        UserDTO dto = new UserDTO();
        dto.setId(1L);
        dto.setUsername("updated");
        dto.setRealName("Updated User");
        // No password set

        SysUser existing = new SysUser();
        existing.setId(1L);
        existing.setUsername("oldname");

        when(baseMapper.selectById(1L)).thenReturn(existing);
        when(baseMapper.selectByUsername("updated")).thenReturn(null);
        when(baseMapper.updateById(any(SysUser.class))).thenReturn(1);

        userService.updateUser(dto);

        verify(baseMapper).updateById(argThat((SysUser user) -> user.getPassword() == null));
    }

    @Test
    void updateUser_shouldThrowWhenUserIdNull() {
        UserDTO dto = new UserDTO();
        dto.setUsername("noid");

        BusinessException ex = assertThrows(BusinessException.class, () -> userService.updateUser(dto));
        assertTrue(ex.getMessage().contains("用户ID不能为空"));
    }

    @Test
    void updateUser_shouldThrowWhenUserNotFound() {
        UserDTO dto = new UserDTO();
        dto.setId(999L);
        dto.setUsername("ghost");

        when(baseMapper.selectById(999L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> userService.updateUser(dto));
        assertTrue(ex.getMessage().contains("用户不存在"));
    }

    @Test
    void updateUser_shouldThrowWhenUsernameTakenByOtherUser() {
        UserDTO dto = new UserDTO();
        dto.setId(1L);
        dto.setUsername("duplicate");

        SysUser existing = new SysUser();
        existing.setId(1L);
        existing.setUsername("original");

        SysUser duplicateUser = new SysUser();
        duplicateUser.setId(2L);
        duplicateUser.setUsername("duplicate");

        when(baseMapper.selectById(1L)).thenReturn(existing);
        when(baseMapper.selectByUsername("duplicate")).thenReturn(duplicateUser);

        BusinessException ex = assertThrows(BusinessException.class, () -> userService.updateUser(dto));
        assertTrue(ex.getMessage().contains("用户名已存在"));
    }

    @Test
    void deleteUser_shouldSucceedForNormalUser() {
        SysUser user = new SysUser();
        user.setId(5L);
        user.setUsername("normaluser");

        when(baseMapper.selectById(5L)).thenReturn(user);
        when(baseMapper.deleteById(5L)).thenReturn(1);

        userService.deleteUser(5L);

        verify(userRoleMapper).deleteByUserId(5L);
        verify(baseMapper).deleteById(5L);
    }

    @Test
    void deleteUser_shouldFailForAdmin() {
        SysUser admin = new SysUser();
        admin.setId(1L);
        admin.setUsername("admin");

        when(baseMapper.selectById(1L)).thenReturn(admin);

        BusinessException ex = assertThrows(BusinessException.class, () -> userService.deleteUser(1L));
        assertTrue(ex.getMessage().contains("超级管理员不可删除"));
        verify(baseMapper, never()).deleteById(any());
    }

    @Test
    void deleteUser_shouldFailForNonExistentUser() {
        when(baseMapper.selectById(999L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> userService.deleteUser(999L));
        assertTrue(ex.getMessage().contains("用户不存在"));
    }

    @Test
    void resetPassword_shouldUpdatePassword() {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("zhangsan");

        when(baseMapper.selectById(1L)).thenReturn(user);
        when(baseMapper.updateById(any(SysUser.class))).thenReturn(1);

        userService.resetPassword(1L, "NewSecurePass123");

        verify(baseMapper).updateById(argThat((SysUser updateUser) -> {
            if (!Long.valueOf(1L).equals(updateUser.getId())) return false;
            if (updateUser.getPassword() == null) return false;
            if (!updateUser.getPassword().startsWith("$2")) return false;
            if (!PasswordEncoderUtil.matches("NewSecurePass123", updateUser.getPassword())) return false;
            return true;
        }));
    }

    @Test
    void resetPassword_shouldFailForNonExistentUser() {
        when(baseMapper.selectById(999L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> userService.resetPassword(999L, "newpass"));
        assertTrue(ex.getMessage().contains("用户不存在"));
    }

    @Test
    void changeStatus_shouldUpdateStatus() {
        SysUser user = new SysUser();
        user.setId(1L);

        when(baseMapper.selectById(1L)).thenReturn(user);
        when(baseMapper.updateById(any(SysUser.class))).thenReturn(1);

        userService.changeStatus(1L, 0);

        verify(baseMapper).updateById(argThat((SysUser updateUser) ->
            Long.valueOf(1L).equals(updateUser.getId()) && Integer.valueOf(0).equals(updateUser.getStatus())));
    }

    @Test
    void changeStatus_shouldFailForNonExistentUser() {
        when(baseMapper.selectById(999L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> userService.changeStatus(999L, 1));
        assertTrue(ex.getMessage().contains("用户不存在"));
    }

    @Test
    void assignRoles_shouldDeleteOldAndAddNew() {
        List<Long> roleIds = Arrays.asList(1L, 2L, 3L);

        userService.assignRoles(5L, roleIds);

        verify(userRoleMapper).deleteByUserId(5L);
        verify(userRoleMapper, times(3)).insert(argThat((SysUserRole userRole) ->
            Long.valueOf(5L).equals(userRole.getUserId()) && userRole.getCreateTime() != null));
    }

    @Test
    void assignRoles_shouldHandleEmptyRoleIds() {
        userService.assignRoles(5L, Collections.emptyList());

        verify(userRoleMapper).deleteByUserId(5L);
        verify(userRoleMapper, never()).insert(any());
    }

    @Test
    void assignRoles_shouldHandleNullRoleIds() {
        userService.assignRoles(5L, null);

        verify(userRoleMapper).deleteByUserId(5L);
        verify(userRoleMapper, never()).insert(any());
    }

    @Test
    void getByUsername_shouldReturnUser() {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("zhangsan");

        when(baseMapper.selectByUsername("zhangsan")).thenReturn(user);

        SysUser result = userService.getByUsername("zhangsan");

        assertNotNull(result);
        assertEquals("zhangsan", result.getUsername());
    }

    @Test
    void getByUsername_shouldReturnNullForNonExistent() {
        when(baseMapper.selectByUsername("nobody")).thenReturn(null);

        SysUser result = userService.getByUsername("nobody");

        assertNull(result);
    }
}
