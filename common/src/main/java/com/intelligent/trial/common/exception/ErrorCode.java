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

    // Auth 认证模块错误码 (1000-1999)
    AUTH_USERNAME_EMPTY(1001, "用户名不能为空"),
    AUTH_PASSWORD_EMPTY(1002, "密码不能为空"),
    AUTH_USER_NOT_FOUND(1003, "用户不存在"),
    AUTH_USER_DISABLED(1004, "用户已被禁用"),
    AUTH_USERNAME_EXISTS(1005, "用户名已存在"),
    AUTH_PASSWORD_WRONG(1006, "密码错误"),
    AUTH_TOKEN_EXPIRED(1007, "Token已过期"),
    AUTH_TOKEN_INVALID(1008, "Token无效"),
    AUTH_NO_PERMISSION(1009, "无权限访问"),
    AUTH_SUPER_ADMIN_PROTECTED(1010, "超级管理员不可删除"),
    AUTH_ROLE_NOT_FOUND(1011, "角色不存在"),
    AUTH_ROLE_CODE_EXISTS(1012, "角色编码已存在"),
    AUTH_ROLE_HAS_USERS(1013, "角色下有用户，不可删除"),
    AUTH_MENU_NOT_FOUND(1014, "菜单不存在"),
    AUTH_MENU_REFERENCED(1015, "菜单已被角色引用，不可删除"),
    AUTH_DEPT_NOT_FOUND(1016, "部门不存在"),
    AUTH_DEPT_HAS_CHILDREN(1017, "部门下有子部门，不可删除"),
    AUTH_USER_ID_EMPTY(1018, "用户ID不能为空"),
    AUTH_OLD_PASSWORD_WRONG(1019, "旧密码错误"),

    // Document 文档模块错误码 (2000-2999)
    DOC_NOT_FOUND(2001, "文档不存在"),
    DOC_UPLOAD_EMPTY(2002, "上传文件不能为空"),
    DOC_UPLOAD_FAILED(2003, "文件上传失败"),
    DOC_DOWNLOAD_FAILED(2004, "文件下载失败"),
    DOC_PARSE_FAILED(2005, "文档解析失败"),
    DOC_TYPE_UNSUPPORTED(2006, "不支持的文件类型"),
    DOC_PREVIEW_FAILED(2007, "文件预览失败"),
    DOC_TOO_LARGE(2008, "文件过大"),
    DOC_PARSE_TASK_NOT_FOUND(2009, "解析任务不存在"),
    DOC_VECTOR_FAILED(2010, "向量生成失败"),

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
