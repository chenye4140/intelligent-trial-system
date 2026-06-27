package com.intelligent.trial.auth.interceptor;

import com.intelligent.trial.common.annotation.RequirePermission;
import com.intelligent.trial.common.util.UserContext;
import com.intelligent.trial.auth.entity.SysMenu;
import com.intelligent.trial.auth.mapper.SysMenuMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * PermissionInterceptor 单元测试
 */
@ExtendWith(MockitoExtension.class)
class PermissionInterceptorTest {

    @InjectMocks
    private PermissionInterceptor permissionInterceptor;

    @Mock
    private SysMenuMapper sysMenuMapper;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        UserContext.clear();
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    /**
     * 模拟控制器方法（带权限注解）
     */
    @RequirePermission("system:user:add")
    public void mockControllerMethodWithPermission() {
    }

    /**
     * 模拟控制器方法（不带权限注解）
     */
    public void mockControllerMethodWithoutPermission() {
    }

    @Test
    void preHandle_shouldAllowWhenNoPermissionAnnotation() throws Exception {
        Method method = PermissionInterceptorTest.class.getMethod("mockControllerMethodWithoutPermission");
        HandlerMethod handlerMethod = new HandlerMethod(this, method);

        UserContext.setUserId(1L);
        UserContext.setUsername("test");

        boolean result = permissionInterceptor.preHandle(request, response, handlerMethod);

        assertTrue(result);
        assertEquals(200, response.getStatus());
    }

    @Test
    void preHandle_shouldAllowSuperAdmin() throws Exception {
        Method method = PermissionInterceptorTest.class.getMethod("mockControllerMethodWithPermission");
        HandlerMethod handlerMethod = new HandlerMethod(this, method);

        UserContext.setUserId(1L); // 超级管理员
        UserContext.setUsername("admin");

        boolean result = permissionInterceptor.preHandle(request, response, handlerMethod);

        assertTrue(result);
        verify(sysMenuMapper, never()).selectMenusByUserId(anyLong());
    }

    @Test
    void preHandle_shouldDenyWhenUserIdNull() throws Exception {
        Method method = PermissionInterceptorTest.class.getMethod("mockControllerMethodWithPermission");
        HandlerMethod handlerMethod = new HandlerMethod(this, method);

        boolean result = permissionInterceptor.preHandle(request, response, handlerMethod);

        assertFalse(result);
        assertEquals(403, response.getStatus());
    }

    @Test
    void preHandle_shouldAllowWhenUserHasPermission() throws Exception {
        Method method = PermissionInterceptorTest.class.getMethod("mockControllerMethodWithPermission");
        HandlerMethod handlerMethod = new HandlerMethod(this, method);

        UserContext.setUserId(5L);
        UserContext.setUsername("testuser");

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("auth:perms:5")).thenReturn(null);

        List<SysMenu> menus = Collections.singletonList(createMenu("system:user:add"));
        when(sysMenuMapper.selectMenusByUserId(5L)).thenReturn(menus);

        boolean result = permissionInterceptor.preHandle(request, response, handlerMethod);

        assertTrue(result);
        verify(valueOperations).set(eq("auth:perms:5"), anyList(), eq(30L), any());
    }

    @Test
    void preHandle_shouldDenyWhenUserLacksPermission() throws Exception {
        Method method = PermissionInterceptorTest.class.getMethod("mockControllerMethodWithPermission");
        HandlerMethod handlerMethod = new HandlerMethod(this, method);

        UserContext.setUserId(5L);
        UserContext.setUsername("testuser");

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("auth:perms:5")).thenReturn(null);

        // 用户只有其他权限
        List<SysMenu> menus = Collections.singletonList(createMenu("system:user:view"));
        when(sysMenuMapper.selectMenusByUserId(5L)).thenReturn(menus);

        boolean result = permissionInterceptor.preHandle(request, response, handlerMethod);

        assertFalse(result);
        assertEquals(403, response.getStatus());
    }

    @Test
    void preHandle_shouldUseCacheWhenAvailable() throws Exception {
        Method method = PermissionInterceptorTest.class.getMethod("mockControllerMethodWithPermission");
        HandlerMethod handlerMethod = new HandlerMethod(this, method);

        UserContext.setUserId(5L);
        UserContext.setUsername("testuser");

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        // 缓存中已有权限
        when(valueOperations.get("auth:perms:5")).thenReturn(Arrays.asList("system:user:add", "system:user:edit"));

        boolean result = permissionInterceptor.preHandle(request, response, handlerMethod);

        assertTrue(result);
        verify(sysMenuMapper, never()).selectMenusByUserId(anyLong());
    }

    @Test
    void evictUserPermissions_shouldDeleteCache() {
        when(redisTemplate.delete("auth:perms:5")).thenReturn(true);

        permissionInterceptor.evictUserPermissions(5L);

        verify(redisTemplate).delete("auth:perms:5");
    }

    @Test
    void getUserPermissions_shouldFilterNullAndEmptyPerms() throws Exception {
        Method method = PermissionInterceptorTest.class.getMethod("mockControllerMethodWithPermission");
        HandlerMethod handlerMethod = new HandlerMethod(this, method);

        UserContext.setUserId(5L);
        UserContext.setUsername("testuser");

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("auth:perms:5")).thenReturn(null);

        // 包含 null 和空 perms 的菜单
        SysMenu menuWithPerm = createMenu("system:user:add");
        SysMenu menuWithNullPerm = new SysMenu();
        menuWithNullPerm.setId(2L);
        menuWithNullPerm.setPerms(null);
        SysMenu menuWithEmptyPerm = new SysMenu();
        menuWithEmptyPerm.setId(3L);
        menuWithEmptyPerm.setPerms("");

        List<SysMenu> menus = Arrays.asList(menuWithPerm, menuWithNullPerm, menuWithEmptyPerm);
        when(sysMenuMapper.selectMenusByUserId(5L)).thenReturn(menus);

        boolean result = permissionInterceptor.preHandle(request, response, handlerMethod);

        assertTrue(result); // 应该放行，因为有 "system:user:add"
    }

    private SysMenu createMenu(String perms) {
        SysMenu menu = new SysMenu();
        menu.setId(1L);
        menu.setName("用户管理");
        menu.setPerms(perms);
        menu.setStatus(1);
        menu.setVisible(1);
        return menu;
    }
}
