package com.intelligent.trial.auth.util;

import javax.servlet.http.HttpServletRequest;

/**
 * HTTP 上下文工具类
 */
public class HttpContextUtil {

    /**
     * 获取客户端 IP
     */
    public static String getIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多级代理时取第一个 IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 获取当前请求的用户ID
     */
    public static Long getCurrentUserId(HttpServletRequest request) {
        Object userId = request.getAttribute("userId");
        return userId != null ? (Long) userId : null;
    }

    /**
     * 获取当前请求的用户名
     */
    public static String getCurrentUsername(HttpServletRequest request) {
        Object username = request.getAttribute("username");
        return username != null ? (String) username : null;
    }
}
