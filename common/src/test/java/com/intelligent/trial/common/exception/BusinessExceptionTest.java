package com.intelligent.trial.common.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 业务异常 BusinessException 单元测试
 */
class BusinessExceptionTest {

    @Test
    void testConstructor_withMessage() {
        BusinessException ex = new BusinessException("用户名已存在");
        assertEquals("用户名已存在", ex.getMessage());
        assertEquals(500, ex.getCode());
    }

    @Test
    void testConstructor_withCodeAndMessage() {
        BusinessException ex = new BusinessException(404, "资源不存在");
        assertEquals("资源不存在", ex.getMessage());
        assertEquals(404, ex.getCode());
    }

    @Test
    void testConstructor_withErrorCode() {
        BusinessException ex = new BusinessException(ErrorCode.AUTH_USER_NOT_FOUND);
        assertEquals("用户不存在", ex.getMessage());
        assertEquals(1003, ex.getCode());
    }

    @Test
    void testConstructor_withErrorCode_unauthorized() {
        BusinessException ex = new BusinessException(ErrorCode.UNAUTHORIZED);
        assertEquals("未授权", ex.getMessage());
        assertEquals(401, ex.getCode());
    }

    @Test
    void testConstructor_withErrorCode_forbidden() {
        BusinessException ex = new BusinessException(ErrorCode.FORBIDDEN);
        assertEquals("禁止访问", ex.getMessage());
        assertEquals(403, ex.getCode());
    }

    @Test
    void testIsRuntimeException() {
        // BusinessException extends RuntimeException
        assertTrue(new BusinessException("test") instanceof RuntimeException);
    }

    @Test
    void testAllErrorCodeEnums() {
        // Test that all ErrorCode enums can create BusinessException
        for (ErrorCode code : ErrorCode.values()) {
            BusinessException ex = new BusinessException(code);
            assertNotNull(ex.getMessage());
            assertNotNull(ex.getCode());
            assertEquals(code.getCode(), ex.getCode());
            assertEquals(code.getMessage(), ex.getMessage());
        }
    }

    @Test
    void testErrorCodeCoverage() {
        // Verify key error code ranges
        // Auth: 1000-1999
        assertEquals(1001, ErrorCode.AUTH_USERNAME_EMPTY.getCode());
        assertEquals(1019, ErrorCode.AUTH_OLD_PASSWORD_WRONG.getCode());

        // Document: 2000-2999
        assertEquals(2001, ErrorCode.DOC_NOT_FOUND.getCode());
        assertEquals(2011, ErrorCode.DOC_VECTOR_FAILED.getCode());

        // Case: 3000-3999
        assertEquals(3001, ErrorCode.CASE_NOT_FOUND.getCode());
        assertEquals(3010, ErrorCode.VIOLATION_CASE_ID_EMPTY.getCode());

        // AI: 4000-4999
        assertEquals(4001, ErrorCode.AI_SERVICE_UNAVAILABLE.getCode());

        // Storage: 5000-5999
        assertEquals(5001, ErrorCode.STORAGE_BUCKET_ERROR.getCode());

        // Punishment: 6000-6999
        assertEquals(6001, ErrorCode.PUNISHMENT_NOT_FOUND.getCode());

        // Note: 7000-7999
        assertEquals(7001, ErrorCode.NOTE_NOT_FOUND.getCode());

        // Directory: 8000-8999
        assertEquals(8001, ErrorCode.DIRECTORY_NOT_FOUND.getCode());

        // Workflow: 9000-9999
        assertEquals(9001, ErrorCode.WORKFLOW_INSTANCE_NOT_FOUND.getCode());

        // Report: 10000-10999
        assertEquals(10001, ErrorCode.REPORT_CASE_NOT_FOUND.getCode());

        // Promotion: 11000-11999
        assertEquals(11001, ErrorCode.PROMOTION_CASE_NOT_FOUND.getCode());
    }

    @Test
    void testErrorCodeMessageNotNull() {
        for (ErrorCode code : ErrorCode.values()) {
            assertNotNull(code.getMessage(), "ErrorCode " + code.name() + " has null message");
            assertFalse(code.getMessage().isEmpty(), "ErrorCode " + code.name() + " has empty message");
        }
    }
}
