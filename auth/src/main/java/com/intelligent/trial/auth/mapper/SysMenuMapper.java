package com.intelligent.trial.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.intelligent.trial.auth.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 菜单 Mapper 接口
 */
@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    /**
     * 根据用户ID查询菜单列表
     *
     * @param userId 用户ID
     * @return 菜单列表
     */
    List<SysMenu> selectMenusByUserId(@Param("userId") Long userId);

    /**
     * 根据角色ID查询菜单列表
     *
     * @param roleId 角色ID
     * @return 菜单列表
     */
    List<SysMenu> selectMenusByRoleId(@Param("roleId") Long roleId);

    /**
     * 查询所有可见菜单
     *
     * @return 菜单列表
     */
    List<SysMenu> selectAllVisibleMenus();

    /**
     * 根据父ID查询子菜单
     *
     * @param parentId 父菜单ID
     * @return 子菜单列表
     */
    List<SysMenu> selectByParentId(@Param("parentId") Long parentId);

    /**
     * 根据菜单ID查询关联的角色ID列表
     *
     * @param menuId 菜单ID
     * @return 角色ID列表
     */
    List<Long> selectRoleIdsByMenuId(@Param("menuId") Long menuId);
}
