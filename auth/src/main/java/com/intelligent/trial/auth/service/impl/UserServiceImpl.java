package com.intelligent.trial.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.intelligent.trial.auth.dto.UserDTO;
import com.intelligent.trial.auth.entity.SysUser;
import com.intelligent.trial.auth.entity.SysUserRole;
import com.intelligent.trial.auth.mapper.SysUserMapper;
import com.intelligent.trial.auth.mapper.SysUserRoleMapper;
import com.intelligent.trial.auth.service.ISysUserService;
import com.intelligent.trial.auth.util.PasswordEncoderUtil;
import com.intelligent.trial.auth.vo.UserVO;
import com.intelligent.trial.common.exception.BusinessException;
import com.intelligent.trial.common.exception.ErrorCode;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户管理服务实现类
 */
@Service
public class UserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

    @Autowired
    private SysUserRoleMapper userRoleMapper;

    @Override
    public Page<UserVO> pageUser(Integer pageNum, Integer pageSize, String username, String realName, Long deptId, Integer status) {
        Page<UserVO> page = new Page<>(pageNum, pageSize);
        return baseMapper.selectUserPage(page, username, realName, deptId, status);
    }

    @Override
    public UserVO getUserDetail(Long id) {
        UserVO vo = baseMapper.selectUserDetailById(id);
        if (vo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "用户不存在");
        }
        // 查询用户角色
        List<Long> roleIds = userRoleMapper.selectRoleIdsByUserId(id);
        vo.setRoleIds(roleIds);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addUser(UserDTO dto) {
        // 检查用户名是否已存在
        SysUser existUser = baseMapper.selectByUsername(dto.getUsername());
        if (existUser != null) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR.getCode(), "用户名已存在");
        }

        SysUser user = new SysUser();
        BeanUtils.copyProperties(dto, user);
        // 加密密码
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(PasswordEncoderUtil.encode(user.getPassword()));
        }
        user.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        baseMapper.insert(user);

        // 分配角色
        if (dto.getRoleIds() != null && !dto.getRoleIds().isEmpty()) {
            assignRoles(user.getId(), dto.getRoleIds());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(UserDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "用户ID不能为空");
        }

        SysUser existUser = baseMapper.selectById(dto.getId());
        if (existUser == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "用户不存在");
        }

        // 检查用户名是否被其他用户使用
        SysUser existUsername = baseMapper.selectByUsername(dto.getUsername());
        if (existUsername != null && !existUsername.getId().equals(dto.getId())) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR.getCode(), "用户名已存在");
        }

        SysUser user = new SysUser();
        BeanUtils.copyProperties(dto, user);
        // 如果提供了新密码则加密
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(PasswordEncoderUtil.encode(user.getPassword()));
        } else {
            user.setPassword(null); // 不更新密码
        }
        user.setUpdateTime(new Date());
        baseMapper.updateById(user);

        // 更新角色
        if (dto.getRoleIds() != null) {
            assignRoles(dto.getId(), dto.getRoleIds());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long id) {
        SysUser existUser = baseMapper.selectById(id);
        if (existUser == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "用户不存在");
        }
        // 超级管理员不可删除
        if ("admin".equals(existUser.getUsername())) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR.getCode(), "超级管理员不可删除");
        }
        // 先删除角色关联，再删除用户
        userRoleMapper.deleteByUserId(id);
        baseMapper.deleteById(id);
    }

    @Override
    public void resetPassword(Long userId, String newPassword) {
        SysUser user = baseMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "用户不存在");
        }
        SysUser updateUser = new SysUser();
        updateUser.setId(userId);
        updateUser.setPassword(PasswordEncoderUtil.encode(newPassword));
        updateUser.setUpdateTime(new Date());
        baseMapper.updateById(updateUser);
    }

    @Override
    public void changeStatus(Long id, Integer status) {
        SysUser user = baseMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "用户不存在");
        }
        SysUser updateUser = new SysUser();
        updateUser.setId(id);
        updateUser.setStatus(status);
        updateUser.setUpdateTime(new Date());
        baseMapper.updateById(updateUser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRoles(Long userId, List<Long> roleIds) {
        // 先删除原有角色
        userRoleMapper.deleteByUserId(userId);
        // 添加新角色
        if (roleIds != null && !roleIds.isEmpty()) {
            Date now = new Date();
            for (Long roleId : roleIds) {
                SysUserRole userRole = new SysUserRole();
                userRole.setUserId(userId);
                userRole.setRoleId(roleId);
                userRole.setCreateTime(now);
                userRole.setUpdateTime(now);
                userRoleMapper.insert(userRole);
            }
        }
    }

    @Override
    public SysUser getByUsername(String username) {
        return baseMapper.selectByUsername(username);
    }
}
