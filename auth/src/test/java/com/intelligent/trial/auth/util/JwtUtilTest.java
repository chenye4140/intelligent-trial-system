package com.intelligent.trial.auth.util;

import com.intelligent.trial.auth.config.JwtConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JwtUtil 单元测试
 */
class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        JwtConfig jwtConfig = new JwtConfig();
        jwtConfig.setSecret("dGVzdC1zZWNyZXQta2V5LWZvci11bml0LXRlc3RpbmctbXVzdC1iZS1hdC1sZWFzdC0yNTYtYml0cw==");
        jwtConfig.setExpiration(3600000L);  // 1 hour
        jwtConfig.setRefreshExpiration(86400000L);  // 24 hours
        jwtConfig.setHeader("Authorization");
        jwtConfig.setPrefix("Bearer ");
        // Manually inject since we're not using Spring context
        try {
            java.lang.reflect.Field field = JwtUtil.class.getDeclaredField("jwtConfig");
            field.setAccessible(true);
            field.set(jwtUtil, jwtConfig);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject JwtConfig", e);
        }
    }

    @Test
    void generateToken_shouldReturnNonNullString() {
        String token = jwtUtil.generateToken(1L, "testuser");
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void generateToken_shouldContainThreeParts() {
        String token = jwtUtil.generateToken(1L, "testuser");
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length, "JWT should have 3 parts: header.payload.signature");
    }

    @Test
    void generateToken_shouldBeValidatable() {
        String token = jwtUtil.generateToken(1L, "testuser");
        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    void generateToken_shouldExtractCorrectUserId() {
        String token = jwtUtil.generateToken(42L, "testuser");
        assertEquals(42L, jwtUtil.getUserId(token));
    }

    @Test
    void generateToken_shouldExtractCorrectUsername() {
        String token = jwtUtil.generateToken(1L, "zhangsan");
        assertEquals("zhangsan", jwtUtil.getUsername(token));
    }

    @Test
    void generateRefreshToken_shouldReturnNonNullString() {
        String token = jwtUtil.generateRefreshToken(1L, "testuser");
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void generateRefreshToken_shouldBeValidatable() {
        String token = jwtUtil.generateRefreshToken(1L, "testuser");
        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    void generateRefreshToken_shouldExtractCorrectUserId() {
        String token = jwtUtil.generateRefreshToken(99L, "admin");
        assertEquals(99L, jwtUtil.getUserId(token));
    }

    @Test
    void validateToken_shouldReturnFalseForInvalidToken() {
        assertFalse(jwtUtil.validateToken("invalid.token.here"));
    }

    @Test
    void validateToken_shouldReturnFalseForEmptyToken() {
        assertFalse(jwtUtil.validateToken(""));
    }

    @Test
    void validateToken_shouldReturnFalseForNullToken() {
        assertFalse(jwtUtil.validateToken(null));
    }

    @Test
    void validateToken_shouldReturnFalseForTamperedToken() {
        String token = jwtUtil.generateToken(1L, "testuser");
        String tampered = token.substring(0, token.length() - 5) + "XXXXX";
        assertFalse(jwtUtil.validateToken(tampered));
    }

    @Test
    void parseToken_shouldReturnClaimsForValidToken() {
        String token = jwtUtil.generateToken(1L, "testuser");
        io.jsonwebtoken.Claims claims = jwtUtil.parseToken(token);
        assertNotNull(claims);
        assertEquals("testuser", claims.getSubject());
        assertEquals(1L, claims.get("userId", Long.class));
        assertEquals("testuser", claims.get("username", String.class));
    }

    @Test
    void parseToken_shouldThrowForInvalidToken() {
        assertThrows(Exception.class, () -> jwtUtil.parseToken("invalid.token.here"));
    }

    @Test
    void differentTokensForSameUser_shouldBothBeValid() {
        String token1 = jwtUtil.generateToken(1L, "testuser");
        String token2 = jwtUtil.generateToken(1L, "testuser");
        // Both tokens should be valid (may or may not differ depending on clock resolution)
        assertTrue(jwtUtil.validateToken(token1));
        assertTrue(jwtUtil.validateToken(token2));
        // Both should contain the same user identity
        assertEquals(1L, jwtUtil.getUserId(token1));
        assertEquals(1L, jwtUtil.getUserId(token2));
    }
}
