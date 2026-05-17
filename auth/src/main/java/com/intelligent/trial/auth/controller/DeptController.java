package com.intelligent.trial.auth.controller;

import com.intelligent.trial.common.annotation.RequireLog;
import com.intelligent.trial.auth.annotation.RequirePermission;
import com.intelligent.trial.auth.dto.DeptDTO;
import com.intelligent.trial.auth.service.ISysDeptService;
import com.intelligent.trial.auth.vo.DeptTreeVO;
import com.intelligent.trial.auth.vo.UserVO;
import com.intelligent.trial.common.dto.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "部门管理", description = "部门树CRUD等部门管理接口")
@RestController
@RequestMapping("/api/system/dept")
public class DeptController {

    @Autowired private ISysDeptService deptService;

    @Operation(summary = "获取部门树", description = "获取完整的部门树形结构")
    @GetMapping("/tree")
    @RequirePermission("system:dept:list")
    public R<List<DeptTreeVO>> tree() {
        return R.ok(deptService.getDeptTree());
    }

    @Operation(summary = "新增部门", description = "创建新部门")
    @PostMapping
    @RequirePermission("system:dept:add")
    @RequireLog(module = "部门管理", action = "新增", description = "新增部门")
    public R<Void> add(@Validated @RequestBody DeptDTO dto) {
        deptService.addDept(dto);
        return R.ok();
    }

    @Operation(summary = "编辑部门", description = "更新部门信息")
    @PutMapping
    @RequirePermission("system:dept:edit")
    @RequireLog(module = "部门管理", action = "编辑", description = "编辑部门")
    public R<Void> update(@Validated @RequestBody DeptDTO dto) {
        deptService.updateDept(dto);
        return R.ok();
    }

    @Operation(summary = "删除部门", description = "根据ID删除部门")
    @DeleteMapping("/{id}")
    @RequirePermission("system:dept:remove")
    @RequireLog(module = "部门管理", action = "删除", description = "删除部门")
    public R<Void> delete(@Parameter(description = "部门ID") @PathVariable Long id) {
        deptService.deleteDept(id);
        return R.ok();
    }

    @Operation(summary = "获取部门下的用户", description = "查询指定部门关联的所有用户")
    @GetMapping("/{deptId}/users")
    @RequirePermission("system:dept:query")
    public R<List<UserVO>> getUsers(@Parameter(description = "部门ID") @PathVariable Long deptId) {
        return R.ok(deptService.getUsersByDeptId(deptId));
    }
}
