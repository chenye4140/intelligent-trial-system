#!/bin/bash
# Controllers + entity classes for join tables + run all scripts
BASE="/home/chenye/intelligent-trial-system/auth/src/main/java/com/intelligent/trial/auth"

########################################
# JOIN TABLE ENTITIES
########################################
cat > $BASE/entity/SysUserRole.java << 'EOF'
package com.intelligent.trial.auth.entity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

@Data
@TableName("sys_user_role")
public class SysUserRole implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long userId;
    private Long roleId;
    private Date createTime;
    private Date updateTime;
}
EOF

cat > $BASE/entity/SysRoleMenu.java << 'EOF'
package com.intelligent.trial.auth.entity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

@Data
@TableName("sys_role_menu")
public class SysRoleMenu implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long roleId;
    private Long menuId;
    private Date createTime;
    private Date updateTime;
}
EOF

########################################
# CONTROLLERS
########################################
cat > $BASE/controller/AuthController.java << 'EOF'
package com.intelligent.trial.auth.controller;

import com.intelligent.trial.auth.annotation.RequireLog;
import com.intelligent.trial.auth.dto.LoginDTO;
import com.intelligent.trial.auth.service.IAuthService;
import com.intelligent.trial.auth.vo.LoginVO;
import com.intelligent.trial.common.dto.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired private IAuthService authService;

    @PostMapping("/login")
    @RequireLog(module = "认证", action = "登录", description = "用户登录")
    public R<LoginVO> login(@Validated @RequestBody LoginDTO loginDTO) {
        return R.ok(authService.login(loginDTO));
    }

    @PostMapping("/refresh")
    public R<LoginVO> refreshToken(@RequestParam String refreshToken) {
        return R.ok(authService.refreshToken(refreshToken));
    }

    @PostMapping("/logout")
    @RequireLog(module = "认证", action = "退出", description = "用户退出登录")
    public R<Void> logout(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            authService.logout(header.substring(7));
        }
        return R.ok();
    }

    @GetMapping("/info")
    public R<LoginVO.UserInfo> getUserInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return R.ok(null);
    }
}
EOF

cat > $BASE/controller/UserController.java << 'EOF'
package com.intelligent.trial.auth.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.intelligent.trial.auth.annotation.RequireLog;
import com.intelligent.trial.auth.annotation.RequirePermission;
import com.intelligent.trial.auth.dto.ResetPasswordDTO;
import com.intelligent.trial.auth.dto.UserDTO;
import com.intelligent.trial.auth.service.ISysUserService;
import com.intelligent.trial.auth.vo.UserVO;
import com.intelligent.trial.common.dto.PageResult;
import com.intelligent.trial.common.dto.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/system/user")
public class UserController {

    @Autowired private ISysUserService userService;

    @GetMapping("/page")
    @RequirePermission("system:user:list")
    public R<PageResult<UserVO>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String realName,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) Integer status) {
        Page<UserVO> page = userService.pageUser(pageNum, pageSize, username, realName, deptId, status);
        return R.ok(PageResult.of(page.getTotal(), pageNum, pageSize, page.getRecords()));
    }

    @GetMapping("/{id}")
    @RequirePermission("system:user:query")
    public R<UserVO> detail(@PathVariable Long id) {
        return R.ok(userService.getUserDetail(id));
    }

    @PostMapping
    @RequirePermission("system:user:add")
    @RequireLog(module = "用户管理", action = "新增", description = "新增用户")
    public R<Void> add(@Validated @RequestBody UserDTO dto) {
        userService.addUser(dto);
        return R.ok();
    }

    @PutMapping
    @RequirePermission("system:user:edit")
    @RequireLog(module = "用户管理", action = "编辑", description = "编辑用户")
    public R<Void> update(@Validated @RequestBody UserDTO dto) {
        userService.updateUser(dto);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @RequirePermission("system:user:remove")
    @RequireLog(module = "用户管理", action = "删除", description = "删除用户")
    public R<Void> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return R.ok();
    }

    @PutMapping("/reset-password")
    @RequirePermission("system:user:resetPwd")
    @RequireLog(module = "用户管理", action = "重置密码", description = "重置用户密码")
    public R<Void> resetPassword(@Validated @RequestBody ResetPasswordDTO dto) {
        userService.resetPassword(dto.getUserId(), dto.getNewPassword());
        return R.ok();
    }

    @PutMapping("/status/{id}")
    @RequirePermission("system:user:edit")
    @RequireLog(module = "用户管理", action = "修改状态", description = "修改用户状态")
    public R<Void> changeStatus(@PathVariable Long id, @RequestParam Integer status) {
        userService.changeStatus(id, status);
        return R.ok();
    }

    @PutMapping("/roles/{userId}")
    @RequirePermission("system:user:edit")
    @RequireLog(module = "用户管理", action = "分配角色", description = "分配用户角色")
    public R<Void> assignRoles(@PathVariable Long userId, @RequestBody List<Long> roleIds) {
        userService.assignRoles(userId, roleIds);
        return R.ok();
    }
}
EOF

