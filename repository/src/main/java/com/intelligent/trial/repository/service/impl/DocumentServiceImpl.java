package com.intelligent.trial.repository.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.intelligent.trial.common.exception.BusinessException;
import com.intelligent.trial.repository.config.MinioConfig;
import com.intelligent.trial.repository.dto.DocumentSearchDTO;
import com.intelligent.trial.repository.entity.Document;
import com.intelligent.trial.repository.mapper.DocumentMapper;
import com.intelligent.trial.repository.service.DocumentService;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * 文档管理服务实现
 */
@Service
public class DocumentServiceImpl implements DocumentService {

    private final DocumentMapper documentMapper;
    private final MinioClient minioClient;
    private final MinioConfig minioConfig;

    public DocumentServiceImpl(DocumentMapper documentMapper,
                               MinioClient minioClient,
                               MinioConfig minioConfig) {
        this.documentMapper = documentMapper;
        this.minioClient = minioClient;
        this.minioConfig = minioConfig;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Document create(Document document) {
        if (document.getStatus() == null) {
            document.setStatus(0); // 默认草稿
        }
        documentMapper.insert(document);
        return documentMapper.selectById(document.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Document update(Document document) {
        Document existing = documentMapper.selectById(document.getId());
        if (existing == null) {
            throw new BusinessException("文档不存在");
        }
        documentMapper.updateById(document);
        return documentMapper.selectById(document.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Document document = documentMapper.selectById(id);
        if (document == null) {
            throw new BusinessException("文档不存在");
        }

        // 删除 MinIO 中的文件
        if (document.getFilePath() != null && !document.getFilePath().isEmpty()) {
            try {
                minioClient.removeObject(RemoveObjectArgs.builder()
                        .bucket(minioConfig.getBucketName())
                        .object(document.getFilePath())
                        .build());
            } catch (Exception e) {
                // 记录日志但不阻止数据库删除
                // log.warn("删除MinIO文件失败: {}", document.getFilePath(), e);
            }
        }

        documentMapper.deleteById(id);
    }

    @Override
    public Document getById(Long id) {
        Document document = documentMapper.selectById(id);
        if (document == null) {
            throw new BusinessException("文档不存在");
        }
        return document;
    }

    @Override
    public IPage<Document> search(DocumentSearchDTO searchDTO) {
        Page<Document> page = new Page<>(searchDTO.getPageNum(), searchDTO.getPageSize());
        return documentMapper.searchDocuments(
                page,
                searchDTO.getRepoType(),
                searchDTO.getKeyword(),
                searchDTO.getDirectoryId(),
                searchDTO.getValidityStatus(),
                searchDTO.getPublishDateStart(),
                searchDTO.getPublishDateEnd(),
                searchDTO.getClassificationLevelId(),
                searchDTO.getPublishUnit()
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Document upload(Document document, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }

        // 生成 MinIO 对象路径
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String objectName = "documents/" + new Date().getTime() + "/" + UUID.randomUUID().toString() + extension;

        try {
            // 上传到 MinIO
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());

            // 设置文档属性
            document.setFilePath(objectName);
            document.setFileSize(file.getSize());
            document.setFileType(extension.replaceFirst("^\\.", ""));

            if (document.getStatus() == null) {
                document.setStatus(1); // 上传成功直接发布
            }

            documentMapper.insert(document);
            return documentMapper.selectById(document.getId());

        } catch (Exception e) {
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Document> batchUpload(Long directoryId, Integer repoType, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new BusinessException("上传文件列表不能为空");
        }

        List<Document> results = new ArrayList<>();
        for (MultipartFile file : files) {
            Document doc = new Document();
            doc.setDirectoryId(directoryId);
            doc.setRepoType(repoType);
            doc.setTitle(file.getOriginalFilename());
            doc.setStatus(1);
            results.add(upload(doc, file));
        }
        return results;
    }

    @Override
    public InputStream preview(Long id) {
        Document document = getById(id);
        if (document.getFilePath() == null || document.getFilePath().isEmpty()) {
            throw new BusinessException("文档文件不存在");
        }

        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(document.getFilePath())
                    .build());
        } catch (Exception e) {
            throw new BusinessException("文件预览失败: " + e.getMessage());
        }
    }

    @Override
    public FileDownloadResult download(Long id) {
        Document document = getById(id);
        if (document.getFilePath() == null || document.getFilePath().isEmpty()) {
            throw new BusinessException("文档文件不存在");
        }

        try {
            String fileName = document.getTitle();
            if (fileName == null || fileName.isEmpty()) {
                fileName = document.getFilePath().substring(document.getFilePath().lastIndexOf("/") + 1);
            }
            // 添加文件扩展名
            if (document.getFileType() != null && !fileName.endsWith("." + document.getFileType())) {
                fileName += "." + document.getFileType();
            }

            InputStream inputStream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(document.getFilePath())
                    .build());

            return new FileDownloadResult(
                    inputStream,
                    fileName,
                    getContentType(document.getFileType()),
                    document.getFileSize()
            );
        } catch (Exception e) {
            throw new BusinessException("文件下载失败: " + e.getMessage());
        }
    }

    /**
     * 根据文件类型获取 MIME ContentType
     */
    private String getContentType(String fileType) {
        if (fileType == null) return "application/octet-stream";
        switch (fileType.toLowerCase()) {
            case "pdf":
                return "application/pdf";
            case "doc":
                return "application/msword";
            case "docx":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls":
                return "application/vnd.ms-excel";
            case "xlsx":
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "txt":
                return "text/plain";
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            default:
                return "application/octet-stream";
        }
    }
}
