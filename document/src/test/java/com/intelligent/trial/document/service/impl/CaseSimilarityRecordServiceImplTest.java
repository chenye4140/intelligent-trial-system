package com.intelligent.trial.document.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.intelligent.trial.document.entity.CaseSimilarityRecord;
import com.intelligent.trial.document.mapper.CaseSimilarityRecordMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * CaseSimilarityRecordServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CaseSimilarityRecordServiceImpl 单元测试")
class CaseSimilarityRecordServiceImplTest {

    @Mock
    private CaseSimilarityRecordMapper caseSimilarityRecordMapper;

    @InjectMocks
    private CaseSimilarityRecordServiceImpl caseSimilarityRecordService;

    private CaseSimilarityRecord sampleRecord;

    @BeforeEach
    void setUp() {
        sampleRecord = new CaseSimilarityRecord();
        sampleRecord.setId(1L);
        sampleRecord.setSourceCaseId("CASE-001");
        sampleRecord.setSimilarCaseId("CASE-002");
        sampleRecord.setSimilarityScore(new BigDecimal("0.8500"));
        sampleRecord.setContentScore(new BigDecimal("0.9000"));
        sampleRecord.setAmountScore(new BigDecimal("0.7500"));
        sampleRecord.setTypeScore(new BigDecimal("0.8800"));
        sampleRecord.setCreateTime(new Date());
        sampleRecord.setUpdateTime(new Date());
    }

    // ==================== saveOrUpdateRecord Tests ====================

    @Test
    @DisplayName("保存或更新记录 - 成功插入")
    void testSaveOrUpdateRecordInsertSuccess() {
        when(caseSimilarityRecordMapper.insertOrUpdate(any(CaseSimilarityRecord.class))).thenReturn(1);

        boolean result = caseSimilarityRecordService.saveOrUpdateRecord(sampleRecord);

        assertTrue(result);
        verify(caseSimilarityRecordMapper).insertOrUpdate(sampleRecord);
    }

    @Test
    @DisplayName("保存或更新记录 - 返回true当影响行数>0")
    void testSaveOrUpdateRecordReturnsTrueWhenRowsGreaterThanZero() {
        when(caseSimilarityRecordMapper.insertOrUpdate(any(CaseSimilarityRecord.class))).thenReturn(2);

        boolean result = caseSimilarityRecordService.saveOrUpdateRecord(sampleRecord);

        assertTrue(result);
    }

    @Test
    @DisplayName("保存或更新记录 - 完整字段插入")
    void testSaveOrUpdateRecordWithAllFields() {
        CaseSimilarityRecord record = new CaseSimilarityRecord();
        record.setSourceCaseId("CASE-100");
        record.setSimilarCaseId("CASE-200");
        record.setSimilarityScore(new BigDecimal("0.9200"));
        record.setContentScore(new BigDecimal("0.9500"));
        record.setAmountScore(new BigDecimal("0.8800"));
        record.setTypeScore(new BigDecimal("0.9100"));

        when(caseSimilarityRecordMapper.insertOrUpdate(any(CaseSimilarityRecord.class))).thenReturn(1);

        boolean result = caseSimilarityRecordService.saveOrUpdateRecord(record);

        assertTrue(result);
        verify(caseSimilarityRecordMapper).insertOrUpdate(record);
    }

    @Test
    @DisplayName("保存或更新记录 - 影响行数为0返回false")
    void testSaveOrUpdateRecordReturnsFalseWhenRowsZero() {
        when(caseSimilarityRecordMapper.insertOrUpdate(any(CaseSimilarityRecord.class))).thenReturn(0);

        boolean result = caseSimilarityRecordService.saveOrUpdateRecord(sampleRecord);

        assertFalse(result);
    }

    @Test
    @DisplayName("保存或更新记录 - 更新操作")
    void testSaveOrUpdateRecordUpdate() {
        CaseSimilarityRecord updateRecord = new CaseSimilarityRecord();
        updateRecord.setId(5L);
        updateRecord.setSourceCaseId("CASE-001");
        updateRecord.setSimilarCaseId("CASE-002");
        updateRecord.setSimilarityScore(new BigDecimal("0.9000"));

        when(caseSimilarityRecordMapper.insertOrUpdate(any(CaseSimilarityRecord.class))).thenReturn(1);

        boolean result = caseSimilarityRecordService.saveOrUpdateRecord(updateRecord);

        assertTrue(result);
        verify(caseSimilarityRecordMapper).insertOrUpdate(updateRecord);
    }

