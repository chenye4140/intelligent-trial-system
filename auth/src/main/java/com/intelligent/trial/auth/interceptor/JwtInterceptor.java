package com.intelligent.trial.auth.interceptor;

import com.alibaba.fastjson2.JSON;
import com.intelligent.trial.auth.util.JwtUtil;
import com.intelligent.trial.common.dto.R;
import com.intelligent.trial.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * JWT 验证拦截器
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
            sendUnauthorized(response, "未提供认证令牌");
            return false;
        }

        // 检查 Token 是否在黑名单中
        if (Boolean.TRUE.equals(redisTemplate.hasKey(TOKEN_BLACKLIST_PREFIX + token))) {
            sendUnauthorized(response, "令牌已失效，请重新登录");
            return false;
        }

        // 验证 Token
        if (!jwtUtil.validateToken(token)) {
            sendUnauthorized(response, "令牌无效或已过期");
            return false;
        }

        // 将用户信息存入请求属性
        Long userId = jwtUtil.getUserId(token);
        String username = jwtUtil.getUsername(token);
        request.setAttribute("userId", userId);
        request.setAttribute("username", username);

        return true;
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
