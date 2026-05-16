package com.intelligent.trial.report.client;

import com.intelligent.trial.common.exception.BusinessException;
import com.intelligent.trial.common.exception.ErrorCode;
import com.intelligent.trial.report.config.DashScopeConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * DeepSeekClient 单元测试
 * 
 * Note: OkHttpClient.Builder is a final class in OkHttp 4.x and cannot be mocked.
 * We test the configuration/validation logic (enabled/disabled states) and
 * skip full HTTP mocking which would require an integration test setup.
 */
@ExtendWith(MockitoExtension.class)
class DeepSeekClientTest {

    @InjectMocks
    private DeepSeekClient deepSeekClient;

    @Mock
    private DashScopeConfig dashScopeConfig;

    @Test
    void init_shouldEnableDeepSeek_whenConfigured() {
        when(dashScopeConfig.getDeepseekBaseUrl()).thenReturn("https://api.deepseek.com");
        when(dashScopeConfig.getDeepseekApiKey()).thenReturn("sk-test-key-12345");
        when(dashScopeConfig.getDeepseekModel()).thenReturn("deepseek-v4-pro");

        deepSeekClient.init();
        // Client initialized successfully - no exception means config was accepted
        assertNotNull(deepSeekClient);
    }

    @Test
    void generateContent_shouldThrow_whenDeepSeekNotEnabled_apiKeyNull() {
        when(dashScopeConfig.getDeepseekApiKey()).thenReturn(null);
        when(dashScopeConfig.getDeepseekBaseUrl()).thenReturn("https://api.deepseek.com");
        deepSeekClient.init();

        BusinessException ex = assertThrows(BusinessException.class, () -> {
            deepSeekClient.generateContent("system prompt", "user prompt");
        });
        assertEquals(ErrorCode.AI_SERVICE_UNAVAILABLE.getCode(), ex.getCode());
    }

    @Test
    void generateContent_shouldThrow_whenDeepSeekNotEnabled_apiKeyEmpty() {
        when(dashScopeConfig.getDeepseekApiKey()).thenReturn("");
        when(dashScopeConfig.getDeepseekBaseUrl()).thenReturn("https://api.deepseek.com");
        deepSeekClient.init();

        BusinessException ex = assertThrows(BusinessException.class, () -> {
            deepSeekClient.generateContent("system prompt", "user prompt");
        });
        assertEquals(ErrorCode.AI_SERVICE_UNAVAILABLE.getCode(), ex.getCode());
    }

    @Test
    void generateContent_shouldThrow_whenDeepSeekNotEnabled_baseUrlNull() {
        when(dashScopeConfig.getDeepseekBaseUrl()).thenReturn(null);
        when(dashScopeConfig.getDeepseekApiKey()).thenReturn("sk-test-key");
        deepSeekClient.init();

        BusinessException ex = assertThrows(BusinessException.class, () -> {
            deepSeekClient.generateContent("system prompt", "user prompt");
        });
        assertEquals(ErrorCode.AI_SERVICE_UNAVAILABLE.getCode(), ex.getCode());
    }

    @Test
    void generateContent_shouldThrow_whenDeepSeekNotEnabled_baseUrlEmpty() {
        when(dashScopeConfig.getDeepseekBaseUrl()).thenReturn("");
        when(dashScopeConfig.getDeepseekApiKey()).thenReturn("sk-test-key");
        deepSeekClient.init();

        BusinessException ex = assertThrows(BusinessException.class, () -> {
            deepSeekClient.generateContent("system prompt", "user prompt");
        });
        assertEquals(ErrorCode.AI_SERVICE_UNAVAILABLE.getCode(), ex.getCode());
    }

    @Test
    void init_shouldAcceptBaseUrlWithTrailingSlash() {
        when(dashScopeConfig.getDeepseekBaseUrl()).thenReturn("https://api.deepseek.com/");
        when(dashScopeConfig.getDeepseekApiKey()).thenReturn("sk-test-key-12345");
        when(dashScopeConfig.getDeepseekModel()).thenReturn("deepseek-v4-pro");

        deepSeekClient.init();
        // Should succeed - the trailing slash should be trimmed internally
        assertNotNull(deepSeekClient);
    }
}
