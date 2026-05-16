package com.intelligent.trial.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.intelligent.trial.auth.entity.SysRole;
import com.intelligent.trial.auth.vo.RoleVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色 Mapper 接口
 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {

    /**
     * 分页查询角色列表
     *
     * @param page     分页对象
     * @param roleName 角色名称
     * @param roleCode 角色编码
     * @param status   状态
     * @return 分页角色VO列表
     */
    Page<RoleVO> selectRolePage(Page<RoleVO> page,
                                @Param("roleName") String roleName,
                                @Param("roleCode") String roleCode,
                                @Param("status") Integer status);

    /**
     * 根据用户ID查询角色列表
     *
     * @param userId 用户ID
     * @return 角色VO列表
     */
    List<RoleVO> selectRolesByUserId(@Param("userId") Long userId);

    /**
     * 查询角色关联的菜单ID列表
     *
     * @param roleId 角色ID
     * @return 菜单ID列表
     */
    List<Long> selectMenuIdsByRoleId(@Param("roleId") Long roleId);
}
