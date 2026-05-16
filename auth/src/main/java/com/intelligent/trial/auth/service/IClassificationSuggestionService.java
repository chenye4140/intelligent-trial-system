package com.intelligent.trial.auth.service;

import com.intelligent.trial.auth.dto.ClassificationSuggestionDTO;
import com.intelligent.trial.auth.vo.ClassificationSuggestionVO;

/**
 * 定密建议服务接口
 */
public interface IClassificationSuggestionService {

    /**
     * 获取案件定密建议（如有缓存且非强制刷新则返回缓存）
     */
    ClassificationSuggestionVO getSuggestion(Long caseId);

    /**
     * 生成定密建议（调用 AI 分析）
     */
    ClassificationSuggestionVO generateSuggestion(ClassificationSuggestionDTO dto, Long operatorId);

    /**
     * 采纳定密建议（将建议密级应用到案件）
     */
    void adoptSuggestion(Long suggestionId);
}
