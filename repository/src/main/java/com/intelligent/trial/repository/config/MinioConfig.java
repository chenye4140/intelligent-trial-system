package com.intelligent.trial.repository.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO 配置类
 * 用于文档文件存储和预览
 */
@Configuration
public class MinioConfig {

    /**
     * MinIO 服务端点
     */
    @Value("${minio.endpoint:http://localhost:9000}")
    private String endpoint;

    /**
     * MinIO 访问密钥
     */
    @Value("${minio.access-key:minioadmin}")
    private String accessKey;

    /**
     * MinIO 密钥
     */
    @Value("${minio.secret-key:minioadmin}")
    private String secretKey;

    /**
     * 默认存储桶
     */
    @Value("${minio.bucket-name:trial-documents}")
    private String bucketName;

    /**
     * 创建 MinIO 客户端 Bean
     */
    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public String getBucketName() {
        return bucketName;
    }
}
