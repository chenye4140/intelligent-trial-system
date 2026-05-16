package com.intelligent.trial.auth.service.impl;

import com.intelligent.trial.auth.dto.RoleDTO;
import com.intelligent.trial.auth.entity.SysRole;
import com.intelligent.trial.auth.entity.SysRoleMenu;
import com.intelligent.trial.auth.entity.SysUser;
import com.intelligent.trial.auth.mapper.SysRoleMapper;
import com.intelligent.trial.auth.mapper.SysRoleMenuMapper;
import com.intelligent.trial.auth.mapper.SysUserMapper;
import com.intelligent.trial.auth.mapper.SysUserRoleMapper;
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
 * RoleServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {

    @InjectMocks
    private RoleServiceImpl roleService;

    @Mock
    private SysRoleMapper baseMapper;

    @Mock
    private SysRoleMenuMapper roleMenuMapper;

    @Mock
    private SysUserRoleMapper userRoleMapper;

    @Mock
    private SysUserMapper userMapper;

    @Test
    void addRole_shouldCreateRole() {
        RoleDTO dto = new RoleDTO();
        dto.setRoleName("测试角色");
        dto.setRoleCode("test_role");
        dto.setDescription("用于测试");
        dto.setStatus(1);

        when(baseMapper.selectCount(any())).thenReturn(0L);
        when(baseMapper.insert(any(SysRole.class))).thenAnswer(invocation -> {
            SysRole role = invocation.getArgument(0);
            role.setId(10L);
            return 1;
        });

        roleService.addRole(dto);

        verify(baseMapper).insert(argThat((SysRole role) -> {
            if (!"测试角色".equals(role.getRoleName())) return false;
            if (!"test_role".equals(role.getRoleCode())) return false;
            if (!Integer.valueOf(1).equals(role.getStatus())) return false;
            if (role.getCreateTime() == null) return false;
            return true;
        }));
    }

    @Test
    void addRole_shouldAssignMenusWhenProvided() {
        RoleDTO dto = new RoleDTO();
        dto.setRoleName("菜单角色");
        dto.setRoleCode("menu_role");
        dto.setMenuIds(Arrays.asList(1L, 2L, 3L));

        when(baseMapper.selectCount(any())).thenReturn(0L);
        when(baseMapper.insert(any(SysRole.class))).thenAnswer(invocation -> {
            SysRole role = invocation.getArgument(0);
            role.setId(20L);
            return 1;
        });

        roleService.addRole(dto);

        verify(roleMenuMapper, times(3)).insert(any(SysRoleMenu.class));
    }

    @Test
    void addRole_shouldFailWhenRoleCodeExists() {
        RoleDTO dto = new RoleDTO();
        dto.setRoleName("Duplicate");
        dto.setRoleCode("dup_code");

        when(baseMapper.selectCount(any())).thenReturn(1L);

        BusinessException ex = assertThrows(BusinessException.class, () -> roleService.addRole(dto));
        assertTrue(ex.getMessage().contains("角色编码已存在"));
    }

    @Test
    void updateRole_shouldUpdateRole() {
        RoleDTO dto = new RoleDTO();
        dto.setId(1L);
        dto.setRoleName("Updated Role");
        dto.setRoleCode("updated_code");

        SysRole existing = new SysRole();
        existing.setId(1L);
        existing.setRoleName("Old Role");
        existing.setRoleCode("old_code");

        when(baseMapper.selectById(1L)).thenReturn(existing);
        when(baseMapper.selectCount(any())).thenReturn(0L);
        when(baseMapper.updateById(any(SysRole.class))).thenReturn(1);

        roleService.updateRole(dto);

        verify(baseMapper).updateById(argThat((SysRole role) -> {
            if (!Long.valueOf(1L).equals(role.getId())) return false;
            if (!"Updated Role".equals(role.getRoleName())) return false;
            if (!"updated_code".equals(role.getRoleCode())) return false;
            if (role.getUpdateTime() == null) return false;
            return true;
        }));
    }

    @Test
    void updateRole_shouldFailWhenRoleIdNull() {
        RoleDTO dto = new RoleDTO();
        dto.setRoleName("No ID");
        dto.setRoleCode("no_id");

        BusinessException ex = assertThrows(BusinessException.class, () -> roleService.updateRole(dto));
        assertTrue(ex.getMessage().contains("角色ID不能为空"));
    }

    @Test
    void updateRole_shouldFailWhenRoleNotFound() {
        RoleDTO dto = new RoleDTO();
        dto.setId(999L);
        dto.setRoleName("Ghost");
        dto.setRoleCode("ghost");

        when(baseMapper.selectById(999L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> roleService.updateRole(dto));
        assertTrue(ex.getMessage().contains("角色不存在"));
    }

    @Test
    void updateRole_shouldFailWhenRoleCodeTakenByOther() {
        RoleDTO dto = new RoleDTO();
        dto.setId(1L);
        dto.setRoleCode("taken_code");

        SysRole existing = new SysRole();
        existing.setId(1L);
        existing.setRoleCode("original_code");

        SysRole otherRole = new SysRole();
        otherRole.setId(2L);
        otherRole.setRoleCode("taken_code");

        when(baseMapper.selectById(1L)).thenReturn(existing);
        when(baseMapper.selectCount(any())).thenReturn(1L);

        BusinessException ex = assertThrows(BusinessException.class, () -> roleService.updateRole(dto));
        assertTrue(ex.getMessage().contains("角色编码已存在"));
    }

    @Test
    void deleteRole_shouldSucceedWhenNoUsers() {
        SysRole role = new SysRole();
        role.setId(5L);
        role.setRoleName("Deletable");

        when(baseMapper.selectById(5L)).thenReturn(role);
        when(userMapper.selectUsersByRoleId(5L)).thenReturn(Collections.emptyList());
        when(baseMapper.deleteById(5L)).thenReturn(1);

        roleService.deleteRole(5L);

        verify(roleMenuMapper).deleteByRoleId(5L);
        verify(userRoleMapper).deleteByRoleId(5L);
        verify(baseMapper).deleteById(5L);
    }

    @Test
    void deleteRole_shouldFailWhenUsersAssociated() {
        SysRole role = new SysRole();
        role.setId(5L);
        role.setRoleName("In Use");

        List<UserVO> users = Arrays.asList(new UserVO(), new UserVO());

        when(baseMapper.selectById(5L)).thenReturn(role);
        when(userMapper.selectUsersByRoleId(5L)).thenReturn(users);

        BusinessException ex = assertThrows(BusinessException.class, () -> roleService.deleteRole(5L));
        assertTrue(ex.getMessage().contains("角色下有用户"));
        verify(baseMapper, never()).deleteById(any());
    }

    @Test
    void deleteRole_shouldFailWhenRoleNotFound() {
        when(baseMapper.selectById(999L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> roleService.deleteRole(999L));
        assertTrue(ex.getMessage().contains("角色不存在"));
    }

    @Test
    void assignMenus_shouldDeleteOldAndAddNew() {
        List<Long> menuIds = Arrays.asList(1L, 2L, 3L, 4L);

        roleService.assignMenus(10L, menuIds);

        verify(roleMenuMapper).deleteByRoleId(10L);
        verify(roleMenuMapper, times(4)).insert(argThat((SysRoleMenu roleMenu) ->
            Long.valueOf(10L).equals(roleMenu.getRoleId()) && roleMenu.getCreateTime() != null));
    }

    @Test
    void assignMenus_shouldHandleEmptyMenuIds() {
        roleService.assignMenus(10L, Collections.emptyList());

        verify(roleMenuMapper).deleteByRoleId(10L);
        verify(roleMenuMapper, never()).insert(any());
    }

    @Test
    void assignMenus_shouldHandleNullMenuIds() {
        roleService.assignMenus(10L, null);

        verify(roleMenuMapper).deleteByRoleId(10L);
        verify(roleMenuMapper, never()).insert(any());
    }

    @Test
    void getMenuIdsByRoleId_shouldReturnMenuIds() {
        List<Long> expectedMenuIds = Arrays.asList(1L, 2L, 3L);
        when(baseMapper.selectMenuIdsByRoleId(5L)).thenReturn(expectedMenuIds);

        List<Long> result = roleService.getMenuIdsByRoleId(5L);

        assertEquals(3, result.size());
        assertEquals(1L, result.get(0));
    }

    @Test
    void getUsersByRoleId_shouldReturnUsers() {
        List<UserVO> expectedUsers = Arrays.asList(new UserVO(), new UserVO());
        when(userMapper.selectUsersByRoleId(5L)).thenReturn(expectedUsers);

        List<UserVO> result = roleService.getUsersByRoleId(5L);

        assertEquals(2, result.size());
    }
}
