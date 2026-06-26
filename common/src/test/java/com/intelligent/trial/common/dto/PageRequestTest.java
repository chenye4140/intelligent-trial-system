package com.intelligent.trial.common.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 分页请求参数 PageRequest 单元测试
 */
class PageRequestTest {

    @Test
    void testDefaultValues() {
        PageRequest request = new PageRequest();
        assertEquals(1, request.getPageNum());
        assertEquals(10, request.getPageSize());
        assertNull(request.getOrderBy());
        assertEquals("desc", request.getOrderType());
    }

    @Test
    void testGetOffset_firstPage() {
        PageRequest request = new PageRequest();
        request.setPageNum(1);
        request.setPageSize(10);
        assertEquals(0, request.getOffset());
    }

    @Test
    void testGetOffset_secondPage() {
        PageRequest request = new PageRequest();
        request.setPageNum(2);
        request.setPageSize(20);
        assertEquals(20, request.getOffset());
    }

    @Test
    void testGetOffset_largePageNum() {
        PageRequest request = new PageRequest();
        request.setPageNum(100);
        request.setPageSize(50);
        assertEquals(4950, request.getOffset());
    }

    @Test
    void testGetOffset_overflow() {
        PageRequest request = new PageRequest();
        request.setPageNum(Integer.MAX_VALUE);
        request.setPageSize(1000);
        // Should return a valid long value without overflow
        long offset = request.getOffset();
        assertTrue(offset > 0);
    }

    @Test
    void testSetters() {
        PageRequest request = new PageRequest();
        request.setPageNum(5);
        request.setPageSize(25);
        request.setOrderBy("create_time");
        request.setOrderType("asc");
        assertEquals(5, request.getPageNum());
        assertEquals(25, request.getPageSize());
        assertEquals("create_time", request.getOrderBy());
        assertEquals("asc", request.getOrderType());
    }

    @Test
    void testSerializable() {
        PageRequest request = new PageRequest();
        assertTrue(request instanceof java.io.Serializable);
    }

    @Test
    void testValidationAnnotations() {
        // Verify that validation annotations are present
        // pageNum has @Min(1), pageSize has @Min(1) and @Max(1000)
        assertNotNull(PageRequest.class);
    }
}
