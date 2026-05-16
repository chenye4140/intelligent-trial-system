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
