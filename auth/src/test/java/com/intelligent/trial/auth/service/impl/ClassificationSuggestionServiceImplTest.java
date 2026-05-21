package com.intelligent.trial.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.intelligent.trial.auth.client.DeepSeekClient;
import com.intelligent.trial.auth.dto.ClassificationSuggestionDTO;
import com.intelligent.trial.auth.entity.ClassificationSuggestion;
import com.intelligent.trial.auth.entity.SysClassificationLevel;
import com.intelligent.trial.auth.mapper.ClassificationSuggestionMapper;
import com.intelligent.trial.auth.mapper.SysClassificationLevelMapper;
import com.intelligent.trial.auth.vo.ClassificationSuggestionVO;
import com.intelligent.trial.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ClassificationSuggestionServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
class ClassificationSuggestionServiceImplTest {

    @InjectMocks
    private ClassificationSuggestionServiceImpl suggestionService;

    @Mock
    private ClassificationSuggestionMapper suggestionMapper;

    @Mock
    private SysClassificationLevelMapper levelMapper;

    @Mock
    private DeepSeekClient deepSeekClient;

    @Mock
    private ClassificationSuggestionServiceImpl.CaseInfoCrossMapper caseInfoMapper;

    @Mock
    private ClassificationSuggestionServiceImpl.CaseViolationFactCrossMapper violationFactMapper;

    private ClassificationSuggestionServiceImpl.CaseInfoDTO caseInfo;
    private ClassificationSuggestionServiceImpl.CaseViolationFactDTO fact1;
    private ClassificationSuggestionServiceImpl.CaseViolationFactDTO fact2;

    @BeforeEach
    void setUp() {
        caseInfo = new ClassificationSuggestionServiceImpl.CaseInfoDTO();
        caseInfo.setId(1L);
        caseInfo.setCaseName("张三受贿案");
        caseInfo.setCaseType(2);
        caseInfo.setRespondentName("张三");
        caseInfo.setRespondentDept("某局局长");
        caseInfo.setRespondentPosition("局长");
        caseInfo.setBriefDescription("利用职务便利收受他人贿赂");

        fact1 = new ClassificationSuggestionServiceImpl.CaseViolationFactDTO();
        fact1.setId(1L);
        fact1.setCaseId(1L);
        fact1.setFactTitle("受贿事实一");
        fact1.setFactContent("2023年收受李某50万元");
        fact1.setViolationType("受贿");
        fact1.setAmount(new BigDecimal("500000"));

        fact2 = new ClassificationSuggestionServiceImpl.CaseViolationFactDTO();
        fact2.setId(2L);
        fact2.setCaseId(1L);
        fact2.setFactTitle("受贿事实二");
        fact2.setFactContent("2024年收受王某30万元");
        fact2.setViolationType("受贿");
        fact2.setAmount(new BigDecimal("300000"));
    }

    @Test
    void getSuggestion_shouldReturnExistingSuggestion() {
        ClassificationSuggestion existing = createSuggestion(1L, 1L, "机密", 85);

        when(suggestionMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existing);

        ClassificationSuggestionVO result = suggestionService.getSuggestion(1L);

        assertNotNull(result);
        assertEquals("机密", result.getSuggestedLevelName());
        assertEquals(85, result.getConfidence());
        assertEquals(Long.valueOf(1L), result.getCaseId());
    }

    @Test
    void getSuggestion_shouldThrowWhenNoSuggestion() {
        when(suggestionMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
            () -> suggestionService.getSuggestion(999L));
        assertTrue(ex.getMessage().contains("暂无定密建议"));
    }

    @Test
    void generateSuggestion_shouldReturnExistingWhenNotForceRefresh() {
        ClassificationSuggestion existing = createSuggestion(1L, 1L, "机密", 80);
        existing.setAdopted(0);

        ClassificationSuggestionDTO dto = new ClassificationSuggestionDTO();
        dto.setCaseId(1L);
        dto.setForceRefresh(false);

        when(suggestionMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existing);

        ClassificationSuggestionVO result = suggestionService.generateSuggestion(dto, 1L);

        assertNotNull(result);
        assertEquals("机密", result.getSuggestedLevelName());
        // Should NOT call AI
        verify(deepSeekClient, never()).generateContent(anyString(), anyString());
    }

