package com.intelligent.trial.document.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.intelligent.trial.common.exception.BusinessException;
import com.intelligent.trial.common.exception.ErrorCode;
import com.intelligent.trial.document.entity.IncomingDoc;
import com.intelligent.trial.document.mapper.IncomingDocMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * IncomingDocServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("IncomingDocServiceImpl 单元测试")
class IncomingDocServiceImplTest {

    @Mock
    private IncomingDocMapper incomingDocMapper;

    @InjectMocks
    private IncomingDocServiceImpl incomingDocService;

    private IncomingDoc sampleDoc;
    private Date sampleDate;

    @BeforeEach
    void setUp() throws ParseException {
        sampleDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2026-05-01 09:00:00");

        sampleDoc = new IncomingDoc();
        sampleDoc.setId(1L);
        sampleDoc.setDocNo("DOC-2026-001");
        sampleDoc.setFromUnit("某市政府办公厅");
        sampleDoc.setTitle("关于开展安全生产检查的通知");
        sampleDoc.setReceiveDate(sampleDate);
        sampleDoc.setSubject("安全生产检查工作安排");
        sampleDoc.setOcrContent("OCR识别的文档内容...");
        sampleDoc.setStatus(0);
        sampleDoc.setHandlerId(100L);
        sampleDoc.setCreateTime(sampleDate);
        sampleDoc.setUpdateTime(sampleDate);
    }

    // ==================== pageIncomingDoc Tests ====================

    @Test
    @DisplayName("分页查询 - 所有参数均提供")
    void testPageIncomingDocWithAllParams() throws ParseException {
        Date startDate = new SimpleDateFormat("yyyy-MM-dd").parse("2026-05-01");
        Date endDate = new SimpleDateFormat("yyyy-MM-dd").parse("2026-05-31");

        Page<IncomingDoc> expectedPage = new Page<>(1, 10);
        expectedPage.setRecords(Collections.singletonList(sampleDoc));
        expectedPage.setTotal(1);

        when(incomingDocMapper.selectIncomingDocPage(any(Page.class), eq("通知"),
                eq("某市政府办公厅"), eq(0), eq(startDate), eq(endDate)))
                .thenReturn(expectedPage);

        Page<IncomingDoc> result = incomingDocService.pageIncomingDoc(1, 10,
                "通知", "某市政府办公厅", 0, startDate, endDate);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
        assertEquals("关于开展安全生产检查的通知", result.getRecords().get(0).getTitle());
        verify(incomingDocMapper).selectIncomingDocPage(any(Page.class), eq("通知"),
                eq("某市政府办公厅"), eq(0), eq(startDate), eq(endDate));
    }

    @Test
    @DisplayName("分页查询 - 所有参数为null")
    void testPageIncomingDocWithNullParams() {
        Page<IncomingDoc> expectedPage = new Page<>(1, 10);
        expectedPage.setRecords(Collections.singletonList(sampleDoc));
        expectedPage.setTotal(1);

        when(incomingDocMapper.selectIncomingDocPage(any(Page.class), isNull(),
                isNull(), isNull(), isNull(), isNull()))
                .thenReturn(expectedPage);

        Page<IncomingDoc> result = incomingDocService.pageIncomingDoc(1, 10,
                null, null, null, null, null);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        verify(incomingDocMapper).selectIncomingDocPage(any(Page.class), isNull(),
                isNull(), isNull(), isNull(), isNull());
    }

    @Test
    @DisplayName("分页查询 - 空结果")
    void testPageIncomingDocEmptyResult() {
        Page<IncomingDoc> expectedPage = new Page<>(1, 10);
        expectedPage.setRecords(Collections.emptyList());
        expectedPage.setTotal(0);

        when(incomingDocMapper.selectIncomingDocPage(any(Page.class), isNull(),
                isNull(), isNull(), isNull(), isNull()))
                .thenReturn(expectedPage);

        Page<IncomingDoc> result = incomingDocService.pageIncomingDoc(1, 10,
                null, null, null, null, null);

        assertNotNull(result);
        assertEquals(0, result.getTotal());
        assertTrue(result.getRecords().isEmpty());
    }

