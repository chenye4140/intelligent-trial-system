package com.intelligent.trial.auth.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解
 * 用于标记需要记录操作日志的方法
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireLog {
    /**
     * 操作模块名称
     */
    String module() default "";

    /**
     * 操作类型（增/删/改/查/导出等）
     */
    String action() default "";

    /**
     * 操作描述
     */
    String description() default "";
}
