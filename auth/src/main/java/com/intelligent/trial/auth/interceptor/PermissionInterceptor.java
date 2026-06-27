package com.intelligent.trial.auth.interceptor;

import com.alibaba.fastjson2.JSON;
import com.intelligent.trial.common.annotation.RequirePermission;
import com.intelligent.trial.common.dto.R;
import com.intelligent.trial.common.exception.ErrorCode;
import com.intelligent.trial.common.util.UserContext;
import com.intelligent.trial.auth.mapper.SysMenuMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 权限验证拦截器
 *
 * <p>功能：</p>
 * <ul>
 *   <li>拦截带有 @RequirePermission 注解的方法</li>
 *   <li>校验当前用户是否具有所需的权限标识</li>
 *   <li>使用 Redis 缓存用户权限列表，避免每次请求都查询数据库</li>
 *   <li>超级管理员（userId=1）自动放行所有权限检查</li>
 * </ul>
 *
 * <p>权限校验流程：</p>
 * <ol>
 *   <li>从方法上获取 @RequirePermission 注解</li>
 *   <li>从 UserContext 获取当前用户ID</li>
 *   <li>从 Redis 缓存获取用户权限列表（缓存未命中时从数据库加载）</li>
 *   <li>校验所需权限是否在用户权限列表中</li>
 *   <li>通过则放行，否则返回 403 禁止访问</li>
 * </ol>
 *
 * <p>缓存策略：</p>
 * <ul>
 *   <li>缓存 Key: auth:perms:{userId}</li>
 *   <li>TTL: 30 分钟</li>
 *   <li>角色分配变更时需主动清除缓存（见 RoleServiceImpl / UserServiceImpl）</li>
 * </ul>
 *
 * @author intelligent-trial
 */
@Slf4j
@Component
public class PermissionInterceptor implements HandlerInterceptor {

    @Autowired
    private SysMenuMapper sysMenuMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 权限缓存前缀
     */
    private static final String PERMS_CACHE_PREFIX = "auth:perms:";

    /**
     * 缓存过期时间（分钟）
     */
    private static final long PERMS_CACHE_TTL = 30;

    /**
     * 超级管理员用户ID（自动拥有所有权限）
     */
    private static final Long SUPER_ADMIN_ID = 1L;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 只处理方法级别的请求
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;

        // 检查方法是否有 @RequirePermission 注解
        RequirePermission requirePermission = handlerMethod.getMethodAnnotation(RequirePermission.class);
        if (requirePermission == null) {
            // 没有权限注解，直接放行
            return true;
        }

        String requiredPerm = requirePermission.value();
        Long userId = UserContext.getUserId();

        if (userId == null) {
            log.warn("权限拦截: 用户未登录，URI={}, 需要权限={}", request.getRequestURI(), requiredPerm);
            sendForbidden(response, "用户未登录");
            return false;
        }

        // 超级管理员自动放行
        if (SUPER_ADMIN_ID.equals(userId)) {
            log.debug("权限放行: 超级管理员, userId={}, 权限={}", userId, requiredPerm);
            return true;
        }

        // 获取用户权限列表（带缓存）
        List<String> userPerms = getUserPermissions(userId);

        // 校验权限
        boolean hasPermission = userPerms.contains(requiredPerm);

        if (hasPermission) {
            log.debug("权限放行: userId={}, 权限={}, URI={}", userId, requiredPerm, request.getRequestURI());
            return true;
        }

        // 权限不足
        log.warn("权限拦截: userId={}, 需要权限={}, 用户权限={}, URI={}",
                userId, requiredPerm, userPerms, request.getRequestURI());
        sendForbidden(response, "没有权限执行此操作");
        return false;
    }

    /**
     * 获取用户权限列表（优先从缓存读取）
     *
     * @param userId 用户ID
     * @return 权限标识列表
     */
    @SuppressWarnings("unchecked")
    private List<String> getUserPermissions(Long userId) {
        String cacheKey = PERMS_CACHE_PREFIX + userId;

        // 尝试从 Redis 缓存读取
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            if (cached instanceof List) {
                return (List<String>) cached;
            }
            // 如果缓存的是字符串（JSON 反序列化后的情况），尝试解析
            return JSON.parseArray(cached.toString(), String.class);
        }

        // 缓存未命中，从数据库加载
        List<String> perms = loadPermissionsFromDatabase(userId);

        // 写入缓存
        if (!perms.isEmpty()) {
            redisTemplate.opsForValue().set(cacheKey, perms, PERMS_CACHE_TTL, TimeUnit.MINUTES);
        }

        return perms;
    }

    /**
     * 从数据库加载用户权限列表
     *
     * @param userId 用户ID
     * @return 权限标识列表
     */
    private List<String> loadPermissionsFromDatabase(Long userId) {
        return sysMenuMapper.selectMenusByUserId(userId).stream()
                .map(menu -> menu.getPerms())
                .filter(perms -> perms != null && !perms.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 清除指定用户的权限缓存
     * <p>在角色分配、菜单分配等权限变更操作后调用</p>
     *
     * @param userId 用户ID
     */
    public void evictUserPermissions(Long userId) {
        String cacheKey = PERMS_CACHE_PREFIX + userId;
        redisTemplate.delete(cacheKey);
        log.debug("清除用户权限缓存: userId={}", userId);
    }

    /**
     * 返回 403 禁止访问响应
     */
    private void sendForbidden(HttpServletResponse response, String message) throws Exception {
        response.setStatus(403);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(JSON.toJSONString(R.fail(ErrorCode.FORBIDDEN.getCode(), message)));
    }
}
