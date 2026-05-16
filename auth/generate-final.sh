#!/bin/bash
# Final comprehensive script: mappers, XML, services, controllers
BASE="/home/chenye/intelligent-trial-system/auth/src/main/java/com/intelligent/trial/auth"
RES="/home/chenye/intelligent-trial-system/auth/src/main/resources"
mkdir -p $BASE/{mapper,service/impl,controller}
mkdir -p $RES/mapper

########################################
# MAPPER INTERFACES
########################################
cat > $BASE/mapper/SysUserMapper.java << 'EOF'
package com.intelligent.trial.auth.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.intelligent.trial.auth.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
    List<String> selectPermsByUserId(@Param("userId") Long userId);
    List<Long> selectRoleIdsByUserId(@Param("userId") Long userId);
    int selectMaxClassificationLevelSortByUserId(@Param("userId") Long userId);
}
EOF

cat > $BASE/mapper/SysRoleMapper.java << 'EOF'
package com.intelligent.trial.auth.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.intelligent.trial.auth.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {
    List<Long> selectMenuIdsByRoleId(@Param("roleId") Long roleId);
    List<Long> selectUserIdsByRoleId(@Param("roleId") Long roleId);
}
EOF

cat > $BASE/mapper/SysMenuMapper.java << 'EOF'
package com.intelligent.trial.auth.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.intelligent.trial.auth.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {
    List<SysMenu> selectMenuTreeByUserId(@Param("userId") Long userId);
    List<SysMenu> selectMenuTreeByRoleId(@Param("roleId") Long roleId);
}
EOF

cat > $BASE/mapper/SysDeptMapper.java << 'EOF'
package com.intelligent.trial.auth.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.intelligent.trial.auth.entity.SysDept;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysDeptMapper extends BaseMapper<SysDept> {
}
EOF

cat > $BASE/mapper/SysAuditLogMapper.java << 'EOF'
package com.intelligent.trial.auth.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.intelligent.trial.auth.entity.SysAuditLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysAuditLogMapper extends BaseMapper<SysAuditLog> {
}
EOF

cat > $BASE/mapper/SysClassificationLevelMapper.java << 'EOF'
package com.intelligent.trial.auth.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.intelligent.trial.auth.entity.SysClassificationLevel;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysClassificationLevelMapper extends BaseMapper<SysClassificationLevel> {
}
EOF

########################################
# MYBATIS XML
########################################
cat > $RES/mapper/SysUserMapper.xml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.intelligent.trial.auth.mapper.SysUserMapper">
    <select id="selectPermsByUserId" resultType="string">
        SELECT DISTINCT m.perms FROM sys_menu m
        INNER JOIN sys_role_menu rm ON m.id = rm.menu_id
        INNER JOIN sys_user_role ur ON rm.role_id = ur.role_id
        WHERE ur.user_id = #{userId} AND m.perms IS NOT NULL AND m.perms != '' AND m.status = 1
    </select>
    <select id="selectRoleIdsByUserId" resultType="long">
        SELECT role_id FROM sys_user_role WHERE user_id = #{userId}
    </select>
    <select id="selectMaxClassificationLevelSortByUserId" resultType="int">
        SELECT MIN(cl.sort) FROM sys_classification_level cl
        INNER JOIN sys_role_menu rm ON 1=1
        INNER JOIN sys_menu m ON rm.menu_id = m.id
        INNER JOIN sys_user_role ur ON rm.role_id = ur.role_id
        WHERE ur.user_id = #{userId} AND cl.status = 1
        LIMIT 1
    </select>
</mapper>
EOF

cat > $RES/mapper/SysRoleMapper.xml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.intelligent.trial.auth.mapper.SysRoleMapper">
    <select id="selectMenuIdsByRoleId" resultType="long">
        SELECT menu_id FROM sys_role_menu WHERE role_id = #{roleId}
    </select>
    <select id="selectUserIdsByRoleId" resultType="long">
        SELECT user_id FROM sys_user_role WHERE role_id = #{roleId}
    </select>
</mapper>
EOF