    @Test
    @DisplayName("分页查询 - 分页参数生效")
    void testPageIncomingDocPagination() throws ParseException {
        Date startDate = new SimpleDateFormat("yyyy-MM-dd").parse("2026-05-01");
        Page<IncomingDoc> expectedPage = new Page<>(2, 5);
        expectedPage.setRecords(Arrays.asList(sampleDoc, sampleDoc));
        expectedPage.setTotal(12);

        when(incomingDocMapper.selectIncomingDocPage(any(Page.class), isNull(),
                isNull(), isNull(), eq(startDate), isNull()))
                .thenReturn(expectedPage);

        Page<IncomingDoc> result = incomingDocService.pageIncomingDoc(2, 5,
                null, null, null, startDate, null);

        assertNotNull(result);
        assertEquals(12, result.getTotal());
        assertEquals(2, result.getRecords().size());
        assertEquals(2, result.getCurrent());
        assertEquals(5, result.getSize());
    }

    @Test
    @DisplayName("分页查询 - 多结果列表")
    void testPageIncomingDocMultipleResults() {
        IncomingDoc doc2 = new IncomingDoc();
        doc2.setId(2L);
        doc2.setTitle("第二份来文");
        doc2.setReceiveDate(sampleDate);
        doc2.setStatus(1);

        Page<IncomingDoc> expectedPage = new Page<>(1, 10);
        expectedPage.setRecords(Arrays.asList(sampleDoc, doc2));
        expectedPage.setTotal(2);

        when(incomingDocMapper.selectIncomingDocPage(any(Page.class), isNull(),
                isNull(), isNull(), isNull(), isNull()))
                .thenReturn(expectedPage);

        Page<IncomingDoc> result = incomingDocService.pageIncomingDoc(1, 10,
                null, null, null, null, null);

        assertNotNull(result);
        assertEquals(2, result.getTotal());
        assertEquals(2, result.getRecords().size());
    }

    // ==================== addIncomingDoc Tests ====================

    @Test
    @DisplayName("新增来文 - 所有字段")
    void testAddIncomingDocWithAllFields() {
        IncomingDoc newDoc = new IncomingDoc();
        newDoc.setDocNo("DOC-2026-002");
        newDoc.setFromUnit("某法院");
        newDoc.setTitle("新来文");
        newDoc.setReceiveDate(sampleDate);
        newDoc.setSubject("新事由");
        newDoc.setStatus(1);
        newDoc.setHandlerId(200L);

        when(incomingDocMapper.insert(any(IncomingDoc.class))).thenReturn(1);

        incomingDocService.addIncomingDoc(newDoc);

        assertNotNull(newDoc.getCreateTime());
        assertNotNull(newDoc.getUpdateTime());
        assertEquals(1, newDoc.getStatus());
        verify(incomingDocMapper).insert(any(IncomingDoc.class));
    }

    @Test
    @DisplayName("新增来文 - 最小字段（状态默认为0）")
    void testAddIncomingDocWithMinimalFields() {
        IncomingDoc newDoc = new IncomingDoc();
        newDoc.setTitle("最简来文");
        newDoc.setReceiveDate(sampleDate);

        when(incomingDocMapper.insert(any(IncomingDoc.class))).thenReturn(1);

        incomingDocService.addIncomingDoc(newDoc);

        assertEquals(0, newDoc.getStatus().intValue());
        assertNotNull(newDoc.getCreateTime());
        assertNotNull(newDoc.getUpdateTime());
        verify(incomingDocMapper).insert(any(IncomingDoc.class));
    }

    @Test
    @DisplayName("新增来文 - 标题为null抛出异常")
    void testAddIncomingDocWithNullTitle() {
        IncomingDoc newDoc = new IncomingDoc();
        newDoc.setTitle(null);
        newDoc.setReceiveDate(sampleDate);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> incomingDocService.addIncomingDoc(newDoc));

