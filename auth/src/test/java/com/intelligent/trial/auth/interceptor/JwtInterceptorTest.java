package com.intelligent.trial.auth.interceptor;

import com.intelligent.trial.auth.config.JwtConfig;
import com.intelligent.trial.auth.util.JwtUtil;
import com.intelligent.trial.common.dto.R;
import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * JwtInterceptor 单元测试
 */
@ExtendWith(MockitoExtension.class)
class JwtInterceptorTest {

    @InjectMocks
    private JwtInterceptor interceptor;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    void preHandle_shouldAllowOptionsRequest() throws Exception {
        request.setMethod("OPTIONS");

        boolean result = interceptor.preHandle(request, response, new Object());

        assertTrue(result);
        assertEquals(200, response.getStatus());
    }

    @Test
    void preHandle_shouldRejectWhenNoToken() throws Exception {
        request.setMethod("GET");

        boolean result = interceptor.preHandle(request, response, new Object());

        assertFalse(result);
        assertEquals(401, response.getStatus());
        String body = response.getContentAsString();
        assertTrue(body.contains("未提供认证令牌"));
    }

    @Test
    void preHandle_shouldRejectWhenTokenInBlacklist() throws Exception {
        request.setMethod("GET");
        request.addHeader("Authorization", "Bearer valid-token");

        when(redisTemplate.hasKey(anyString())).thenReturn(true);

        boolean result = interceptor.preHandle(request, response, new Object());

        assertFalse(result);
        assertEquals(401, response.getStatus());
        String body = response.getContentAsString();
        assertTrue(body.contains("令牌已失效"));
    }

    @Test
    void preHandle_shouldRejectWhenTokenInvalid() throws Exception {
        request.setMethod("GET");
        request.addHeader("Authorization", "Bearer invalid-token");

        when(redisTemplate.hasKey(anyString())).thenReturn(false);
        when(jwtUtil.validateToken("invalid-token")).thenReturn(false);

        boolean result = interceptor.preHandle(request, response, new Object());

        assertFalse(result);
        assertEquals(401, response.getStatus());
        String body = response.getContentAsString();
        assertTrue(body.contains("令牌无效或已过期"));
    }

    @Test
    void preHandle_shouldAllowWhenTokenValid() throws Exception {
        request.setMethod("GET");
        request.addHeader("Authorization", "Bearer valid-token");

        when(redisTemplate.hasKey(anyString())).thenReturn(false);
        when(jwtUtil.validateToken("valid-token")).thenReturn(true);
        when(jwtUtil.getUserId("valid-token")).thenReturn(42L);
        when(jwtUtil.getUsername("valid-token")).thenReturn("zhangsan");

        boolean result = interceptor.preHandle(request, response, new Object());

        assertTrue(result);
        assertEquals(42L, request.getAttribute("userId"));
        assertEquals("zhangsan", request.getAttribute("username"));
    }

    @Test
    void preHandle_shouldExtractTokenFromBearerHeader() throws Exception {
        request.setMethod("GET");
        request.addHeader("Authorization", "Bearer my-jwt-token");

        when(redisTemplate.hasKey(anyString())).thenReturn(false);
        when(jwtUtil.validateToken("my-jwt-token")).thenReturn(true);
        when(jwtUtil.getUserId("my-jwt-token")).thenReturn(1L);
        when(jwtUtil.getUsername("my-jwt-token")).thenReturn("testuser");

        boolean result = interceptor.preHandle(request, response, new Object());

        assertTrue(result);
        assertEquals(1L, request.getAttribute("userId"));
    }

    @Test
    void preHandle_shouldRejectWhenHeaderNoBearerPrefix() throws Exception {
        request.setMethod("GET");
        request.addHeader("Authorization", "Token some-value");

        boolean result = interceptor.preHandle(request, response, new Object());

        assertFalse(result);
        assertEquals(401, response.getStatus());
    }

    @Test
    void preHandle_shouldRejectWhenEmptyToken() throws Exception {
        request.setMethod("GET");
        request.addHeader("Authorization", "Bearer ");

        boolean result = interceptor.preHandle(request, response, new Object());

        assertFalse(result);
        assertEquals(401, response.getStatus());
    }

    @Test
    void sendUnauthorized_shouldReturnJsonResponse() throws Exception {
        request.setMethod("GET");
        request.addHeader("Authorization", "Bearer bad-token");

        when(redisTemplate.hasKey(anyString())).thenReturn(false);
        when(jwtUtil.validateToken("bad-token")).thenReturn(false);

        interceptor.preHandle(request, response, new Object());

        String body = response.getContentAsString();
        assertNotNull(body);
        assertFalse(body.isEmpty());
        // Verify it's valid JSON
        assertDoesNotThrow(() -> JSON.parse(body));

        R<?> r = JSON.parseObject(body, R.class);
        assertEquals(401, r.getCode());
    }
}
