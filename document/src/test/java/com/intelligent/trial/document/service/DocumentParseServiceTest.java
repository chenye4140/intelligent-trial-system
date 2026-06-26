package com.intelligent.trial.document.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.intelligent.trial.common.exception.BusinessException;
import com.intelligent.trial.common.exception.ErrorCode;
import com.intelligent.trial.document.client.LlmClient;
import com.intelligent.trial.document.entity.DocParseTask;
import com.intelligent.trial.document.mapper.DocParseTaskMapper;
import com.intelligent.trial.document.sse.ParseProgressBroadcaster;
import com.intelligent.trial.document.util.MinioUtil;
import com.intelligent.trial.repository.service.DocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * DocumentParseService 单元测试
 * 测试解析任务的查询、重试、删除等同步操作逻辑
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DocumentParseService 单元测试")
class DocumentParseServiceTest {

    @Mock
    private DocParseTaskMapper taskMapper;

    @Mock
    private MinioUtil minioUtil;

    @Mock
    private LlmClient llmClient;

    @Mock
    private VectorStorageService vectorStorageService;

    @Mock
    private DocumentService documentService;

    @Mock
    private ParseProgressBroadcaster progressBroadcaster;

    @Spy
    @InjectMocks
    private DocumentParseService documentParseService;

    private DocParseTask sampleTask;

    @BeforeEach
    void setUp() {
        sampleTask = new DocParseTask();
        sampleTask.setId(1L);
        sampleTask.setFileName("test_document.pdf");
        sampleTask.setFilePath("docs/test_document.pdf");
        sampleTask.setFileType("pdf");
        sampleTask.setStatus(0); // pending
        sampleTask.setProgress(0);
        sampleTask.setVectorCount(0);
        sampleTask.setCreateTime(new Date());
        sampleTask.setUpdateTime(new Date());
    }

    // ==================== getTaskById Tests ====================

    @Test
    @DisplayName("查询任务 - 存在")
    void getTaskById_exists() {
        when(taskMapper.selectById(1L)).thenReturn(sampleTask);

        DocParseTask result = documentParseService.getTaskById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("test_document.pdf", result.getFileName());
        assertEquals("pdf", result.getFileType());
        verify(taskMapper).selectById(1L);
    }

    @Test
    @DisplayName("查询任务 - 不存在返回 null")
    void getTaskById_notFound() {
        when(taskMapper.selectById(999L)).thenReturn(null);

        DocParseTask result = documentParseService.getTaskById(999L);

        assertNull(result);
        verify(taskMapper).selectById(999L);
    }

    // ==================== listTasks Tests ====================