        assertEquals("来文标题不能为空", exception.getMessage());
        assertEquals(ErrorCode.PARAM_ERROR.getCode(), exception.getCode());
        verify(incomingDocMapper, never()).insert(any());
    }

    @Test
    @DisplayName("新增来文 - 标题为空字符串抛出异常")
    void testAddIncomingDocWithEmptyTitle() {
        IncomingDoc newDoc = new IncomingDoc();
        newDoc.setTitle("");
        newDoc.setReceiveDate(sampleDate);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> incomingDocService.addIncomingDoc(newDoc));

        assertEquals("来文标题不能为空", exception.getMessage());
        verify(incomingDocMapper, never()).insert(any());
    }

    @Test
    @DisplayName("新增来文 - 收到日期为null抛出异常")
    void testAddIncomingDocWithNullReceiveDate() {
        IncomingDoc newDoc = new IncomingDoc();
        newDoc.setTitle("有效标题");
        newDoc.setReceiveDate(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> incomingDocService.addIncomingDoc(newDoc));

        assertEquals("收到日期不能为空", exception.getMessage());
        verify(incomingDocMapper, never()).insert(any());
    }

    @Test
    @DisplayName("新增来文 - 只设置标题和日期")
    void testAddIncomingDocWithOnlyTitleAndDate() {
        IncomingDoc newDoc = new IncomingDoc();
        newDoc.setTitle("仅标题日期来文");
        newDoc.setReceiveDate(sampleDate);

        when(incomingDocMapper.insert(any(IncomingDoc.class))).thenReturn(1);

        incomingDocService.addIncomingDoc(newDoc);

        assertNull(newDoc.getDocNo());
        assertNull(newDoc.getFromUnit());
        assertEquals(0, newDoc.getStatus().intValue());
        verify(incomingDocMapper).insert(any(IncomingDoc.class));
    }

    // ==================== updateIncomingDoc Tests ====================

    @Test
    @DisplayName("更新来文 - 成功")
    void testUpdateIncomingDocSuccess() {
        IncomingDoc updateDoc = new IncomingDoc();
        updateDoc.setId(1L);
        updateDoc.setTitle("更新后的标题");
        updateDoc.setFromUnit("更新后的单位");

        when(incomingDocMapper.selectById(1L)).thenReturn(sampleDoc);
        when(incomingDocMapper.updateById(any(IncomingDoc.class))).thenReturn(1);

        incomingDocService.updateIncomingDoc(updateDoc);

        verify(incomingDocMapper).selectById(1L);
        verify(incomingDocMapper).updateById(updateDoc);
        assertNotNull(updateDoc.getUpdateTime());
    }

    @Test
    @DisplayName("更新来文 - ID为null抛出异常")
    void testUpdateIncomingDocWithNullId() {
        IncomingDoc updateDoc = new IncomingDoc();
        updateDoc.setId(null);
        updateDoc.setTitle("更新标题");

        BusinessException exception = assertThrows(BusinessException.class,
                () -> incomingDocService.updateIncomingDoc(updateDoc));

        assertEquals("来文ID不能为空", exception.getMessage());
        verify(incomingDocMapper, never()).selectById(anyLong());
        verify(incomingDocMapper, never()).updateById(any());
    }

    @Test
    @DisplayName("更新来文 - 来文不存在抛出异常")
    void testUpdateIncomingDocNotFound() {
        IncomingDoc updateDoc = new IncomingDoc();
        updateDoc.setId(999L);
        updateDoc.setTitle("不存在的来文");

        when(incomingDocMapper.selectById(999L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> incomingDocService.updateIncomingDoc(updateDoc));

        assertEquals("来文不存在", exception.getMessage());
        assertEquals(ErrorCode.INCOMING_DOC_NOT_FOUND.getCode(), exception.getCode());
        verify(incomingDocMapper, never()).updateById(any());
    }

    @Test
    @DisplayName("更新来文 - 仅更新部分字段")
    void testUpdateIncomingDocPartialFields() {
        IncomingDoc updateDoc = new IncomingDoc();
        updateDoc.setId(1L);
        updateDoc.setStatus(2);

        when(incomingDocMapper.selectById(1L)).thenReturn(sampleDoc);
        when(incomingDocMapper.updateById(any(IncomingDoc.class))).thenReturn(1);

        incomingDocService.updateIncomingDoc(updateDoc);

        verify(incomingDocMapper).updateById(updateDoc);
        assertEquals(2, updateDoc.getStatus().intValue());
    }

    // ==================== deleteIncomingDoc Tests ====================

    @Test
    @DisplayName("删除来文 - 成功")
    void testDeleteIncomingDocSuccess() {
        when(incomingDocMapper.selectById(1L)).thenReturn(sampleDoc);
        when(incomingDocMapper.deleteById(1L)).thenReturn(1);

        incomingDocService.deleteIncomingDoc(1L);

        verify(incomingDocMapper).selectById(1L);
        verify(incomingDocMapper).deleteById(1L);
    }

    @Test
    @DisplayName("删除来文 - 来文不存在抛出异常")
    void testDeleteIncomingDocNotFound() {
        when(incomingDocMapper.selectById(999L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> incomingDocService.deleteIncomingDoc(999L));

        assertEquals("来文不存在", exception.getMessage());
        verify(incomingDocMapper, never()).deleteById(anyLong());
    }

    // ==================== changeStatus Tests ====================

    @Test
    @DisplayName("变更状态 - 成功（待处理→处理中）")
    void testChangeStatusToProcessing() {
        when(incomingDocMapper.selectById(1L)).thenReturn(sampleDoc);
        when(incomingDocMapper.updateById(any(IncomingDoc.class))).thenReturn(1);

        incomingDocService.changeStatus(1L, 1);

        verify(incomingDocMapper).updateById(argThat(doc ->
                doc.getId().equals(1L) && doc.getStatus().equals(1)));
    }

    @Test
    @DisplayName("变更状态 - 成功（处理中→已办结）")
    void testChangeStatusToCompleted() {
        IncomingDoc processingDoc = new IncomingDoc();
        processingDoc.setId(1L);
        processingDoc.setStatus(1);

        when(incomingDocMapper.selectById(1L)).thenReturn(processingDoc);
        when(incomingDocMapper.updateById(any(IncomingDoc.class))).thenReturn(1);

        incomingDocService.changeStatus(1L, 2);

        verify(incomingDocMapper).updateById(argThat(doc ->
                doc.getId().equals(1L) && doc.getStatus().equals(2)));
    }

    @Test
    @DisplayName("变更状态 - 成功（已办结→已归档）")
    void testChangeStatusToArchived() {
        IncomingDoc completedDoc = new IncomingDoc();
        completedDoc.setId(1L);
        completedDoc.setStatus(2);

        when(incomingDocMapper.selectById(1L)).thenReturn(completedDoc);
        when(incomingDocMapper.updateById(any(IncomingDoc.class))).thenReturn(1);

        incomingDocService.changeStatus(1L, 3);

        verify(incomingDocMapper).updateById(argThat(doc ->
                doc.getId().equals(1L) && doc.getStatus().equals(3)));
    }

    @Test
    @DisplayName("变更状态 - 来文不存在抛出异常")
    void testChangeStatusNotFound() {
        when(incomingDocMapper.selectById(999L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> incomingDocService.changeStatus(999L, 1));

        assertEquals("来文不存在", exception.getMessage());
        verify(incomingDocMapper, never()).updateById(any());
    }

    // ==================== Additional Edge Cases ====================

    @Test
    @DisplayName("分页查询 - 仅按标题过滤")
    void testPageIncomingDocOnlyTitle() {
        Page<IncomingDoc> expectedPage = new Page<>(1, 10);
        expectedPage.setRecords(Collections.singletonList(sampleDoc));
        expectedPage.setTotal(1);

        when(incomingDocMapper.selectIncomingDocPage(any(Page.class), eq("通知"),
                isNull(), isNull(), isNull(), isNull()))
                .thenReturn(expectedPage);

        Page<IncomingDoc> result = incomingDocService.pageIncomingDoc(1, 10,
                "通知", null, null, null, null);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
    }

    @Test
    @DisplayName("分页查询 - 仅按来文单位过滤")
    void testPageIncomingDocOnlyFromUnit() {
        Page<IncomingDoc> expectedPage = new Page<>(1, 10);
        expectedPage.setRecords(Collections.singletonList(sampleDoc));
        expectedPage.setTotal(1);

        when(incomingDocMapper.selectIncomingDocPage(any(Page.class), isNull(),
                eq("某市政府办公厅"), isNull(), isNull(), isNull()))
                .thenReturn(expectedPage);

        Page<IncomingDoc> result = incomingDocService.pageIncomingDoc(1, 10,
                null, "某市政府办公厅", null, null, null);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
    }

    @Test
    @DisplayName("分页查询 - 仅按状态过滤")
    void testPageIncomingDocOnlyStatus() {
        Page<IncomingDoc> expectedPage = new Page<>(1, 10);
        expectedPage.setRecords(Collections.singletonList(sampleDoc));
        expectedPage.setTotal(1);

        when(incomingDocMapper.selectIncomingDocPage(any(Page.class), isNull(),
                isNull(), eq(0), isNull(), isNull()))
                .thenReturn(expectedPage);

        Page<IncomingDoc> result = incomingDocService.pageIncomingDoc(1, 10,
                null, null, 0, null, null);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
    }

    @Test
    @DisplayName("分页查询 - 仅按日期范围过滤")
    void testPageIncomingDocOnlyDateRange() throws ParseException {
        Date startDate = new SimpleDateFormat("yyyy-MM-dd").parse("2026-05-01");
        Date endDate = new SimpleDateFormat("yyyy-MM-dd").parse("2026-05-31");

        Page<IncomingDoc> expectedPage = new Page<>(1, 10);
        expectedPage.setRecords(Collections.singletonList(sampleDoc));
        expectedPage.setTotal(1);

        when(incomingDocMapper.selectIncomingDocPage(any(Page.class), isNull(),
                isNull(), isNull(), eq(startDate), eq(endDate)))
                .thenReturn(expectedPage);

        Page<IncomingDoc> result = incomingDocService.pageIncomingDoc(1, 10,
                null, null, null, startDate, endDate);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
    }

    @Test
    @DisplayName("新增来文 - 自定义状态为1")
    void testAddIncomingDocCustomStatus() {
        IncomingDoc newDoc = new IncomingDoc();
        newDoc.setTitle("自定义状态来文");
        newDoc.setReceiveDate(sampleDate);
        newDoc.setStatus(1);

        when(incomingDocMapper.insert(any(IncomingDoc.class))).thenReturn(1);

        incomingDocService.addIncomingDoc(newDoc);

        assertEquals(1, newDoc.getStatus().intValue());
        verify(incomingDocMapper).insert(any(IncomingDoc.class));
    }

    @Test
    @DisplayName("新增来文 - 包含文号、单位等完整信息")
    void testAddIncomingDocWithDocNoAndFromUnit() {
        IncomingDoc newDoc = new IncomingDoc();
        newDoc.setDocNo("DOC-NEW-001");
        newDoc.setFromUnit("新单位");
        newDoc.setTitle("带文号和单位的来文");
        newDoc.setReceiveDate(sampleDate);
        newDoc.setStatus(0);

        when(incomingDocMapper.insert(any(IncomingDoc.class))).thenReturn(1);

        incomingDocService.addIncomingDoc(newDoc);

        assertEquals("DOC-NEW-001", newDoc.getDocNo());
        assertEquals("新单位", newDoc.getFromUnit());
        verify(incomingDocMapper).insert(any(IncomingDoc.class));
    }

    @Test
    @DisplayName("删除来文 - 删除已归档来文")
    void testDeleteIncomingDocArchivedDoc() {
        IncomingDoc archivedDoc = new IncomingDoc();
        archivedDoc.setId(1L);
        archivedDoc.setStatus(3);

        when(incomingDocMapper.selectById(1L)).thenReturn(archivedDoc);
        when(incomingDocMapper.deleteById(1L)).thenReturn(1);

        incomingDocService.deleteIncomingDoc(1L);

        verify(incomingDocMapper).deleteById(1L);
    }

    @Test
    @DisplayName("变更状态 - 更新时间的设置")
    void testChangeStatusUpdateTimeSet() {
        when(incomingDocMapper.selectById(1L)).thenReturn(sampleDoc);
        when(incomingDocMapper.updateById(any(IncomingDoc.class))).thenReturn(1);

        incomingDocService.changeStatus(1L, 2);

        ArgumentCaptor<IncomingDoc> captor = ArgumentCaptor.forClass(IncomingDoc.class);
        verify(incomingDocMapper).updateById(captor.capture());
        IncomingDoc updatedDoc = captor.getValue();
        assertNotNull(updatedDoc.getUpdateTime());
        assertEquals(1L, updatedDoc.getId());
        assertEquals(2, updatedDoc.getStatus().intValue());
    }
}
