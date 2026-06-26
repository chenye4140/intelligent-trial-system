package com.intelligent.trial.common.annotation;

import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 自定义注解单元测试
 */
class AnnotationTest {

    @Test
    void testRequireLog_exists() {
        // Verify @RequireLog annotation class exists and has expected properties
        assertNotNull(RequireLog.class);
        assertTrue(java.lang.annotation.Annotation.class.isAssignableFrom(RequireLog.class));
    }

    @Test
    void testRequireLog_hasModuleMethod() throws NoSuchMethodException {
        assertNotNull(RequireLog.class.getMethod("module"));
    }

    @Test
    void testRequireLog_hasActionMethod() throws NoSuchMethodException {
        assertNotNull(RequireLog.class.getMethod("action"));
    }

    @Test
    void testRequireLog_hasDescriptionMethod() throws NoSuchMethodException {
        assertNotNull(RequireLog.class.getMethod("description"));
    }

    @Test
    void testRequirePermission_exists() {
        assertNotNull(RequirePermission.class);
        assertTrue(java.lang.annotation.Annotation.class.isAssignableFrom(RequirePermission.class));
    }

    @Test
    void testRequirePermission_hasValueMethod() throws NoSuchMethodException {
        // @RequirePermission should have a value() method for the permission string
        assertNotNull(RequirePermission.class.getMethod("value"));
    }

    @Test
    void testRequireLog_retentionPolicy() {
        // Verify annotation is retained at runtime
        java.lang.annotation.Retention retention = RequireLog.class.getAnnotation(java.lang.annotation.Retention.class);
        assertNotNull(retention);
        assertEquals(java.lang.annotation.RetentionPolicy.RUNTIME, retention.value());
    }

    @Test
    void testRequirePermission_retentionPolicy() {
        java.lang.annotation.Retention retention = RequirePermission.class.getAnnotation(java.lang.annotation.Retention.class);
        assertNotNull(retention);
        assertEquals(java.lang.annotation.RetentionPolicy.RUNTIME, retention.value());
    }

    @Test
    void testRequireLog_targetType() {
        java.lang.annotation.Target target = RequireLog.class.getAnnotation(java.lang.annotation.Target.class);
        assertNotNull(target);
        // Should be applicable to methods
        boolean hasMethod = false;
        for (java.lang.annotation.ElementType et : target.value()) {
            if (et == java.lang.annotation.ElementType.METHOD) {
                hasMethod = true;
                break;
            }
        }
        assertTrue(hasMethod, "@RequireLog should be applicable to methods");
    }

    @Test
    void testRequirePermission_targetType() {
        java.lang.annotation.Target target = RequirePermission.class.getAnnotation(java.lang.annotation.Target.class);
        assertNotNull(target);
        boolean hasMethod = false;
        for (java.lang.annotation.ElementType et : target.value()) {
            if (et == java.lang.annotation.ElementType.METHOD) {
                hasMethod = true;
                break;
            }
        }
        assertTrue(hasMethod, "@RequirePermission should be applicable to methods");
    }
}
