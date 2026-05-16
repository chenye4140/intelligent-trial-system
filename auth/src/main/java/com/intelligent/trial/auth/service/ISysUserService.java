package com.intelligent.trial.auth.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.intelligent.trial.auth.dto.UserDTO;
import com.intelligent.trial.auth.entity.SysUser;
import com.intelligent.trial.auth.vo.UserVO;

import java.util.List;

/**
 * 用户管理服务接口
 */
public interface ISysUserService extends IService<SysUser> {

    /**
     * 分页查询用户列表
     *
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @param username 用户名
     * @param realName 真实姓名
     * @param deptId   部门ID
     * @param status   状态
     * @return 分页用户VO
     */
    Page<UserVO> pageUser(Integer pageNum, Integer pageSize, String username, String realName, Long deptId, Integer status);

    /**
     * 根据ID查询用户详情
     *
     * @param id 用户ID
     * @return 用户VO
     */
    UserVO getUserDetail(Long id);

    /**
     * 新增用户
     *
     * @param dto 用户信息
     */
    void addUser(UserDTO dto);

    /**
     * 更新用户
     *
     * @param dto 用户信息
     */
    void updateUser(UserDTO dto);

    /**
     * 删除用户
     *
     * @param id 用户ID
     */
    void deleteUser(Long id);

    /**
     * 重置用户密码
     *
     * @param userId      用户ID
     * @param newPassword 新密码
     */
    void resetPassword(Long userId, String newPassword);

    /**
     * 修改用户状态
     *
     * @param id     用户ID
     * @param status 状态
     */
    void changeStatus(Long id, Integer status);

    /**
     * 分配用户角色
     *
     * @param userId  用户ID
     * @param roleIds 角色ID列表
     */
    void assignRoles(Long userId, List<Long> roleIds);

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户实体
     */
    SysUser getByUsername(String username);
}
