package com.intelligent.trial.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.intelligent.trial.auth.dto.MenuDTO;
import com.intelligent.trial.auth.entity.SysMenu;
import com.intelligent.trial.auth.entity.SysRoleMenu;
import com.intelligent.trial.auth.mapper.SysMenuMapper;
import com.intelligent.trial.auth.mapper.SysRoleMenuMapper;
import com.intelligent.trial.auth.service.ISysMenuService;
import com.intelligent.trial.auth.vo.MenuTreeVO;
import com.intelligent.trial.common.exception.BusinessException;
import com.intelligent.trial.common.exception.ErrorCode;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 菜单管理服务实现类
 */
@Service
public class MenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements ISysMenuService {

    @Autowired
    private SysRoleMenuMapper roleMenuMapper;

    @Override
    public List<MenuTreeVO> getMenuTree() {
        // 查询所有菜单
        List<SysMenu> allMenus = baseMapper.selectAllVisibleMenus();
        return buildMenuTree(allMenus, 0L);
    }

    @Override
    public List<MenuTreeVO> getMenuTreeByUserId(Long userId) {
        // 根据用户ID查询菜单
        List<SysMenu> menus = baseMapper.selectMenusByUserId(userId);
        return buildMenuTree(menus, 0L);
    }

    @Override
    public List<MenuTreeVO> getMenuTreeByRoleId(Long roleId) {
        // 根据角色ID查询菜单
        List<SysMenu> menus = baseMapper.selectMenusByRoleId(roleId);
        return buildMenuTree(menus, 0L);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addMenu(MenuDTO dto) {
        SysMenu menu = new SysMenu();
        BeanUtils.copyProperties(dto, menu);
        menu.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        menu.setVisible(dto.getVisible() != null ? dto.getVisible() : 1);
        menu.setCreateTime(new Date());
        menu.setUpdateTime(new Date());
        baseMapper.insert(menu);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMenu(MenuDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "菜单ID不能为空");
        }
        SysMenu existMenu = baseMapper.selectById(dto.getId());
        if (existMenu == null) {
            throw new BusinessException(ErrorCode.AUTH_MENU_NOT_FOUND);
        }
        SysMenu menu = new SysMenu();
        BeanUtils.copyProperties(dto, menu);
        menu.setUpdateTime(new Date());
        baseMapper.updateById(menu);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMenu(Long id) {
        SysMenu existMenu = baseMapper.selectById(id);
        if (existMenu == null) {
            throw new BusinessException(ErrorCode.AUTH_MENU_NOT_FOUND);
        }
        // 检查是否有子菜单
        List<SysMenu> children = baseMapper.selectByParentId(id);
        if (children != null && !children.isEmpty()) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR.getCode(), "存在子菜单，无法删除");
        }
        // 检查是否有角色关联此菜单
        List<Long> roleIds = baseMapper.selectRoleIdsByMenuId(id);
        if (roleIds != null && !roleIds.isEmpty()) {
            throw new BusinessException(ErrorCode.AUTH_MENU_REFERENCED);
        }
        baseMapper.deleteById(id);
    }

    /**
     * 构建菜单树
     */
    private List<MenuTreeVO> buildMenuTree(List<SysMenu> menus, Long parentId) {
        List<MenuTreeVO> tree = new ArrayList<>();
        if (menus == null) return tree;

        for (SysMenu menu : menus) {
            if (parentId.equals(menu.getParentId())) {
                MenuTreeVO node = new MenuTreeVO();
                BeanUtils.copyProperties(menu, node);
                node.setChildren(buildMenuTree(menus, menu.getId()));
                tree.add(node);
            }
        }

        // 按 sort 排序
        tree.sort(Comparator.comparing(MenuTreeVO::getSort, Comparator.nullsLast(Comparator.naturalOrder())));
        return tree;
    }
}
