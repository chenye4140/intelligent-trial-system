package com.intelligent.trial.document.service;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.intelligent.trial.document.client.LlmClient;
import com.intelligent.trial.document.dto.ParseResultDTO;
import com.intelligent.trial.document.entity.DocParseTask;
import com.intelligent.trial.document.mapper.DocParseTaskMapper;
import com.intelligent.trial.document.util.MinioUtil;
import com.intelligent.trial.document.util.WordParseUtil;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 文档解析服务
 * 核心业务逻辑：文件上传 → 解析 → AI分类 → 向量生成 → 入库
 *
 * @author intelligent-trial
 */
@Service
public class DocumentParseService {

    private static final Logger log = LoggerFactory.getLogger(DocumentParseService.class);

    /**
     * 支持的文件类型
     */
    private static final Set<String> SUPPORTED_TYPES = new HashSet<>(Arrays.asList(
            "doc", "docx", "pdf", "png", "jpg", "jpeg", "bmp", "gif", "tiff"
    ));

    @Autowired
    private DocParseTaskMapper taskMapper;

    @Autowired
    private MinioUtil minioUtil;

    @Autowired
    private LlmClient llmClient;

    @Autowired
    private VectorStorageService vectorStorageService;

    /**
     * 临时文件目录
     */
    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir") + "/doc-parse/";

    /**
     * 上传文件并创建解析任务
     *
     * @param file 上传的文件
     * @return 任务ID
     */
    public Long uploadAndCreateTask(MultipartFile file) {
        // 校验文件类型
        String fileType = getFileExtension(file.getOriginalFilename());
        validateFileType(fileType);

        // 上传到 MinIO
        String objectKey = minioUtil.uploadFile(file);

        // 创建解析任务记录
        DocParseTask task = new DocParseTask();
        task.setFileName(file.getOriginalFilename());
        task.setFilePath(objectKey);
        task.setFileType(fileType);
        task.setStatus(0); // 待处理
        task.setProgress(0);
        task.setVectorCount(0);
        task.setCreateTime(new Date());
        task.setUpdateTime(new Date());

        taskMapper.insert(task);
        log.info("创建解析任务: id={}, fileName={}", task.getId(), file.getOriginalFilename());

        // 异步执行解析
        asyncParse(task.getId(), objectKey, fileType);

        return task.getId();
    }

    /**
     * 异步解析文档
     *
     * @param taskId   任务ID
     * @param objectKey MinIO 对象键
     * @param fileType 文件类型
     */
    @Async("documentParseExecutor")
    public void asyncParse(Long taskId, String objectKey, String fileType) {
        long startTime = System.currentTimeMillis();
        DocParseTask task = new DocParseTask();
        task.setId(taskId);

        try {
            log.info("开始异步解析任务: taskId={}, fileType={}", taskId, fileType);

            // 更新状态为解析中
            task.setStatus(1);
            task.setProgress(5);
            task.setUpdateTime(new Date());
            taskMapper.updateById(task);

            ParseResultDTO result;

            // 根据文件类型选择解析方式
            if ("doc".equalsIgnoreCase(fileType) || "docx".equalsIgnoreCase(fileType)) {
                result = parseWord(taskId, objectKey);
            } else if ("pdf".equalsIgnoreCase(fileType)) {
                result = parsePdf(taskId, objectKey);
            } else if (isImageType(fileType)) {
                result = parseImage(taskId, objectKey, fileType);
            } else {
                throw new UnsupportedOperationException("不支持的文件类型: " + fileType);
            }

            // 段落分类（进度 60%-80%）
            updateProgress(taskId, 60);
            if (result != null && result.getParagraphs() != null) {
                List<ParseResultDTO.ParagraphDTO> classified =
                        llmClient.classifyParagraphs(result.getParagraphs());
                result.setParagraphs(classified);
            }

            // 向量生成与入库（进度 80%-95%）
            updateProgress(taskId, 80);
            int vectorCount = generateAndStoreVectors(taskId, result);
            result.getMetadata().setParseDurationMs(System.currentTimeMillis() - startTime);

            // 更新任务状态为完成
            updateProgress(taskId, 100);
            task.setStatus(2);
            task.setProgress(100);
            task.setResultJson(JSON.toJSONString(result));
            task.setVectorCount(vectorCount);
            task.setParseTime(new Date());
            task.setUpdateTime(new Date());
            taskMapper.updateById(task);

            log.info("解析任务完成: taskId={}, 段落数={}, 向量数={}",
                    taskId, result.getParagraphs().size(), vectorCount);

        } catch (Exception e) {
            log.error("解析任务失败: taskId={}", taskId, e);
            // 更新状态为失败
            task.setStatus(3);
            task.setErrorMsg(e.getMessage());
            task.setUpdateTime(new Date());
            taskMapper.updateById(task);
        }
    }

