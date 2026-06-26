package com.intelligent.trial.common.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 统一响应类 R<T> 单元测试
 */
class RTest {

    @Test
    void testOk_noArgs() {
        R<Void> result = R.ok();
        assertEquals(200, result.getCode());
        assertEquals("操作成功", result.getMsg());
        assertNull(result.getData());
        assertTrue(result.isSuccess());
    }

    @Test
    void testOk_withData() {
        String data = "hello";
        R<String> result = R.ok(data);
        assertEquals(200, result.getCode());
        assertEquals("操作成功", result.getMsg());
        assertEquals("hello", result.getData());
        assertTrue(result.isSuccess());
    }

    @Test
    void testOk_withMsgAndData() {
        R<Integer> result = R.ok("查询成功", 42);
        assertEquals(200, result.getCode());
        assertEquals("查询成功", result.getMsg());
        assertEquals(42, result.getData());
        assertTrue(result.isSuccess());
    }

    @Test
    void testFail_noArgs() {
        R<Void> result = R.fail();
        assertEquals(500, result.getCode());
        assertEquals("操作失败", result.getMsg());
        assertNull(result.getData());
        assertFalse(result.isSuccess());
    }

    @Test
    void testFail_withMsg() {
        R<Void> result = R.fail("参数不合法");
        assertEquals(500, result.getCode());
        assertEquals("参数不合法", result.getMsg());
        assertNull(result.getData());
        assertFalse(result.isSuccess());
    }

    @Test
    void testFail_withCodeAndMsg() {
        R<Void> result = R.fail(404, "资源不存在");
        assertEquals(404, result.getCode());
        assertEquals("资源不存在", result.getMsg());
        assertNull(result.getData());
        assertFalse(result.isSuccess());
    }

    @Test
    void testIsSuccess_nullCode() {
        R<Void> result = new R<>();
        assertFalse(result.isSuccess());
    }

    @Test
    void testIsSuccess_non200() {
        R<Void> result = new R<>(401, "未授权", null);
        assertFalse(result.isSuccess());
    }

    @Test
    void testSettersAndGetters() {
        R<String> result = new R<>();
        result.setCode(200);
        result.setMsg("success");
        result.setData("test");
        assertEquals(200, result.getCode());
        assertEquals("success", result.getMsg());
        assertEquals("test", result.getData());
        assertTrue(result.isSuccess());
    }

    @Test
    void testSerializable() {
        R<String> result = R.ok("data");
        // Verify it implements Serializable
        assertTrue(result instanceof java.io.Serializable);
        assertNotNull(result);
    }

    @Test
    void testOk_withNullData() {
        R<String> result = R.ok(null);
        assertEquals(200, result.getCode());
        assertNull(result.getData());
        assertTrue(result.isSuccess());
    }

    @Test
    void testConstructor_withAllArgs() {
        R<String> result = new R<>(400, "参数错误", "detail");
        assertEquals(400, result.getCode());
        assertEquals("参数错误", result.getMsg());
        assertEquals("detail", result.getData());
        assertFalse(result.isSuccess());
    }
}
