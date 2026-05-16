package com.intelligent.trial.auth.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.intelligent.trial.auth.client.DeepSeekClient;
import com.intelligent.trial.auth.dto.ClassificationSuggestionDTO;
import com.intelligent.trial.auth.entity.ClassificationSuggestion;
import com.intelligent.trial.auth.entity.SysClassificationLevel;
import com.intelligent.trial.auth.mapper.ClassificationSuggestionMapper;
import com.intelligent.trial.auth.mapper.SysClassificationLevelMapper;
import com.intelligent.trial.auth.service.IClassificationSuggestionService;
import com.intelligent.trial.auth.vo.ClassificationSuggestionVO;
import com.intelligent.trial.common.exception.BusinessException;
import com.intelligent.trial.common.exception.ErrorCode;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 定密建议服务实现
 */
@Service
public class ClassificationSuggestionServiceImpl implements IClassificationSuggestionService {

    private static final Logger log = LoggerFactory.getLogger(ClassificationSuggestionServiceImpl.class);

    @Autowired
    private ClassificationSuggestionMapper suggestionMapper;

    @Autowired
    private SysClassificationLevelMapper levelMapper;

    @Autowired
    private DeepSeekClient deepSeekClient;

    @Override
    public ClassificationSuggestionVO getSuggestion(Long caseId) {
        // 查询最新的定密建议
        LambdaQueryWrapper<ClassificationSuggestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ClassificationSuggestion::getCaseId, caseId);
        wrapper.orderByDesc(ClassificationSuggestion::getCreateTime);
        wrapper.last("LIMIT 1");
        ClassificationSuggestion suggestion = suggestionMapper.selectOne(wrapper);

