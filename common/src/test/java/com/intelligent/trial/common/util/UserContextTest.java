package com.intelligent.trial.common.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户上下文 UserContext 单元测试
 */
class UserContextTest {

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void testSetAndGetUserId() {
        UserContext.setUserId(123L);
        assertEquals(123L, UserContext.getUserId());
    }

    @Test
    void testSetAndGetUsername() {
        UserContext.setUsername("admin");
        assertEquals("admin", UserContext.getUsername());
    }

    @Test
    void testSetAndGetBoth() {
        UserContext.setUserId(456L);
        UserContext.setUsername("testuser");
        assertEquals(456L, UserContext.getUserId());
        assertEquals("testuser", UserContext.getUsername());
    }

    @Test
    void testClear_resetsUserId() {
        UserContext.setUserId(789L);
        assertNotNull(UserContext.getUserId());
        UserContext.clear();
        assertNull(UserContext.getUserId());
    }

    @Test
    void testClear_resetsUsername() {
        UserContext.setUsername("cleared");
        assertNotNull(UserContext.getUsername());
        UserContext.clear();
        assertNull(UserContext.getUsername());
    }

    @Test
    void testGetBeforeSet_returnsNull() {
        UserContext.clear();
        assertNull(UserContext.getUserId());
        assertNull(UserContext.getUsername());
    }

    @Test
    void testOverwriteUserId() {
        UserContext.setUserId(1L);
        UserContext.setUserId(2L);
        assertEquals(2L, UserContext.getUserId());
    }

    @Test
    void testOverwriteUsername() {
        UserContext.setUsername("old");
        UserContext.setUsername("new");
        assertEquals("new", UserContext.getUsername());
    }

    @Test
    void testThreadIsolation() throws InterruptedException {
        // Verify ThreadLocal isolation between threads
        UserContext.setUserId(100L);
        UserContext.setUsername("main");

        final Long[] threadUserId = new Long[1];
        final String[] threadUsername = new String[1];

        Thread thread = new Thread(() -> {
            // In a new thread, values should be null
            threadUserId[0] = UserContext.getUserId();
            threadUsername[0] = UserContext.getUsername();

            // Set different values in this thread
            UserContext.setUserId(200L);
            UserContext.setUsername("worker");
        });
        thread.start();
        thread.join();

        // New thread should have null initially
        assertNull(threadUserId[0]);
        assertNull(threadUsername[0]);

        // Main thread values should be unchanged
        assertEquals(100L, UserContext.getUserId());
        assertEquals("main", UserContext.getUsername());
    }

    @Test
    void testClear_isIdempotent() {
        UserContext.clear();
        UserContext.clear(); // Should not throw
        assertNull(UserContext.getUserId());
        assertNull(UserContext.getUsername());
    }

    @Test
    void testSetNullUserId() {
        UserContext.setUserId(null);
        assertNull(UserContext.getUserId());
    }

    @Test
    void testSetNullUsername() {
        UserContext.setUsername(null);
        assertNull(UserContext.getUsername());
    }
}
