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
@RequestMapping("/api/system/user")
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
