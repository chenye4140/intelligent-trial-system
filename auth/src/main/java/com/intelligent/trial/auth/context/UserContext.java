package com.intelligent.trial.auth.context;

/**
 * 用户上下文（兼容层）
 * 
 * <p>此类已废弃，所有功能已迁移到 {@link com.intelligent.trial.common.util.UserContext}。</p>
 * <p>保留此类仅为向后兼容，所有调用直接委托到 common 模块的 UserContext。</p>
 *
 * @deprecated 请使用 {@link com.intelligent.trial.common.util.UserContext}
 */
@Deprecated
public class UserContext {

    public static void setUserId(Long userId) {
        com.intelligent.trial.common.util.UserContext.setUserId(userId);
    }

    public static Long getUserId() {
        return com.intelligent.trial.common.util.UserContext.getUserId();
    }

    public static void setUsername(String username) {
        com.intelligent.trial.common.util.UserContext.setUsername(username);
    }

    public static String getUsername() {
        return com.intelligent.trial.common.util.UserContext.getUsername();
    }

    public static void clear() {
        com.intelligent.trial.common.util.UserContext.clear();
    }
}