cat > $RES/mapper/SysMenuMapper.xml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.intelligent.trial.auth.mapper.SysMenuMapper">
    <select id="selectMenuTreeByUserId" resultType="com.intelligent.trial.auth.entity.SysMenu">
        SELECT DISTINCT m.* FROM sys_menu m
        INNER JOIN sys_role_menu rm ON m.id = rm.menu_id
        INNER JOIN sys_user_role ur ON rm.role_id = ur.role_id
        WHERE ur.user_id = #{userId} AND m.status = 1
        ORDER BY m.sort
    </select>
    <select id="selectMenuTreeByRoleId" resultType="com.intelligent.trial.auth.entity.SysMenu">
        SELECT m.* FROM sys_menu m
        INNER JOIN sys_role_menu rm ON m.id = rm.menu_id
        WHERE rm.role_id = #{roleId} AND m.status = 1
        ORDER BY m.sort
    </select>
</mapper>
EOF

########################################
# SERVICE INTERFACES
########################################
cat > $BASE/service/IAuthService.java << 'EOF'
package com.intelligent.trial.auth.service;
import com.intelligent.trial.auth.dto.LoginDTO;
import com.intelligent.trial.auth.vo.LoginVO;

public interface IAuthService {
    LoginVO login(LoginDTO loginDTO);
    LoginVO refreshToken(String refreshToken);
    void logout(String token);
}
EOF

cat > $BASE/service/ISysUserService.java << 'EOF'
package com.intelligent.trial.auth.service;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.intelligent.trial.auth.dto.UserDTO;
import com.intelligent.trial.auth.entity.SysUser;
import com.intelligent.trial.auth.vo.UserVO;
import java.util.List;

public interface ISysUserService extends IService<SysUser> {
    Page<UserVO> pageUser(Integer pageNum, Integer pageSize, String username, String realName, Long deptId, Integer status);
    void addUser(UserDTO dto);
    void updateUser(UserDTO dto);
    void deleteUser(Long id);
    void resetPassword(Long userId, String newPassword);
    void changeStatus(Long userId, Integer status);
    void assignRoles(Long userId, List<Long> roleIds);
    UserVO getUserDetail(Long id);
    List<String> getPermsByUserId(Long userId);
}
EOF

cat > $BASE/service/ISysRoleService.java << 'EOF'
package com.intelligent.trial.auth.service;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.intelligent.trial.auth.dto.RoleDTO;
import com.intelligent.trial.auth.entity.SysRole;
import com.intelligent.trial.auth.vo.RoleVO;
import com.intelligent.trial.auth.vo.UserVO;
import java.util.List;

public interface ISysRoleService extends IService<SysRole> {
    Page<RoleVO> pageRole(Integer pageNum, Integer pageSize, String roleName, String roleCode, Integer status);
    void addRole(RoleDTO dto);
    void updateRole(RoleDTO dto);
    void deleteRole(Long id);
    void assignMenus(Long roleId, List<Long> menuIds);
    List<Long> getMenuIdsByRoleId(Long roleId);
    List<UserVO> getUsersByRoleId(Long roleId);
}
EOF

cat > $BASE/service/ISysMenuService.java << 'EOF'
package com.intelligent.trial.auth.service;
import com.baomidou.mybatisplus.extension.service.IService;
import com.intelligent.trial.auth.dto.MenuDTO;
import com.intelligent.trial.auth.entity.SysMenu;
import com.intelligent.trial.auth.vo.MenuTreeVO;
import java.util.List;

public interface ISysMenuService extends IService<SysMenu> {
    List<MenuTreeVO> getMenuTree();
    List<MenuTreeVO> getMenuTreeByUserId(Long userId);
    List<MenuTreeVO> getMenuTreeByRoleId(Long roleId);
    void addMenu(MenuDTO dto);
    void updateMenu(MenuDTO dto);
    void deleteMenu(Long id);
}
EOF

cat > $BASE/service/ISysDeptService.java << 'EOF'
package com.intelligent.trial.auth.service;
import com.baomidou.mybatisplus.extension.service.IService;
import com.intelligent.trial.auth.dto.DeptDTO;
import com.intelligent.trial.auth.entity.SysDept;
import com.intelligent.trial.auth.vo.DeptTreeVO;
import com.intelligent.trial.auth.vo.UserVO;
import java.util.List;

public interface ISysDeptService extends IService<SysDept> {
    List<DeptTreeVO> getDeptTree();
    void addDept(DeptDTO dto);
    void updateDept(DeptDTO dto);
    void deleteDept(Long id);
    List<UserVO> getUsersByDeptId(Long deptId);
}
EOF

