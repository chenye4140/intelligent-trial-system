package com.intelligent.trial.auth;

import com.intelligent.trial.auth.util.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Auth 模块集成测试 — 使用 H2 内存数据库 + Mock Redis，
 * 对核心 REST API 进行端到端 HTTP 级别测试。
 */
@SpringBootTest(
        classes = AuthApplication.class,
        properties = {
                "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration,org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration"
        }
)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Auth 模块集成测试")
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    // Mock Redis — 集成测试不依赖真实 Redis
    @MockBean
    private RedisTemplate<String, Object> redisTemplate;

    private void mockRedis() {
        ValueOperations<String, Object> valueOps = mock(ValueOperations.class);
        when(redisTemplate.hasKey(anyString())).thenReturn(false);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(redisTemplate.delete(anyString())).thenReturn(true);
    }

    @Test
    @DisplayName("POST /api/auth/login - 登录成功")
    void login_success() throws Exception {
        mockRedis();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.data.userInfo.username").value("admin"));
    }

    @Test
    @DisplayName("POST /api/auth/login - 密码错误")
    void login_wrongPassword() throws Exception {
        mockRedis();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"wrong\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1006));
    }

    @Test
    @DisplayName("POST /api/auth/login - 用户不存在")
    void login_userNotFound() throws Exception {
        mockRedis();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"nonexistent\",\"password\":\"test\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1003));
    }

    @Test
    @DisplayName("POST /api/auth/login - 空请求体")
    void login_emptyBody() throws Exception {
        mockRedis();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/auth/info - 有效 Token")
    void getUserInfo_withValidToken() throws Exception {
        mockRedis();

        String token = jwtUtil.generateToken(1L, "admin");
        mockMvc.perform(get("/api/auth/info")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("admin"));
    }

    @Test
    @DisplayName("GET /api/auth/info - 无 Token")
    void getUserInfo_noToken() throws Exception {
        mockMvc.perform(get("/api/auth/info"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/auth/info - 无效 Token")
    void getUserInfo_invalidToken() throws Exception {
        mockRedis();

        mockMvc.perform(get("/api/auth/info")
                        .header("Authorization", "Bearer invalid-token-12345"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/auth/refresh - Token 刷新")
    void refreshToken_success() throws Exception {
        mockRedis();

        // 先登录获取 refreshToken
        String loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String refreshToken = loginResult.split("\"refreshToken\":\"")[1].split("\"")[0];

        mockMvc.perform(post("/api/auth/refresh")
                        .param("refreshToken", refreshToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty());
    }

    @Test
    @DisplayName("POST /api/auth/logout - 退出登录")
    void logout_success() throws Exception {
        mockRedis();

        String token = jwtUtil.generateToken(1L, "admin");
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("GET /api/system/user/page - 分页查询用户（需 Token）")
    void listUsers_withToken() throws Exception {
        mockRedis();

        String token = jwtUtil.generateToken(1L, "admin");
        mockMvc.perform(get("/api/system/user/page")
                        .header("Authorization", "Bearer " + token)
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").isNumber())
                .andExpect(jsonPath("$.data.list").isArray());
    }

    @Test
    @DisplayName("GET /api/system/menu/tree - 菜单树（需 Token）")
    void getMenuTree_withToken() throws Exception {
        mockRedis();

        String token = jwtUtil.generateToken(1L, "admin");
        mockMvc.perform(get("/api/system/menu/tree")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("GET /api/system/role/page - 角色分页（需 Token）")
    void listRoles_withToken() throws Exception {
        mockRedis();

        String token = jwtUtil.generateToken(1L, "admin");
        mockMvc.perform(get("/api/system/role/page")
                        .header("Authorization", "Bearer " + token)
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").isNumber())
                .andExpect(jsonPath("$.data.list").isArray());
    }
}
