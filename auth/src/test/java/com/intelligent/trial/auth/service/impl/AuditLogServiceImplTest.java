package com.intelligent.trial.auth.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.intelligent.trial.auth.entity.SysAuditLog;
import com.intelligent.trial.auth.mapper.SysAuditLogMapper;
import com.intelligent.trial.auth.vo.AuditLogVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AuditLogServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
class AuditLogServiceImplTest {

    @InjectMocks
    private AuditLogServiceImpl auditLogService;

    @Mock
    private SysAuditLogMapper baseMapper;

    @Test
    void pageLog_shouldReturnPagedResults() {
        Page<AuditLogVO> expectedPage = new Page<>(1, 10);
        AuditLogVO log1 = new AuditLogVO();
        log1.setId(1L);
        log1.setModule("auth");
        log1.setAction("LOGIN");
        log1.setUserId(1L);

        AuditLogVO log2 = new AuditLogVO();
        log2.setId(2L);
        log2.setModule("case");
        log2.setAction("CREATE");
        log2.setUserId(2L);

        expectedPage.setRecords(Arrays.asList(log1, log2));
        expectedPage.setTotal(2);

        when(baseMapper.selectLogPage(any(Page.class), isNull(), isNull(), isNull(), isNull(), isNull()))
            .thenReturn(expectedPage);

        Page<AuditLogVO> result = auditLogService.pageLog(1, 10, null, null, null, null, null);

        assertNotNull(result);
        assertEquals(2, result.getTotal());
        assertEquals(2, result.getRecords().size());
        assertEquals("auth", result.getRecords().get(0).getModule());
    }

    @Test
    void pageLog_shouldPassFiltersToMapper() {
        Page<AuditLogVO> emptyPage = new Page<>(1, 10);
        emptyPage.setRecords(Arrays.asList());
        emptyPage.setTotal(0);

        when(baseMapper.selectLogPage(any(Page.class), eq("auth"), eq("LOGIN"), eq(1L), eq("2024-01-01"), eq("2024-12-31")))
            .thenReturn(emptyPage);

        auditLogService.pageLog(1, 10, "auth", "LOGIN", 1L, "2024-01-01", "2024-12-31");

        verify(baseMapper).selectLogPage(
            any(Page.class),
            eq("auth"),
            eq("LOGIN"),
            eq(1L),
            eq("2024-01-01"),
            eq("2024-12-31")
        );
    }

    @Test
    void pageLog_shouldHandleNullFilters() {
        Page<AuditLogVO> page = new Page<>(2, 20);
        page.setRecords(Arrays.asList());
        page.setTotal(0);

        when(baseMapper.selectLogPage(any(Page.class), isNull(), isNull(), isNull(), isNull(), isNull()))
            .thenReturn(page);

        Page<AuditLogVO> result = auditLogService.pageLog(2, 20, null, null, null, null, null);

        assertNotNull(result);
        assertEquals(2, result.getCurrent());
        assertEquals(20, result.getSize());
    }

    @Test
    void pageLog_shouldReturnEmptyWhenNoRecords() {
        Page<AuditLogVO> emptyPage = new Page<>(1, 10);
        emptyPage.setRecords(Arrays.asList());
        emptyPage.setTotal(0);

        when(baseMapper.selectLogPage(any(Page.class), any(), any(), any(), any(), any()))
            .thenReturn(emptyPage);

        Page<AuditLogVO> result = auditLogService.pageLog(1, 10, "case", null, null, null, null);

        assertNotNull(result);
        assertEquals(0, result.getTotal());
        assertTrue(result.getRecords().isEmpty());
    }

    @Test
    void pageLog_shouldHandlePartialFilters() {
        Page<AuditLogVO> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList());
        page.setTotal(0);

        when(baseMapper.selectLogPage(any(Page.class), eq("auth"), isNull(), isNull(), isNull(), isNull()))
            .thenReturn(page);

        auditLogService.pageLog(1, 10, "auth", null, null, null, null);

        verify(baseMapper).selectLogPage(
            any(Page.class),
            eq("auth"),
            isNull(),
            isNull(),
            isNull(),
            isNull()
        );
    }

    @Test
    void pageLog_shouldFilterByUserIdOnly() {
        Page<AuditLogVO> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList());
        page.setTotal(0);

        when(baseMapper.selectLogPage(any(Page.class), isNull(), isNull(), eq(5L), isNull(), isNull()))
            .thenReturn(page);

        auditLogService.pageLog(1, 10, null, null, 5L, null, null);

        verify(baseMapper).selectLogPage(
            any(Page.class),
            isNull(),
            isNull(),
            eq(5L),
            isNull(),
            isNull()
        );
    }

    @Test
    void pageLog_shouldFilterByTimeRangeOnly() {
        Page<AuditLogVO> page = new Page<>(1, 50);
        page.setRecords(Arrays.asList());
        page.setTotal(0);

        when(baseMapper.selectLogPage(any(Page.class), isNull(), isNull(), isNull(), eq("2024-01-01 00:00:00"), eq("2024-12-31 23:59:59")))
            .thenReturn(page);

        auditLogService.pageLog(1, 50, null, null, null, "2024-01-01 00:00:00", "2024-12-31 23:59:59");

        verify(baseMapper).selectLogPage(
            any(Page.class),
            isNull(),
            isNull(),
            isNull(),
            eq("2024-01-01 00:00:00"),
            eq("2024-12-31 23:59:59")
        );
    }
}
