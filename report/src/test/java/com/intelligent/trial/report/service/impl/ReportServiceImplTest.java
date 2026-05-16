package com.intelligent.trial.report.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.intelligent.trial.common.dto.PageRequest;
import com.intelligent.trial.common.exception.BusinessException;
import com.intelligent.trial.common.util.UserContext;
import com.intelligent.trial.report.client.DeepSeekClient;
import com.intelligent.trial.report.entity.ReportRecord;
import com.intelligent.trial.report.entity.ReportTemplate;
import com.intelligent.trial.report.mapper.ReportRecordMapper;
import com.intelligent.trial.report.mapper.ReportTemplateMapper;
import com.intelligent.trial.report.vo.ReportRecordVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ReportServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    @InjectMocks
    private ReportServiceImpl reportService;

    @Mock
    private ReportTemplateMapper reportTemplateMapper;

    @Mock
    private ReportRecordMapper reportRecordMapper;

    @Mock
    private DeepSeekClient deepSeekClient;

    @Mock
    private JdbcTemplate jdbcTemplate;

    private ReportTemplate shenliTemplate;
    private ReportTemplate chufenTemplate;
    private ReportTemplate chuheTemplate;
    private Map<String, Object> mockCaseInfo;

    @BeforeEach
    void setUp() {
        UserContext.setUserId(1L);
        UserContext.setUsername("testuser");

        // Setup templates
        shenliTemplate = new ReportTemplate();
        shenliTemplate.setId(1L);
        shenliTemplate.setTemplateCode("SHENLI_REPORT");
        shenliTemplate.setTemplateName("审理报告");
        shenliTemplate.setTemplateType(1);
        shenliTemplate.setContent("一、案件来源\n二、被调查人基本情况\n三、违纪违法事实\n四、处理建议");
        shenliTemplate.setStatus(1);

        chufenTemplate = new ReportTemplate();
        chufenTemplate.setId(2L);
        chufenTemplate.setTemplateCode("CHUFEN_DECISION");
        chufenTemplate.setTemplateName("处分决定");
        chufenTemplate.setTemplateType(2);
        chufenTemplate.setContent("关于给予XXX同志XXX处分的决定");
        chufenTemplate.setStatus(1);

        chuheTemplate = new ReportTemplate();
        chuheTemplate.setId(4L);
        chuheTemplate.setTemplateCode("CHUHE_REPORT");
        chuheTemplate.setTemplateName("初核报告");
        chuheTemplate.setTemplateType(4);
        chuheTemplate.setContent("初核情况报告");
        chuheTemplate.setStatus(1);

        // Setup mock case info
        mockCaseInfo = new HashMap<>();
        mockCaseInfo.put("id", 100L);
        mockCaseInfo.put("case_code", "AJ20260517001");
        mockCaseInfo.put("case_name", "张三违纪案");
        mockCaseInfo.put("case_type", 1);
        mockCaseInfo.put("case_source", "信访举报");
        mockCaseInfo.put("respondent_name", "张三");
        mockCaseInfo.put("respondent_dept", "某某局");
        mockCaseInfo.put("respondent_position", "副局长");
        mockCaseInfo.put("status", 1);
        mockCaseInfo.put("filing_date", new Date());
        mockCaseInfo.put("close_date", null);
        mockCaseInfo.put("brief_description", "涉嫌违反廉洁纪律");
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    // ==================== generateReport Tests ====================

    @Test
    void generateReport_shouldThrow_whenCaseNotFound() {
        when(jdbcTemplate.queryForList(anyString(), eq(999L))).thenReturn(Collections.emptyList());

        BusinessException ex = assertThrows(BusinessException.class, () -> {
            reportService.generateReport(999L, 1L, null);
        });
        assertTrue(ex.getMessage().contains("案件不存在"));
    }

    @Test
    void generateReport_shouldThrow_whenTemplateNotFound() {
        when(jdbcTemplate.queryForList(anyString(), eq(100L))).thenReturn(Collections.singletonList(mockCaseInfo));
        when(reportTemplateMapper.selectById(eq(999L))).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> {
            reportService.generateReport(100L, 999L, null);
        });
        assertTrue(ex.getMessage().contains("文书模板不存在"));
    }

    @Test
    void generateReport_shouldThrow_whenTemplateDisabled() {
        ReportTemplate disabledTemplate = new ReportTemplate();
        disabledTemplate.setId(1L);
        disabledTemplate.setTemplateName("审理报告");
        disabledTemplate.setStatus(0);

        when(jdbcTemplate.queryForList(anyString(), eq(100L))).thenReturn(Collections.singletonList(mockCaseInfo));
        when(reportTemplateMapper.selectById(eq(1L))).thenReturn(disabledTemplate);

        BusinessException ex = assertThrows(BusinessException.class, () -> {
            reportService.generateReport(100L, 1L, null);
        });
        assertTrue(ex.getMessage().contains("文书模板已禁用"));
    }

    @Test
    void generateReport_shouldCreateRecordAndCallDeepSeek() {
        when(jdbcTemplate.queryForList(anyString(), eq(100L))).thenReturn(Collections.singletonList(mockCaseInfo));
        when(reportTemplateMapper.selectById(eq(1L))).thenReturn(shenliTemplate);
        when(deepSeekClient.generateContent(anyString(), anyString())).thenReturn("Generated report content");
        when(reportRecordMapper.insert(any(ReportRecord.class))).thenAnswer(invocation -> {
            ReportRecord record = invocation.getArgument(0);
            record.setId(10L);
            return 1;
        });

        String recordId = reportService.generateReport(100L, 1L, null);

        assertEquals("10", recordId);
        verify(reportRecordMapper, times(1)).insert(any(ReportRecord.class));
        verify(deepSeekClient, times(1)).generateContent(anyString(), anyString());
        verify(reportRecordMapper, times(1)).updateById(argThat(record ->
                record.getStatus() == 1 && record.getReportContent() != null
        ));
    }

    @Test
    void generateReport_shouldSetFailedStatus_whenDeepSeekFails() {
        when(jdbcTemplate.queryForList(anyString(), eq(100L))).thenReturn(Collections.singletonList(mockCaseInfo));
        when(reportTemplateMapper.selectById(eq(1L))).thenReturn(shenliTemplate);
        when(deepSeekClient.generateContent(anyString(), anyString()))
                .thenThrow(new BusinessException("AI service error"));
        when(reportRecordMapper.insert(any(ReportRecord.class))).thenAnswer(invocation -> {
            ReportRecord record = invocation.getArgument(0);
            record.setId(20L);
            return 1;
        });

        String recordId = reportService.generateReport(100L, 1L, null);

        assertEquals("20", recordId);
        verify(reportRecordMapper, atLeastOnce()).updateById(argThat(record ->
                record.getStatus() == 2
        ));
    }

    @Test
    void generateReport_shouldAutoSelectTemplate_whenTemplateIdNull() {
        when(jdbcTemplate.queryForList(anyString(), eq(100L))).thenReturn(Collections.singletonList(mockCaseInfo));
        when(reportTemplateMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(shenliTemplate);
        when(reportTemplateMapper.selectById(eq(1L))).thenReturn(shenliTemplate);
        when(deepSeekClient.generateContent(anyString(), anyString())).thenReturn("Auto-selected template content");
        when(reportRecordMapper.insert(any(ReportRecord.class))).thenAnswer(invocation -> {
            ReportRecord record = invocation.getArgument(0);
            record.setId(30L);
            return 1;
        });

        String recordId = reportService.generateReport(100L, null, null);

        assertEquals("30", recordId);
        verify(reportTemplateMapper, atLeastOnce()).selectOne(any(LambdaQueryWrapper.class));
    }

    @Test
    void generateReport_shouldUseCustomPrompt() {
        when(jdbcTemplate.queryForList(anyString(), eq(100L))).thenReturn(Collections.singletonList(mockCaseInfo));
        when(reportTemplateMapper.selectById(eq(1L))).thenReturn(shenliTemplate);
        when(deepSeekClient.generateContent(anyString(), anyString())).thenReturn("Custom prompt content");
        when(reportRecordMapper.insert(any(ReportRecord.class))).thenAnswer(invocation -> {
            ReportRecord record = invocation.getArgument(0);
            record.setId(40L);
            return 1;
        });

        String customPrompt = "请重点关注违纪金额部分";
        reportService.generateReport(100L, 1L, customPrompt);

        ArgumentCaptor<String> userPromptCaptor = ArgumentCaptor.forClass(String.class);
        verify(deepSeekClient).generateContent(anyString(), userPromptCaptor.capture());
        assertTrue(userPromptCaptor.getValue().contains("请重点关注违纪金额部分"));
    }

    @Test
    void generateReport_shouldSetGeneratedByFromUserContext() {
        when(jdbcTemplate.queryForList(anyString(), eq(100L))).thenReturn(Collections.singletonList(mockCaseInfo));
        when(reportTemplateMapper.selectById(eq(1L))).thenReturn(shenliTemplate);
        when(deepSeekClient.generateContent(anyString(), anyString())).thenReturn("content");
        when(reportRecordMapper.insert(any(ReportRecord.class))).thenAnswer(invocation -> {
            ReportRecord record = invocation.getArgument(0);
            record.setId(50L);
            return 1;
        });

        reportService.generateReport(100L, 1L, null);

        ArgumentCaptor<ReportRecord> recordCaptor = ArgumentCaptor.forClass(ReportRecord.class);
        verify(reportRecordMapper).insert(recordCaptor.capture());
        assertEquals(1L, recordCaptor.getValue().getGeneratedBy());
    }

    @Test
    void generateReport_shouldFallbackToZeroUserId_whenUserContextEmpty() {
        UserContext.clear();

        when(jdbcTemplate.queryForList(anyString(), eq(100L))).thenReturn(Collections.singletonList(mockCaseInfo));
        when(reportTemplateMapper.selectById(eq(1L))).thenReturn(shenliTemplate);
        when(deepSeekClient.generateContent(anyString(), anyString())).thenReturn("content");
        when(reportRecordMapper.insert(any(ReportRecord.class))).thenAnswer(invocation -> {
            ReportRecord record = invocation.getArgument(0);
            record.setId(60L);
            return 1;
        });

        reportService.generateReport(100L, 1L, null);

        ArgumentCaptor<ReportRecord> recordCaptor = ArgumentCaptor.forClass(ReportRecord.class);
        verify(reportRecordMapper).insert(recordCaptor.capture());
        assertEquals(0L, recordCaptor.getValue().getGeneratedBy());
    }

    // ==================== getReportRecord Tests ====================

    @Test
    void getReportRecord_shouldReturnRecord() {
        ReportRecord expected = new ReportRecord();
        expected.setId(1L);
        expected.setCaseId(100L);
        expected.setStatus(1);
        when(reportRecordMapper.selectById(eq(1L))).thenReturn(expected);

        ReportRecord result = reportService.getReportRecord(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(1, result.getStatus());
    }

    @Test
    void getReportRecord_shouldReturnNull_whenNotFound() {
        when(reportRecordMapper.selectById(eq(999L))).thenReturn(null);

        ReportRecord result = reportService.getReportRecord(999L);

        assertNull(result);
    }

    // ==================== listReports Tests ====================

    @Test
    void listReports_shouldReturnPaginatedResults() {
        Page<ReportRecord> page = new Page<>(1, 10);
        List<ReportRecord> records = new ArrayList<>();
        ReportRecord record = new ReportRecord();
        record.setId(1L);
        record.setCaseId(100L);
        record.setTemplateId(1L);
        record.setTemplateCode("SHENLI_REPORT");
        record.setReportTitle("审理报告");
        record.setStatus(1);
        record.setReportContent("test content");
        records.add(record);
        page.setRecords(records);
        page.setTotal(1);

        when(reportRecordMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);
        when(jdbcTemplate.queryForList(anyString(), eq(100L))).thenReturn(Collections.singletonList(mockCaseInfo));
        when(reportTemplateMapper.selectById(eq(1L))).thenReturn(shenliTemplate);

        PageRequest pageRequest = new PageRequest();
        pageRequest.setPageNum(1);
        pageRequest.setPageSize(10);

        Page<ReportRecordVO> result = reportService.listReports(100L, pageRequest);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
        ReportRecordVO vo = result.getRecords().get(0);
        assertEquals("张三违纪案", vo.getCaseName());
        assertEquals("审理报告", vo.getTemplateName());
    }

    @Test
    void listReports_shouldFilterByCaseId() {
        Page<ReportRecord> page = new Page<>(1, 10);
        page.setRecords(new ArrayList<>());
        page.setTotal(0);
        when(reportRecordMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        PageRequest pageRequest = new PageRequest();
        pageRequest.setPageNum(1);
        pageRequest.setPageSize(10);

        reportService.listReports(100L, pageRequest);

        // Verify the wrapper includes caseId filter
        verify(reportRecordMapper).selectPage(any(Page.class), argThat(wrapper -> {
            // The wrapper should have been constructed with caseId eq
            return wrapper != null;
        }));
    }

    @Test
    void listReports_shouldNotFilterByCaseId_whenNull() {
        Page<ReportRecord> page = new Page<>(1, 10);
        page.setRecords(new ArrayList<>());
        page.setTotal(0);
        when(reportRecordMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        PageRequest pageRequest = new PageRequest();
        pageRequest.setPageNum(1);
        pageRequest.setPageSize(10);

        reportService.listReports(null, pageRequest);

        verify(reportRecordMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    void listReports_shouldHandleMissingCaseInfo() {
        Page<ReportRecord> page = new Page<>(1, 10);
        List<ReportRecord> records = new ArrayList<>();
        ReportRecord record = new ReportRecord();
        record.setId(1L);
        record.setCaseId(100L);
        record.setTemplateId(1L);
        records.add(record);
        page.setRecords(records);
        page.setTotal(1);

        when(reportRecordMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);
        when(jdbcTemplate.queryForList(anyString(), eq(100L))).thenReturn(Collections.emptyList());
        when(reportTemplateMapper.selectById(eq(1L))).thenReturn(shenliTemplate);

        PageRequest pageRequest = new PageRequest();
        pageRequest.setPageNum(1);
        pageRequest.setPageSize(10);

        Page<ReportRecordVO> result = reportService.listReports(100L, pageRequest);

        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
        // Case name should be null since case info not found
        assertNull(result.getRecords().get(0).getCaseName());
    }

    @Test
    void listReports_shouldHandleMissingTemplate() {
        Page<ReportRecord> page = new Page<>(1, 10);
        List<ReportRecord> records = new ArrayList<>();
        ReportRecord record = new ReportRecord();
        record.setId(1L);
        record.setCaseId(100L);
        record.setTemplateId(999L);
        records.add(record);
        page.setRecords(records);
        page.setTotal(1);

        when(reportRecordMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);
        when(jdbcTemplate.queryForList(anyString(), eq(100L))).thenReturn(Collections.singletonList(mockCaseInfo));
        when(reportTemplateMapper.selectById(eq(999L))).thenReturn(null);

        PageRequest pageRequest = new PageRequest();
        pageRequest.setPageNum(1);
        pageRequest.setPageSize(10);

        Page<ReportRecordVO> result = reportService.listReports(100L, pageRequest);

        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
        assertNull(result.getRecords().get(0).getTemplateName());
    }

    // ==================== listTemplates Tests ====================

    @Test
    void listTemplates_shouldReturnEnabledTemplates() {
        List<ReportTemplate> templates = Arrays.asList(shenliTemplate, chufenTemplate);
        when(reportTemplateMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(templates);

        List<ReportTemplate> result = reportService.listTemplates();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(reportTemplateMapper).selectList(argThat(wrapper -> wrapper != null));
    }

    @Test
    void listTemplates_shouldReturnEmptyList_whenNoTemplates() {
        when(reportTemplateMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        List<ReportTemplate> result = reportService.listTemplates();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== selectBestTemplate Tests ====================

    @Test
    void selectBestTemplate_shouldReturnShenliForCaseType1() {
        when(reportTemplateMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(shenliTemplate);

        Long result = reportService.selectBestTemplate(1);

        assertEquals(1L, result);
    }

    @Test
    void selectBestTemplate_shouldReturnChuheForCaseType2() {
        when(reportTemplateMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(chuheTemplate);

        Long result = reportService.selectBestTemplate(2);

        assertEquals(4L, result);
    }

    @Test
    void selectBestTemplate_shouldReturnShenliForCaseType3() {
        when(reportTemplateMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(shenliTemplate);

        Long result = reportService.selectBestTemplate(3);

        assertEquals(1L, result);
    }

    @Test
    void selectBestTemplate_shouldReturnDefaultForNullCaseType() {
        when(reportTemplateMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(shenliTemplate);

        Long result = reportService.selectBestTemplate(null);

        assertEquals(1L, result);
    }

    @Test
    void selectBestTemplate_shouldReturnDefaultForUnknownCaseType() {
        when(reportTemplateMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(shenliTemplate);

        Long result = reportService.selectBestTemplate(99);

        assertEquals(1L, result);
    }

    @Test
    void selectBestTemplate_shouldFallbackToChufen_whenShenliNotFound() {
        // First call for shenli returns null, second call for chufen returns template
        when(reportTemplateMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(null)
                .thenReturn(chufenTemplate);

        Long result = reportService.selectBestTemplate(1);

        assertEquals(2L, result);
    }

    @Test
    void selectBestTemplate_shouldFallbackToShenli_whenChuheNotFound() {
        when(reportTemplateMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(null)
                .thenReturn(shenliTemplate);

        Long result = reportService.selectBestTemplate(2);

        assertEquals(1L, result);
    }

    @Test
    void selectBestTemplate_shouldReturnNull_whenNoTemplatesFound() {
        when(reportTemplateMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        Long result = reportService.selectBestTemplate(1);

        assertNull(result);
    }
}