cat > $BASE/service/ISysClassificationLevelService.java << 'EOF'
package com.intelligent.trial.auth.service;
import com.baomidou.mybatisplus.extension.service.IService;
import com.intelligent.trial.auth.dto.ClassificationLevelDTO;
import com.intelligent.trial.auth.entity.SysClassificationLevel;
import com.intelligent.trial.auth.vo.ClassificationAccessVO;
import com.intelligent.trial.auth.vo.ClassificationLevelVO;
import java.util.List;

public interface ISysClassificationLevelService extends IService<SysClassificationLevel> {
    void addLevel(ClassificationLevelDTO dto);
    void updateLevel(ClassificationLevelDTO dto);
    void deleteLevel(Long id);
    List<ClassificationLevelVO> listAll();
    ClassificationAccessVO getAccessibleLevels(Long userId);
}
EOF

cat > $BASE/service/ISysAuditLogService.java << 'EOF'
package com.intelligent.trial.auth.service;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.intelligent.trial.auth.entity.SysAuditLog;
import com.intelligent.trial.auth.vo.AuditLogVO;

public interface ISysAuditLogService extends IService<SysAuditLog> {
    Page<AuditLogVO> pageLog(Integer pageNum, Integer pageSize, String module, String action, Long userId, String startTime, String endTime);
}
EOF

########################################
# SERVICE IMPLEMENTATIONS
########################################
cat > $BASE/service/impl/AuthServiceImpl.java << 'EOF'
package com.intelligent.trial.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.intelligent.trial.auth.config.JwtConfig;
import com.intelligent.trial.auth.dto.LoginDTO;
import com.intelligent.trial.auth.entity.SysUser;
import com.intelligent.trial.auth.mapper.SysUserMapper;
import com.intelligent.trial.auth.service.IAuthService;
import com.intelligent.trial.auth.service.ISysDeptService;
import com.intelligent.trial.auth.util.JwtUtil;
import com.intelligent.trial.auth.util.PasswordEncoderUtil;
import com.intelligent.trial.auth.vo.LoginVO;
import com.intelligent.trial.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class AuthServiceImpl implements IAuthService {

    @Autowired private SysUserMapper userMapper;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private JwtConfig jwtConfig;
    @Autowired private RedisTemplate<String, Object> redisTemplate;
    @Autowired private ISysDeptService deptService;

    private static final String TOKEN_BLACKLIST_PREFIX = "auth:token:blacklist:";

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        SysUser user = userMapper.selectOne(
            new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, loginDTO.getUsername()));
        if (user == null) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        if (!PasswordEncoderUtil.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(403, "用户已被禁用");
        }

        String accessToken = jwtUtil.generateToken(user.getId(), user.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername());

        LoginVO vo = new LoginVO();
        vo.setAccessToken(accessToken);
        vo.setRefreshToken(refreshToken);
        vo.setExpiresIn(jwtConfig.getExpiration() / 1000);

        LoginVO.UserInfo ui = new LoginVO.UserInfo();
        ui.setId(user.getId());
        ui.setUsername(user.getUsername());
        ui.setRealName(user.getRealName());
        ui.setPhone(user.getPhone());
        ui.setEmail(user.getEmail());
        if (user.getDeptId() != null) {
            com.intelligent.trial.auth.entity.SysDept dept = deptService.getById(user.getDeptId());
            ui.setDeptName(dept != null ? dept.getDeptName() : null);
        }
        vo.setUserInfo(ui);
        return vo;
    }

    @Override
    public LoginVO refreshToken(String refreshToken) {
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new BusinessException(401, "刷新令牌无效或已过期");
        }
        Long userId = jwtUtil.getUserId(refreshToken);
        String username = jwtUtil.getUsername(refreshToken);
        String newAccessToken = jwtUtil.generateToken(userId, username);
        String newRefreshToken = jwtUtil.generateRefreshToken(userId, username);

        LoginVO vo = new LoginVO();
        vo.setAccessToken(newAccessToken);
        vo.setRefreshToken(newRefreshToken);
        vo.setExpiresIn(jwtConfig.getExpiration() / 1000);
        return vo;
    }

    @Override
    public void logout(String token) {
        Long expire = jwtUtil.parseToken(token).getExpiration().getTime() - System.currentTimeMillis();
        if (expire > 0) {
            redisTemplate.opsForValue().set(TOKEN_BLACKLIST_PREFIX + token, "1", expire, TimeUnit.MILLISECONDS);
        }
    }
}
EOF

