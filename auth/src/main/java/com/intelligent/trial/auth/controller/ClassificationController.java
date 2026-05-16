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
