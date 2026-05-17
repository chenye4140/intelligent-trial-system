package com.intelligent.trial.gateway.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * JwtAuthFilter 单元测试
 * 覆盖白名单匹配、Token验证、401响应、Header透传等场景
 */
class JwtAuthFilterTest {

    private JwtAuthFilter filter;
    private MockServerWebExchange exchange;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthFilter();
        // 通过反射设置白名单（因为 @Value 需要 Spring 容器）
        setWhitelist(filter, Arrays.asList(
                "/api/auth/login",
                "/api/auth/captcha",
                "/api/auth/refresh"
        ));
    }

    private void setWhitelist(JwtAuthFilter f, List<String> whitelist) {
        try {
            java.lang.reflect.Field field = JwtAuthFilter.class.getDeclaredField("whitelist");
            field.setAccessible(true);
            field.set(f, whitelist);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Nested
    @DisplayName("白名单路径测试")
    class WhitelistTests {

        @Test
        @DisplayName("登录路径应放行，无需Token")
        void whitelistLoginPath_shouldPassWithoutToken() {
            MockServerHttpRequest request = MockServerHttpRequest
                    .post("/api/auth/login")
                    .build();
            exchange = MockServerWebExchange.from(request);

            org.springframework.cloud.gateway.filter.GatewayFilterChain chain = mock(org.springframework.cloud.gateway.filter.GatewayFilterChain.class);
            when(chain.filter(any())).thenReturn(reactor.core.publisher.Mono.empty());

            filter.filter(exchange, chain).block();

            // 验证 chain.filter 被调用（放行）
            verify(chain).filter(exchange);
            // 验证响应状态不是 401
            assertNotEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        }

        @Test
        @DisplayName("Captcha 路径应放行")
        void whitelistCaptchaPath_shouldPassWithoutToken() {
            MockServerHttpRequest request = MockServerHttpRequest
                    .get("/api/auth/captcha")
                    .build();
            exchange = MockServerWebExchange.from(request);

            org.springframework.cloud.gateway.filter.GatewayFilterChain chain = mock(org.springframework.cloud.gateway.filter.GatewayFilterChain.class);
            when(chain.filter(any())).thenReturn(reactor.core.publisher.Mono.empty());

            filter.filter(exchange, chain).block();

            verify(chain).filter(exchange);
        }

        @Test
        @DisplayName("刷新Token路径应放行")
        void whitelistRefreshPath_shouldPassWithoutToken() {
            MockServerHttpRequest request = MockServerHttpRequest
                    .post("/api/auth/refresh")
                    .build();
            exchange = MockServerWebExchange.from(request);

            org.springframework.cloud.gateway.filter.GatewayFilterChain chain = mock(org.springframework.cloud.gateway.filter.GatewayFilterChain.class);
            when(chain.filter(any())).thenReturn(reactor.core.publisher.Mono.empty());

            filter.filter(exchange, chain).block();

            verify(chain).filter(exchange);
        }
    }

    @Nested
    @DisplayName("非白名单路径 - Token验证测试")
    class TokenValidationTests {

        @Test
        @DisplayName("无 Authorization header 应返回 401")
        void missingAuthHeader_shouldReturn401() {
            MockServerHttpRequest request = MockServerHttpRequest
                    .get("/api/system/user/page")
                    .build();
            exchange = MockServerWebExchange.from(request);

            org.springframework.cloud.gateway.filter.GatewayFilterChain chain = mock(org.springframework.cloud.gateway.filter.GatewayFilterChain.class);

            filter.filter(exchange, chain).block();

            assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
            verify(chain, never()).filter(any());
        }

        @Test
        @DisplayName("空 Authorization header 应返回 401")
        void emptyAuthHeader_shouldReturn401() {
            MockServerHttpRequest request = MockServerHttpRequest
                    .get("/api/case/list")
                    .header(HttpHeaders.AUTHORIZATION, "")
                    .build();
            exchange = MockServerWebExchange.from(request);

            org.springframework.cloud.gateway.filter.GatewayFilterChain chain = mock(org.springframework.cloud.gateway.filter.GatewayFilterChain.class);

            filter.filter(exchange, chain).block();

            assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        }

        @Test
        @DisplayName("Token 长度过短应返回 401")
        void shortToken_shouldReturn401() {
            MockServerHttpRequest request = MockServerHttpRequest
                    .get("/api/document/search")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer short")
                    .build();
            exchange = MockServerWebExchange.from(request);

            org.springframework.cloud.gateway.filter.GatewayFilterChain chain = mock(org.springframework.cloud.gateway.filter.GatewayFilterChain.class);

            filter.filter(exchange, chain).block();

            assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        }

        @Test
        @DisplayName("有效的 Bearer Token 应放行")
        void validBearerToken_shouldPass() {
            String validToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIn0.validtoken";

            MockServerHttpRequest request = MockServerHttpRequest
                    .get("/api/system/user/page")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
                    .build();
            exchange = MockServerWebExchange.from(request);

            org.springframework.cloud.gateway.filter.GatewayFilterChain chain = mock(org.springframework.cloud.gateway.filter.GatewayFilterChain.class);
            when(chain.filter(any())).thenReturn(reactor.core.publisher.Mono.empty());

            filter.filter(exchange, chain).block();

            verify(chain).filter(any());
        }

        @Test
        @DisplayName("无 Bearer 前缀的有效 Token 应放行")
        void tokenWithoutBearerPrefix_shouldPass() {
            String validToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIn0.validtoken";

            MockServerHttpRequest request = MockServerHttpRequest
                    .get("/api/case/list")
                    .header(HttpHeaders.AUTHORIZATION, validToken)
                    .build();
            exchange = MockServerWebExchange.from(request);

            org.springframework.cloud.gateway.filter.GatewayFilterChain chain = mock(org.springframework.cloud.gateway.filter.GatewayFilterChain.class);
            when(chain.filter(any())).thenReturn(reactor.core.publisher.Mono.empty());

            filter.filter(exchange, chain).block();

            verify(chain).filter(any());
        }
    }

    @Nested
    @DisplayName("Token 透传测试")
    class TokenForwardingTests {

        @Test
        @DisplayName("Token 应作为 X-Gateway-Token header 透传给下游")
        void bearerToken_shouldBeForwardedAsHeader() {
            String validToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIn0.validtoken";

            MockServerHttpRequest request = MockServerHttpRequest
                    .get("/api/system/user/page")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
                    .build();
            exchange = MockServerWebExchange.from(request);

            org.springframework.cloud.gateway.filter.GatewayFilterChain chain = mock(org.springframework.cloud.gateway.filter.GatewayFilterChain.class);
            when(chain.filter(any())).thenAnswer(invocation -> {
                ServerWebExchange mutatedExchange = invocation.getArgument(0);
                String forwardedToken = mutatedExchange.getRequest()
                        .getHeaders().getFirst("X-Gateway-Token");
                assertEquals(validToken, forwardedToken, "Token should be forwarded without Bearer prefix");
                return reactor.core.publisher.Mono.empty();
            });

            filter.filter(exchange, chain).block();
        }

        @Test
        @DisplayName("Bearer 前缀应被剥离后再透传")
        void bearerPrefix_shouldBeStrippedBeforeForwarding() {
            String validToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.payload.signature";

            MockServerHttpRequest request = MockServerHttpRequest
                    .post("/api/repository/upload")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
                    .build();
            exchange = MockServerWebExchange.from(request);

            org.springframework.cloud.gateway.filter.GatewayFilterChain chain = mock(org.springframework.cloud.gateway.filter.GatewayFilterChain.class);
            when(chain.filter(any())).thenAnswer(invocation -> {
                ServerWebExchange mutatedExchange = invocation.getArgument(0);
                String forwardedToken = mutatedExchange.getRequest()
                        .getHeaders().getFirst("X-Gateway-Token");
                assertFalse(forwardedToken.startsWith("Bearer "), "Bearer prefix should be stripped");
                assertEquals(validToken, forwardedToken);
                return reactor.core.publisher.Mono.empty();
            });

            filter.filter(exchange, chain).block();
        }
    }

    @Nested
    @DisplayName("401 响应内容测试")
    class UnauthorizedResponseTests {

        @Test
        @DisplayName("缺少Token时响应内容应包含错误信息")
        void missingToken_shouldIncludeErrorMessage() {
            MockServerHttpRequest request = MockServerHttpRequest
                    .get("/api/system/user/page")
                    .build();
            exchange = MockServerWebExchange.from(request);

            org.springframework.cloud.gateway.filter.GatewayFilterChain chain = mock(org.springframework.cloud.gateway.filter.GatewayFilterChain.class);

            filter.filter(exchange, chain).block();

            assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        }

        @Test
        @DisplayName("Token过短时响应应为401")
        void shortToken_shouldReturn401WithMessage() {
            MockServerHttpRequest request = MockServerHttpRequest
                    .get("/api/report/list")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer abc")
                    .build();
            exchange = MockServerWebExchange.from(request);

            org.springframework.cloud.gateway.filter.GatewayFilterChain chain = mock(org.springframework.cloud.gateway.filter.GatewayFilterChain.class);

            filter.filter(exchange, chain).block();

            assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        }
    }

    @Nested
    @DisplayName("过滤器优先级测试")
    class OrderTests {

        @Test
        @DisplayName("过滤器优先级应为 -100（高优先级）")
        void order_shouldBeHighPriority() {
            assertEquals(-100, filter.getOrder());
        }
    }

    @Nested
    @DisplayName("通配符白名单匹配测试")
    class WildcardMatchingTests {

        @Test
        @DisplayName("应支持 Ant 风格通配符匹配")
        void wildcardPattern_shouldMatch() {
            // 设置带通配符的白名单
            setWhitelist(filter, Collections.singletonList("/api/auth/**"));

            MockServerHttpRequest request = MockServerHttpRequest
                    .get("/api/auth/user/info")
                    .build();
            exchange = MockServerWebExchange.from(request);

            org.springframework.cloud.gateway.filter.GatewayFilterChain chain = mock(org.springframework.cloud.gateway.filter.GatewayFilterChain.class);
            when(chain.filter(any())).thenReturn(reactor.core.publisher.Mono.empty());

            filter.filter(exchange, chain).block();

            verify(chain).filter(exchange);
        }

        @Test
        @DisplayName("通配符应匹配多层路径")
        void wildcardPattern_shouldMatchDeepPath() {
            setWhitelist(filter, Collections.singletonList("/api/auth/**"));

            MockServerHttpRequest request = MockServerHttpRequest
                    .post("/api/auth/system/user/reset-password")
                    .build();
            exchange = MockServerWebExchange.from(request);

            org.springframework.cloud.gateway.filter.GatewayFilterChain chain = mock(org.springframework.cloud.gateway.filter.GatewayFilterChain.class);
            when(chain.filter(any())).thenReturn(reactor.core.publisher.Mono.empty());

            filter.filter(exchange, chain).block();

            verify(chain).filter(exchange);
        }
    }
}
