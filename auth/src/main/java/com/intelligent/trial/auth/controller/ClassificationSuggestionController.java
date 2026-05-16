package com.intelligent.trial.auth.controller;

import com.intelligent.trial.auth.annotation.RequireLog;
import com.intelligent.trial.auth.annotation.RequirePermission;
import com.intelligent.trial.auth.dto.ClassificationSuggestionDTO;
import com.intelligent.trial.auth.service.IClassificationSuggestionService;
import com.intelligent.trial.auth.vo.ClassificationSuggestionVO;
import com.intelligent.trial.common.dto.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 定密建议控制器
 */
@RestController
@RequestMapping("/api/system/classification/suggestion")
public class ClassificationSuggestionController {

    @Autowired
    private IClassificationSuggestionService suggestionService;

    /**
     * 获取案件定密建议
     */
    @GetMapping("/{caseId}")
    @RequirePermission("system:classification:suggestion:view")
    public R<ClassificationSuggestionVO> getSuggestion(@PathVariable Long caseId) {
        ClassificationSuggestionVO vo = suggestionService.getSuggestion(caseId);
        return R.ok(vo);
    }

    /**
     * 生成/重新生成定密建议
     */
    @PostMapping("/generate")
    @RequirePermission("system:classification:suggestion:generate")
    @RequireLog(module = "定密建议", action = "生成", description = "生成案件定密建议")
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
    @PutMapping("/adopt/{suggestionId}")
    @RequirePermission("system:classification:suggestion:adopt")
    @RequireLog(module = "定密建议", action = "采纳", description = "采纳定密建议")
    public R<Void> adoptSuggestion(@PathVariable Long suggestionId) {
        suggestionService.adoptSuggestion(suggestionId);
        return R.ok();
    }
}
