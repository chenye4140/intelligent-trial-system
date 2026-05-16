package com.intelligent.trial.common.util;

/**
 * 用户上下文（ThreadLocal）
 * 由各模块的 JWT 拦截器填充，业务代码通过此工具类获取当前登录用户信息。
 * 
 * <p>使用方式：</p>
 * <pre>
 * // 拦截器中设置
 * UserContext.setUserId(userId);
 * UserContext.setUsername(username);
 * 
 * // 业务代码中获取
 * Long userId = UserContext.getUserId();
 * String username = UserContext.getUsername();
 * 
 * // 请求结束后清理（重要！防止内存泄漏）
 * UserContext.clear();
 * </pre>
 *
 * @author intelligent-trial
 */
public class UserContext {

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> USERNAME = new ThreadLocal<>();

    public static void setUserId(Long userId) {
        USER_ID.set(userId);
    }

    public static Long getUserId() {
        return USER_ID.get();
    }

    public static void setUsername(String username) {
        USERNAME.set(username);
    }

    public static String getUsername() {
        return USERNAME.get();
    }

    public static void clear() {
        USER_ID.remove();
        USERNAME.remove();
    }
}