cat > $BASE/service/impl/SysUserServiceImpl.java << 'EOF'
package com.intelligent.trial.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.intelligent.trial.auth.dto.UserDTO;
import com.intelligent.trial.auth.entity.SysDept;
import com.intelligent.trial.auth.entity.SysRole;
import com.intelligent.trial.auth.entity.SysUser;
import com.intelligent.trial.auth.entity.SysUserRole;
import com.intelligent.trial.auth.mapper.SysUserMapper;
import com.intelligent.trial.auth.service.ISysDeptService;
import com.intelligent.trial.auth.service.ISysRoleService;
import com.intelligent.trial.auth.service.ISysUserService;
import com.intelligent.trial.auth.util.PasswordEncoderUtil;
import com.intelligent.trial.auth.vo.UserVO;
import com.intelligent.trial.common.exception.BusinessException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

    @Autowired private SysUserMapper userMapper;
    @Autowired private ISysDeptService deptService;
    @Autowired private ISysRoleService roleService;
    @Autowired private com.baomidou.mybatisplus.core.mapper.BaseMapper<SysUserRole> userRoleMapper;

    @Override
    public Page<UserVO> pageUser(Integer pageNum, Integer pageSize, String username, String realName, Long deptId, Integer status) {
        Page<SysUser> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (username != null && !username.isEmpty()) wrapper.like(SysUser::getUsername, username);
        if (realName != null && !realName.isEmpty()) wrapper.like(SysUser::getRealName, realName);
        if (deptId != null) wrapper.eq(SysUser::getDeptId, deptId);
        if (status != null) wrapper.eq(SysUser::getStatus, status);
        wrapper.orderByDesc(SysUser::getCreateTime);
        Page<SysUser> userPage = userMapper.selectPage(page, wrapper);

        Page<UserVO> voPage = new Page<>(pageNum, pageSize, userPage.getTotal());
        List<UserVO> voList = userPage.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    @Transactional
    public void addUser(UserDTO dto) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, dto.getUsername());
        if (userMapper.selectCount(wrapper) > 0) throw new BusinessException("用户名已存在");
        SysUser user = new SysUser();
        BeanUtils.copyProperties(dto, user);
        user.setPassword(PasswordEncoderUtil.encode(dto.getPassword() != null ? dto.getPassword() : "123456"));
        user.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        userMapper.insert(user);
        if (dto.getRoleIds() != null && !dto.getRoleIds().isEmpty()) assignRoles(user.getId(), dto.getRoleIds());
    }

    @Override
    @Transactional
    public void updateUser(UserDTO dto) {
        SysUser exist = userMapper.selectById(dto.getId());
        if (exist == null) throw new BusinessException("用户不存在");
        SysUser user = new SysUser();
        BeanUtils.copyProperties(dto, user);
        user.setPassword(null);
        userMapper.updateById(user);
        if (dto.getRoleIds() != null) {
            userMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, dto.getId()));
            if (!dto.getRoleIds().isEmpty()) assignRoles(dto.getId(), dto.getRoleIds());
        }
    }

    @Override
    public void deleteUser(Long id) {
        SysUser exist = userMapper.selectById(id);
        if (exist == null) throw new BusinessException("用户不存在");
        if ("admin".equals(exist.getUsername())) throw new BusinessException("不能删除管理员用户");
        userMapper.deleteById(id);
        userMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, id));
    }

    @Override
    public void resetPassword(Long userId, String newPassword) {
        SysUser exist = userMapper.selectById(userId);
        if (exist == null) throw new BusinessException("用户不存在");
        SysUser user = new SysUser();
        user.setId(userId);
        user.setPassword(PasswordEncoderUtil.encode(newPassword));
        userMapper.updateById(user);
    }

    @Override
    public void changeStatus(Long userId, Integer status) {
        SysUser exist = userMapper.selectById(userId);
        if (exist == null) throw new BusinessException("用户不存在");
        SysUser user = new SysUser();
        user.setId(userId);
        user.setStatus(status);
        userMapper.updateById(user);
    }

    @Override
    @Transactional
    public void assignRoles(Long userId, List<Long> roleIds) {
        userMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        if (roleIds != null && !roleIds.isEmpty()) {
            List<SysUserRole> list = roleIds.stream().map(rid -> {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(userId);
                ur.setRoleId(rid);
                return ur;
            }).collect(Collectors.toList());
            for (SysUserRole ur : list) { userMapper.insert(ur); }
        }
    }

    @Override
    public UserVO getUserDetail(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) throw new BusinessException("用户不存在");
        UserVO vo = toVO(user);
        vo.setRoleIds(userMapper.selectRoleIdsByUserId(id));
        return vo;
    }

    @Override
    public List<String> getPermsByUserId(Long userId) {
        return userMapper.selectPermsByUserId(userId);
    }

    private UserVO toVO(SysUser user) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        if (user.getDeptId() != null) {
            SysDept dept = deptService.getById(user.getDeptId());
            vo.setDeptName(dept != null ? dept.getDeptName() : null);
        }
        return vo;
    }
}
EOF

