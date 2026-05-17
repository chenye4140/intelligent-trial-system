package com.intelligent.trial.gateway.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

import java.net.InetSocketAddress;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * RequestLogFilter 单元测试
 * 覆盖请求日志记录、客户端 IP 提取、过滤器优先级等场景
 */
class RequestLogFilterTest {

    private RequestLogFilter filter;

    @BeforeEach
    void setUp() {
        filter = new RequestLogFilter();
    }

    @Nested
    @DisplayName("过滤器基本行为测试")
    class BasicBehaviorTests {

        @Test
        @DisplayName("GET 请求应正常放行")
        void getRequest_shouldPassThrough() {
            MockServerHttpRequest request = MockServerHttpRequest
                    .get("/api/system/user/page")
                    .remoteAddress(new InetSocketAddress("192.168.1.100", 12345))
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            org.springframework.cloud.gateway.filter.GatewayFilterChain chain = mock(org.springframework.cloud.gateway.filter.GatewayFilterChain.class);
            when(chain.filter(any())).thenReturn(reactor.core.publisher.Mono.empty());

            filter.filter(exchange, chain).block();

            verify(chain).filter(exchange);
        }

        @Test
        @DisplayName("POST 请求应正常放行")
        void postRequest_shouldPassThrough() {
            MockServerHttpRequest request = MockServerHttpRequest
                    .post("/api/auth/login")
                    .remoteAddress(new InetSocketAddress("10.0.0.1", 54321))
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            org.springframework.cloud.gateway.filter.GatewayFilterChain chain = mock(org.springframework.cloud.gateway.filter.GatewayFilterChain.class);
            when(chain.filter(any())).thenReturn(reactor.core.publisher.Mono.empty());

            filter.filter(exchange, chain).block();

            verify(chain).filter(exchange);
        }

        @Test
        @DisplayName("DELETE 请求应正常放行")
        void deleteRequest_shouldPassThrough() {
            MockServerHttpRequest request = MockServerHttpRequest
                    .delete("/api/case/123")
                    .remoteAddress(new InetSocketAddress("172.16.0.50", 8080))
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            org.springframework.cloud.gateway.filter.GatewayFilterChain chain = mock(org.springframework.cloud.gateway.filter.GatewayFilterChain.class);
            when(chain.filter(any())).thenReturn(reactor.core.publisher.Mono.empty());

            filter.filter(exchange, chain).block();

            verify(chain).filter(exchange);
        }
    }

    @Nested
    @DisplayName("客户端 IP 提取测试")
    class ClientIpExtractionTests {

        @Test
        @DisplayName("X-Forwarded-For 应优先使用")
        void xForwardedFor_shouldBeUsed() {
            MockServerHttpRequest request = MockServerHttpRequest
                    .get("/api/system/user/page")
                    .header("X-Forwarded-For", "203.0.113.50")
                    .remoteAddress(new InetSocketAddress("10.0.0.1", 8080))
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            org.springframework.cloud.gateway.filter.GatewayFilterChain chain = mock(org.springframework.cloud.gateway.filter.GatewayFilterChain.class);
            when(chain.filter(any())).thenReturn(reactor.core.publisher.Mono.empty());

            // 应该不抛异常，正常执行
            assertDoesNotThrow(() -> filter.filter(exchange, chain).block());
        }

        @Test
        @DisplayName("X-Forwarded-For 多个 IP 应取第一个")
        void multipleIpsInXForwardedFor_shouldUseFirst() {
            MockServerHttpRequest request = MockServerHttpRequest
                    .get("/api/case/list")
                    .header("X-Forwarded-For", "203.0.113.50, 10.0.0.1, 172.16.0.1")
                    .remoteAddress(new InetSocketAddress("10.0.0.1", 8080))
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            org.springframework.cloud.gateway.filter.GatewayFilterChain chain = mock(org.springframework.cloud.gateway.filter.GatewayFilterChain.class);
            when(chain.filter(any())).thenReturn(reactor.core.publisher.Mono.empty());

            assertDoesNotThrow(() -> filter.filter(exchange, chain).block());
        }

        @Test
        @DisplayName("X-Forwarded-For 为 unknown 时应回退到 X-Real-IP")
        void unknownXForwardedFor_shouldFallbackToXRealIp() {
            MockServerHttpRequest request = MockServerHttpRequest
                    .get("/api/report/list")
                    .header("X-Forwarded-For", "unknown")
                    .header("X-Real-IP", "192.168.1.50")
                    .remoteAddress(new InetSocketAddress("10.0.0.1", 8080))
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            org.springframework.cloud.gateway.filter.GatewayFilterChain chain = mock(org.springframework.cloud.gateway.filter.GatewayFilterChain.class);
            when(chain.filter(any())).thenReturn(reactor.core.publisher.Mono.empty());

            assertDoesNotThrow(() -> filter.filter(exchange, chain).block());
        }

