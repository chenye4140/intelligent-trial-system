package com.intelligent.trial.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.intelligent.trial.auth.entity.SysUser;
import com.intelligent.trial.auth.vo.UserVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户 Mapper 接口
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 分页查询用户列表
     *
     * @param page     分页对象
     * @param username 用户名
     * @param realName 真实姓名
     * @param deptId   部门ID
     * @param status   状态
     * @return 分页用户VO列表
     */
    Page<UserVO> selectUserPage(Page<UserVO> page,
                                @Param("username") String username,
                                @Param("realName") String realName,
                                @Param("deptId") Long deptId,
                                @Param("status") Integer status);

    /**
     * 根据ID查询用户详情
     *
     * @param id 用户ID
     * @return 用户VO
     */
    UserVO selectUserDetailById(@Param("id") Long id);

    /**
     * 根据部门ID查询用户列表
     *
     * @param deptId 部门ID
     * @return 用户VO列表
     */
    List<UserVO> selectUsersByDeptId(@Param("deptId") Long deptId);

    /**
     * 根据角色ID查询用户列表
     *
     * @param roleId 角色ID
     * @return 用户VO列表
     */
    List<UserVO> selectUsersByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户实体
     */
    SysUser selectByUsername(@Param("username") String username);
}
