package com.intelligent.trial.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * JWT 配置属性
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {

    /**
     * JWT 签名密钥
     */
    private String secret;

    /**
     * Token 过期时间（毫秒），默认 24 小时
     */
    private Long expiration = 86400000L;

    /**
     * 刷新 Token 过期时间（毫秒），默认 7 天
     */
    private Long refreshExpiration = 604800000L;

    /**
     * Token 请求头名称
     */
    private String header = "Authorization";

    /**
     * Token 前缀
     */
    private String prefix = "Bearer ";
}
