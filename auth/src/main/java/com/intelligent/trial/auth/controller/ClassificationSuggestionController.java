package com.intelligent.trial.auth.controller;

import com.intelligent.trial.common.annotation.RequireLog;
import com.intelligent.trial.auth.annotation.RequirePermission;
import com.intelligent.trial.auth.dto.ClassificationSuggestionDTO;
import com.intelligent.trial.auth.service.IClassificationSuggestionService;
import com.intelligent.trial.auth.vo.ClassificationSuggestionVO;
import com.intelligent.trial.common.dto.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 定密建议控制器
 */
@Tag(name = "定密建议", description = "AI五级定密建议生成、查看、采纳等接口")
@RestController
@RequestMapping("/api/system/classification/suggestion")
public class ClassificationSuggestionController {

    @Autowired
    private IClassificationSuggestionService suggestionService;

    /**
     * 获取案件定密建议
     */
    @Operation(summary = "获取定密建议", description = "获取指定案件的AI定密建议")
    @GetMapping("/{caseId}")
    @RequirePermission("system:classification:suggestion:view")
    public R<ClassificationSuggestionVO> getSuggestion(@Parameter(description = "案件ID") @PathVariable Long caseId) {
        ClassificationSuggestionVO vo = suggestionService.getSuggestion(caseId);
        return R.ok(vo);
    }

    /**
     * 生成/重新生成定密建议
     */
    @Operation(summary = "生成定密建议", description = "基于案件信息调用AI生成五级定密建议")
    @RequirePermission("system:classification:suggestion:generate")
    @RequireLog(module = "定密建议", action = "生成", description = "生成案件定密建议")
    @PostMapping("/generate")
    public R<ClassificationSuggestionVO> generateSuggestion(
            @Validated @RequestBody ClassificationSuggestionDTO dto,
            HttpServletRequest request) {
        Long operatorId = (Long) request.getAttribute("userId");
        ClassificationSuggestionVO vo = suggestionService.generateSuggestion(dto, operatorId);
        return R.ok(vo);
    }

    /**
     * 采纳定密建议
     */
    @Operation(summary = "采纳定密建议", description = "将定密建议标记为已采纳")
    @RequirePermission("system:classification:suggestion:adopt")
    @RequireLog(module = "定密建议", action = "采纳", description = "采纳定密建议")
    @PutMapping("/adopt/{suggestionId}")
    public R<Void> adoptSuggestion(@Parameter(description = "建议ID") @PathVariable Long suggestionId) {
        suggestionService.adoptSuggestion(suggestionId);
        return R.ok();
    }
}
