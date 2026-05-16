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