    @Test
    @DisplayName("分页查询 - 无过滤条件")
    void listTasks_noFilter() {
        Page<DocParseTask> expectedPage = new Page<>(1, 10);
        expectedPage.setRecords(Collections.singletonList(sampleTask));
        expectedPage.setTotal(1);

        when(taskMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(expectedPage);

        Page<DocParseTask> result = documentParseService.listTasks(1, 10, null, null);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
        verify(taskMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("分页查询 - 按状态过滤")
    void listTasks_filterByStatus() {
        DocParseTask completedTask = new DocParseTask();
        completedTask.setId(2L);
        completedTask.setFileName("completed.pdf");
        completedTask.setStatus(2);
        completedTask.setCreateTime(new Date());

        Page<DocParseTask> expectedPage = new Page<>(1, 10);
        expectedPage.setRecords(Collections.singletonList(completedTask));
        expectedPage.setTotal(1);

        when(taskMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(expectedPage);

        Page<DocParseTask> result = documentParseService.listTasks(1, 10, 2, null);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(2, result.getRecords().get(0).getStatus());
    }

    @Test
    @DisplayName("分页查询 - 按文件类型过滤")
    void listTasks_filterByFileType() {
        Page<DocParseTask> expectedPage = new Page<>(1, 10);
        expectedPage.setRecords(Arrays.asList(sampleTask));
        expectedPage.setTotal(1);

        when(taskMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(expectedPage);

        Page<DocParseTask> result = documentParseService.listTasks(1, 10, null, "pdf");

        assertNotNull(result);
        assertEquals(1, result.getTotal());
    }

    @Test
    @DisplayName("分页查询 - 空结果")
    void listTasks_emptyResult() {
        Page<DocParseTask> expectedPage = new Page<>(1, 10);
        expectedPage.setRecords(Collections.emptyList());
        expectedPage.setTotal(0);

        when(taskMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(expectedPage);

        Page<DocParseTask> result = documentParseService.listTasks(1, 10, null, null);

        assertNotNull(result);
        assertEquals(0, result.getTotal());
        assertTrue(result.getRecords().isEmpty());
    }

    @Test
    @DisplayName("分页查询 - 分页参数生效")
    void listTasks_pagination() {
        Page<DocParseTask> expectedPage = new Page<>(2, 5);
        expectedPage.setRecords(Arrays.asList(sampleTask, sampleTask));
        expectedPage.setTotal(12);

        when(taskMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(expectedPage);

        Page<DocParseTask> result = documentParseService.listTasks(2, 5, null, null);

        assertNotNull(result);
        assertEquals(12, result.getTotal());
        assertEquals(2, result.getCurrent());
        assertEquals(5, result.getSize());
    }

    // ==================== retryTask Tests ====================

    @Test
    @DisplayName("重试任务 - 成功")
    void retryTask_success() {
        DocParseTask failedTask = new DocParseTask();
        failedTask.setId(1L);
        failedTask.setFileName("failed.pdf");
        failedTask.setFilePath("docs/failed.pdf");
        failedTask.setFileType("pdf");
        failedTask.setStatus(3); // failed
        failedTask.setProgress(50);
        failedTask.setErrorMsg("解析超时");
        failedTask.setDocumentId(100L);

        when(taskMapper.selectById(1L)).thenReturn(failedTask);
        when(taskMapper.updateById(any(DocParseTask.class))).thenReturn(1);

        // Do nothing for asyncParse to avoid NPE from mock dependencies
        doNothing().when(documentParseService).asyncParse(anyLong(), anyString(), anyString());

        documentParseService.retryTask(1L);

        // Verify the task was reset to pending state
        verify(taskMapper).updateById(argThat(task ->
                task.getId().equals(1L)
                        && task.getStatus() == 0
                        && task.getProgress() == 0
                        && task.getErrorMsg() == null
                        && task.getDocumentId() == null
        ));
    }

    @Test
    @DisplayName("重试任务 - 任务不存在抛出异常")
    void retryTask_notFound() {
        when(taskMapper.selectById(999L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> documentParseService.retryTask(999L));

        assertTrue(exception.getMessage().contains("解析任务不存在"));
        assertEquals(ErrorCode.DOC_PARSE_TASK_NOT_FOUND.getCode(), exception.getCode());
        verify(taskMapper, never()).updateById(any());
    }

    // ==================== deleteTaskById Tests ====================

    @Test
    @DisplayName("删除任务 - 成功")
    void deleteTaskById_success() {
        when(taskMapper.deleteById(1L)).thenReturn(1);

        boolean result = documentParseService.deleteTaskById(1L);

        assertTrue(result);
        verify(taskMapper).deleteById(1L);
    }

    @Test
    @DisplayName("删除任务 - 失败（影响行数为0）")
    void deleteTaskById_failure() {
        when(taskMapper.deleteById(999L)).thenReturn(0);

        boolean result = documentParseService.deleteTaskById(999L);

        assertFalse(result);
        verify(taskMapper).deleteById(999L);
    }

    // ==================== Edge Cases ====================

    @Test
    @DisplayName("分页查询 - 状态和文件类型同时过滤")
    void listTasks_combinedFilter() {
        Page<DocParseTask> expectedPage = new Page<>(1, 10);
        expectedPage.setRecords(Collections.singletonList(sampleTask));
        expectedPage.setTotal(1);

        when(taskMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(expectedPage);

        Page<DocParseTask> result = documentParseService.listTasks(1, 10, 0, "pdf");

        assertNotNull(result);
        verify(taskMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("查询任务 - 已完成任务")
    void getTaskById_completedTask() {
        DocParseTask completedTask = new DocParseTask();
        completedTask.setId(2L);
        completedTask.setFileName("done.docx");
        completedTask.setFilePath("docs/done.docx");
        completedTask.setFileType("docx");
        completedTask.setStatus(2);
        completedTask.setProgress(100);
        completedTask.setVectorCount(45);
        completedTask.setDocumentId(100L);
        completedTask.setCreateTime(new Date());
        completedTask.setParseTime(new Date());

        when(taskMapper.selectById(2L)).thenReturn(completedTask);

        DocParseTask result = documentParseService.getTaskById(2L);

        assertNotNull(result);
        assertEquals(2, result.getStatus());
        assertEquals(100, result.getProgress());
        assertEquals(45, result.getVectorCount());
        assertEquals(100L, result.getDocumentId());
    }

    @Test
    @DisplayName("重试任务 - 重置所有失败相关字段")
    void retryTask_resetAllFields() {
        DocParseTask failedTask = new DocParseTask();
        failedTask.setId(5L);
        failedTask.setFileName("error.pdf");
        failedTask.setFilePath("docs/error.pdf");
        failedTask.setFileType("pdf");
        failedTask.setStatus(3);
        failedTask.setProgress(30);
        failedTask.setErrorMsg("OOM");
        failedTask.setDocumentId(200L);
        failedTask.setParseTime(new Date());

        when(taskMapper.selectById(5L)).thenReturn(failedTask);
        when(taskMapper.updateById(any(DocParseTask.class))).thenReturn(1);

        // Do nothing for asyncParse to avoid NPE from mock dependencies
        doNothing().when(documentParseService).asyncParse(anyLong(), anyString(), anyString());

        documentParseService.retryTask(5L);

        verify(taskMapper).updateById(argThat(task ->
                task.getStatus() == 0
                        && task.getProgress() == 0
                        && task.getErrorMsg() == null
                        && task.getVectorCount() == 0
                        && task.getDocumentId() == null
                        && task.getParseTime() == null
        ));
    }

    @Test
    @DisplayName("分页查询 - 多结果列表")
    void listTasks_multipleResults() {
        DocParseTask task2 = new DocParseTask();
        task2.setId(2L);
        task2.setFileName("report.docx");
        task2.setFileType("docx");
        task2.setStatus(1);
        task2.setCreateTime(new Date());

        Page<DocParseTask> expectedPage = new Page<>(1, 10);
        expectedPage.setRecords(Arrays.asList(sampleTask, task2));
        expectedPage.setTotal(2);

        when(taskMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(expectedPage);

        Page<DocParseTask> result = documentParseService.listTasks(1, 10, null, null);

        assertNotNull(result);
        assertEquals(2, result.getTotal());
        assertEquals(2, result.getRecords().size());
    }
}