    // ==================== batchSaveOrUpdate Tests ====================

    @Test
    @DisplayName("批量保存或更新 - 成功保存多条记录")
    void testBatchSaveOrUpdateSuccess() {
        CaseSimilarityRecord record2 = new CaseSimilarityRecord();
        record2.setSourceCaseId("CASE-001");
        record2.setSimilarCaseId("CASE-003");
        record2.setSimilarityScore(new BigDecimal("0.7200"));
        record2.setContentScore(new BigDecimal("0.7500"));
        record2.setAmountScore(new BigDecimal("0.6800"));
        record2.setTypeScore(new BigDecimal("0.7300"));

        List<CaseSimilarityRecord> records = Arrays.asList(sampleRecord, record2);

        when(caseSimilarityRecordMapper.batchInsertOrUpdate(anyList())).thenReturn(2);

        boolean result = caseSimilarityRecordService.batchSaveOrUpdate(records);

        assertTrue(result);
        verify(caseSimilarityRecordMapper).batchInsertOrUpdate(records);
    }

    @Test
    @DisplayName("批量保存或更新 - 空列表返回true")
    void testBatchSaveOrUpdateEmptyList() {
        boolean result = caseSimilarityRecordService.batchSaveOrUpdate(Collections.emptyList());

        assertTrue(result);
        verify(caseSimilarityRecordMapper, never()).batchInsertOrUpdate(anyList());
    }

    @Test
    @DisplayName("批量保存或更新 - null列表返回true")
    void testBatchSaveOrUpdateNullList() {
        boolean result = caseSimilarityRecordService.batchSaveOrUpdate(null);

        assertTrue(result);
        verify(caseSimilarityRecordMapper, never()).batchInsertOrUpdate(anyList());
    }

    @Test
    @DisplayName("批量保存或更新 - 单条记录")
    void testBatchSaveOrUpdateSingleRecord() {
        List<CaseSimilarityRecord> records = Collections.singletonList(sampleRecord);

        when(caseSimilarityRecordMapper.batchInsertOrUpdate(anyList())).thenReturn(1);

        boolean result = caseSimilarityRecordService.batchSaveOrUpdate(records);

        assertTrue(result);
        verify(caseSimilarityRecordMapper).batchInsertOrUpdate(records);
    }

    @Test
    @DisplayName("批量保存或更新 - 多条记录批量插入")
    void testBatchSaveOrUpdateMultipleRecords() {
        CaseSimilarityRecord record2 = new CaseSimilarityRecord();
        record2.setSourceCaseId("CASE-002");
        record2.setSimilarCaseId("CASE-004");
        record2.setSimilarityScore(new BigDecimal("0.6500"));
        record2.setContentScore(new BigDecimal("0.7000"));
        record2.setAmountScore(new BigDecimal("0.6000"));
        record2.setTypeScore(new BigDecimal("0.6600"));

        CaseSimilarityRecord record3 = new CaseSimilarityRecord();
        record3.setSourceCaseId("CASE-003");
        record3.setSimilarCaseId("CASE-005");
        record3.setSimilarityScore(new BigDecimal("0.5800"));
        record3.setContentScore(new BigDecimal("0.6200"));
        record3.setAmountScore(new BigDecimal("0.5500"));
        record3.setTypeScore(new BigDecimal("0.5700"));

        List<CaseSimilarityRecord> records = Arrays.asList(sampleRecord, record2, record3);

        when(caseSimilarityRecordMapper.batchInsertOrUpdate(anyList())).thenReturn(3);

        boolean result = caseSimilarityRecordService.batchSaveOrUpdate(records);

        assertTrue(result);
        assertEquals(3, records.size());
    }

    @Test
    @DisplayName("批量保存或更新 - 影响行数>0返回true")
    void testBatchSaveOrUpdateReturnsTrueWhenRowsGreaterThanZero() {
        List<CaseSimilarityRecord> records = Arrays.asList(sampleRecord);

        when(caseSimilarityRecordMapper.batchInsertOrUpdate(anyList())).thenReturn(5);

        boolean result = caseSimilarityRecordService.batchSaveOrUpdate(records);

        assertTrue(result);
    }