    /**
     * 解析 Word 文档
     */
    private ParseResultDTO parseWord(Long taskId, String objectKey) throws IOException {
        log.info("开始解析Word文档: {}", objectKey);
        updateProgress(taskId, 15);

        // 下载到临时文件
        File tempFile = downloadToTemp(objectKey);
        try {
            String fileType = getFileExtension(objectKey);
            ParseResultDTO result = WordParseUtil.parseWord(tempFile.getAbsolutePath(), fileType);
            updateProgress(taskId, 40);
            log.info("Word解析完成，共 {} 个段落", result.getParagraphs().size());
            return result;
        } finally {
            deleteTempFile(tempFile);
        }
    }

    /**
     * 解析 PDF 文档（使用 OCR）
     */
    private ParseResultDTO parsePdf(Long taskId, String objectKey) throws IOException {
        log.info("开始解析PDF文档: {}", objectKey);
        updateProgress(taskId, 10);

        // 下载PDF到临时文件
        File pdfFile = downloadToTemp(objectKey);
        File tempDir = new File(TEMP_DIR + "/pdf-images-" + taskId);
        tempDir.mkdirs();

        List<ParseResultDTO.ParagraphDTO> allParagraphs = new ArrayList<>();
        int totalPages = 0;

        try (PDDocument document = PDDocument.load(pdfFile)) {
            totalPages = document.getNumberOfPages();
            PDFRenderer renderer = new PDFRenderer(document);

            // 将每一页渲染为图片，然后进行 OCR
            for (int i = 0; i < totalPages; i++) {
                log.info("PDF第 {}/{} 页渲染中", i + 1, totalPages);
                BufferedImage image = renderer.renderImageWithDPI(i, 300);
                String imagePath = tempDir.getAbsolutePath() + "/page-" + (i + 1) + ".png";
                ImageIO.write(image, "png", new File(imagePath));

                // 上传到 MinIO 获取 URL
                String pageObjectKey = "temp/pdf-" + taskId + "/page-" + (i + 1) + ".png";
                try (FileInputStream fis = new FileInputStream(imagePath)) {
                    minioUtil.uploadStream(fis, "page-" + (i + 1) + ".png", "image/png",
                            Files.size(new File(imagePath).toPath()));
                }

                // 获取公开 URL 进行 OCR
                String imageUrl = minioUtil.getPermanentUrl(pageObjectKey);

                // 调用 OCR
                String ocrText = llmClient.recognizeImage(imageUrl,
                        "请识别并提取此文档页面的所有文字内容，按原文段落结构输出，保持原文的层次结构。");

                // 将 OCR 结果拆分为段落
                List<ParseResultDTO.ParagraphDTO> pageParagraphs = splitToParagraphs(ocrText, i + 1);
                allParagraphs.addAll(pageParagraphs);

                int progress = 15 + (int) ((i + 1) * 40.0 / totalPages);
                updateProgress(taskId, Math.min(progress, 55));
            }
        } finally {
            // 清理临时文件
            deleteTempFile(pdfFile);
            deleteDirectory(tempDir);
        }

        ParseResultDTO result = new ParseResultDTO();
        result.setParagraphs(allParagraphs);
        ParseResultDTO.MetadataDTO metadata = new ParseResultDTO.MetadataDTO();
        metadata.setTotalParagraphs(allParagraphs.size());
        metadata.setTotalPages(totalPages);
        int totalChars = allParagraphs.stream().mapToInt(p ->
                p.getContent() != null ? p.getContent().length() : 0).sum();
        metadata.setTotalCharacters(totalChars);
        result.setMetadata(metadata);

        log.info("PDF解析完成，共 {} 页，{} 个段落", totalPages, allParagraphs.size());
        return result;
    }