cat > $BASE/controller/RoleController.java << 'EOF'
package com.intelligent.trial.auth.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.intelligent.trial.auth.annotation.RequireLog;
import com.intelligent.trial.auth.annotation.RequirePermission;
import com.intelligent.trial.auth.dto.RoleDTO;
import com.intelligent.trial.auth.service.ISysRoleService;
import com.intelligent.trial.auth.vo.RoleVO;
import com.intelligent.trial.auth.vo.UserVO;
import com.intelligent.trial.common.dto.PageResult;
import com.intelligent.trial.common.dto.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/system/role")
public class RoleController {

    @Autowired private ISysRoleService roleService;

    @GetMapping("/page")
    @RequirePermission("system:role:list")
    public R<PageResult<RoleVO>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String roleName,
            @RequestParam(required = false) String roleCode,
            @RequestParam(required = false) Integer status) {
        Page<RoleVO> page = roleService.pageRole(pageNum, pageSize, roleName, roleCode, status);
        return R.ok(PageResult.of(page.getTotal(), pageNum, pageSize, page.getRecords()));
    }

    @GetMapping("/{id}")
    @RequirePermission("system:role:query")
    public R<RoleVO> detail(@PathVariable Long id) {
        RoleVO vo = new RoleVO();
        org.springframework.beans.BeanUtils.copyProperties(roleService.getById(id), vo);
        vo.setMenuIds(roleService.getMenuIdsByRoleId(id));
        return R.ok(vo);
    }

    @PostMapping
    @RequirePermission("system:role:add")
    @RequireLog(module = "角色管理", action = "新增", description = "新增角色")
    public R<Void> add(@Validated @RequestBody RoleDTO dto) {
        roleService.addRole(dto);
        return R.ok();
    }

    @PutMapping
    @RequirePermission("system:role:edit")
    @RequireLog(module = "角色管理", action = "编辑", description = "编辑角色")
    public R<Void> update(@Validated @RequestBody RoleDTO dto) {
        roleService.updateRole(dto);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @RequirePermission("system:role:remove")
    @RequireLog(module = "角色管理", action = "删除", description = "删除角色")
    public R<Void> delete(@PathVariable Long id) {
        roleService.deleteRole(id);
        return R.ok();
    }

    @PutMapping("/menus/{roleId}")
    @RequirePermission("system:role:edit")
    @RequireLog(module = "角色管理", action = "分配权限", description = "为角色分配菜单权限")
    public R<Void> assignMenus(@PathVariable Long roleId, @RequestBody List<Long> menuIds) {
        roleService.assignMenus(roleId, menuIds);
        return R.ok();
    }

    @GetMapping("/{roleId}/users")
    @RequirePermission("system:role:query")
    public R<List<UserVO>> getUsers(@PathVariable Long roleId) {
        return R.ok(roleService.getUsersByRoleId(roleId));
    }
}
EOF

cat > $BASE/controller/MenuController.java << 'EOF'
package com.intelligent.trial.auth.controller;

import com.intelligent.trial.auth.annotation.RequireLog;
import com.intelligent.trial.auth.annotation.RequirePermission;
import com.intelligent.trial.auth.dto.MenuDTO;
import com.intelligent.trial.auth.service.ISysMenuService;
import com.intelligent.trial.auth.vo.MenuTreeVO;
import com.intelligent.trial.common.dto.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/system/menu")
public class MenuController {

    @Autowired private ISysMenuService menuService;

    @GetMapping("/tree")
    @RequirePermission("system:menu:list")
    public R<List<MenuTreeVO>> tree() {
        return R.ok(menuService.getMenuTree());
    }

    @GetMapping("/user-tree")
    public R<List<MenuTreeVO>> userTree(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return R.ok(menuService.getMenuTreeByUserId(userId));
    }

    @GetMapping("/role-tree/{roleId}")
    @RequirePermission("system:menu:list")
    public R<List<MenuTreeVO>> roleTree(@PathVariable Long roleId) {
        return R.ok(menuService.getMenuTreeByRoleId(roleId));
    }

    @PostMapping
    @RequirePermission("system:menu:add")
    @RequireLog(module = "菜单管理", action = "新增", description = "新增菜单")
    public R<Void> add(@Validated @RequestBody MenuDTO dto) {
        menuService.addMenu(dto);
        return R.ok();
    }

