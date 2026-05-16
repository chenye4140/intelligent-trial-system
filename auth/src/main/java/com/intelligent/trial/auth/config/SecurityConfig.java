package com.intelligent.trial.auth.config;

import com.intelligent.trial.auth.interceptor.JwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

/**
 * Web 安全配置（自定义拦截器方案，不使用 Spring Security 全量）
 */
@Configuration
public class SecurityConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    /**
     * 白名单路径（不需要 JWT 认证）
     */
    private static final List<String> WHITE_LIST = Arrays.asList(
            "/auth/login",
            "/auth/captcha",
            "/error",
            "/doc.html",
            "/webjars/**",
            "/swagger-resources/**",
            "/v2/api-docs/**",
            "/swagger-ui.html"
    );

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(WHITE_LIST);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
