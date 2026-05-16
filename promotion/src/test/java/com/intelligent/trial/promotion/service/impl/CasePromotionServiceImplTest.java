package com.intelligent.trial.promotion.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.intelligent.trial.common.exception.BusinessException;
import com.intelligent.trial.promotion.client.DeepSeekClient;
import com.intelligent.trial.promotion.dto.CasePromotionGenerateDTO;
import com.intelligent.trial.promotion.dto.CasePromotionSearchDTO;
import com.intelligent.trial.promotion.entity.CasePromotion;
import com.intelligent.trial.promotion.mapper.CasePromotionMapper;
import com.intelligent.trial.promotion.vo.CasePromotionVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * CasePromotionServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CasePromotionServiceImplTest {

    @Mock
    private CasePromotionMapper casePromotionMapper;

    @Mock
    private DeepSeekClient deepSeekClient;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private CasePromotionServiceImpl casePromotionService;

    private CasePromotion samplePromotion;
    private CasePromotionGenerateDTO generateDTO;
    private CasePromotionSearchDTO searchDTO;

    @BeforeEach
    void setUp() {
        reset(casePromotionMapper, deepSeekClient, redisTemplate, jdbcTemplate, valueOperations);

        samplePromotion = new CasePromotion();
        samplePromotion.setId(1L);
        samplePromotion.setCaseId("CASE-001");
        samplePromotion.setTemplateId(100L);
        samplePromotion.setContent("Test promotion content");
        samplePromotion.setStatus(1);
        samplePromotion.setUserId(10L);
        samplePromotion.setCreateTime(new Date());
        samplePromotion.setUpdateTime(new Date());

        generateDTO = new CasePromotionGenerateDTO();
        generateDTO.setCaseId("CASE-001");
        generateDTO.setTemplateId(100L);
        generateDTO.setAnalysisType("comprehensive");
        generateDTO.setUserId(10L);

        searchDTO = new CasePromotionSearchDTO();
        searchDTO.setPageNum(1);
        searchDTO.setPageSize(10);
    }

    // ==================== CRUD Tests ====================

    @Nested
    @DisplayName("getById")
    class GetByIdTests {

        @Test
        @DisplayName("should return promotion when found")
        void testGetById_Success() {
            when(casePromotionMapper.selectById(1L)).thenReturn(samplePromotion);

            CasePromotion result = casePromotionService.getById(1L);

            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals("CASE-001", result.getCaseId());
            verify(casePromotionMapper).selectById(1L);
        }

        @Test
        @DisplayName("should return null when not found")
        void testGetById_NotFound() {
            when(casePromotionMapper.selectById(999L)).thenReturn(null);

            CasePromotion result = casePromotionService.getById(999L);

            assertNull(result);
            verify(casePromotionMapper).selectById(999L);
        }
    }

    @Nested
    @DisplayName("getByCaseId")
    class GetByCaseIdTests {

        @Test
        @DisplayName("should return promotion by caseId")
        void testGetByCaseId_Success() {
            when(casePromotionMapper.selectByCaseId("CASE-001")).thenReturn(samplePromotion);

            CasePromotion result = casePromotionService.getByCaseId("CASE-001");

            assertNotNull(result);
            assertEquals("CASE-001", result.getCaseId());
            verify(casePromotionMapper).selectByCaseId("CASE-001");
        }

        @Test
        @DisplayName("should return null when caseId not found")
        void testGetByCaseId_NotFound() {
            when(casePromotionMapper.selectByCaseId("CASE-999")).thenReturn(null);

            CasePromotion result = casePromotionService.getByCaseId("CASE-999");

            assertNull(result);
        }
    }

    @Nested
    @DisplayName("create")
    class CreateTests {

        @Test
        @DisplayName("should create promotion with default status")
        void testCreate_WithDefaultStatus() {
            CasePromotion entity = new CasePromotion();
            entity.setCaseId("CASE-002");
            entity.setUserId(20L);
            when(casePromotionMapper.insert(any(CasePromotion.class))).thenReturn(1);

            CasePromotion result = casePromotionService.create(entity);

            assertNotNull(result);
            assertNotNull(result.getCreateTime());
            assertNotNull(result.getUpdateTime());
            assertEquals(0, result.getStatus());
            verify(casePromotionMapper).insert(entity);
        }

        @Test
        @DisplayName("should create promotion with specified status")
        void testCreate_WithSpecifiedStatus() {
            CasePromotion entity = new CasePromotion();
            entity.setCaseId("CASE-003");
            entity.setStatus(2);
            entity.setCreateTime(new Date());
            entity.setUpdateTime(new Date());
            entity.setUserId(20L);
            when(casePromotionMapper.insert(any(CasePromotion.class))).thenReturn(1);

            CasePromotion result = casePromotionService.create(entity);

            assertNotNull(result);
            assertEquals(2, result.getStatus());
            verify(casePromotionMapper).insert(entity);
        }
    }

    @Nested
    @DisplayName("update")
    class UpdateTests {

        @Test
        @DisplayName("should update promotion successfully")
        void testUpdate_Success() {
            when(casePromotionMapper.updateById(any(CasePromotion.class))).thenReturn(1);

            CasePromotion entity = new CasePromotion();
            entity.setId(1L);
            entity.setContent("Updated content");

            boolean result = casePromotionService.update(entity);

            assertTrue(result);
            assertNotNull(entity.getUpdateTime());
            verify(casePromotionMapper).updateById(entity);
        }

        @Test
        @DisplayName("should return false when update fails")
        void testUpdate_Failure() {
            when(casePromotionMapper.updateById(any(CasePromotion.class))).thenReturn(0);

            CasePromotion entity = new CasePromotion();
            entity.setId(999L);
            entity.setContent("Updated content");

            boolean result = casePromotionService.update(entity);

            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("deleteById")
    class DeleteByIdTests {

        @Test
        @DisplayName("should delete promotion successfully")
        void testDeleteById_Success() {
            when(casePromotionMapper.deleteById(1L)).thenReturn(1);

            boolean result = casePromotionService.deleteById(1L);

            assertTrue(result);
            verify(casePromotionMapper).deleteById(1L);
        }

        @Test
        @DisplayName("should return false when delete fails")
        void testDeleteById_Failure() {
            when(casePromotionMapper.deleteById(999L)).thenReturn(0);

            boolean result = casePromotionService.deleteById(999L);

            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("updateStatus")
    class UpdateStatusTests {

        @Test
        @DisplayName("should update status successfully")
        void testUpdateStatus_Success() {
            when(casePromotionMapper.updateById(any(CasePromotion.class))).thenReturn(1);

            boolean result = casePromotionService.updateStatus(1L, 2);

            assertTrue(result);
            ArgumentCaptor<CasePromotion> captor = ArgumentCaptor.forClass(CasePromotion.class);
            verify(casePromotionMapper).updateById(captor.capture());
            CasePromotion captured = captor.getValue();
            assertEquals(1L, captured.getId());
            assertEquals(2, captured.getStatus());
            assertNotNull(captured.getUpdateTime());
        }

        @Test
        @DisplayName("should return false when status update fails")
        void testUpdateStatus_Failure() {
            when(casePromotionMapper.updateById(any(CasePromotion.class))).thenReturn(0);

            boolean result = casePromotionService.updateStatus(999L, 2);

            assertFalse(result);
        }
    }

    // ==================== Search Tests ====================

    @Nested
    @DisplayName("search")
    class SearchTests {

        @Test
        @DisplayName("should search with no filters")
        void testSearch_NoFilters() {
            Page<CasePromotion> page = new Page<>(1, 10, 2);
            page.setRecords(Arrays.asList(samplePromotion, samplePromotion));
            when(casePromotionMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);
            // Use explicit type to avoid ambiguity
            when(jdbcTemplate.queryForList(anyString(), (Object[]) any()))
                    .thenReturn(Collections.emptyList());

            searchDTO = new CasePromotionSearchDTO();
            IPage<CasePromotionVO> result = casePromotionService.search(searchDTO);

            assertNotNull(result);
            assertEquals(2, result.getTotal());
            assertEquals(2, result.getRecords().size());
        }

        @Test
        @DisplayName("should search with caseId filter")
        void testSearch_WithCaseIdFilter() {
            Page<CasePromotion> page = new Page<>(1, 10, 1);
            page.setRecords(Collections.singletonList(samplePromotion));
            when(casePromotionMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);
            when(jdbcTemplate.queryForList(anyString(), (Object[]) any()))
                    .thenReturn(Collections.emptyList());

            searchDTO = new CasePromotionSearchDTO();
            searchDTO.setCaseId("CASE-001");
            IPage<CasePromotionVO> result = casePromotionService.search(searchDTO);

            assertNotNull(result);
            assertEquals(1, result.getTotal());
        }

        @Test
        @DisplayName("should search with status filter")
        void testSearch_WithStatusFilter() {
            Page<CasePromotion> page = new Page<>(1, 10, 1);
            page.setRecords(Collections.singletonList(samplePromotion));
            when(casePromotionMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);
            when(jdbcTemplate.queryForList(anyString(), (Object[]) any()))
                    .thenReturn(Collections.emptyList());

            searchDTO = new CasePromotionSearchDTO();
            searchDTO.setStatus(1);
            IPage<CasePromotionVO> result = casePromotionService.search(searchDTO);

            assertNotNull(result);
            assertEquals(1, result.getTotal());
        }

        @Test
        @DisplayName("should search with userId filter")
        void testSearch_WithUserIdFilter() {
            Page<CasePromotion> page = new Page<>(1, 10, 1);
            page.setRecords(Collections.singletonList(samplePromotion));
            when(casePromotionMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);
            when(jdbcTemplate.queryForList(anyString(), (Object[]) any()))
                    .thenReturn(Collections.emptyList());
            when(jdbcTemplate.queryForList(anyString(), any(Long.class))).thenReturn(Collections.emptyList());

            searchDTO = new CasePromotionSearchDTO();
            searchDTO.setUserId(10L);
            IPage<CasePromotionVO> result = casePromotionService.search(searchDTO);

            assertNotNull(result);
            assertEquals(1, result.getTotal());
        }

        @Test
        @DisplayName("should search with all filters combined")
        void testSearch_AllFilters() {
            Page<CasePromotion> page = new Page<>(1, 10, 1);
            page.setRecords(Collections.singletonList(samplePromotion));
            when(casePromotionMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);
            when(jdbcTemplate.queryForList(anyString(), (Object[]) any()))
                    .thenReturn(Collections.emptyList());
            when(jdbcTemplate.queryForList(anyString(), any(Long.class))).thenReturn(Collections.emptyList());

            searchDTO = new CasePromotionSearchDTO();
            searchDTO.setCaseId("CASE-001");
            searchDTO.setStatus(1);
            searchDTO.setUserId(10L);
            IPage<CasePromotionVO> result = casePromotionService.search(searchDTO);

            assertNotNull(result);
            assertEquals(1, result.getRecords().size());
        }

        @Test
        @DisplayName("should fill caseTitle and userName in VO")
        void testSearch_FillVoFields() {
            CasePromotion promotion = new CasePromotion();
            promotion.setId(1L);
            promotion.setCaseId("CASE-001");
            promotion.setUserId(10L);
            promotion.setStatus(1);

            Page<CasePromotion> page = new Page<>(1, 10, 1);
            page.setRecords(Collections.singletonList(promotion));
            when(casePromotionMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

            Map<String, Object> caseInfo = new HashMap<>();
            caseInfo.put("case_name", "Test Case Title");
            // Explicit varargs to avoid ambiguity
            when(jdbcTemplate.queryForList(contains("case_name"), eq("CASE-001"), eq("CASE-001")))
                    .thenReturn(Collections.singletonList(caseInfo));

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("real_name", "Test User");
            when(jdbcTemplate.queryForList(contains("real_name"), any(Long.class)))
                    .thenReturn(Collections.singletonList(userInfo));

            searchDTO = new CasePromotionSearchDTO();
            IPage<CasePromotionVO> result = casePromotionService.search(searchDTO);

            assertNotNull(result);
            assertEquals(1, result.getRecords().size());
            CasePromotionVO vo = result.getRecords().get(0);
            assertEquals("Test Case Title", vo.getCaseTitle());
            assertEquals("Test User", vo.getUserName());
        }

        @Test
        @DisplayName("should return empty page when no records")
        void testSearch_EmptyResult() {
            Page<CasePromotion> page = new Page<>(1, 10, 0);
            page.setRecords(Collections.emptyList());
            when(casePromotionMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

            searchDTO = new CasePromotionSearchDTO();
            IPage<CasePromotionVO> result = casePromotionService.search(searchDTO);

            assertNotNull(result);
            assertEquals(0, result.getTotal());
            assertTrue(result.getRecords().isEmpty());
        }
    }

    // ==================== Analysis Tests ====================

    @Nested
    @DisplayName("generateAnalysis")
    class GenerateAnalysisTests {

        @Test
        @DisplayName("should generate task ID and set running status")
        void testGenerateAnalysis_Success() {
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            doNothing().when(valueOperations).set(anyString(), eq("running"), eq(24L), eq(TimeUnit.HOURS));

            String taskId = casePromotionService.generateAnalysis(generateDTO);

            assertNotNull(taskId);
            assertFalse(taskId.isEmpty());
            assertEquals(32, taskId.length());

            ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
            verify(valueOperations).set(keyCaptor.capture(), eq("running"), eq(24L), eq(TimeUnit.HOURS));
            assertTrue(keyCaptor.getValue().startsWith("promotion:task:"));
        }

        @Test
        @DisplayName("should generate task ID with different analysis types")
        void testGenerateAnalysis_DifferentAnalysisTypes() {
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            doNothing().when(valueOperations).set(anyString(), eq("running"), eq(24L), eq(TimeUnit.HOURS));

            String[] types = {"discipline", "management", "system", "comprehensive"};
            for (String type : types) {
                generateDTO.setAnalysisType(type);
                String taskId = casePromotionService.generateAnalysis(generateDTO);
                assertNotNull(taskId);
            }
        }
    }

    @Nested
    @DisplayName("getAnalysisStatus")
    class GetAnalysisStatusTests {

        @Test
        @DisplayName("should return running status")
        void testGetAnalysisStatus_Running() {
            String taskId = "test-task-id";
            String redisKey = "promotion:task:" + taskId;
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get(redisKey)).thenReturn("running");

            String status = casePromotionService.getAnalysisStatus(taskId);

            assertEquals("running", status);
        }

        @Test
        @DisplayName("should return completed status")
        void testGetAnalysisStatus_Completed() {
            String taskId = "test-task-id";
            String redisKey = "promotion:task:" + taskId;
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get(redisKey)).thenReturn("completed");

            String status = casePromotionService.getAnalysisStatus(taskId);

            assertEquals("completed", status);
        }

        @Test
        @DisplayName("should return failed status")
        void testGetAnalysisStatus_Failed() {
            String taskId = "test-task-id";
            String redisKey = "promotion:task:" + taskId;
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get(redisKey)).thenReturn("failed:some error");

            String status = casePromotionService.getAnalysisStatus(taskId);

            assertEquals("failed:some error", status);
        }

        @Test
        @DisplayName("should return not_found when task does not exist")
        void testGetAnalysisStatus_NotFound() {
            String taskId = "nonexistent-task";
            String redisKey = "promotion:task:" + taskId;
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get(redisKey)).thenReturn(null);

            String status = casePromotionService.getAnalysisStatus(taskId);

            assertEquals("not_found", status);
        }
    }

    // ==================== AnalyzeAndSave Tests ====================

    @Nested
    @DisplayName("analyzeAndSave")
    class AnalyzeAndSaveTests {

        @Test
        @DisplayName("should complete analysis successfully")
        void testAnalyzeAndSave_Success() {
            String taskId = "test-task-id";
            String redisKey = "promotion:task:" + taskId;

            when(redisTemplate.opsForValue()).thenReturn(valueOperations);

            Map<String, Object> caseInfo = new HashMap<>();
            caseInfo.put("id", "1");
            caseInfo.put("case_code", "CASE-001");
            caseInfo.put("case_name", "Test Case");
            caseInfo.put("case_type", 1);
            caseInfo.put("case_source", "举报");
            caseInfo.put("respondent_name", "张三");
            caseInfo.put("respondent_dept", "某单位");
            caseInfo.put("respondent_position", "处长");
            caseInfo.put("brief_description", "涉嫌违纪");
            when(jdbcTemplate.queryForList(
                    contains("case_code"), eq("CASE-001"), eq("CASE-001")))
                    .thenReturn(Collections.singletonList(caseInfo));

            Map<String, Object> fact = new HashMap<>();
            fact.put("fact_title", "受贿事实");
            fact.put("fact_content", "收受他人财物");
            fact.put("violation_type", "受贿");
            fact.put("evidence", "转账记录");
            when(jdbcTemplate.queryForList(
                    contains("case_violation_fact"), (Object[]) any()))
                    .thenReturn(Collections.singletonList(fact));

            when(deepSeekClient.chat(anyString(), anyString()))
                    .thenReturn("Analysis result content here");

            when(casePromotionMapper.insert(any(CasePromotion.class))).thenReturn(1);

            doNothing().when(valueOperations).set(eq(redisKey), eq("completed"), eq(24L), eq(TimeUnit.HOURS));

            casePromotionService.analyzeAndSave(taskId, generateDTO);

            verify(deepSeekClient).chat(anyString(), anyString());
            verify(casePromotionMapper).insert(any(CasePromotion.class));
            verify(valueOperations).set(eq(redisKey), eq("completed"), eq(24L), eq(TimeUnit.HOURS));
        }

        @Test
        @DisplayName("should fail when case not found")
        void testAnalyzeAndSave_CaseNotFound() {
            String taskId = "test-task-id";
            String redisKey = "promotion:task:" + taskId;

            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(jdbcTemplate.queryForList(anyString(), (Object[]) any()))
                    .thenReturn(Collections.emptyList());
            doNothing().when(valueOperations).set(eq(redisKey), eq("failed:case_not_found"),
                    eq(24L), eq(TimeUnit.HOURS));

            casePromotionService.analyzeAndSave(taskId, generateDTO);

            verify(casePromotionMapper, never()).insert(any());
            verify(deepSeekClient, never()).chat(anyString(), anyString());
            verify(valueOperations).set(eq(redisKey), eq("failed:case_not_found"),
                    eq(24L), eq(TimeUnit.HOURS));
        }

        @Test
        @DisplayName("should fail when DeepSeek throws exception")
        void testAnalyzeAndSave_DeepSeekException() {
            String taskId = "test-task-id";
            String redisKey = "promotion:task:" + taskId;

            when(redisTemplate.opsForValue()).thenReturn(valueOperations);

            Map<String, Object> caseInfo = new HashMap<>();
            caseInfo.put("id", "1");
            caseInfo.put("case_code", "CASE-001");
            caseInfo.put("case_name", "Test Case");
            caseInfo.put("case_type", 1);
            caseInfo.put("case_source", "举报");
            caseInfo.put("respondent_name", "张三");
            caseInfo.put("respondent_dept", "某单位");
            caseInfo.put("respondent_position", "处长");
            caseInfo.put("brief_description", "涉嫌违纪");
            when(jdbcTemplate.queryForList(
                    contains("case_info"), eq("CASE-001"), eq("CASE-001")))
                    .thenReturn(Collections.singletonList(caseInfo));

            when(jdbcTemplate.queryForList(
                    contains("case_violation_fact"), (Object[]) any()))
                    .thenReturn(Collections.emptyList());

            when(deepSeekClient.chat(anyString(), anyString()))
                    .thenThrow(new RuntimeException("DeepSeek API error"));

            doNothing().when(valueOperations).set(eq(redisKey), contains("failed:"),
                    eq(24L), eq(TimeUnit.HOURS));

            casePromotionService.analyzeAndSave(taskId, generateDTO);

            verify(casePromotionMapper, never()).insert(any());
            verify(valueOperations).set(eq(redisKey), contains("failed:"),
                    eq(24L), eq(TimeUnit.HOURS));
        }

        @Test
        @DisplayName("should handle empty violation facts gracefully")
        void testAnalyzeAndSave_EmptyViolationFacts() {
            String taskId = "test-task-id";
            String redisKey = "promotion:task:" + taskId;

            when(redisTemplate.opsForValue()).thenReturn(valueOperations);

            Map<String, Object> caseInfo = new HashMap<>();
            caseInfo.put("id", "1");
            caseInfo.put("case_code", "CASE-001");
            caseInfo.put("case_name", "Test Case");
            caseInfo.put("case_type", 1);
            caseInfo.put("case_source", "举报");
            caseInfo.put("respondent_name", "张三");
            caseInfo.put("respondent_dept", "某单位");
            caseInfo.put("respondent_position", "处长");
            caseInfo.put("brief_description", "涉嫌违纪");
            when(jdbcTemplate.queryForList(
                    contains("case_info"), eq("CASE-001"), eq("CASE-001")))
                    .thenReturn(Collections.singletonList(caseInfo));

            when(jdbcTemplate.queryForList(
                    contains("case_violation_fact"), (Object[]) any()))
                    .thenReturn(Collections.emptyList());

            when(deepSeekClient.chat(anyString(), anyString()))
                    .thenReturn("Analysis without violation facts");

            when(casePromotionMapper.insert(any(CasePromotion.class))).thenReturn(1);
            doNothing().when(valueOperations).set(eq(redisKey), eq("completed"), eq(24L), eq(TimeUnit.HOURS));

            casePromotionService.analyzeAndSave(taskId, generateDTO);

            verify(deepSeekClient).chat(anyString(), anyString());
            verify(casePromotionMapper).insert(any(CasePromotion.class));
        }

        @Test
        @DisplayName("should handle JDBC exception during case query")
        void testAnalyzeAndSave_JdbcException() {
            String taskId = "test-task-id";
            String redisKey = "promotion:task:" + taskId;

            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(jdbcTemplate.queryForList(anyString(), (Object[]) any()))
                    .thenThrow(new RuntimeException("Database connection lost"));
            doNothing().when(valueOperations).set(eq(redisKey), eq("failed:case_not_found"),
                    eq(24L), eq(TimeUnit.HOURS));

            casePromotionService.analyzeAndSave(taskId, generateDTO);

            verify(casePromotionMapper, never()).insert(any());
            verify(valueOperations).set(eq(redisKey), eq("failed:case_not_found"),
                    eq(24L), eq(TimeUnit.HOURS));
        }
    }

    // ==================== Edge Case Tests ====================

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("should handle null caseId in search")
        void testSearch_NullCaseIdInSearchResult() {
            CasePromotion promotion = new CasePromotion();
            promotion.setId(1L);
            promotion.setCaseId(null);
            promotion.setUserId(null);
            promotion.setStatus(0);

            Page<CasePromotion> page = new Page<>(1, 10, 1);
            page.setRecords(Collections.singletonList(promotion));
            when(casePromotionMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                    .thenReturn(page);

            searchDTO = new CasePromotionSearchDTO();
            IPage<CasePromotionVO> result = casePromotionService.search(searchDTO);

            assertNotNull(result);
            assertEquals(1, result.getRecords().size());
            verify(jdbcTemplate, never()).queryForList(anyString(), (Object[]) any());
            verify(jdbcTemplate, never()).queryForList(anyString(), any(Long.class));
        }

        @Test
        @DisplayName("should handle JDBC returning null case name")
        void testSearch_NullCaseName() {
            CasePromotion promotion = new CasePromotion();
            promotion.setId(1L);
            promotion.setCaseId("CASE-001");
            promotion.setUserId(10L);
            promotion.setStatus(1);

            Page<CasePromotion> page = new Page<>(1, 10, 1);
            page.setRecords(Collections.singletonList(promotion));
            when(casePromotionMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                    .thenReturn(page);

            Map<String, Object> caseInfo = new HashMap<>();
            caseInfo.put("case_name", null);
            when(jdbcTemplate.queryForList(contains("case_name"), eq("CASE-001"), eq("CASE-001")))
                    .thenReturn(Collections.singletonList(caseInfo));

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("real_name", "Test User");
            when(jdbcTemplate.queryForList(contains("real_name"), any(Long.class)))
                    .thenReturn(Collections.singletonList(userInfo));

            searchDTO = new CasePromotionSearchDTO();
            IPage<CasePromotionVO> result = casePromotionService.search(searchDTO);

            assertNotNull(result);
            CasePromotionVO vo = result.getRecords().get(0);
            assertNull(vo.getCaseTitle());
        }

        @Test
        @DisplayName("should handle JDBC exception in queryCaseTitle")
        void testSearch_JdbcExceptionInCaseTitle() {
            CasePromotion promotion = new CasePromotion();
            promotion.setId(1L);
            promotion.setCaseId("CASE-001");
            promotion.setUserId(10L);
            promotion.setStatus(1);

            Page<CasePromotion> page = new Page<>(1, 10, 1);
            page.setRecords(Collections.singletonList(promotion));
            when(casePromotionMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                    .thenReturn(page);

            when(jdbcTemplate.queryForList(contains("case_name"), eq("CASE-001"), eq("CASE-001")))
                    .thenThrow(new RuntimeException("DB error"));

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("real_name", "Test User");
            when(jdbcTemplate.queryForList(contains("real_name"), any(Long.class)))
                    .thenReturn(Collections.singletonList(userInfo));

            searchDTO = new CasePromotionSearchDTO();
            IPage<CasePromotionVO> result = casePromotionService.search(searchDTO);

            assertNotNull(result);
            CasePromotionVO vo = result.getRecords().get(0);
            assertNull(vo.getCaseTitle());
        }

        @Test
        @DisplayName("should handle JDBC exception in queryUserName")
        void testSearch_JdbcExceptionInUserName() {
            CasePromotion promotion = new CasePromotion();
            promotion.setId(1L);
            promotion.setCaseId("CASE-001");
            promotion.setUserId(10L);
            promotion.setStatus(1);

            Page<CasePromotion> page = new Page<>(1, 10, 1);
            page.setRecords(Collections.singletonList(promotion));
            when(casePromotionMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                    .thenReturn(page);

            Map<String, Object> caseInfo = new HashMap<>();
            caseInfo.put("case_name", "Test Case");
            when(jdbcTemplate.queryForList(contains("case_name"), eq("CASE-001"), eq("CASE-001")))
                    .thenReturn(Collections.singletonList(caseInfo));

            when(jdbcTemplate.queryForList(contains("real_name"), any(Long.class)))
                    .thenThrow(new RuntimeException("DB error"));

            searchDTO = new CasePromotionSearchDTO();
            IPage<CasePromotionVO> result = casePromotionService.search(searchDTO);

            assertNotNull(result);
            CasePromotionVO vo = result.getRecords().get(0);
            assertNull(vo.getUserName());
        }

        @Test
        @DisplayName("should create promotion with null fields set properly")
        void testCreate_NullFieldsDefaulted() {
            CasePromotion entity = new CasePromotion();
            entity.setCaseId("CASE-NEW");
            when(casePromotionMapper.insert(any(CasePromotion.class))).thenReturn(1);

            CasePromotion result = casePromotionService.create(entity);

            assertNotNull(result.getCreateTime());
            assertNotNull(result.getUpdateTime());
            assertEquals(0, result.getStatus());
        }
    }

    // ==================== AnalyzeAndSave Integration Tests ====================

    @Nested
    @DisplayName("AnalyzeAndSave Integration")
    class AnalyzeAndSaveIntegrationTests {

        @Test
        @DisplayName("should save promotion with correct status PENDING=1")
        void testAnalyzeAndSave_PromotionStatus() {
            String taskId = "test-task-id";
            String redisKey = "promotion:task:" + taskId;

            when(redisTemplate.opsForValue()).thenReturn(valueOperations);

            Map<String, Object> caseInfo = new HashMap<>();
            caseInfo.put("id", "1");
            caseInfo.put("case_code", "CASE-001");
            caseInfo.put("case_name", "Test Case");
            caseInfo.put("case_type", 2);
            caseInfo.put("case_source", "巡视");
            caseInfo.put("respondent_name", "李四");
            caseInfo.put("respondent_dept", "某局");
            caseInfo.put("respondent_position", "副局长");
            caseInfo.put("brief_description", "滥用职权");
            caseInfo.put("filing_date", new Date());
            caseInfo.put("close_date", null);

            when(jdbcTemplate.queryForList(
                    contains("case_info"), eq("CASE-001"), eq("CASE-001")))
                    .thenReturn(Collections.singletonList(caseInfo));

            Map<String, Object> fact1 = new HashMap<>();
            fact1.put("fact_title", "事实1");
            fact1.put("fact_content", "内容1");
            fact1.put("violation_type", "滥用职权");
            fact1.put("occurred_date", new Date());
            fact1.put("amount", 100000);
            fact1.put("evidence", "证据1");

            Map<String, Object> fact2 = new HashMap<>();
            fact2.put("fact_title", "事实2");
            fact2.put("fact_content", "内容2");
            fact2.put("violation_type", "受贿");
            fact2.put("evidence", "证据2");

            when(jdbcTemplate.queryForList(
                    contains("case_violation_fact"), (Object[]) any()))
                    .thenReturn(Arrays.asList(fact1, fact2));

            when(deepSeekClient.chat(anyString(), anyString()))
                    .thenReturn("Detailed analysis report...");

            when(casePromotionMapper.insert(any(CasePromotion.class))).thenReturn(1);
            doNothing().when(valueOperations).set(eq(redisKey), eq("completed"), eq(24L), eq(TimeUnit.HOURS));

            generateDTO.setAnalysisType("management");

            casePromotionService.analyzeAndSave(taskId, generateDTO);

            ArgumentCaptor<CasePromotion> captor = ArgumentCaptor.forClass(CasePromotion.class);
            verify(casePromotionMapper).insert(captor.capture());
            CasePromotion saved = captor.getValue();

            assertEquals("CASE-001", saved.getCaseId());
            assertEquals(100L, saved.getTemplateId());
            assertEquals("Detailed analysis report...", saved.getContent());
            assertEquals(1, saved.getStatus());
            assertEquals(10L, saved.getUserId());
            assertNotNull(saved.getCreateTime());
            assertNotNull(saved.getUpdateTime());
        }

        @Test
        @DisplayName("should handle BusinessException during analysis")
        void testAnalyzeAndSave_BusinessException() {
            String taskId = "test-task-id";
            String redisKey = "promotion:task:" + taskId;

            when(redisTemplate.opsForValue()).thenReturn(valueOperations);

            Map<String, Object> caseInfo = new HashMap<>();
            caseInfo.put("id", "1");
            caseInfo.put("case_code", "CASE-001");
            caseInfo.put("case_name", "Test Case");
            caseInfo.put("case_type", 1);
            caseInfo.put("respondent_name", "测试");
            when(jdbcTemplate.queryForList(
                    contains("case_info"), eq("CASE-001"), eq("CASE-001")))
                    .thenReturn(Collections.singletonList(caseInfo));

            when(jdbcTemplate.queryForList(
                    contains("case_violation_fact"), (Object[]) any()))
                    .thenReturn(Collections.emptyList());

            when(deepSeekClient.chat(anyString(), anyString()))
                    .thenThrow(new BusinessException("AI service timeout"));

            doNothing().when(valueOperations).set(eq(redisKey), contains("failed:"),
                    eq(24L), eq(TimeUnit.HOURS));

            assertDoesNotThrow(() -> casePromotionService.analyzeAndSave(taskId, generateDTO));

            verify(valueOperations).set(eq(redisKey), contains("failed:"),
                    eq(24L), eq(TimeUnit.HOURS));
        }
    }
}
