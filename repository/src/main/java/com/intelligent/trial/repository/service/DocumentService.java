package com.intelligent.trial.repository.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.intelligent.trial.repository.dto.DocumentSearchDTO;
import com.intelligent.trial.repository.entity.Document;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

/**
 * 文档管理服务接口
 */
public interface DocumentService {

    /**
     * 新增文档
     *
     * @param document 文档信息
     * @return 创建后的文档
     */
    Document create(Document document);

    /**
     * 更新文档
     *
     * @param document 文档信息
     * @return 更新后的文档
     */
    Document update(Document document);

    /**
     * 删除文档
     *
     * @param id 文档ID
     */
    void delete(Long id);

    /**
     * 根据ID查询文档
     *
     * @param id 文档ID
     * @return 文档信息
     */
    Document getById(Long id);

    /**
     * 搜索文档（支持多条件 + 分页）
     *
     * @param searchDTO 搜索条件
     * @return 分页文档结果
     */
    IPage<Document> search(DocumentSearchDTO searchDTO);

    /**
     * 上传单个文件并创建文档记录
     *
     * @param document 文档元数据
     * @param file     上传的文件
     * @return 创建后的文档
     */
    Document upload(Document document, MultipartFile file);

    /**
     * 批量上传文件
     *
     * @param directoryId 目标目录ID
     * @param repoType    库类型
     * @param files       上传的文件列表
     * @return 成功上传的文档列表
     */
    List<Document> batchUpload(Long directoryId, Integer repoType, List<MultipartFile> files);

    /**
     * 预览文档（获取 MinIO 文件流）
     *
     * @param id 文档ID
     * @return 文件输入流
     */
    InputStream preview(Long id);

    /**
     * 下载文档（获取 MinIO 文件流和文件名）
     *
     * @param id 文档ID
     * @return 包含文件流和文件名的对象
     */
    FileDownloadResult download(Long id);

    /**
     * 文件下载结果封装
     */
    class FileDownloadResult {
        private final InputStream inputStream;
        private final String fileName;
        private final String contentType;
        private final Long fileSize;

        public FileDownloadResult(InputStream inputStream, String fileName, String contentType, Long fileSize) {
            this.inputStream = inputStream;
            this.fileName = fileName;
            this.contentType = contentType;
            this.fileSize = fileSize;
        }

        public InputStream getInputStream() {
            return inputStream;
        }

        public String getFileName() {
            return fileName;
        }

        public String getContentType() {
            return contentType;
        }

        public Long getFileSize() {
            return fileSize;
        }
    }
}
