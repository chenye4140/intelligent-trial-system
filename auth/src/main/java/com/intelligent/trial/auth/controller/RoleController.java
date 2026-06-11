package com.intelligent.trial.auth.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.intelligent.trial.common.annotation.RequireLog;
import com.intelligent.trial.auth.annotation.RequirePermission;
import com.intelligent.trial.auth.dto.RoleDTO;
import com.intelligent.trial.auth.service.ISysRoleService;
import com.intelligent.trial.auth.vo.RoleVO;
import com.intelligent.trial.auth.vo.UserVO;
import com.intelligent.trial.common.dto.PageResult;
import com.intelligent.trial.common.dto.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Tag(name = "角色管理", description = "角色CRUD、菜单权限分配等角色管理接口")
@RestController
@RequestMapping("/api/system/role")
public class RoleController {

    @Autowired private ISysRoleService roleService;

    @Operation(summary = "分页查询角色", description = "多条件分页查询角色列表")
    @GetMapping("/page")
    @RequirePermission("system:role:list")
    public R<PageResult<RoleVO>> page(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "角色名称") @RequestParam(required = false) String roleName,
            @Parameter(description = "角色编码") @RequestParam(required = false) String roleCode,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        Page<RoleVO> page = roleService.pageRole(pageNum, pageSize, roleName, roleCode, status);
        return R.ok(PageResult.of(page.getTotal(), pageNum, pageSize, page.getRecords()));
    }

    @Operation(summary = "角色详情", description = "根据ID获取角色详细信息及关联菜单")
    @GetMapping("/{id}")
    @RequirePermission("system:role:query")
    public R<RoleVO> detail(@Parameter(description = "角色ID") @PathVariable Long id) {
        RoleVO vo = new RoleVO();
        org.springframework.beans.BeanUtils.copyProperties(roleService.getById(id), vo);
        vo.setMenuIds(roleService.getMenuIdsByRoleId(id));
        return R.ok(vo);
    }

    @Operation(summary = "新增角色", description = "创建新角色")
    @PostMapping
    @RequirePermission("system:role:add")
    @RequireLog(module = "角色管理", action = "新增", description = "新增角色")
    public R<Void> add(@Validated @RequestBody RoleDTO dto) {
        roleService.addRole(dto);
        return R.ok();
    }

    @Operation(summary = "编辑角色", description = "更新角色信息")
    @PutMapping
    @RequirePermission("system:role:edit")
    @RequireLog(module = "角色管理", action = "编辑", description = "编辑角色")
    public R<Void> update(@Validated @RequestBody RoleDTO dto) {
        roleService.updateRole(dto);
        return R.ok();
    }

    @Operation(summary = "删除角色", description = "根据ID删除角色")
    @DeleteMapping("/{id}")
    @RequirePermission("system:role:remove")
    @RequireLog(module = "角色管理", action = "删除", description = "删除角色")
    public R<Void> delete(@Parameter(description = "角色ID") @PathVariable Long id) {
        roleService.deleteRole(id);
        return R.ok();
    }

    @Operation(summary = "分配菜单权限", description = "为角色分配菜单权限")
    @PutMapping("/menus/{roleId}")
    @RequirePermission("system:role:edit")
    @RequireLog(module = "角色管理", action = "分配权限", description = "为角色分配菜单权限")
    public R<Void> assignMenus(@Parameter(description = "角色ID") @PathVariable Long roleId, @RequestBody @NotEmpty(message = "菜单ID列表不能为空") List<Long> menuIds) {
        roleService.assignMenus(roleId, menuIds);
        return R.ok();
    }

    @Operation(summary = "获取角色下的用户", description = "查询指定角色关联的所有用户")
    @GetMapping("/{roleId}/users")
    @RequirePermission("system:role:query")
    public R<List<UserVO>> getUsers(@Parameter(description = "角色ID") @PathVariable Long roleId) {
        return R.ok(roleService.getUsersByRoleId(roleId));
    }
}
