package com.intelligent.trial.common.annotation;

import java.lang.annotation.*;

/**
 * 权限注解
 * 用于标记接口需要的权限标识
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {
    /**
     * 权限标识（如 system:user:add）
     */
    String value();
}