cat > $BASE/service/impl/SysRoleServiceImpl.java << 'EOF'
package com.intelligent.trial.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.intelligent.trial.auth.dto.RoleDTO;
import com.intelligent.trial.auth.entity.SysRole;
import com.intelligent.trial.auth.entity.SysRoleMenu;
import com.intelligent.trial.auth.entity.SysUser;
import com.intelligent.trial.auth.mapper.SysRoleMapper;
import com.intelligent.trial.auth.mapper.SysUserMapper;
import com.intelligent.trial.auth.service.ISysRoleService;
import com.intelligent.trial.auth.service.ISysUserService;
import com.intelligent.trial.auth.vo.RoleVO;
import com.intelligent.trial.auth.vo.UserVO;
import com.intelligent.trial.common.exception.BusinessException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements ISysRoleService {

    @Autowired private SysRoleMapper roleMapper;
    @Autowired private SysUserMapper userMapper;
    @Autowired private ISysUserService userService;
    @Autowired private com.baomidou.mybatisplus.core.mapper.BaseMapper<SysRoleMenu> roleMenuMapper;

    @Override
    public Page<RoleVO> pageRole(Integer pageNum, Integer pageSize, String roleName, String roleCode, Integer status) {
        Page<SysRole> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        if (roleName != null && !roleName.isEmpty()) wrapper.like(SysRole::getRoleName, roleName);
        if (roleCode != null && !roleCode.isEmpty()) wrapper.like(SysRole::getRoleCode, roleCode);
        if (status != null) wrapper.eq(SysRole::getStatus, status);
        wrapper.orderByDesc(SysRole::getCreateTime);
        Page<SysRole> rolePage = roleMapper.selectPage(page, wrapper);
        Page<RoleVO> voPage = new Page<>(pageNum, pageSize, rolePage.getTotal());
        voPage.setRecords(rolePage.getRecords().stream().map(this::toVO).collect(Collectors.toList()));
        return voPage;
    }

    @Override
    @Transactional
    public void addRole(RoleDTO dto) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleCode, dto.getRoleCode());
        if (roleMapper.selectCount(wrapper) > 0) throw new BusinessException("角色编码已存在");
        SysRole role = new SysRole();
        BeanUtils.copyProperties(dto, role);
        role.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        roleMapper.insert(role);
        if (dto.getMenuIds() != null && !dto.getMenuIds().isEmpty()) assignMenus(role.getId(), dto.getMenuIds());
    }

    @Override
    @Transactional
    public void updateRole(RoleDTO dto) {
        SysRole exist = roleMapper.selectById(dto.getId());
        if (exist == null) throw new BusinessException("角色不存在");
        SysRole role = new SysRole();
        BeanUtils.copyProperties(dto, role);
        roleMapper.updateById(role);
        if (dto.getMenuIds() != null) {
            roleMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, dto.getId()));
            if (!dto.getMenuIds().isEmpty()) assignMenus(dto.getId(), dto.getMenuIds());
        }
    }

    @Override
    public void deleteRole(Long id) {
        SysRole exist = roleMapper.selectById(id);
        if (exist == null) throw new BusinessException("角色不存在");
        roleMapper.deleteById(id);
        roleMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, id));
        userMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.intelligent.trial.auth.entity.SysUserRole>().eq(com.intelligent.trial.auth.entity.SysUserRole::getRoleId, id));
    }

    @Override
    @Transactional
    public void assignMenus(Long roleId, List<Long> menuIds) {
        roleMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId));
        if (menuIds != null && !menuIds.isEmpty()) {
            for (Long menuId : menuIds) {
                SysRoleMenu rm = new SysRoleMenu();
                rm.setRoleId(roleId);
                rm.setMenuId(menuId);
                roleMapper.insert(rm);
            }
        }
    }

    @Override
    public List<Long> getMenuIdsByRoleId(Long roleId) {
        return roleMapper.selectMenuIdsByRoleId(roleId);
    }

    @Override
    public List<UserVO> getUsersByRoleId(Long roleId) {
        List<Long> userIds = roleMapper.selectUserIdsByRoleId(roleId);
        if (userIds.isEmpty()) return java.util.Collections.emptyList();
        return userIds.stream().map(uid -> userService.getUserDetail(uid)).collect(Collectors.toList());
    }

    private RoleVO toVO(SysRole role) {
        RoleVO vo = new RoleVO();
        BeanUtils.copyProperties(role, vo);
        return vo;
    }
}
EOF

