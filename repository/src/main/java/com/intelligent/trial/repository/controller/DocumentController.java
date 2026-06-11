package com.intelligent.trial.repository.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.intelligent.trial.common.annotation.RequireLog;
import com.intelligent.trial.common.dto.R;
import com.intelligent.trial.repository.dto.DocumentSearchDTO;
import com.intelligent.trial.repository.entity.Document;
import com.intelligent.trial.repository.service.DocumentService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 文档管理 API 控制器
 * 提供文档 CRUD、搜索、上传、预览、下载等功能
 */
@Tag(name = "文档存储", description = "文档CRUD、上传下载预览等文档管理接口")
@RestController
@RequestMapping("/api/repository/document")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    // ==================== 基础 CRUD ====================

    /**
     * 新增文档（不带文件）
     */
    @Operation(summary = "新增文档", description = "创建文档记录（不带文件）")
    @RequireLog(module = "文档存储", action = "新增", description = "创建文档记录")
    @PostMapping("/create")
    public R<Document> create(@Valid @RequestBody Document document) {
        Document created = documentService.create(document);
        return R.ok(created);
    }

    /**
     * 更新文档
     */
    @Operation(summary = "更新文档", description = "更新文档信息")
    @RequireLog(module = "文档存储", action = "编辑", description = "更新文档")
    @PutMapping("/update")
    public R<Document> update(@Valid @RequestBody Document document) {
        Document updated = documentService.update(document);
        return R.ok(updated);
    }

    /**
     * 删除文档
     */
    @Operation(summary = "删除文档", description = "根据ID删除文档")
    @RequireLog(module = "文档存储", action = "删除", description = "删除文档")
    @DeleteMapping("/delete/{id}")
    public R<Void> delete(@Parameter(description = "文档ID") @PathVariable Long id) {
        documentService.delete(id);
        return R.ok();
    }

    /**
     * 批量删除文档
     */
    @Operation(summary = "批量删除文档", description = "批量删除多个文档")
    @RequireLog(module = "文档存储", action = "批量删除", description = "批量删除文档")
    @DeleteMapping("/batch-delete")
    public R<Void> batchDelete(@Valid @RequestBody @NotEmpty(message = "删除文档ID列表不能为空") List<Long> ids) {
        documentService.batchDelete(ids);
        return R.ok();
    }

    /**
     * 根据ID获取文档详情
     */
    @Operation(summary = "获取文档详情", description = "根据ID获取文档详细信息")
    @GetMapping("/get/{id}")
    public R<Document> getById(@Parameter(description = "文档ID") @PathVariable Long id) {
        return R.ok(documentService.getById(id));
    }

    // ==================== 智能搜索 ====================

    /**
     * 搜索文档（支持多条件 + 分页）
     * 跨库联合搜索：不传 repoType 时搜索所有库
     */
    @Operation(summary = "搜索文档", description = "多条件搜索文档，支持跨库联合搜索")
    @PostMapping("/search")
    public R<IPage<Document>> search(@Valid @RequestBody DocumentSearchDTO searchDTO) {
        IPage<Document> result = documentService.search(searchDTO);
        return R.ok(result);
    }

    // ==================== 文件上传 ====================

    /**
     * 上传单个文件并创建文档
     * 文件元数据通过表单参数传递，文件通过 file 字段传递
     */
    @Operation(summary = "上传文档", description = "上传单个文件并创建文档记录")
    @RequireLog(module = "文档存储", action = "上传", description = "上传文档")
    @PostMapping("/upload")
    public R<Document> upload(@RequestPart("document") Document document,
                              @RequestPart("file") MultipartFile file) {
        Document created = documentService.upload(document, file);
        return R.ok(created);
    }

    /**
     * 批量上传文件
     * 自动将文件标题设为文件名，归类到指定目录
     *
     * @param directoryId 目标目录ID
     * @param repoType    库类型
     * @param files       文件列表
     */
    @Operation(summary = "批量上传文档", description = "批量上传多个文件并自动归类到指定目录")
    @RequireLog(module = "文档存储", action = "批量上传", description = "批量上传文档")
    @PostMapping("/batch-upload")
    public R<List<Document>> batchUpload(@Parameter(description = "目标目录ID") @RequestParam Long directoryId,
                                         @Parameter(description = "库类型") @RequestParam Integer repoType,
                                         @Parameter(description = "文件列表") @RequestParam("files") List<MultipartFile> files) {
        List<Document> results = documentService.batchUpload(directoryId, repoType, files);
        return R.ok("成功上传 " + results.size() + " 个文件", results);
    }

    // ==================== 文件预览和下载 ====================

    /**
     * 预览文档（流式返回）
     * 浏览器可直接展示 PDF、图片等支持的格式
     */
    @Operation(summary = "预览文档", description = "流式返回文档内容，浏览器可直接展示PDF/图片等")
    @GetMapping("/preview/{id}")
    public void preview(@Parameter(description = "文档ID") @PathVariable Long id, HttpServletResponse response) throws IOException {
        Document document = documentService.getById(id);

        try (InputStream inputStream = documentService.preview(id)) {
            response.setContentType(getContentType(document.getFileType()));
            response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");

            OutputStream out = response.getOutputStream();
            byte[] buffer = new byte[8192];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            out.flush();
        }
    }

    /**
     * 下载文档（流式响应，避免大文件 OOM）
     */
    @Operation(summary = "下载文档", description = "流式下载文档文件，支持大文件")
    @GetMapping("/download/{id}")
    public ResponseEntity<StreamingResponseBody> download(@Parameter(description = "文档ID") @PathVariable Long id) throws IOException {
        final DocumentService.FileDownloadResult result = documentService.download(id);
        final String fileName = URLEncoder.encode(result.getFileName(), "UTF-8")
                .replace("+", "%20");

        StreamingResponseBody stream = new StreamingResponseBody() {
            @Override
            public void writeTo(OutputStream outputStream) throws IOException {
                InputStream inputStream = result.getInputStream();
                try {
                    byte[] buffer = new byte[8192];
                    int len;
                    while ((len = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, len);
                    }
                    outputStream.flush();
                } finally {
                    inputStream.close();
                }
            }
        };

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + fileName)
                .contentType(MediaType.parseMediaType(result.getContentType()))
                .contentLength(result.getFileSize())
                .body(stream);
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
