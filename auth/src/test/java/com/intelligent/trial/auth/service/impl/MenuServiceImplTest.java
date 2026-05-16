package com.intelligent.trial.auth.service.impl;

import com.intelligent.trial.auth.dto.MenuDTO;
import com.intelligent.trial.auth.entity.SysMenu;
import com.intelligent.trial.auth.mapper.SysMenuMapper;
import com.intelligent.trial.auth.mapper.SysRoleMenuMapper;
import com.intelligent.trial.auth.vo.MenuTreeVO;
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
 * MenuServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
class MenuServiceImplTest {

    @InjectMocks
    private MenuServiceImpl menuService;

    @Mock
    private SysMenuMapper baseMapper;

    @Mock
    private SysRoleMenuMapper roleMenuMapper;

    @Test
    void getMenuTree_shouldReturnTreeStructure() {
        List<SysMenu> menus = Arrays.asList(
            createMenu(1L, "系统管理", 0L, 1),
            createMenu(2L, "用户管理", 1L, 2),
            createMenu(3L, "角色管理", 1L, 3)
        );

        when(baseMapper.selectAllVisibleMenus()).thenReturn(menus);

        List<MenuTreeVO> tree = menuService.getMenuTree();

        assertNotNull(tree);
        assertEquals(1, tree.size());
        assertEquals("系统管理", tree.get(0).getName());
        assertEquals(2, tree.get(0).getChildren().size());
    }

    @Test
    void getMenuTree_shouldReturnEmptyWhenNoMenus() {
        when(baseMapper.selectAllVisibleMenus()).thenReturn(Collections.emptyList());

        List<MenuTreeVO> tree = menuService.getMenuTree();

        assertNotNull(tree);
        assertTrue(tree.isEmpty());
    }

    @Test
    void getMenuTree_shouldReturnEmptyWhenMenusNull() {
        when(baseMapper.selectAllVisibleMenus()).thenReturn(null);

        List<MenuTreeVO> tree = menuService.getMenuTree();

        assertNotNull(tree);
        assertTrue(tree.isEmpty());
    }

    @Test
    void getMenuTreeByUserId_shouldReturnUserMenus() {
        List<SysMenu> menus = Arrays.asList(
            createMenu(1L, "案件管理", 0L, 1),
            createMenu(2L, "案件列表", 1L, 2)
        );

        when(baseMapper.selectMenusByUserId(1L)).thenReturn(menus);

        List<MenuTreeVO> tree = menuService.getMenuTreeByUserId(1L);

        assertNotNull(tree);
        assertEquals(1, tree.size());
    }

    @Test
    void getMenuTreeByRoleId_shouldReturnRoleMenus() {
        List<SysMenu> menus = Collections.singletonList(createMenu(1L, "Dashboard", 0L, 1));

        when(baseMapper.selectMenusByRoleId(2L)).thenReturn(menus);

        List<MenuTreeVO> tree = menuService.getMenuTreeByRoleId(2L);

        assertNotNull(tree);
        assertEquals(1, tree.size());
    }

    @Test
    void addMenu_shouldCreateMenu() {
        MenuDTO dto = new MenuDTO();
        dto.setName("新菜单");
        dto.setParentId(1L);
        dto.setPath("/new-menu");
        dto.setType(1);
        dto.setSort(10);

        when(baseMapper.insert(any(SysMenu.class))).thenReturn(1);

        menuService.addMenu(dto);

        verify(baseMapper).insert(argThat((SysMenu menu) -> {
            if (!"新菜单".equals(menu.getName())) return false;
            if (!Integer.valueOf(1).equals(menu.getStatus())) return false;
            if (!Integer.valueOf(1).equals(menu.getVisible())) return false;
            if (menu.getCreateTime() == null) return false;
            return true;
        }));
    }

    @Test
    void updateMenu_shouldUpdateMenu() {
        MenuDTO dto = new MenuDTO();
        dto.setId(1L);
        dto.setName("更新后的菜单");

        SysMenu existing = new SysMenu();
        existing.setId(1L);
        existing.setName("原菜单");

        when(baseMapper.selectById(1L)).thenReturn(existing);
        when(baseMapper.updateById(any(SysMenu.class))).thenReturn(1);

        menuService.updateMenu(dto);

        verify(baseMapper).updateById(argThat((SysMenu menu) ->
            Long.valueOf(1L).equals(menu.getId()) && "更新后的菜单".equals(menu.getName())));
    }

    @Test
    void updateMenu_shouldFailWhenMenuIdNull() {
        MenuDTO dto = new MenuDTO();
        dto.setName("No ID");

        BusinessException ex = assertThrows(BusinessException.class, () -> menuService.updateMenu(dto));
        assertTrue(ex.getMessage().contains("菜单ID不能为空"));
    }

    @Test
    void updateMenu_shouldFailWhenMenuNotFound() {
        MenuDTO dto = new MenuDTO();
        dto.setId(999L);
        dto.setName("Ghost Menu");

        when(baseMapper.selectById(999L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> menuService.updateMenu(dto));
        assertTrue(ex.getMessage().contains("菜单不存在"));
    }

    @Test
    void deleteMenu_shouldSucceedWhenNoChildrenAndNoReferences() {
        SysMenu menu = new SysMenu();
        menu.setId(5L);
        menu.setName("Deletable Menu");

        when(baseMapper.selectById(5L)).thenReturn(menu);
        when(baseMapper.selectByParentId(5L)).thenReturn(Collections.emptyList());
        when(baseMapper.selectRoleIdsByMenuId(5L)).thenReturn(Collections.emptyList());
        when(baseMapper.deleteById(5L)).thenReturn(1);

        menuService.deleteMenu(5L);

        verify(baseMapper).deleteById(5L);
    }

    @Test
    void deleteMenu_shouldFailWhenHasChildren() {
        SysMenu menu = new SysMenu();
        menu.setId(5L);

        List<SysMenu> children = Collections.singletonList(createMenu(6L, "Child", 5L, 1));

        when(baseMapper.selectById(5L)).thenReturn(menu);
        when(baseMapper.selectByParentId(5L)).thenReturn(children);

        BusinessException ex = assertThrows(BusinessException.class, () -> menuService.deleteMenu(5L));
        assertTrue(ex.getMessage().contains("存在子菜单"));
        verify(baseMapper, never()).deleteById(any());
    }

    @Test
    void deleteMenu_shouldFailWhenReferencedByRole() {
        SysMenu menu = new SysMenu();
        menu.setId(5L);

        when(baseMapper.selectById(5L)).thenReturn(menu);
        when(baseMapper.selectByParentId(5L)).thenReturn(Collections.emptyList());
        when(baseMapper.selectRoleIdsByMenuId(5L)).thenReturn(Arrays.asList(1L, 2L));

        BusinessException ex = assertThrows(BusinessException.class, () -> menuService.deleteMenu(5L));
        assertTrue(ex.getMessage().contains("已被角色引用"));
        verify(baseMapper, never()).deleteById(any());
    }

    @Test
    void deleteMenu_shouldFailWhenMenuNotFound() {
        when(baseMapper.selectById(999L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> menuService.deleteMenu(999L));
        assertTrue(ex.getMessage().contains("菜单不存在"));
    }

    private SysMenu createMenu(Long id, String name, Long parentId, Integer sort) {
        SysMenu menu = new SysMenu();
        menu.setId(id);
        menu.setName(name);
        menu.setParentId(parentId);
        menu.setSort(sort);
        return menu;
    }
}