cat > $BASE/service/impl/SysMenuServiceImpl.java << 'EOF'
package com.intelligent.trial.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.intelligent.trial.auth.dto.MenuDTO;
import com.intelligent.trial.auth.entity.SysMenu;
import com.intelligent.trial.auth.mapper.SysMenuMapper;
import com.intelligent.trial.auth.service.ISysMenuService;
import com.intelligent.trial.auth.vo.MenuTreeVO;
import com.intelligent.trial.common.exception.BusinessException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements ISysMenuService {

    @Autowired private SysMenuMapper menuMapper;

    @Override
    public List<MenuTreeVO> getMenuTree() {
        List<SysMenu> all = menuMapper.selectList(new LambdaQueryWrapper<SysMenu>().orderByAsc(SysMenu::getSort));
        return buildTree(all, 0L);
    }

    @Override
    public List<MenuTreeVO> getMenuTreeByUserId(Long userId) {
        List<SysMenu> menus = menuMapper.selectMenuTreeByUserId(userId);
        return buildTree(menus, 0L);
    }

    @Override
    public List<MenuTreeVO> getMenuTreeByRoleId(Long roleId) {
        List<SysMenu> menus = menuMapper.selectMenuTreeByRoleId(roleId);
        return buildTree(menus, 0L);
    }

    @Override
    @Transactional
    public void addMenu(MenuDTO dto) {
        SysMenu menu = new SysMenu();
        BeanUtils.copyProperties(dto, menu);
        if (menu.getParentId() == null) menu.setParentId(0L);
        if (menu.getSort() == null) menu.setSort(0);
        if (menu.getVisible() == null) menu.setVisible(1);
        if (menu.getStatus() == null) menu.setStatus(1);
        if (menu.getType() == null) menu.setType(2);
        menuMapper.insert(menu);
    }

    @Override
    public void updateMenu(MenuDTO dto) {
        SysMenu exist = menuMapper.selectById(dto.getId());
        if (exist == null) throw new BusinessException("菜单不存在");
        SysMenu menu = new SysMenu();
        BeanUtils.copyProperties(dto, menu);
        menuMapper.updateById(menu);
    }

    @Override
    public void deleteMenu(Long id) {
        long count = menuMapper.selectCount(new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getParentId, id));
        if (count > 0) throw new BusinessException("存在子菜单，不能删除");
        menuMapper.deleteById(id);
    }

    private List<MenuTreeVO> buildTree(List<SysMenu> menus, Long parentId) {
        return menus.stream()
            .filter(m -> m.getParentId().equals(parentId))
            .map(m -> {
                MenuTreeVO vo = new MenuTreeVO();
                BeanUtils.copyProperties(m, vo);
                List<MenuTreeVO> children = buildTree(menus, m.getId());
                if (!children.isEmpty()) vo.setChildren(children);
                return vo;
            }).collect(Collectors.toList());
    }
}
EOF

