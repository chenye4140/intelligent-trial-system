package com.intelligent.trial.common.dto;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 分页响应结果 PageResult<T> 单元测试
 */
class PageResultTest {

    @Test
    void testOf_basic() {
        List<String> data = Arrays.asList("a", "b", "c");
        PageResult<String> result = PageResult.of(30L, 1, 10, data);
        assertEquals(30L, result.getTotal());
        assertEquals(1, result.getPageNum());
        assertEquals(10, result.getPageSize());
        assertEquals(3, result.getPages());
        assertEquals(3, result.getList().size());
    }

    @Test
    void testOf_zeroTotal() {
        PageResult<String> result = PageResult.of(0L, 1, 10, Collections.emptyList());
        assertEquals(0L, result.getTotal());
        assertEquals(0, result.getPages());
        assertTrue(result.getList().isEmpty());
    }

    @Test
    void testOf_exactPages() {
        // 100 items, 10 per page = 10 pages exactly
        PageResult<String> result = PageResult.of(100L, 5, 10, Collections.emptyList());
        assertEquals(10, result.getPages());
    }

    @Test
    void testOf_partialPage() {
        // 95 items, 10 per page = 10 pages (ceil)
        PageResult<String> result = PageResult.of(95L, 1, 10, Collections.emptyList());
        assertEquals(10, result.getPages());
    }

    @Test
    void testOf_singleItem() {
        PageResult<String> result = PageResult.of(1L, 1, 10, Collections.singletonList("only"));
        assertEquals(1, result.getPages());
    }

    @Test
    void testHasPrevious_firstPage() {
        PageResult<String> result = PageResult.of(50L, 1, 10, Collections.emptyList());
        result.setPageNum(1);
        assertFalse(result.hasPrevious());
    }

    @Test
    void testHasPrevious_middlePage() {
        PageResult<String> result = PageResult.of(50L, 3, 10, Collections.emptyList());
        result.setPageNum(3);
        assertTrue(result.hasPrevious());
    }

    @Test
    void testHasNext_notLastPage() {
        PageResult<String> result = PageResult.of(50L, 3, 10, Collections.emptyList());
        result.setPageNum(3);
        result.setPages(5);
        assertTrue(result.hasNext());
    }

    @Test
    void testHasNext_lastPage() {
        PageResult<String> result = PageResult.of(50L, 5, 10, Collections.emptyList());
        result.setPageNum(5);
        result.setPages(5);
        assertFalse(result.hasNext());
    }

    @Test
    void testHasNext_beyondLastPage() {
        PageResult<String> result = PageResult.of(50L, 6, 10, Collections.emptyList());
        result.setPageNum(6);
        result.setPages(5);
        assertFalse(result.hasNext());
    }

    @Test
    void testSetters() {
        PageResult<String> result = new PageResult<>();
        result.setTotal(100L);
        result.setPageNum(2);
        result.setPageSize(20);
        result.setPages(5);
        result.setList(Arrays.asList("x", "y"));
        assertEquals(100L, result.getTotal());
        assertEquals(2, result.getPageNum());
        assertEquals(20, result.getPageSize());
        assertEquals(5, result.getPages());
        assertEquals(2, result.getList().size());
    }

    @Test
    void testSerializable() {
        PageResult<String> result = PageResult.of(10L, 1, 5, Collections.emptyList());
        assertTrue(result instanceof java.io.Serializable);
    }

    @Test
    void testOf_largeDataset() {
        // Test with large total count
        PageResult<String> result = PageResult.of(1_000_000L, 1, 100, Collections.emptyList());
        assertEquals(10_000, result.getPages());
    }
}