    @Test
    void generateSuggestion_shouldCallAIWhenForceRefresh() {
        ClassificationSuggestionDTO dto = new ClassificationSuggestionDTO();
        dto.setCaseId(1L);
        dto.setForceRefresh(true);

        when(caseInfoMapper.selectCaseInfo(1L)).thenReturn(caseInfo);
        when(violationFactMapper.selectByCaseId(1L)).thenReturn(Arrays.asList(fact1, fact2));

        String aiResponse = "{\n" +
            "  \"suggestedLevel\": \"机密\",\n" +
            "  \"confidence\": 85,\n" +
            "  \"reason\": \"涉及厅局级干部，金额较大\",\n" +
            "  \"referencedRegulations\": \"《中国共产党纪律处分条例》第X条\"\n" +
            "}";
        when(deepSeekClient.generateContent(anyString(), anyString())).thenReturn(aiResponse);

        // Setup level lookup
        SysClassificationLevel level = new SysClassificationLevel();
        level.setId(2L);
        level.setLevelName("机密");
        when(levelMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(level);

        when(suggestionMapper.insert(any(ClassificationSuggestion.class))).thenReturn(1);

        ClassificationSuggestionVO result = suggestionService.generateSuggestion(dto, 1L);

        assertNotNull(result);
        assertEquals("机密", result.getSuggestedLevelName());
        assertEquals(85, result.getConfidence());
        verify(deepSeekClient).generateContent(anyString(), anyString());
    }

    @Test
    void generateSuggestion_shouldThrowWhenCaseNotFound() {
        ClassificationSuggestionDTO dto = new ClassificationSuggestionDTO();
        dto.setCaseId(999L);
        dto.setForceRefresh(true);

        when(caseInfoMapper.selectCaseInfo(999L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
            () -> suggestionService.generateSuggestion(dto, 1L));
        assertTrue(ex.getMessage().contains("案件不存在"));
    }

    @Test
    void generateSuggestion_shouldHandleAIResponseFailure() {
        ClassificationSuggestionDTO dto = new ClassificationSuggestionDTO();
        dto.setCaseId(1L);
        dto.setForceRefresh(true);

        when(caseInfoMapper.selectCaseInfo(1L)).thenReturn(caseInfo);
        when(violationFactMapper.selectByCaseId(1L)).thenReturn(Collections.emptyList());
        when(deepSeekClient.generateContent(anyString(), anyString()))
            .thenThrow(new RuntimeException("AI service unavailable"));

        BusinessException ex = assertThrows(BusinessException.class,
            () -> suggestionService.generateSuggestion(dto, 1L));
        assertTrue(ex.getMessage().contains("AI 分析服务暂时不可用"));
    }

    @Test
    void generateSuggestion_shouldHandleInvalidAIResponse() {
        ClassificationSuggestionDTO dto = new ClassificationSuggestionDTO();
        dto.setCaseId(1L);
        dto.setForceRefresh(true);

        when(caseInfoMapper.selectCaseInfo(1L)).thenReturn(caseInfo);
        when(violationFactMapper.selectByCaseId(1L)).thenReturn(Collections.emptyList());

        // Invalid JSON response
        when(deepSeekClient.generateContent(anyString(), anyString()))
            .thenReturn("This is not JSON at all!!!");

        // Level lookup returns null for default
        when(levelMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        when(suggestionMapper.insert(any(ClassificationSuggestion.class))).thenReturn(1);

        ClassificationSuggestionVO result = suggestionService.generateSuggestion(dto, 1L);

        // Should fallback to "内部" with confidence 30
        assertEquals("内部", result.getSuggestedLevelName());
        assertEquals(30, result.getConfidence());
        assertTrue(result.getReason().contains("AI 分析结果解析异常"));
    }

    @Test
    void generateSuggestion_shouldHandleMarkdownAIResponse() {
        ClassificationSuggestionDTO dto = new ClassificationSuggestionDTO();
        dto.setCaseId(1L);
        dto.setForceRefresh(true);

        when(caseInfoMapper.selectCaseInfo(1L)).thenReturn(caseInfo);
        when(violationFactMapper.selectByCaseId(1L)).thenReturn(Collections.emptyList());

        // Markdown wrapped JSON
        String aiResponse = "Here is my analysis:\n\n```json\n{\n  \"suggestedLevel\": \"秘密\",\n  \"confidence\": 70,\n  \"reason\": \"一般经济犯罪\",\n  \"referencedRegulations\": \"相关规定\"\n}\n```\n\nHope this helps!";
        when(deepSeekClient.generateContent(anyString(), anyString())).thenReturn(aiResponse);

        SysClassificationLevel level = new SysClassificationLevel();
        level.setId(3L);
        level.setLevelName("秘密");
        when(levelMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(level);

        when(suggestionMapper.insert(any(ClassificationSuggestion.class))).thenReturn(1);

        ClassificationSuggestionVO result = suggestionService.generateSuggestion(dto, 1L);

        assertEquals("秘密", result.getSuggestedLevelName());
        assertEquals(70, result.getConfidence());
    }

    @Test
    void generateSuggestion_shouldUseDefaultsWhenConfidenceMissing() {
        ClassificationSuggestionDTO dto = new ClassificationSuggestionDTO();
        dto.setCaseId(1L);
        dto.setForceRefresh(true);

        when(caseInfoMapper.selectCaseInfo(1L)).thenReturn(caseInfo);
        when(violationFactMapper.selectByCaseId(1L)).thenReturn(Collections.emptyList());

        String aiResponse = "{\n" +
            "  \"suggestedLevel\": \"内部\",\n" +
            "  \"reason\": \"轻微违纪\"\n" +
            "}";
        when(deepSeekClient.generateContent(anyString(), anyString())).thenReturn(aiResponse);

        SysClassificationLevel level = new SysClassificationLevel();
        level.setId(4L);
        level.setLevelName("内部");
        when(levelMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(level);

        when(suggestionMapper.insert(any(ClassificationSuggestion.class))).thenReturn(1);

        ClassificationSuggestionVO result = suggestionService.generateSuggestion(dto, 1L);

        assertEquals(50, result.getConfidence()); // default when missing
    }

    @Test
    void adoptSuggestion_shouldAdoptAndApply() {
        ClassificationSuggestion suggestion = createSuggestion(1L, 1L, "机密", 85);
        suggestion.setSuggestedLevelId(2L);
        suggestion.setAdopted(0);

        when(suggestionMapper.selectById(1L)).thenReturn(suggestion);
        when(caseInfoMapper.updateClassificationLevel(1L, 2L)).thenReturn(1);

        suggestionService.adoptSuggestion(1L);

        verify(suggestionMapper).updateById(argThat((ClassificationSuggestion s) ->
            Long.valueOf(1L).equals(s.getId())
                && Integer.valueOf(1).equals(s.getAdopted())));
        verify(caseInfoMapper).updateClassificationLevel(1L, 2L);
    }

    @Test
    void adoptSuggestion_shouldFailWhenAlreadyAdopted() {
        ClassificationSuggestion suggestion = createSuggestion(1L, 1L, "机密", 85);
        suggestion.setAdopted(1);

        when(suggestionMapper.selectById(1L)).thenReturn(suggestion);

        BusinessException ex = assertThrows(BusinessException.class,
            () -> suggestionService.adoptSuggestion(1L));
        assertTrue(ex.getMessage().contains("已被采纳"));
    }

    @Test
    void adoptSuggestion_shouldFailWhenNotFound() {
        when(suggestionMapper.selectById(999L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
            () -> suggestionService.adoptSuggestion(999L));
        assertTrue(ex.getMessage().contains("定密建议不存在"));
    }

    @Test
    void adoptSuggestion_shouldFailWhenCaseUpdateFails() {
        ClassificationSuggestion suggestion = createSuggestion(1L, 1L, "机密", 85);
        suggestion.setAdopted(0);

        when(suggestionMapper.selectById(1L)).thenReturn(suggestion);
        when(caseInfoMapper.updateClassificationLevel(1L, 1L))
            .thenThrow(new RuntimeException("DB error"));

        BusinessException ex = assertThrows(BusinessException.class,
            () -> suggestionService.adoptSuggestion(1L));
        assertTrue(ex.getMessage().contains("更新案件密级失败"));
    }

    private ClassificationSuggestion createSuggestion(Long id, Long caseId, String levelName, int confidence) {
        ClassificationSuggestion s = new ClassificationSuggestion();
        s.setId(id);
        s.setCaseId(caseId);
        s.setSuggestedLevelId(2L);
        s.setSuggestedLevelName(levelName);
        s.setConfidence(confidence);
        s.setReason("AI 分析理由");
        s.setReferencedRegulations("相关法规");
        s.setAdopted(0);
        s.setOperatorId(1L);
        return s;
    }
}