cat > $BASE/service/impl/SysDeptServiceImpl.java << 'EOF'
package com.intelligent.trial.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.intelligent.trial.auth.dto.DeptDTO;
import com.intelligent.trial.auth.entity.SysDept;
import com.intelligent.trial.auth.entity.SysUser;
import com.intelligent.trial.auth.mapper.SysDeptMapper;
import com.intelligent.trial.auth.mapper.SysUserMapper;
import com.intelligent.trial.auth.service.ISysDeptService;
import com.intelligent.trial.auth.service.ISysUserService;
import com.intelligent.trial.auth.vo.DeptTreeVO;
import com.intelligent.trial.auth.vo.UserVO;
import com.intelligent.trial.common.exception.BusinessException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements ISysDeptService {

    @Autowired private SysDeptMapper deptMapper;
    @Autowired private SysUserMapper userMapper;
    @Autowired private ISysUserService userService;

    @Override
    public List<DeptTreeVO> getDeptTree() {
        List<SysDept> all = deptMapper.selectList(new LambdaQueryWrapper<SysDept>().orderByAsc(SysDept::getSort));
        return buildTree(all, 0L);
    }

    @Override
    @Transactional
    public void addDept(DeptDTO dto) {
        SysDept dept = new SysDept();
        BeanUtils.copyProperties(dto, dept);
        if (dept.getParentId() == null) dept.setParentId(0L);
        if (dept.getSort() == null) dept.setSort(0);
        if (dept.getStatus() == null) dept.setStatus(1);
        deptMapper.insert(dept);
    }

    @Override
    public void updateDept(DeptDTO dto) {
        SysDept exist = deptMapper.selectById(dto.getId());
        if (exist == null) throw new BusinessException("部门不存在");
        SysDept dept = new SysDept();
        BeanUtils.copyProperties(dto, dept);
        deptMapper.updateById(dept);
    }

    @Override
    public void deleteDept(Long id) {
        long count = deptMapper.selectCount(new LambdaQueryWrapper<SysDept>().eq(SysDept::getParentId, id));
        if (count > 0) throw new BusinessException("存在子部门，不能删除");
        long userCount = userMapper.selectCount(new LambdaQueryWrapper<SysUser>().eq(SysUser::getDeptId, id));
        if (userCount > 0) throw new BusinessException("部门下存在用户，不能删除");
        deptMapper.deleteById(id);
    }

    @Override
    public List<UserVO> getUsersByDeptId(Long deptId) {
        List<SysUser> users = userMapper.selectList(new LambdaQueryWrapper<SysUser>().eq(SysUser::getDeptId, deptId));
        return users.stream().map(u -> userService.getUserDetail(u.getId())).collect(Collectors.toList());
    }

    private List<DeptTreeVO> buildTree(List<SysDept> depts, Long parentId) {
        return depts.stream()
            .filter(d -> d.getParentId().equals(parentId))
            .map(d -> {
                DeptTreeVO vo = new DeptTreeVO();
                BeanUtils.copyProperties(d, vo);
                List<DeptTreeVO> children = buildTree(depts, d.getId());
                if (!children.isEmpty()) vo.setChildren(children);
                return vo;
            }).collect(Collectors.toList());
    }
}
EOF

cat > $BASE/service/impl/SysClassificationLevelServiceImpl.java << 'EOF'
package com.intelligent.trial.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.intelligent.trial.auth.dto.ClassificationLevelDTO;
import com.intelligent.trial.auth.entity.SysClassificationLevel;
import com.intelligent.trial.auth.mapper.SysClassificationLevelMapper;
import com.intelligent.trial.auth.mapper.SysUserMapper;
import com.intelligent.trial.auth.service.ISysClassificationLevelService;
import com.intelligent.trial.auth.vo.ClassificationAccessVO;
import com.intelligent.trial.auth.vo.ClassificationLevelVO;
import com.intelligent.trial.common.exception.BusinessException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SysClassificationLevelServiceImpl extends ServiceImpl<SysClassificationLevelMapper, SysClassificationLevel> implements ISysClassificationLevelService {

    @Autowired private SysClassificationLevelMapper levelMapper;
    @Autowired private SysUserMapper userMapper;

    @Override
    @Transactional
    public void addLevel(ClassificationLevelDTO dto) {
        LambdaQueryWrapper<SysClassificationLevel> wrapper = new LambdaQueryWrapper<SysClassificationLevel>().eq(SysClassificationLevel::getLevelCode, dto.getLevelCode());
        if (levelMapper.selectCount(wrapper) > 0) throw new BusinessException("密级编码已存在");
        SysClassificationLevel level = new SysClassificationLevel();
        BeanUtils.copyProperties(dto, level);
        if (level.getStatus() == null) level.setStatus(1);
        levelMapper.insert(level);
    }

    @Override
    public void updateLevel(ClassificationLevelDTO dto) {
        SysClassificationLevel exist = levelMapper.selectById(dto.getId());
        if (exist == null) throw new BusinessException("密级不存在");
        SysClassificationLevel level = new SysClassificationLevel();
        BeanUtils.copyProperties(dto, level);
        levelMapper.updateById(level);
    }

    @Override
    public void deleteLevel(Long id) {
        levelMapper.deleteById(id);
    }

    @Override
    public List<ClassificationLevelVO> listAll() {
        return levelMapper.selectList(new LambdaQueryWrapper<SysClassificationLevel>().orderByAsc(SysClassificationLevel::getSort))
            .stream().map(l -> {
                ClassificationLevelVO vo = new ClassificationLevelVO();
                BeanUtils.copyProperties(l, vo);
                return vo;
            }).collect(Collectors.toList());
    }

    @Override
    public ClassificationAccessVO getAccessibleLevels(Long userId) {
        int maxSort = userMapper.selectMaxClassificationLevelSortByUserId(userId);
        if (maxSort <= 0) maxSort = 5; // 默认公开

        ClassificationAccessVO vo = new ClassificationAccessVO();
        vo.setUserId(userId);
        vo.setMaxLevelSort(maxSort);

        List<ClassificationAccessVO.ClassificationLevelInfo> levels = levelMapper.selectList(
                new LambdaQueryWrapper<SysClassificationLevel>()
                    .le(SysClassificationLevel::getSort, maxSort)
                    .eq(SysClassificationLevel::getStatus, 1)
                    .orderByAsc(SysClassificationLevel::getSort))
            .stream().map(l -> {
                ClassificationAccessVO.ClassificationLevelInfo info = new ClassificationAccessVO.ClassificationLevelInfo();
                info.setId(l.getId());
                info.setLevelCode(l.getLevelCode());
                info.setLevelName(l.getLevelName());
                info.setSort(l.getSort());
                return info;
            }).collect(Collectors.toList());
        vo.setAccessibleLevels(levels);
        return vo;
    }
}
EOF

