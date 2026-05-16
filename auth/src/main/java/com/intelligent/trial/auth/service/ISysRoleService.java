package com.intelligent.trial.auth.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.intelligent.trial.auth.dto.RoleDTO;
import com.intelligent.trial.auth.entity.SysRole;
import com.intelligent.trial.auth.vo.RoleVO;
import com.intelligent.trial.auth.vo.UserVO;

import java.util.List;

/**
 * 角色管理服务接口
 */
public interface ISysRoleService extends IService<SysRole> {

    /**
     * 分页查询角色列表
     *
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @param roleName 角色名称
     * @param roleCode 角色编码
     * @param status   状态
     * @return 分页角色VO
     */
    Page<RoleVO> pageRole(Integer pageNum, Integer pageSize, String roleName, String roleCode, Integer status);

    /**
     * 新增角色
     *
     * @param dto 角色信息
     */
    void addRole(RoleDTO dto);

    /**
     * 更新角色
     *
     * @param dto 角色信息
     */
    void updateRole(RoleDTO dto);

    /**
     * 删除角色
     *
     * @param id 角色ID
     */
    void deleteRole(Long id);

    /**
     * 为角色分配菜单
     *
     * @param roleId  角色ID
     * @param menuIds 菜单ID列表
     */
    void assignMenus(Long roleId, List<Long> menuIds);

    /**
     * 获取角色关联的菜单ID列表
     *
     * @param roleId 角色ID
     * @return 菜单ID列表
     */
    List<Long> getMenuIdsByRoleId(Long roleId);

    /**
     * 获取角色关联的用户列表
     *
     * @param roleId 角色ID
     * @return 用户VO列表
     */
    List<UserVO> getUsersByRoleId(Long roleId);
}