    @PutMapping
    @RequirePermission("system:menu:edit")
    @RequireLog(module = "菜单管理", action = "编辑", description = "编辑菜单")
    public R<Void> update(@Validated @RequestBody MenuDTO dto) {
        menuService.updateMenu(dto);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @RequirePermission("system:menu:remove")
    @RequireLog(module = "菜单管理", action = "删除", description = "删除菜单")
    public R<Void> delete(@PathVariable Long id) {
        menuService.deleteMenu(id);
        return R.ok();
    }
}
EOF

cat > $BASE/controller/DeptController.java << 'EOF'
package com.intelligent.trial.auth.controller;

import com.intelligent.trial.auth.annotation.RequireLog;
import com.intelligent.trial.auth.annotation.RequirePermission;
import com.intelligent.trial.auth.dto.DeptDTO;
import com.intelligent.trial.auth.service.ISysDeptService;
import com.intelligent.trial.auth.vo.DeptTreeVO;
import com.intelligent.trial.auth.vo.UserVO;
import com.intelligent.trial.common.dto.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/system/dept")
public class DeptController {

    @Autowired private ISysDeptService deptService;

    @GetMapping("/tree")
    @RequirePermission("system:dept:list")
    public R<List<DeptTreeVO>> tree() {
        return R.ok(deptService.getDeptTree());
    }

    @PostMapping
    @RequirePermission("system:dept:add")
    @RequireLog(module = "部门管理", action = "新增", description = "新增部门")
    public R<Void> add(@Validated @RequestBody DeptDTO dto) {
        deptService.addDept(dto);
        return R.ok();
    }

    @PutMapping
    @RequirePermission("system:dept:edit")
    @RequireLog(module = "部门管理", action = "编辑", description = "编辑部门")
    public R<Void> update(@Validated @RequestBody DeptDTO dto) {
        deptService.updateDept(dto);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @RequirePermission("system:dept:remove")
    @RequireLog(module = "部门管理", action = "删除", description = "删除部门")
    public R<Void> delete(@PathVariable Long id) {
        deptService.deleteDept(id);
        return R.ok();
    }

    @GetMapping("/{deptId}/users")
    @RequirePermission("system:dept:query")
    public R<List<UserVO>> getUsers(@PathVariable Long deptId) {
        return R.ok(deptService.getUsersByDeptId(deptId));
    }
}
EOF

cat > $BASE/controller/ClassificationController.java << 'EOF'
package com.intelligent.trial.auth.controller;

import com.intelligent.trial.auth.annotation.RequireLog;
import com.intelligent.trial.auth.annotation.RequirePermission;
import com.intelligent.trial.auth.dto.ClassificationLevelDTO;
import com.intelligent.trial.auth.service.ISysClassificationLevelService;
import com.intelligent.trial.auth.vo.ClassificationAccessVO;
import com.intelligent.trial.auth.vo.ClassificationLevelVO;
import com.intelligent.trial.common.dto.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/system/classification")
public class ClassificationController {

    @Autowired private ISysClassificationLevelService levelService;

    @GetMapping("/list")
    @RequirePermission("system:classification:list")
    public R<List<ClassificationLevelVO>> list() {
        return R.ok(levelService.listAll());
    }

    @GetMapping("/accessible")
    public R<ClassificationAccessVO> accessible(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return R.ok(levelService.getAccessibleLevels(userId));
    }

    @PostMapping
    @RequirePermission("system:classification:add")
    @RequireLog(module = "定密管理", action = "新增", description = "新增密级")
    public R<Void> add(@Validated @RequestBody ClassificationLevelDTO dto) {
        levelService.addLevel(dto);
        return R.ok();
    }

    @PutMapping
    @RequirePermission("system:classification:edit")
    @RequireLog(module = "定密管理", action = "编辑", description = "编辑密级")
    public R<Void> update(@Validated @RequestBody ClassificationLevelDTO dto) {
        levelService.updateLevel(dto);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @RequirePermission("system:classification:remove")
    @RequireLog(module = "定密管理", action = "删除", description = "删除密级")
    public R<Void> delete(@PathVariable Long id) {
        levelService.deleteLevel(id);
        return R.ok();
    }
}
EOF

cat > $BASE/controller/AuditLogController.java << 'EOF'
package com.intelligent.trial.auth.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.intelligent.trial.auth.annotation.RequirePermission;
import com.intelligent.trial.auth.service.ISysAuditLogService;
import com.intelligent.trial.auth.vo.AuditLogVO;
import com.intelligent.trial.common.dto.PageResult;
import com.intelligent.trial.common.dto.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/system/audit-log")
public class AuditLogController {

    @Autowired private ISysAuditLogService logService;

    @GetMapping("/page")
    @RequirePermission("system:audit:list")
    public R<PageResult<AuditLogVO>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        Page<AuditLogVO> page = logService.pageLog(pageNum, pageSize, module, action, userId, startTime, endTime);
        return R.ok(PageResult.of(page.getTotal(), pageNum, pageSize, page.getRecords()));
    }
}
EOF

# Run previous generation scripts
cd /home/chenye/intelligent-trial-system/auth
bash generate-rbac.sh 2>/dev/null
bash generate-all.sh 2>/dev/null

echo "ALL FILES GENERATED"