cat > $BASE/service/impl/SysAuditLogServiceImpl.java << 'EOF'
package com.intelligent.trial.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.intelligent.trial.auth.entity.SysAuditLog;
import com.intelligent.trial.auth.entity.SysUser;
import com.intelligent.trial.auth.mapper.SysAuditLogMapper;
import com.intelligent.trial.auth.mapper.SysUserMapper;
import com.intelligent.trial.auth.service.ISysAuditLogService;
import com.intelligent.trial.auth.vo.AuditLogVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SysAuditLogServiceImpl extends ServiceImpl<SysAuditLogMapper, SysAuditLog> implements ISysAuditLogService {

    @Autowired private SysAuditLogMapper logMapper;
    @Autowired private SysUserMapper userMapper;

    @Override
    public Page<AuditLogVO> pageLog(Integer pageNum, Integer pageSize, String module, String action, Long userId, String startTime, String endTime) {
        Page<SysAuditLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysAuditLog> wrapper = new LambdaQueryWrapper<>();
        if (module != null && !module.isEmpty()) wrapper.eq(SysAuditLog::getModule, module);
        if (action != null && !action.isEmpty()) wrapper.eq(SysAuditLog::getAction, action);
        if (userId != null) wrapper.eq(SysAuditLog::getUserId, userId);
        if (startTime != null && !startTime.isEmpty()) wrapper.ge(SysAuditLog::getCreateTime, startTime);
        if (endTime != null && !endTime.isEmpty()) wrapper.le(SysAuditLog::getCreateTime, endTime);
        wrapper.orderByDesc(SysAuditLog::getCreateTime);
        Page<SysAuditLog> logPage = logMapper.selectPage(page, wrapper);

        Map<Long, String> nameMap = new HashMap<>();
        for (SysAuditLog log : logPage.getRecords()) {
            if (log.getUserId() != null && !nameMap.containsKey(log.getUserId())) {
                SysUser u = userMapper.selectById(log.getUserId());
                nameMap.put(log.getUserId(), u != null ? u.getUsername() : "未知");
            }
        }

        Page<AuditLogVO> voPage = new Page<>(pageNum, pageSize, logPage.getTotal());
        voPage.setRecords(logPage.getRecords().stream().map(l -> {
            AuditLogVO vo = new AuditLogVO();
            BeanUtils.copyProperties(l, vo);
            vo.setUsername(nameMap.get(l.getUserId()));
            return vo;
        }).collect(Collectors.toList()));
        return voPage;
    }
}
EOF

echo "Phase 3 (mappers, services) done"
