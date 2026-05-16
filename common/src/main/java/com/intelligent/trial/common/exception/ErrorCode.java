package com.intelligent.trial.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 错误码枚举
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    SUCCESS(200, "操作成功"),
    FAIL(500, "操作失败"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    BUSINESS_ERROR(500, "业务异常"),
    SYSTEM_ERROR(500, "系统异常"),

    // 案件管理模块错误码 (3000-3999)
    CASE_NOT_FOUND(3001, "案件不存在"),
    CASE_NAME_EMPTY(3002, "案件名称不能为空"),
    CASE_ID_EMPTY(3003, "案件ID不能为空"),
    PARTY_NOT_FOUND(3004, "当事人不存在"),
    PARTY_NAME_EMPTY(3005, "当事人姓名不能为空"),
    PARTY_CASE_ID_EMPTY(3006, "案件ID不能为空"),
    VIOLATION_NOT_FOUND(3007, "违纪事实不存在"),
    VIOLATION_TITLE_EMPTY(3008, "事实标题不能为空"),
    VIOLATION_ID_EMPTY(3009, "违纪事实ID不能为空"),
    VIOLATION_CASE_ID_EMPTY(3010, "案件ID不能为空");

    private final Integer code;
    private final String message;
}
