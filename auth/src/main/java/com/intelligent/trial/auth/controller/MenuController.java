package com.intelligent.trial.auth.controller;

import com.intelligent.trial.common.annotation.RequireLog;
import com.intelligent.trial.auth.annotation.RequirePermission;
import com.intelligent.trial.auth.dto.MenuDTO;
import com.intelligent.trial.auth.service.ISysMenuService;
import com.intelligent.trial.auth.vo.MenuTreeVO;
import com.intelligent.trial.common.dto.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Tag(name = "菜单管理", description = "菜单树构建、菜单CRUD等菜单管理接口")
@RestController
@RequestMapping("/api/system/menu")
public class MenuController {

    @Autowired private ISysMenuService menuService;

    @Operation(summary = "获取菜单树", description = "获取完整的菜单树形结构")
    @GetMapping("/tree")
    @RequirePermission("system:menu:list")
    public R<List<MenuTreeVO>> tree() {
        return R.ok(menuService.getMenuTree());
    }

    @Operation(summary = "获取当前用户菜单树", description = "根据当前用户权限获取可见的菜单树")
    @GetMapping("/user-tree")
    public R<List<MenuTreeVO>> userTree(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return R.ok(menuService.getMenuTreeByUserId(userId));
    }

    @Operation(summary = "获取角色菜单树", description = "获取指定角色关联的菜单树")
    @GetMapping("/role-tree/{roleId}")
    @RequirePermission("system:menu:list")
    public R<List<MenuTreeVO>> roleTree(@Parameter(description = "角色ID") @PathVariable Long roleId) {
        return R.ok(menuService.getMenuTreeByRoleId(roleId));
    }

    @Operation(summary = "新增菜单", description = "创建新菜单")
    @PostMapping
    @RequirePermission("system:menu:add")
    @RequireLog(module = "菜单管理", action = "新增", description = "新增菜单")
    public R<Void> add(@Validated @RequestBody MenuDTO dto) {
        menuService.addMenu(dto);
        return R.ok();
    }

    @Operation(summary = "编辑菜单", description = "更新菜单信息")
    @PutMapping
    @RequirePermission("system:menu:edit")
    @RequireLog(module = "菜单管理", action = "编辑", description = "编辑菜单")
    public R<Void> update(@Validated @RequestBody MenuDTO dto) {
        menuService.updateMenu(dto);
        return R.ok();
    }

    @Operation(summary = "删除菜单", description = "根据ID删除菜单")
    @DeleteMapping("/{id}")
    @RequirePermission("system:menu:remove")
    @RequireLog(module = "菜单管理", action = "删除", description = "删除菜单")
    public R<Void> delete(@Parameter(description = "菜单ID") @PathVariable Long id) {
        menuService.deleteMenu(id);
        return R.ok();
    }
}
