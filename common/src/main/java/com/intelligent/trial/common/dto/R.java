package com.intelligent.trial.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 统一响应类
 *
 * @param <T> 数据类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class R<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 状态码
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String msg;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 成功响应（无数据）
     */
    public static <T> R<T> ok() {
        return new R<>(200, "操作成功", null);
    }

    /**
     * 成功响应（带数据）
     */
    public static <T> R<T> ok(T data) {
        return new R<>(200, "操作成功", data);
    }

    /**
     * 成功响应（带消息和数据）
     */
    public static <T> R<T> ok(String msg, T data) {
        return new R<>(200, msg, data);
    }

    /**
     * 失败响应
     */
    public static <T> R<T> fail() {
        return new R<>(500, "操作失败", null);
    }

    /**
     * 失败响应（带消息）
     */
    public static <T> R<T> fail(String msg) {
        return new R<>(500, msg, null);
    }

    /**
     * 失败响应（带状态码和消息）
     */
    public static <T> R<T> fail(Integer code, String msg) {
        return new R<>(code, msg, null);
    }

    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return this.code != null && this.code == 200;
    }
}