    @Test
    @DisplayName("批量保存或更新 - 影响行数为0返回false")
    void testBatchSaveOrUpdateReturnsFalseWhenRowsZero() {
        List<CaseSimilarityRecord> records = Arrays.asList(sampleRecord);

        when(caseSimilarityRecordMapper.batchInsertOrUpdate(anyList())).thenReturn(0);

        boolean result = caseSimilarityRecordService.batchSaveOrUpdate(records);

        assertFalse(result);
    }

    // ==================== listBySourceCaseId Tests ====================

    @Test
    @DisplayName("按源案件ID查询 - 成功返回结果")
    void testListBySourceCaseIdSuccess() {
        when(caseSimilarityRecordMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(sampleRecord));

        List<CaseSimilarityRecord> result = caseSimilarityRecordService.listBySourceCaseId("CASE-001");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("CASE-001", result.get(0).getSourceCaseId());
        assertEquals("CASE-002", result.get(0).getSimilarCaseId());
    }

    @Test
    @DisplayName("按源案件ID查询 - 无匹配返回空列表")
    void testListBySourceCaseIdEmpty() {
        when(caseSimilarityRecordMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.emptyList());

        List<CaseSimilarityRecord> result = caseSimilarityRecordService.listBySourceCaseId("CASE-EMPTY");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("按源案件ID查询 - 按相似度降序排序")
    void testListBySourceCaseIdOrderBySimilarityDesc() {
        CaseSimilarityRecord record1 = new CaseSimilarityRecord();
        record1.setId(1L);
        record1.setSourceCaseId("CASE-001");
        record1.setSimilarCaseId("CASE-010");
        record1.setSimilarityScore(new BigDecimal("0.9500"));

        CaseSimilarityRecord record2 = new CaseSimilarityRecord();
        record2.setId(2L);
        record2.setSourceCaseId("CASE-001");
        record2.setSimilarCaseId("CASE-011");
        record2.setSimilarityScore(new BigDecimal("0.8500"));

        CaseSimilarityRecord record3 = new CaseSimilarityRecord();
        record3.setId(3L);
        record3.setSourceCaseId("CASE-001");
        record3.setSimilarCaseId("CASE-012");
        record3.setSimilarityScore(new BigDecimal("0.7500"));

        List<CaseSimilarityRecord> expectedResults = Arrays.asList(record1, record2, record3);

        when(caseSimilarityRecordMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(expectedResults);

        List<CaseSimilarityRecord> result = caseSimilarityRecordService.listBySourceCaseId("CASE-001");

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(0, result.get(0).getSimilarityScore().compareTo(new BigDecimal("0.9500")));
        assertEquals(0, result.get(1).getSimilarityScore().compareTo(new BigDecimal("0.8500")));
        assertEquals(0, result.get(2).getSimilarityScore().compareTo(new BigDecimal("0.7500")));

        // Verify the query wrapper uses orderByDesc on similarityScore
        ArgumentCaptor<LambdaQueryWrapper<CaseSimilarityRecord>> captor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(caseSimilarityRecordMapper).selectList(captor.capture());
    }

    @Test
    @DisplayName("按源案件ID查询 - 多结果验证")
    void testListBySourceCaseIdMultipleResults() {
        CaseSimilarityRecord record1 = new CaseSimilarityRecord();
        record1.setId(1L);
        record1.setSourceCaseId("CASE-001");
        record1.setSimilarCaseId("CASE-100");
        record1.setSimilarityScore(new BigDecimal("0.9000"));

        CaseSimilarityRecord record2 = new CaseSimilarityRecord();
        record2.setId(2L);
        record2.setSourceCaseId("CASE-001");
        record2.setSimilarCaseId("CASE-200");
        record2.setSimilarityScore(new BigDecimal("0.8000"));

        when(caseSimilarityRecordMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(record1, record2));

        List<CaseSimilarityRecord> result = caseSimilarityRecordService.listBySourceCaseId("CASE-001");

        assertNotNull(result);
        assertEquals(2, result.size());
    }
}