        if (suggestion == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "该案件暂无定密建议，请先生成");
        }

        return convertToVO(suggestion);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ClassificationSuggestionVO generateSuggestion(ClassificationSuggestionDTO dto, Long operatorId) {
        Long caseId = dto.getCaseId();

        // 如果非强制刷新且已有建议，直接返回
        if (!Boolean.TRUE.equals(dto.getForceRefresh())) {
            LambdaQueryWrapper<ClassificationSuggestion> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ClassificationSuggestion::getCaseId, caseId);
            wrapper.eq(ClassificationSuggestion::getAdopted, 0);
            wrapper.orderByDesc(ClassificationSuggestion::getCreateTime);
            wrapper.last("LIMIT 1");
            ClassificationSuggestion existing = suggestionMapper.selectOne(wrapper);
            if (existing != null) {
                log.info("案件 {} 已有未采纳的定密建议，直接返回", caseId);
                return convertToVO(existing);
            }
        }

        // 1. 查询案件基本信息
        CaseInfoDTO caseInfo = getCaseInfo(caseId);
        if (caseInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "案件不存在，ID: " + caseId);
        }

        // 2. 查询违纪事实
        List<CaseViolationFactDTO> facts = getViolationFacts(caseId);

        // 3. 构建 AI 提示词
        String systemPrompt = "你是一名资深的纪检监察案件审理专家。请根据以下案件信息，分析并建议该案件的合适密级。";

        String userPrompt = buildAnalysisPrompt(caseInfo, facts);

        // 4. 调用 AI 分析
        String aiResponse;
        try {
            aiResponse = deepSeekClient.generateContent(systemPrompt, userPrompt);
        } catch (Exception e) {
            log.error("调用 DeepSeek API 失败，案件ID: {}", caseId, e);
            throw new BusinessException(ErrorCode.BUSINESS_ERROR.getCode(),
                    "AI 分析服务暂时不可用: " + e.getMessage());
        }

        // 5. 解析 AI 返回结果
        ClassificationSuggestion suggestion = parseAiResponse(aiResponse, caseId, caseInfo.getCaseName(), operatorId);

        // 6. 保存建议
        suggestion.setCreateTime(new Date());
        suggestion.setUpdateTime(new Date());
        suggestionMapper.insert(suggestion);

        log.info("定密建议生成成功，案件ID: {}, 建议密级: {}, 置信度: {}",
                caseId, suggestion.getSuggestedLevelName(), suggestion.getConfidence());

        return convertToVO(suggestion);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adoptSuggestion(Long suggestionId) {
        ClassificationSuggestion suggestion = suggestionMapper.selectById(suggestionId);
        if (suggestion == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "定密建议不存在");
        }

        if (suggestion.getAdopted() != null && suggestion.getAdopted() == 1) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR.getCode(), "该建议已被采纳");
        }

        // 标记为已采纳
        suggestion.setAdopted(1);
        suggestion.setUpdateTime(new Date());
        suggestionMapper.updateById(suggestion);

        // 将建议密级应用到案件（通过直接 SQL 更新 case_info 表）
        updateCaseClassification(suggestion.getCaseId(), suggestion.getSuggestedLevelId());

        log.info("定密建议已采纳，建议ID: {}, 案件ID: {}, 密级: {}",
                suggestionId, suggestion.getCaseId(), suggestion.getSuggestedLevelName());
    }

    // ======================== 内部方法 ========================

    /**
     * 构建 AI 分析提示词
     */
    private String buildAnalysisPrompt(CaseInfoDTO caseInfo, List<CaseViolationFactDTO> facts) {
        StringBuilder sb = new StringBuilder();
        sb.append("五级密级体系（从高到低）：\n");
        sb.append("1. 绝密：涉及国家安全、重大外交机密、核心反腐战略的案件\n");
        sb.append("2. 机密：涉及省部级以上领导干部、重大经济犯罪（涉案金额500万以上）、群体性事件的案件\n");
        sb.append("3. 秘密：涉及厅局级干部、一般经济犯罪（涉案金额50-500万）、可能造成较大社会影响的案件\n");
        sb.append("4. 内部：涉及县处级及以下干部、轻微违纪违规、影响范围有限的案件\n");
        sb.append("5. 公开：不涉及个人隐私、可以对外公示的一般性案件\n\n");

        sb.append("【案件信息】\n");
        sb.append("案件名称：").append(nvl(caseInfo.getCaseName())).append("\n");

        String caseTypeDesc = "";
        if (caseInfo.getCaseType() != null) {
            if (caseInfo.getCaseType() == 1) caseTypeDesc = "违纪";
            else if (caseInfo.getCaseType() == 2) caseTypeDesc = "违法";
            else if (caseInfo.getCaseType() == 3) caseTypeDesc = "职务犯罪";
        }
        sb.append("案件类型：").append(nvl(caseTypeDesc)).append("\n");
        sb.append("被调查人：").append(nvl(caseInfo.getRespondentName())).append("\n");
        sb.append("所在单位：").append(nvl(caseInfo.getRespondentDept())).append("\n");
        sb.append("职务：").append(nvl(caseInfo.getRespondentPosition())).append("\n");
        sb.append("简要案情：").append(nvl(caseInfo.getBriefDescription())).append("\n\n");

        sb.append("【违纪事实】\n");
        if (facts == null || facts.isEmpty()) {
            sb.append("暂无违纪事实记录\n");
        } else {
            for (int i = 0; i < facts.size(); i++) {
                CaseViolationFactDTO f = facts.get(i);
                sb.append(String.format("%d. [%s] %s\n", i + 1, nvl(f.getViolationType()), nvl(f.getFactTitle())));
                sb.append("   内容：").append(nvl(f.getFactContent())).append("\n");
                if (f.getAmount() != null) {
                    sb.append("   涉及金额：").append(f.getAmount()).append("元\n");
                }
                sb.append("\n");
            }
        }

        sb.append("请以 JSON 格式返回分析结果（不要包含其他内容）：\n");
        sb.append("{\n");
        sb.append("  \"suggestedLevel\": \"建议密级名称（绝密/机密/秘密/内部/公开）\",\n");
        sb.append("  \"confidence\": 置信度（0-100的整数）,\n");
        sb.append("  \"reason\": \"详细分析理由（200字以内）\",\n");
        sb.append("  \"referencedRegulations\": \"参考的法规条款（如《中国共产党纪律处分条例》第X条）\"\n");
        sb.append("}");

        return sb.toString();
    }

    /**
     * 解析 AI 返回的 JSON 结果
     */
    private ClassificationSuggestion parseAiResponse(String aiResponse, Long caseId, String caseName, Long operatorId) {
        ClassificationSuggestion suggestion = new ClassificationSuggestion();
        suggestion.setCaseId(caseId);
        suggestion.setOperatorId(operatorId);
        suggestion.setAdopted(0);

        try {
            // 尝试从响应中提取 JSON（可能包含 markdown 代码块）
            String jsonStr = extractJson(aiResponse);
            JSONObject json = JSON.parseObject(jsonStr);

            String suggestedLevelName = json.getString("suggestedLevel");
            Integer confidence = json.getInteger("confidence");
            String reason = json.getString("reason");
            String referencedRegulations = json.getString("referencedRegulations");

            // 根据密级名称查找对应的密级ID
            Long levelId = findLevelIdByName(suggestedLevelName);

            suggestion.setSuggestedLevelId(levelId);
            suggestion.setSuggestedLevelName(suggestedLevelName);
            suggestion.setConfidence(confidence != null ? confidence : 50);
            suggestion.setReason(truncate(reason, 2000));
            suggestion.setReferencedRegulations(truncate(referencedRegulations, 2000));

        } catch (Exception e) {
            log.error("解析 AI 响应失败，原始响应: {}", aiResponse, e);
            // 降级处理：保存原始响应作为理由
            suggestion.setSuggestedLevelName("内部");
            suggestion.setConfidence(30);
            suggestion.setReason("AI 分析结果解析异常，原始响应: " + truncate(aiResponse, 1000));
            suggestion.setReferencedRegulations("解析失败");

            // 尝试设置默认密级ID（内部=4）
            Long defaultLevelId = findLevelIdByName("内部");
            suggestion.setSuggestedLevelId(defaultLevelId);
        }

        return suggestion;
    }

    /**
     * 从文本中提取 JSON 字符串（处理可能的 markdown 格式）
     */
    private String extractJson(String text) {
        if (text == null) return "{}";

        // 尝试查找 JSON 代码块
        int start = text.indexOf("```json");
        if (start >= 0) {
            start += 7; // "```json".length()
            int end = text.indexOf("```", start);
            if (end > start) {
                return text.substring(start, end).trim();
            }
        }

        // 尝试查找纯 JSON 块
        start = text.indexOf("```");
        if (start >= 0) {
            start += 3;
            int end = text.indexOf("```", start);
            if (end > start) {
                return text.substring(start, end).trim();
            }
        }

        // 查找第一个 { 和最后一个 }
        int firstBrace = text.indexOf("{");
        int lastBrace = text.lastIndexOf("}");
        if (firstBrace >= 0 && lastBrace > firstBrace) {
            return text.substring(firstBrace, lastBrace + 1);
        }

        return text;
    }

    /**
     * 根据密级名称查找密级ID
     */
    private Long findLevelIdByName(String levelName) {
        if (levelName == null || levelName.isEmpty()) {
            return null;
        }
        LambdaQueryWrapper<SysClassificationLevel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysClassificationLevel::getLevelName, levelName);
        wrapper.eq(SysClassificationLevel::getStatus, 1);
        wrapper.last("LIMIT 1");
        SysClassificationLevel level = levelMapper.selectOne(wrapper);
        return level != null ? level.getId() : null;
    }

    /**
     * 更新案件密级
     */
    private void updateCaseClassification(Long caseId, Long levelId) {
        try {
            caseInfoMapper.updateClassificationLevel(caseId, levelId);
        } catch (Exception e) {
            log.error("更新案件密级失败，案件ID: {}, 密级ID: {}", caseId, levelId, e);
            throw new BusinessException(ErrorCode.BUSINESS_ERROR.getCode(),
                    "更新案件密级失败: " + e.getMessage());
        }
    }

    /**
     * 转换为 VO
     */
    private ClassificationSuggestionVO convertToVO(ClassificationSuggestion entity) {
        ClassificationSuggestionVO vo = new ClassificationSuggestionVO();
        vo.setId(entity.getId());
        vo.setCaseId(entity.getCaseId());
        vo.setSuggestedLevelId(entity.getSuggestedLevelId());
        vo.setSuggestedLevelName(entity.getSuggestedLevelName());
        vo.setConfidence(entity.getConfidence());
        vo.setReason(entity.getReason());
        vo.setReferencedRegulations(entity.getReferencedRegulations());
        vo.setAdopted(entity.getAdopted());
        vo.setCreateTime(entity.getCreateTime());

        // 查询案件名称
        try {
            CaseInfoDTO caseInfo = getCaseInfo(entity.getCaseId());
            if (caseInfo != null) {
                vo.setCaseName(caseInfo.getCaseName());
            }
        } catch (Exception e) {
            log.warn("查询案件名称失败，案件ID: {}", entity.getCaseId());
        }

        return vo;
    }

    private String nvl(String str) {
        return str != null ? str : "";
    }

    private String truncate(String str, int maxLen) {
        if (str == null) return null;
        return str.length() > maxLen ? str.substring(0, maxLen) : str;
    }

    // ======================== 跨模块查询（直接 SQL） ========================

    /**
     * 案件信息 DTO（用于跨模块查询）
     */
    public static class CaseInfoDTO {
        private Long id;
        private String caseName;
        private Integer caseType;
        private String respondentName;
        private String respondentDept;
        private String respondentPosition;
        private String briefDescription;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getCaseName() { return caseName; }
        public void setCaseName(String caseName) { this.caseName = caseName; }
        public Integer getCaseType() { return caseType; }
        public void setCaseType(Integer caseType) { this.caseType = caseType; }
        public String getRespondentName() { return respondentName; }
        public void setRespondentName(String respondentName) { this.respondentName = respondentName; }
        public String getRespondentDept() { return respondentDept; }
        public void setRespondentDept(String respondentDept) { this.respondentDept = respondentDept; }
        public String getRespondentPosition() { return respondentPosition; }
        public void setRespondentPosition(String respondentPosition) { this.respondentPosition = respondentPosition; }
        public String getBriefDescription() { return briefDescription; }
        public void setBriefDescription(String briefDescription) { this.briefDescription = briefDescription; }
    }

    /**
     * 违纪事实 DTO（用于跨模块查询）
     */
    public static class CaseViolationFactDTO {
        private Long id;
        private Long caseId;
        private String factTitle;
        private String factContent;
        private String violationType;
        private java.math.BigDecimal amount;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getCaseId() { return caseId; }
        public void setCaseId(Long caseId) { this.caseId = caseId; }
        public String getFactTitle() { return factTitle; }
        public void setFactTitle(String factTitle) { this.factTitle = factTitle; }
        public String getFactContent() { return factContent; }
        public void setFactContent(String factContent) { this.factContent = factContent; }
        public String getViolationType() { return violationType; }
        public void setViolationType(String violationType) { this.violationType = violationType; }
        public java.math.BigDecimal getAmount() { return amount; }
        public void setAmount(java.math.BigDecimal amount) { this.amount = amount; }
    }

    /**
     * 跨模块 CaseInfo Mapper
     */
    @org.apache.ibatis.annotations.Mapper
    public interface CaseInfoCrossMapper {
        @Select("SELECT id, case_name, case_type, respondent_name, respondent_dept, " +
                "respondent_position, brief_description " +
                "FROM case_info WHERE id = #{caseId}")
        CaseInfoDTO selectCaseInfo(@Param("caseId") Long caseId);

        @org.apache.ibatis.annotations.Update("UPDATE case_info SET classification_level_id = #{levelId}, " +
                "update_time = NOW() WHERE id = #{caseId}")
        int updateClassificationLevel(@Param("caseId") Long caseId, @Param("levelId") Long levelId);
    }

    @Autowired
    private CaseInfoCrossMapper caseInfoMapper;

    /**
     * 跨模块 CaseViolationFact Mapper
     */
    @org.apache.ibatis.annotations.Mapper
    public interface CaseViolationFactCrossMapper {
        @Select("SELECT id, case_id, fact_title, fact_content, violation_type, amount " +
                "FROM case_violation_fact WHERE case_id = #{caseId} ORDER BY sort ASC")
        List<CaseViolationFactDTO> selectByCaseId(@Param("caseId") Long caseId);
    }

    @Autowired
    private CaseViolationFactCrossMapper violationFactMapper;

    private CaseInfoDTO getCaseInfo(Long caseId) {
        return caseInfoMapper.selectCaseInfo(caseId);
    }

    private List<CaseViolationFactDTO> getViolationFacts(Long caseId) {
        return violationFactMapper.selectByCaseId(caseId);
    }
}
