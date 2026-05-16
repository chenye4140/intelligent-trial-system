package com.intelligent.trial.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.intelligent.trial.auth.dto.RoleDTO;
import com.intelligent.trial.auth.entity.SysRole;
import com.intelligent.trial.auth.entity.SysRoleMenu;
import com.intelligent.trial.auth.entity.SysUserRole;
import com.intelligent.trial.auth.mapper.SysRoleMapper;
import com.intelligent.trial.auth.mapper.SysRoleMenuMapper;
import com.intelligent.trial.auth.mapper.SysUserMapper;
import com.intelligent.trial.auth.mapper.SysUserRoleMapper;
import com.intelligent.trial.auth.service.ISysRoleService;
import com.intelligent.trial.auth.vo.RoleVO;
import com.intelligent.trial.auth.vo.UserVO;
import com.intelligent.trial.common.exception.BusinessException;
import com.intelligent.trial.common.exception.ErrorCode;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 角色管理服务实现类
 */
@Service
public class RoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements ISysRoleService {

    @Autowired
    private SysRoleMenuMapper roleMenuMapper;

    @Autowired
    private SysUserRoleMapper userRoleMapper;

    @Autowired
    private SysUserMapper userMapper;

    @Override
    public Page<RoleVO> pageRole(Integer pageNum, Integer pageSize, String roleName, String roleCode, Integer status) {
        Page<RoleVO> page = new Page<>(pageNum, pageSize);
        return baseMapper.selectRolePage(page, roleName, roleCode, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addRole(RoleDTO dto) {
        // 检查角色编码是否已存在
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getRoleCode, dto.getRoleCode());
        if (baseMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR.getCode(), "角色编码已存在");
        }

        SysRole role = new SysRole();
        BeanUtils.copyProperties(dto, role);
        role.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        role.setCreateTime(new Date());
        role.setUpdateTime(new Date());
        baseMapper.insert(role);

        // 分配菜单
        if (dto.getMenuIds() != null && !dto.getMenuIds().isEmpty()) {
            assignMenus(role.getId(), dto.getMenuIds());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRole(RoleDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "角色ID不能为空");
        }

        SysRole existRole = baseMapper.selectById(dto.getId());
        if (existRole == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "角色不存在");
        }

        // 检查角色编码是否被其他角色使用
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getRoleCode, dto.getRoleCode());
        wrapper.ne(SysRole::getId, dto.getId());
        if (baseMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR.getCode(), "角色编码已存在");
        }

        SysRole role = new SysRole();
        BeanUtils.copyProperties(dto, role);
        role.setUpdateTime(new Date());
        baseMapper.updateById(role);

        // 更新菜单
        if (dto.getMenuIds() != null) {
            assignMenus(dto.getId(), dto.getMenuIds());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long id) {
        SysRole existRole = baseMapper.selectById(id);
        if (existRole == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "角色不存在");
        }
        // 检查是否有用户关联此角色
        List<UserVO> users = userMapper.selectUsersByRoleId(id);
        if (users != null && !users.isEmpty()) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR.getCode(), "该角色下有用户，无法删除");
        }
        // 先删除关联，再删除角色
        roleMenuMapper.deleteByRoleId(id);
        userRoleMapper.deleteByRoleId(id);
        baseMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignMenus(Long roleId, List<Long> menuIds) {
        // 先删除原有菜单
        roleMenuMapper.deleteByRoleId(roleId);
        // 添加新菜单
        if (menuIds != null && !menuIds.isEmpty()) {
            Date now = new Date();
            for (Long menuId : menuIds) {
                SysRoleMenu roleMenu = new SysRoleMenu();
                roleMenu.setRoleId(roleId);
                roleMenu.setMenuId(menuId);
                roleMenu.setCreateTime(now);
                roleMenu.setUpdateTime(now);
                roleMenuMapper.insert(roleMenu);
            }
        }
    }

    @Override
    public List<Long> getMenuIdsByRoleId(Long roleId) {
        return baseMapper.selectMenuIdsByRoleId(roleId);
    }

    @Override
    public List<UserVO> getUsersByRoleId(Long roleId) {
        return userMapper.selectUsersByRoleId(roleId);
    }
}