    /**
     * 解析图片文件
     */
    private ParseResultDTO parseImage(Long taskId, String objectKey, String fileType) throws IOException {
        log.info("开始解析图片: {}", objectKey);
        updateProgress(taskId, 15);

        // 下载图片
        File tempFile = downloadToTemp(objectKey);
        try {
            // 读取图片并转为 Base64
            byte[] imageBytes = Files.readAllBytes(tempFile.toPath());
            String base64 = Base64.getEncoder().encodeToString(imageBytes);
            String mimeType = getMimeType(fileType);

            // 调用 OCR
            String ocrText = llmClient.recognizeImageBase64(base64, mimeType,
                    "请识别并提取图片中的所有文字内容，按原文段落结构输出。");

            // 将 OCR 结果拆分为段落
            List<ParseResultDTO.ParagraphDTO> paragraphs = splitToParagraphs(ocrText, 1);
            updateProgress(taskId, 50);

            ParseResultDTO result = new ParseResultDTO();
            result.setParagraphs(paragraphs);
            ParseResultDTO.MetadataDTO metadata = new ParseResultDTO.MetadataDTO();
            metadata.setTotalParagraphs(paragraphs.size());
            metadata.setTotalPages(1);
            int totalChars = paragraphs.stream().mapToInt(p ->
                    p.getContent() != null ? p.getContent().length() : 0).sum();
            metadata.setTotalCharacters(totalChars);
            result.setMetadata(metadata);

            log.info("图片解析完成，共 {} 个段落", paragraphs.size());
            return result;
        } finally {
            deleteTempFile(tempFile);
        }
    }

    /**
     * 生成向量并存储到数据库
     *
     * @param taskId 任务ID
     * @param result 解析结果
     * @return 生成的向量数量
     */
    private int generateAndStoreVectors(Long taskId, ParseResultDTO result) {
        if (result == null || result.getParagraphs() == null) {
            return 0;
        }

        // 过滤有实际内容的段落，保留原始索引映射
        List<String> texts = new ArrayList<>();
        List<ParseResultDTO.ParagraphDTO> validParagraphs = new ArrayList<>();

        for (ParseResultDTO.ParagraphDTO p : result.getParagraphs()) {
            if (p.getContent() != null && !p.getContent().trim().isEmpty()) {
                texts.add(p.getContent());
                validParagraphs.add(p);
            }
        }

        if (texts.isEmpty()) {
            return 0;
        }

        log.info("开始生成向量，共 {} 段文本", texts.size());

        try {
            // 调用 DashScope 生成向量
            List<float[]> vectors = llmClient.generateEmbeddings(texts);

            // 将向量存入数据库，并回填 vectorId
            vectorStorageService.storeVectors(taskId, texts, vectors, validParagraphs);

            log.info("向量存储完成: taskId={}, {} 个向量，维度: {}",
                    taskId, vectors.size(), vectors.isEmpty() ? 0 : vectors.get(0).length);

            return vectors.size();
        } catch (Exception e) {
            log.error("向量生成或存储失败: taskId={}", taskId, e);
            return 0;
        }
    }

    // ========================= 辅助方法 =========================