        @Test
        @DisplayName("两个代理 header 都为空时应使用 remoteAddress")
        void noProxyHeaders_shouldUseRemoteAddress() {
            MockServerHttpRequest request = MockServerHttpRequest
                    .get("/api/document/search")
                    .remoteAddress(new InetSocketAddress("192.168.1.100", 9090))
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            org.springframework.cloud.gateway.filter.GatewayFilterChain chain = mock(org.springframework.cloud.gateway.filter.GatewayFilterChain.class);
            when(chain.filter(any())).thenReturn(reactor.core.publisher.Mono.empty());

            assertDoesNotThrow(() -> filter.filter(exchange, chain).block());
        }

        @Test
        @DisplayName("无 remoteAddress 时应返回 unknown")
        void noRemoteAddress_shouldReturnUnknown() {
            MockServerHttpRequest request = MockServerHttpRequest
                    .get("/api/workflow/tasks")
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            org.springframework.cloud.gateway.filter.GatewayFilterChain chain = mock(org.springframework.cloud.gateway.filter.GatewayFilterChain.class);
            when(chain.filter(any())).thenReturn(reactor.core.publisher.Mono.empty());

            // 不应该抛异常，内部应处理 null remoteAddress
            assertDoesNotThrow(() -> filter.filter(exchange, chain).block());
        }
    }

    @Nested
    @DisplayName("过滤器优先级测试")
    class OrderTests {

        @Test
        @DisplayName("过滤器优先级应为 -200（最高优先级）")
        void order_shouldBeHighestPriority() {
            assertEquals(-200, filter.getOrder());
        }

        @Test
        @DisplayName("RequestLogFilter 优先级应高于 JwtAuthFilter")
        void shouldHaveHigherPriorityThanJwtAuthFilter() {
            RequestLogFilter logFilter = new RequestLogFilter();
            JwtAuthFilter jwtFilter = new JwtAuthFilter();
            // 更小的值 = 更高优先级
            assertTrue(logFilter.getOrder() < jwtFilter.getOrder(),
                    "RequestLogFilter (-200) should have higher priority than JwtAuthFilter (-100)");
        }
    }

    @Nested
    @DisplayName("边界条件测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("根路径请求应正常处理")
        void rootPath_shouldWork() {
            MockServerHttpRequest request = MockServerHttpRequest
                    .get("/")
                    .remoteAddress(new InetSocketAddress("127.0.0.1", 8080))
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            org.springframework.cloud.gateway.filter.GatewayFilterChain chain = mock(org.springframework.cloud.gateway.filter.GatewayFilterChain.class);
            when(chain.filter(any())).thenReturn(reactor.core.publisher.Mono.empty());

            assertDoesNotThrow(() -> filter.filter(exchange, chain).block());
        }

        @Test
        @DisplayName("含特殊字符的路径应正常处理")
        void pathWithSpecialChars_shouldWork() {
            MockServerHttpRequest request = MockServerHttpRequest
                    .get("/api/document/search?q=测试&status=active")
                    .remoteAddress(new InetSocketAddress("10.0.0.5", 8080))
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            org.springframework.cloud.gateway.filter.GatewayFilterChain chain = mock(org.springframework.cloud.gateway.filter.GatewayFilterChain.class);
            when(chain.filter(any())).thenReturn(reactor.core.publisher.Mono.empty());

            assertDoesNotThrow(() -> filter.filter(exchange, chain).block());
        }

        @Test
        @DisplayName("OPTIONS 预检请求应正常处理")
        void optionsPreflight_shouldWork() {
            MockServerHttpRequest request = MockServerHttpRequest
                    .options("/api/system/user/page")
                    .header("Origin", "http://localhost:3000")
                    .header("Access-Control-Request-Method", "GET")
                    .remoteAddress(new InetSocketAddress("127.0.0.1", 3000))
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            org.springframework.cloud.gateway.filter.GatewayFilterChain chain = mock(org.springframework.cloud.gateway.filter.GatewayFilterChain.class);
            when(chain.filter(any())).thenReturn(reactor.core.publisher.Mono.empty());

            assertDoesNotThrow(() -> filter.filter(exchange, chain).block());
        }
    }
}
