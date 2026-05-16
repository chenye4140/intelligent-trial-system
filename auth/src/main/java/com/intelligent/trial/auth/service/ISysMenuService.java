package com.intelligent.trial.auth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.intelligent.trial.auth.dto.MenuDTO;
import com.intelligent.trial.auth.entity.SysMenu;
import com.intelligent.trial.auth.vo.MenuTreeVO;

import java.util.List;

/**
 * 菜单管理服务接口
 */
public interface ISysMenuService extends IService<SysMenu> {

    /**
     * 获取菜单树
     *
     * @return 菜单树列表
     */
    List<MenuTreeVO> getMenuTree();

    /**
     * 根据用户ID获取菜单树
     *
     * @param userId 用户ID
     * @return 菜单树列表
     */
    List<MenuTreeVO> getMenuTreeByUserId(Long userId);

    /**
     * 根据角色ID获取菜单树
     *
     * @param roleId 角色ID
     * @return 菜单树列表
     */
    List<MenuTreeVO> getMenuTreeByRoleId(Long roleId);

    /**
     * 新增菜单
     *
     * @param dto 菜单信息
     */
    void addMenu(MenuDTO dto);

    /**
     * 更新菜单
     *
     * @param dto 菜单信息
     */
    void updateMenu(MenuDTO dto);

    /**
     * 删除菜单
     *
     * @param id 菜单ID
     */
    void deleteMenu(Long id);
}
