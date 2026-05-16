package com.intelligent.trial.readingnote.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.intelligent.trial.common.exception.BusinessException;
import com.intelligent.trial.common.util.UserContext;
import com.intelligent.trial.readingnote.entity.ReadingNote;
import com.intelligent.trial.readingnote.mapper.ReadingNoteMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReadingNoteServiceImpl 单元测试")
class ReadingNoteServiceImplTest {

    @Mock
    private ReadingNoteMapper readingNoteMapper;

    @InjectMocks
    private ReadingNoteServiceImpl readingNoteService;

    private ReadingNote sampleNote;

    @BeforeEach
    void setUp() {
        sampleNote = new ReadingNote();
        sampleNote.setId(1L);
        sampleNote.setCaseId("CASE-001");
        sampleNote.setTitle("测试笔记");
        sampleNote.setContent("测试内容");
        sampleNote.setTags("标签1,标签2");
        sampleNote.setNoteType(1);
        sampleNote.setIsShared(0);
        sampleNote.setUserId(100L);
        sampleNote.setCreateTime(new Date());
        sampleNote.setUpdateTime(new Date());

        // Set UserContext for tests that need it
        UserContext.setUserId(100L);
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    // ==================== pageQuery Tests ====================

    @Test
    @DisplayName("分页查询 - 无过滤条件")
    void testPageQueryNoFilters() {
        Page<ReadingNote> expectedPage = new Page<>(1, 10);
        expectedPage.setRecords(Arrays.asList(sampleNote));
        expectedPage.setTotal(1);

        when(readingNoteMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(expectedPage);

        Page<ReadingNote> result = readingNoteService.pageQuery(1, 10, null, null);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
        assertEquals("CASE-001", result.getRecords().get(0).getCaseId());
        verify(readingNoteMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("分页查询 - 按caseId过滤")
    void testPageQueryWithCaseId() {
        Page<ReadingNote> expectedPage = new Page<>(1, 10);
        expectedPage.setRecords(Collections.singletonList(sampleNote));
        expectedPage.setTotal(1);

        when(readingNoteMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(expectedPage);

        Page<ReadingNote> result = readingNoteService.pageQuery(1, 10, "CASE-001", null);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        verify(readingNoteMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("分页查询 - 按noteType过滤")
    void testPageQueryWithNoteType() {
        Page<ReadingNote> expectedPage = new Page<>(1, 10);
        expectedPage.setRecords(Collections.singletonList(sampleNote));
        expectedPage.setTotal(1);

        when(readingNoteMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(expectedPage);

        Page<ReadingNote> result = readingNoteService.pageQuery(1, 10, null, 1);

        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
        verify(readingNoteMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("分页查询 - 同时按caseId和noteType过滤")
    void testPageQueryWithBothFilters() {
        Page<ReadingNote> expectedPage = new Page<>(1, 10);
        expectedPage.setRecords(Collections.singletonList(sampleNote));
        expectedPage.setTotal(1);

        when(readingNoteMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(expectedPage);

        Page<ReadingNote> result = readingNoteService.pageQuery(1, 10, "CASE-001", 1);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        verify(readingNoteMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("分页查询 - 空caseId视为无过滤")
    void testPageQueryWithEmptyCaseId() {
        Page<ReadingNote> expectedPage = new Page<>(1, 10);
        expectedPage.setRecords(Collections.singletonList(sampleNote));
        expectedPage.setTotal(1);

        when(readingNoteMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(expectedPage);

        Page<ReadingNote> result = readingNoteService.pageQuery(1, 10, "", null);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        verify(readingNoteMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("分页查询 - 空结果")
    void testPageQueryEmptyResult() {
        Page<ReadingNote> expectedPage = new Page<>(1, 10);
        expectedPage.setRecords(Collections.emptyList());
        expectedPage.setTotal(0);

        when(readingNoteMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(expectedPage);

        Page<ReadingNote> result = readingNoteService.pageQuery(1, 10, null, null);

        assertNotNull(result);
        assertEquals(0, result.getTotal());
        assertTrue(result.getRecords().isEmpty());
    }

    // ==================== getDetail Tests ====================

    @Test
    @DisplayName("获取详情 - 成功")
    void testGetDetailSuccess() {
        when(readingNoteMapper.selectById(1L)).thenReturn(sampleNote);

        ReadingNote result = readingNoteService.getDetail(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("CASE-001", result.getCaseId());
        assertEquals("测试笔记", result.getTitle());
        verify(readingNoteMapper).selectById(1L);
    }

    @Test
    @DisplayName("获取详情 - 笔记不存在抛出异常")
    void testGetDetailNotFound() {
        when(readingNoteMapper.selectById(999L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> readingNoteService.getDetail(999L));

        assertEquals("笔记不存在", exception.getMessage());
        assertEquals(500, exception.getCode());
        verify(readingNoteMapper).selectById(999L);
    }

    // ==================== create Tests ====================

    @Test
    @DisplayName("创建笔记 - 成功")
    void testCreateSuccess() {
        ReadingNote newNote = new ReadingNote();
        newNote.setCaseId("CASE-002");
        newNote.setTitle("新笔记");
        newNote.setContent("新内容");

        when(readingNoteMapper.insert(any(ReadingNote.class))).thenReturn(1);

        ReadingNote result = readingNoteService.create(newNote);

        assertNotNull(result);
        assertEquals("CASE-002", result.getCaseId());
        assertEquals("新笔记", result.getTitle());
        assertEquals(100L, result.getUserId());
        assertEquals(0, result.getIsShared());
        assertEquals(1, result.getNoteType());
        verify(readingNoteMapper).insert(any(ReadingNote.class));
    }

    @Test
    @DisplayName("创建笔记 - 使用默认isShared和noteType")
    void testCreateWithDefaults() {
        ReadingNote newNote = new ReadingNote();
        newNote.setCaseId("CASE-003");
        newNote.setTitle("默认值笔记");

        when(readingNoteMapper.insert(any(ReadingNote.class))).thenReturn(1);

        ReadingNote result = readingNoteService.create(newNote);

        assertNotNull(result);
        assertEquals(0, result.getIsShared());
        assertEquals(1, result.getNoteType());
        verify(readingNoteMapper).insert(any(ReadingNote.class));
    }

    @Test
    @DisplayName("创建笔记 - 自定义isShared和noteType")
    void testCreateWithCustomValues() {
        ReadingNote newNote = new ReadingNote();
        newNote.setCaseId("CASE-004");
        newNote.setTitle("自定义值笔记");
        newNote.setIsShared(1);
        newNote.setNoteType(2);

        when(readingNoteMapper.insert(any(ReadingNote.class))).thenReturn(1);

        ReadingNote result = readingNoteService.create(newNote);

        assertNotNull(result);
        assertEquals(1, result.getIsShared());
        assertEquals(2, result.getNoteType());
        verify(readingNoteMapper).insert(any(ReadingNote.class));
    }

    @Test
    @DisplayName("创建笔记 - caseId为空抛出异常")
    void testCreateWithNullCaseId() {
        ReadingNote newNote = new ReadingNote();
        newNote.setTitle("无效笔记");

        BusinessException exception = assertThrows(BusinessException.class,
                () -> readingNoteService.create(newNote));

        assertEquals("案件ID不能为空", exception.getMessage());
        verify(readingNoteMapper, never()).insert(any());
    }

    @Test
    @DisplayName("创建笔记 - caseId为空字符串抛出异常")
    void testCreateWithEmptyCaseId() {
        ReadingNote newNote = new ReadingNote();
        newNote.setCaseId("");
        newNote.setTitle("无效笔记");

        BusinessException exception = assertThrows(BusinessException.class,
                () -> readingNoteService.create(newNote));

        assertEquals("案件ID不能为空", exception.getMessage());
        verify(readingNoteMapper, never()).insert(any());
    }

    @Test
    @DisplayName("创建笔记 - title为空抛出异常")
    void testCreateWithNullTitle() {
        ReadingNote newNote = new ReadingNote();
        newNote.setCaseId("CASE-005");

        BusinessException exception = assertThrows(BusinessException.class,
                () -> readingNoteService.create(newNote));

        assertEquals("笔记标题不能为空", exception.getMessage());
        verify(readingNoteMapper, never()).insert(any());
    }

    @Test
    @DisplayName("创建笔记 - title为空字符串抛出异常")
    void testCreateWithEmptyTitle() {
        ReadingNote newNote = new ReadingNote();
        newNote.setCaseId("CASE-005");
        newNote.setTitle("");

        BusinessException exception = assertThrows(BusinessException.class,
                () -> readingNoteService.create(newNote));

        assertEquals("笔记标题不能为空", exception.getMessage());
        verify(readingNoteMapper, never()).insert(any());
    }

    // ==================== update Tests ====================

    @Test
    @DisplayName("更新笔记 - 成功")
    void testUpdateSuccess() {
        ReadingNote updateNote = new ReadingNote();
        updateNote.setId(1L);
        updateNote.setTitle("更新后的标题");
        updateNote.setContent("更新后的内容");

        when(readingNoteMapper.selectById(1L)).thenReturn(sampleNote);
        when(readingNoteMapper.updateById(any(ReadingNote.class))).thenReturn(1);

        readingNoteService.update(updateNote);

        verify(readingNoteMapper).selectById(1L);
        verify(readingNoteMapper).updateById(updateNote);
    }

    @Test
    @DisplayName("更新笔记 - ID为空抛出异常")
    void testUpdateWithNullId() {
        ReadingNote updateNote = new ReadingNote();
        updateNote.setTitle("无ID笔记");

        BusinessException exception = assertThrows(BusinessException.class,
                () -> readingNoteService.update(updateNote));

        assertEquals("ID不能为空", exception.getMessage());
        verify(readingNoteMapper, never()).updateById(any());
    }

    @Test
    @DisplayName("更新笔记 - 笔记不存在抛出异常")
    void testUpdateNotFound() {
        ReadingNote updateNote = new ReadingNote();
        updateNote.setId(999L);
        updateNote.setTitle("不存在的笔记");

        when(readingNoteMapper.selectById(999L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> readingNoteService.update(updateNote));

        assertEquals("笔记不存在", exception.getMessage());
        verify(readingNoteMapper, never()).updateById(any());
    }

    // ==================== delete Tests ====================

    @Test
    @DisplayName("删除笔记 - 成功")
    void testDeleteSuccess() {
        when(readingNoteMapper.selectById(1L)).thenReturn(sampleNote);
        when(readingNoteMapper.deleteById(1L)).thenReturn(1);

        readingNoteService.delete(1L);

        verify(readingNoteMapper).selectById(1L);
        verify(readingNoteMapper).deleteById(1L);
    }

    @Test
    @DisplayName("删除笔记 - 笔记不存在抛出异常")
    void testDeleteNotFound() {
        when(readingNoteMapper.selectById(999L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> readingNoteService.delete(999L));

        assertEquals("笔记不存在", exception.getMessage());
        verify(readingNoteMapper, never()).deleteById(anyLong());
    }

    // ==================== getByCaseId Tests ====================

    @Test
    @DisplayName("按案件ID查询 - 成功")
    void testGetByCaseIdSuccess() {
        when(readingNoteMapper.selectByCaseId("CASE-001"))
                .thenReturn(Arrays.asList(sampleNote));

        List<ReadingNote> result = readingNoteService.getByCaseId("CASE-001");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("CASE-001", result.get(0).getCaseId());
        verify(readingNoteMapper).selectByCaseId("CASE-001");
    }

    @Test
    @DisplayName("按案件ID查询 - 空结果")
    void testGetByCaseIdEmpty() {
        when(readingNoteMapper.selectByCaseId("CASE-EMPTY"))
                .thenReturn(Collections.emptyList());

        List<ReadingNote> result = readingNoteService.getByCaseId("CASE-EMPTY");

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(readingNoteMapper).selectByCaseId("CASE-EMPTY");
    }

    // ==================== getSharedNotes Tests ====================

    @Test
    @DisplayName("获取共享笔记 - 成功")
    void testGetSharedNotesSuccess() {
        ReadingNote sharedNote = new ReadingNote();
        sharedNote.setId(2L);
        sharedNote.setCaseId("CASE-001");
        sharedNote.setTitle("共享笔记");
        sharedNote.setIsShared(1);

        when(readingNoteMapper.selectSharedNotes("CASE-001"))
                .thenReturn(Arrays.asList(sharedNote));

        List<ReadingNote> result = readingNoteService.getSharedNotes("CASE-001");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getIsShared());
        verify(readingNoteMapper).selectSharedNotes("CASE-001");
    }

    @Test
    @DisplayName("获取共享笔记 - 空结果")
    void testGetSharedNotesEmpty() {
        when(readingNoteMapper.selectSharedNotes("CASE-NONE"))
                .thenReturn(Collections.emptyList());

        List<ReadingNote> result = readingNoteService.getSharedNotes("CASE-NONE");

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(readingNoteMapper).selectSharedNotes("CASE-NONE");
    }

    // ==================== toggleShared Tests ====================

    @Test
    @DisplayName("切换共享状态 - 设置为共享")
    void testToggleSharedToShared() {
        when(readingNoteMapper.selectById(1L)).thenReturn(sampleNote);
        when(readingNoteMapper.updateById(any(ReadingNote.class))).thenReturn(1);

        readingNoteService.toggleShared(1L, 1);

        ArgumentCaptor<ReadingNote> captor = ArgumentCaptor.forClass(ReadingNote.class);
        verify(readingNoteMapper).updateById(captor.capture());
        assertEquals(1, captor.getValue().getIsShared());
    }

    @Test
    @DisplayName("切换共享状态 - 设置为不共享")
    void testToggleSharedToNotShared() {
        ReadingNote sharedNote = new ReadingNote();
        sharedNote.setId(1L);
        sharedNote.setCaseId("CASE-001");
        sharedNote.setTitle("测试笔记");
        sharedNote.setIsShared(1);

        when(readingNoteMapper.selectById(1L)).thenReturn(sharedNote);
        when(readingNoteMapper.updateById(any(ReadingNote.class))).thenReturn(1);

        readingNoteService.toggleShared(1L, 0);

        ArgumentCaptor<ReadingNote> captor = ArgumentCaptor.forClass(ReadingNote.class);
        verify(readingNoteMapper).updateById(captor.capture());
        assertEquals(0, captor.getValue().getIsShared());
    }

    @Test
    @DisplayName("切换共享状态 - 笔记不存在抛出异常")
    void testToggleSharedNotFound() {
        when(readingNoteMapper.selectById(999L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> readingNoteService.toggleShared(999L, 1));

        assertEquals("笔记不存在", exception.getMessage());
        verify(readingNoteMapper, never()).updateById(any());
    }
}
