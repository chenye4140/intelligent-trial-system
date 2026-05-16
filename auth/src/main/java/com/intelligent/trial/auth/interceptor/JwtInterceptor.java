package com.intelligent.trial.auth.interceptor;

import com.alibaba.fastjson2.JSON;
import com.intelligent.trial.common.dto.R;
import com.intelligent.trial.common.exception.ErrorCode;
import com.intelligent.trial.common.util.UserContext;
import com.intelligent.trial.auth.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * JWT 验证拦截器
 * 
 * <p>功能：</p>
 * <ul>
 *   <li>验证请求中的 JWT Token 有效性</li>
 *   <li>检查 Token 是否在黑名单中（登出后加入黑名单）</li>
 *   <li>将用户信息存入 ThreadLocal（UserContext）供业务代码使用</li>
 *   <li>请求结束后清理 ThreadLocal，防止内存泄漏</li>
 * </ul>
 *
 * @author intelligent-trial
 */
@Slf4j
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String TOKEN_BLACKLIST_PREFIX = "auth:token:blacklist:";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // OPTIONS 请求直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = getToken(request);
        if (token == null || token.isEmpty()) {
            log.warn("JWT 拦截: 未提供认证令牌, URI={}", request.getRequestURI());
            sendUnauthorized(response, "未提供认证令牌");
            return false;
        }

        // 检查 Token 是否在黑名单中（已登出的 Token）
        if (Boolean.TRUE.equals(redisTemplate.hasKey(TOKEN_BLACKLIST_PREFIX + token))) {
            log.warn("JWT 拦截: 令牌已加入黑名单, URI={}", request.getRequestURI());
            sendUnauthorized(response, "令牌已失效，请重新登录");
            return false;
        }

        // 验证 Token 签名和有效期
        if (!jwtUtil.validateToken(token)) {
            log.warn("JWT 拦截: Token 验证失败, URI={}", request.getRequestURI());
            sendUnauthorized(response, "令牌无效或已过期");
            return false;
        }

        // 将用户信息存入 ThreadLocal（UserContext），供后续业务代码使用
        Long userId = jwtUtil.getUserId(token);
        String username = jwtUtil.getUsername(token);
        log.debug("JWT 验证通过: userId={}, username={}, URI={}", userId, username, request.getRequestURI());
        UserContext.setUserId(userId);
        UserContext.setUsername(username);

        // 同时存入 request 属性，兼容旧代码
        request.setAttribute("userId", userId);
        request.setAttribute("username", username);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 请求结束后清理 ThreadLocal，防止内存泄漏和线程复用导致的数据串扰
        UserContext.clear();
    }

    /**
     * 从请求头中获取 Token
     */
    private String getToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    /**
     * 返回未授权响应
     */
    private void sendUnauthorized(HttpServletResponse response, String message) throws Exception {
        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(JSON.toJSONString(R.fail(ErrorCode.UNAUTHORIZED.getCode(), message)));
    }
}