    /**
     * 将 OCR 文本拆分为段落
     */
    private List<ParseResultDTO.ParagraphDTO> splitToParagraphs(String text, int pageNumber) {
        List<ParseResultDTO.ParagraphDTO> paragraphs = new ArrayList<>();
        if (text == null || text.trim().isEmpty()) {
            return paragraphs;
        }

        String[] lines = text.split("\n");
        int position = 0;
        StringBuilder currentParagraph = new StringBuilder();

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                // 空行表示段落结束
                if (currentParagraph.length() > 0) {
                    ParseResultDTO.ParagraphDTO p = new ParseResultDTO.ParagraphDTO();
                    p.setContent(currentParagraph.toString().trim());
                    p.setStyle(WordParseUtil.inferStyleFromContent(p.getContent()));
                    p.setPosition(++position);
                    paragraphs.add(p);
                    currentParagraph = new StringBuilder();
                }
            } else {
                // 检测是否为标题行
                if (currentParagraph.length() == 0 && isHeadingLine(trimmed)) {
                    // 如果之前的段落还未添加，先添加
                    ParseResultDTO.ParagraphDTO p = new ParseResultDTO.ParagraphDTO();
                    p.setContent(trimmed);
                    p.setStyle("heading");
                    Integer level = inferLawLevel(trimmed);
                    p.setLevel(level);
                    p.setPosition(++position);
                    paragraphs.add(p);
                } else {
                    if (currentParagraph.length() > 0) {
                        currentParagraph.append(" ");
                    }
                    currentParagraph.append(trimmed);
                }
            }
        }

        // 添加最后一个段落
        if (currentParagraph.length() > 0) {
            ParseResultDTO.ParagraphDTO p = new ParseResultDTO.ParagraphDTO();
            p.setContent(currentParagraph.toString().trim());
            p.setStyle(WordParseUtil.inferStyleFromContent(p.getContent()));
            p.setPosition(++position);
            paragraphs.add(p);
        }

        return paragraphs;
    }

    /**
     * 判断是否为标题行
     */
    private boolean isHeadingLine(String line) {
        // 匹配法规层级模式：第一章、第二条、第三节等
        return line.matches("^[第][一二三四五六七八九十百千0-9]+[篇章章节条款项目].+")
                || line.matches("^[一二三四五六七八九十]+[、].{1,30}$")
                || (line.length() < 30 && !line.contains("。") && !line.contains("，"));
    }

    /**
     * 推断法规层级
     */
    private Integer inferLawLevel(String text) {
        if (text.contains("第") && text.contains("篇")) return 1;
        if (text.contains("第") && text.contains("章")) return 2;
        if (text.contains("第") && text.contains("节")) return 3;
        if (text.contains("第") && text.contains("条")) return 4;
        if (text.contains("第") && text.contains("款")) return 5;
        if (text.contains("第") && text.contains("项")) return 6;
        return null;
    }

    /**
     * 下载文件到临时目录
     */
    private File downloadToTemp(String objectKey) throws IOException {
        File tempDir = new File(TEMP_DIR);
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }

        String fileName = objectKey.substring(objectKey.lastIndexOf("/") + 1);
        File tempFile = new File(TEMP_DIR + "/" + UUID.randomUUID().toString() + "_" + fileName);

        try (InputStream is = minioUtil.downloadFile(objectKey);
             FileOutputStream fos = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        }

        return tempFile;
    }

    /**
     * 删除临时文件
     */
    private void deleteTempFile(File file) {
        if (file != null && file.exists()) {
            try {
                Files.delete(file.toPath());
            } catch (IOException e) {
                log.warn("删除临时文件失败: {}", file.getAbsolutePath(), e);
            }
        }
    }

    /**
     * 删除临时目录
     */
    private void deleteDirectory(File directory) {
        if (directory != null && directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteTempFile(file);
                }
            }
            try {
                Files.delete(directory.toPath());
            } catch (IOException e) {
                log.warn("删除临时目录失败: {}", directory.getAbsolutePath(), e);
            }
        }
    }

    /**
     * 更新任务进度
     */
    private void updateProgress(Long taskId, int progress) {
        try {
            DocParseTask update = new DocParseTask();
            update.setId(taskId);
            update.setProgress(progress);
            update.setUpdateTime(new Date());
            taskMapper.updateById(update);
        } catch (Exception e) {
            log.warn("更新任务进度失败: taskId={}, progress={}", taskId, progress, e);
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }

    /**
     * 校验文件类型
     */
    private void validateFileType(String fileType) {
        if (!SUPPORTED_TYPES.contains(fileType.toLowerCase())) {
            throw new IllegalArgumentException(
                    "不支持的文件类型: " + fileType + "，支持的类型: " + SUPPORTED_TYPES);
        }
    }

    /**
     * 判断是否为图片类型
     */
    private boolean isImageType(String fileType) {
        return "png".equalsIgnoreCase(fileType) || "jpg".equalsIgnoreCase(fileType)
                || "jpeg".equalsIgnoreCase(fileType) || "bmp".equalsIgnoreCase(fileType)
                || "gif".equalsIgnoreCase(fileType) || "tiff".equalsIgnoreCase(fileType);
    }

    /**
     * 获取 MIME 类型
     */
    private String getMimeType(String fileType) {
        switch (fileType.toLowerCase()) {
            case "png": return "image/png";
            case "jpg":
            case "jpeg": return "image/jpeg";
            case "bmp": return "image/bmp";
            case "gif": return "image/gif";
            case "tiff": return "image/tiff";
            default: return "image/png";
        }
    }

    // ========================= 对外查询方法 =========================

    /**
     * 根据 ID 查询任务
     */
    public DocParseTask getTaskById(Long taskId) {
        return taskMapper.selectById(taskId);
    }

    /**
     * 分页查询任务
     */
    public Page<DocParseTask> listTasks(int pageNum, int pageSize, Integer status, String fileType) {
        LambdaQueryWrapper<DocParseTask> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(DocParseTask::getStatus, status);
        }
        if (fileType != null && !fileType.isEmpty()) {
            wrapper.eq(DocParseTask::getFileType, fileType);
        }
        wrapper.orderByDesc(DocParseTask::getCreateTime);
        return taskMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    /**
     * 重试失败任务
     */
    public void retryTask(Long taskId) {
        DocParseTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在: " + taskId);
        }
        task.setStatus(0);
        task.setProgress(0);
        task.setErrorMsg(null);
        task.setParseTime(null);
        task.setVectorCount(0);
        task.setUpdateTime(new java.util.Date());
        taskMapper.updateById(task);
        asyncParse(task.getId(), task.getFilePath(), task.getFileType());
    }

    /**
     * 删除任务
     */
    public boolean deleteTaskById(Long taskId) {
        return taskMapper.deleteById(taskId) > 0;
    }
}
