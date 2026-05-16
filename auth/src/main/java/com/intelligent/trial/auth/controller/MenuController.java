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
