package com.intelligent.trial.auth.controller;

import com.intelligent.trial.common.annotation.RequireLog;
import com.intelligent.trial.auth.annotation.RequirePermission;
import com.intelligent.trial.auth.dto.ClassificationLevelDTO;
import com.intelligent.trial.auth.service.ISysClassificationLevelService;
import com.intelligent.trial.auth.vo.ClassificationAccessVO;
import com.intelligent.trial.auth.vo.ClassificationLevelVO;
import com.intelligent.trial.common.dto.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Tag(name = "密级管理", description = "密级字典CRUD等密级管理接口")
@RestController
@RequestMapping("/api/system/classification")
public class ClassificationController {

    @Autowired private ISysClassificationLevelService levelService;

    @Operation(summary = "获取密级列表", description = "获取所有密级字典")
    @GetMapping("/list")
    @RequirePermission("system:classification:list")
    public R<List<ClassificationLevelVO>> list() {
        return R.ok(levelService.listAll());
    }

    @Operation(summary = "获取可访问密级", description = "获取当前用户有权访问的密级范围")
    @GetMapping("/accessible")
    public R<ClassificationAccessVO> accessible(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return R.ok(levelService.getAccessibleLevels(userId));
    }

    @Operation(summary = "新增密级", description = "创建新的密级字典项")
    @PostMapping
    @RequirePermission("system:classification:add")
    @RequireLog(module = "定密管理", action = "新增", description = "新增密级")
    public R<Void> add(@Validated @RequestBody ClassificationLevelDTO dto) {
        levelService.addLevel(dto);
        return R.ok();
    }

    @Operation(summary = "编辑密级", description = "更新密级字典项信息")
    @PutMapping
    @RequirePermission("system:classification:edit")
    @RequireLog(module = "定密管理", action = "编辑", description = "编辑密级")
    public R<Void> update(@Validated @RequestBody ClassificationLevelDTO dto) {
        levelService.updateLevel(dto);
        return R.ok();
    }

    @Operation(summary = "删除密级", description = "根据ID删除密级字典项")
    @DeleteMapping("/{id}")
    @RequirePermission("system:classification:remove")
    @RequireLog(module = "定密管理", action = "删除", description = "删除密级")
    public R<Void> delete(@Parameter(description = "密级ID") @PathVariable Long id) {
        levelService.deleteLevel(id);
        return R.ok();
    }
}
