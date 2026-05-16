package com.intelligent.trial.document.util;

import com.intelligent.trial.document.config.MinioConfig;
import io.minio.*;
import io.minio.http.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * MinIO 工具类
 * 提供文件上传、下载、删除、URL生成等操作
 *
 * @author intelligent-trial
 */
@Component
public class MinioUtil {

    private static final Logger log = LoggerFactory.getLogger(MinioUtil.class);

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private MinioConfig minioConfig;

    /**
     * 确保存储桶存在
     */
    private void ensureBucketExists() {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(minioConfig.getBucketName()).build());
            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(minioConfig.getBucketName()).build());
                log.info("创建MinIO存储桶: {}", minioConfig.getBucketName());
            }
        } catch (Exception e) {
            log.error("检查/创建MinIO存储桶失败", e);
            throw new RuntimeException("MinIO存储桶操作失败: " + e.getMessage(), e);
        }
    }

    /**
     * 上传文件到 MinIO
     *
     * @param file 上传的文件
     * @return 存储路径（object key）
     */
    public String uploadFile(MultipartFile file) {
        String objectKey = generateObjectKey(file.getOriginalFilename());
        try {
            ensureBucketExists();
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(objectKey)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());
            log.info("文件上传成功: bucket={}, key={}", minioConfig.getBucketName(), objectKey);
            return objectKey;
        } catch (Exception e) {
            log.error("文件上传失败: {}", file.getOriginalFilename(), e);
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }
    }

    /**
     * 上传输入流到 MinIO
     *
     * @param inputStream 文件输入流
     * @param fileName    文件名
     * @param contentType MIME类型
     * @param size        文件大小
     * @return 存储路径
     */
    public String uploadStream(InputStream inputStream, String fileName, String contentType, long size) {
        String objectKey = generateObjectKey(fileName);
        try {
            ensureBucketExists();
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(objectKey)
                            .stream(inputStream, size, -1)
                            .contentType(contentType)
                            .build());
            log.info("流上传成功: bucket={}, key={}", minioConfig.getBucketName(), objectKey);
            return objectKey;
        } catch (Exception e) {
            log.error("流上传失败: {}", fileName, e);
            throw new RuntimeException("流上传失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取文件下载 URL
     *
     * @param objectKey 对象键
     * @param expiryMinutes 过期时间（分钟）
     * @return 预签名下载 URL
     */
    public String getFileUrl(String objectKey, int expiryMinutes) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(minioConfig.getBucketName())
                            .object(objectKey)
                            .expiry(expiryMinutes, TimeUnit.MINUTES)
                            .build());
        } catch (Exception e) {
            log.error("获取文件URL失败: {}", objectKey, e);
            throw new RuntimeException("获取文件URL失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取永久访问 URL（需配合MinIO公开读策略使用）
     *
     * @param objectKey 对象键
     * @return 永久访问 URL
     */
    public String getPermanentUrl(String objectKey) {
        String endpoint = minioConfig.getEndpoint();
        // 去除末尾斜杠
        if (endpoint.endsWith("/")) {
            endpoint = endpoint.substring(0, endpoint.length() - 1);
        }
        return String.format("%s/%s/%s", endpoint, minioConfig.getBucketName(), objectKey);
    }

    /**
     * 删除文件
     *
     * @param objectKey 对象键
     */
    public void deleteFile(String objectKey) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(objectKey)
                            .build());
            log.info("文件删除成功: {}", objectKey);
        } catch (Exception e) {
            log.error("文件删除失败: {}", objectKey, e);
            throw new RuntimeException("文件删除失败: " + e.getMessage(), e);
        }
    }

    /**
     * 下载文件为输入流
     *
     * @param objectKey 对象键
     * @return 文件输入流
     */
    public InputStream downloadFile(String objectKey) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(objectKey)
                            .build());
        } catch (Exception e) {
            log.error("下载文件失败: {}", objectKey, e);
            throw new RuntimeException("下载文件失败: " + e.getMessage(), e);
        }
    }

    /**
     * 生成唯一对象键
     *
     * @param originalFilename 原始文件名
     * @return 对象键，格式: documents/yyyy/MM/dd/uuid_suffix.ext
     */
    private String generateObjectKey(String originalFilename) {
        String prefix = minioConfig.getPathPrefix() != null ? minioConfig.getPathPrefix() : "documents";
        String datePath = java.time.LocalDate.now().toString().replace("-", "/");
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return String.format("%s/%s/%s%s", prefix, datePath, uuid, extension);
    }
}
