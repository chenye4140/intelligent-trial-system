package com.intelligent.trial.auth.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.intelligent.trial.common.annotation.RequireLog;
import com.intelligent.trial.common.annotation.RequirePermission;
import com.intelligent.trial.auth.dto.ResetPasswordDTO;
import com.intelligent.trial.auth.dto.UserDTO;
import com.intelligent.trial.auth.service.ISysUserService;
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

@Tag(name = "用户管理", description = "用户CRUD、角色分配、密码重置等用户管理接口")
@RestController
@RequestMapping("/api/system/user")
public class UserController {

    @Autowired private ISysUserService userService;

    @Operation(summary = "分页查询用户", description = "多条件分页查询用户列表")
    @GetMapping("/page")
    @RequirePermission("system:user:list")
    public R<PageResult<UserVO>> page(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "用户名") @RequestParam(required = false) String username,
            @Parameter(description = "真实姓名") @RequestParam(required = false) String realName,
            @Parameter(description = "部门ID") @RequestParam(required = false) Long deptId,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        Page<UserVO> page = userService.pageUser(pageNum, pageSize, username, realName, deptId, status);
        return R.ok(PageResult.of(page.getTotal(), pageNum, pageSize, page.getRecords()));
    }

    @Operation(summary = "用户详情", description = "根据ID获取用户详细信息")
    @GetMapping("/{id}")
    @RequirePermission("system:user:query")
    public R<UserVO> detail(@Parameter(description = "用户ID") @PathVariable Long id) {
        return R.ok(userService.getUserDetail(id));
    }

    @Operation(summary = "新增用户", description = "创建新用户")
    @PostMapping
    @RequirePermission("system:user:add")
    @RequireLog(module = "用户管理", action = "新增", description = "新增用户")
    public R<Void> add(@Validated @RequestBody UserDTO dto) {
        userService.addUser(dto);
        return R.ok();
    }

    @Operation(summary = "编辑用户", description = "更新用户信息")
    @PutMapping
    @RequirePermission("system:user:edit")
    @RequireLog(module = "用户管理", action = "编辑", description = "编辑用户")
    public R<Void> update(@Validated @RequestBody UserDTO dto) {
        userService.updateUser(dto);
        return R.ok();
    }

    @Operation(summary = "删除用户", description = "根据ID删除用户")
    @DeleteMapping("/{id}")
    @RequirePermission("system:user:remove")
    @RequireLog(module = "用户管理", action = "删除", description = "删除用户")
    public R<Void> delete(@Parameter(description = "用户ID") @PathVariable Long id) {
        userService.deleteUser(id);
        return R.ok();
    }

    @Operation(summary = "重置密码", description = "重置指定用户的登录密码")
    @PutMapping("/reset-password")
    @RequirePermission("system:user:resetPwd")
    @RequireLog(module = "用户管理", action = "重置密码", description = "重置用户密码")
    public R<Void> resetPassword(@Validated @RequestBody ResetPasswordDTO dto) {
        userService.resetPassword(dto.getUserId(), dto.getNewPassword());
        return R.ok();
    }

    @Operation(summary = "修改用户状态", description = "启用或禁用用户")
    @PutMapping("/status/{id}")
    @RequirePermission("system:user:edit")
    @RequireLog(module = "用户管理", action = "修改状态", description = "修改用户状态")
    public R<Void> changeStatus(@Parameter(description = "用户ID") @PathVariable Long id, @Parameter(description = "状态值") @RequestParam Integer status) {
        userService.changeStatus(id, status);
        return R.ok();
    }

    @Operation(summary = "分配用户角色", description = "为用户分配角色列表")
    @PutMapping("/roles/{userId}")
    @RequirePermission("system:user:edit")
    @RequireLog(module = "用户管理", action = "分配角色", description = "分配用户角色")
    public R<Void> assignRoles(@Parameter(description = "用户ID") @PathVariable Long userId, @RequestBody @NotEmpty(message = "角色ID列表不能为空") List<Long> roleIds) {
        userService.assignRoles(userId, roleIds);
        return R.ok();
    }
}
