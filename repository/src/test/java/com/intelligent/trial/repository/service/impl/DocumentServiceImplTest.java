package com.intelligent.trial.repository.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.intelligent.trial.common.exception.BusinessException;
import com.intelligent.trial.repository.config.MinioConfig;
import com.intelligent.trial.repository.dto.DocumentSearchDTO;
import com.intelligent.trial.repository.entity.Document;
import com.intelligent.trial.repository.mapper.DocumentMapper;
import com.intelligent.trial.repository.service.DocumentService.FileDownloadResult;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.ObjectWriteResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * DocumentServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
class DocumentServiceImplTest {

    @InjectMocks
    private DocumentServiceImpl documentService;

    @Mock
    private DocumentMapper documentMapper;

    @Mock
    private MinioClient minioClient;

    @Mock
    private MinioConfig minioConfig;

    // ==================== create() ====================

    @Test
    void create_shouldSetDefaultStatusWhenNull() {
        Document document = new Document();
        document.setTitle("Test Document");
        document.setStatus(null);

        when(documentMapper.insert(document)).thenReturn(1);
        when(documentMapper.selectById(document.getId())).thenReturn(document);

        Document result = documentService.create(document);

        assertNotNull(result);
        assertEquals(Integer.valueOf(0), document.getStatus());
        verify(documentMapper).insert(document);
    }

    @Test
    void create_shouldKeepExistingStatusWhenSet() {
        Document document = new Document();
        document.setTitle("Test Document");
        document.setStatus(1);

        when(documentMapper.insert(document)).thenReturn(1);
        when(documentMapper.selectById(document.getId())).thenReturn(document);

        Document result = documentService.create(document);

        assertNotNull(result);
        assertEquals(Integer.valueOf(1), document.getStatus());
    }

    // ==================== update() ====================

    @Test
    void update_shouldThrowWhenNotFound() {
        Document document = new Document();
        document.setId(999L);

        when(documentMapper.selectById(999L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> documentService.update(document));
        assertTrue(ex.getMessage().contains("文档不存在"));
        verify(documentMapper, never()).updateById(any());
    }

    @Test
    void update_shouldUpdateExistingDocument() {
        Document existing = new Document();
        existing.setId(1L);
        existing.setTitle("Old Title");

        Document document = new Document();
        document.setId(1L);
        document.setTitle("New Title");

        when(documentMapper.selectById(1L)).thenReturn(existing, document);
        when(documentMapper.updateById(document)).thenReturn(1);

        Document result = documentService.update(document);

        assertNotNull(result);
        verify(documentMapper).updateById(document);
    }

    // ==================== delete() ====================

    @Test
    void delete_shouldThrowWhenNotFound() {
        when(documentMapper.selectById(999L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> documentService.delete(999L));
        assertTrue(ex.getMessage().contains("文档不存在"));
        verify(documentMapper, never()).deleteById(any());
    }

    @Test
    void delete_shouldDeleteFromMinIOAndDB() throws Exception {
        Document document = new Document();
        document.setId(1L);
        document.setFilePath("documents/123/test.pdf");

        when(documentMapper.selectById(1L)).thenReturn(document);
        when(minioConfig.getBucketName()).thenReturn("test-bucket");
        doNothing().when(minioClient).removeObject(any(RemoveObjectArgs.class));
        when(documentMapper.deleteById(1L)).thenReturn(1);

        documentService.delete(1L);

        verify(minioClient).removeObject(any(RemoveObjectArgs.class));
        verify(documentMapper).deleteById(1L);
    }

    @Test
    void delete_shouldDeleteFromDBEvenWhenNoFilePath() throws Exception {
        Document document = new Document();
        document.setId(1L);
        document.setFilePath(null);

        when(documentMapper.selectById(1L)).thenReturn(document);
        when(documentMapper.deleteById(1L)).thenReturn(1);

        documentService.delete(1L);

        verify(minioClient, never()).removeObject(any());
        verify(documentMapper).deleteById(1L);
    }

    @Test
    void delete_shouldDeleteFromDBEvenWhenMinIOFails() throws Exception {
        Document document = new Document();
        document.setId(1L);
        document.setFilePath("documents/123/test.pdf");

        when(documentMapper.selectById(1L)).thenReturn(document);
        when(minioConfig.getBucketName()).thenReturn("test-bucket");
        doThrow(new RuntimeException("MinIO error")).when(minioClient).removeObject(any(RemoveObjectArgs.class));
        when(documentMapper.deleteById(1L)).thenReturn(1);

        documentService.delete(1L);

        verify(documentMapper).deleteById(1L);
    }

    // ==================== getById() ====================

    @Test
    void getById_shouldThrowWhenNotFound() {
        when(documentMapper.selectById(999L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> documentService.getById(999L));
        assertTrue(ex.getMessage().contains("文档不存在"));
    }

    @Test
    void getById_shouldReturnDocumentWhenFound() {
        Document document = new Document();
        document.setId(1L);
        document.setTitle("Test");

        when(documentMapper.selectById(1L)).thenReturn(document);

        Document result = documentService.getById(1L);

        assertNotNull(result);
        assertEquals(Long.valueOf(1L), result.getId());
        assertEquals("Test", result.getTitle());
    }

    // ==================== search() ====================

    @Test
    void search_shouldCallMapperWithCorrectParams() {
        DocumentSearchDTO searchDTO = new DocumentSearchDTO();
        searchDTO.setPageNum(1);
        searchDTO.setPageSize(10);
        searchDTO.setRepoType(1);
        searchDTO.setKeyword("test");
        searchDTO.setDirectoryId(5L);
        searchDTO.setValidityStatus("valid");
        searchDTO.setPublishUnit("Court A");

        IPage<Document> expectedPage = new Page<>(1, 10);

        when(documentMapper.searchDocuments(any(Page.class), eq(1), eq("test"), eq(5L),
                eq("valid"), isNull(), isNull(), isNull(), eq("Court A"), isNull()))
                .thenReturn(expectedPage);

        IPage<Document> result = documentService.search(searchDTO);

        assertNotNull(result);
        verify(documentMapper).searchDocuments(any(Page.class), eq(1), eq("test"), eq(5L),
                eq("valid"), isNull(), isNull(), isNull(), eq("Court A"), isNull());
    }

    @Test
    void search_shouldHandleNullParams() {
        DocumentSearchDTO searchDTO = new DocumentSearchDTO();
        searchDTO.setPageNum(1);
        searchDTO.setPageSize(10);

        IPage<Document> expectedPage = new Page<>(1, 10);
        when(documentMapper.searchDocuments(any(Page.class), isNull(), isNull(), isNull(),
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(expectedPage);

        IPage<Document> result = documentService.search(searchDTO);

        assertNotNull(result);
        verify(documentMapper).searchDocuments(any(Page.class), isNull(), isNull(), isNull(),
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
    }

    // ==================== upload() ====================

    @Test
    void upload_shouldThrowWhenFileIsEmpty() {
        Document document = new Document();
        MockMultipartFile emptyFile = new MockMultipartFile("file", "test.pdf", "application/pdf", new byte[0]);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> documentService.upload(document, emptyFile));
        assertTrue(ex.getMessage().contains("上传文件不能为空"));
    }

    @Test
    void upload_shouldThrowWhenFileIsNull() {
        Document document = new Document();

        BusinessException ex = assertThrows(BusinessException.class,
                () -> documentService.upload(document, null));
        assertTrue(ex.getMessage().contains("上传文件不能为空"));
    }

    @Test
    void upload_shouldGenerateMinIOPathAndSetStatus() throws Exception {
        Document document = new Document();
        document.setTitle("Test");
        document.setStatus(null);

        byte[] fileContent = "test content".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", fileContent);

        when(minioConfig.getBucketName()).thenReturn("test-bucket");
        when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(mock(ObjectWriteResponse.class));
        when(documentMapper.insert(document)).thenReturn(1);
        when(documentMapper.selectById(document.getId())).thenReturn(document);

        Document result = documentService.upload(document, file);

        assertNotNull(result);
        assertNotNull(document.getFilePath());
        assertTrue(document.getFilePath().startsWith("documents/"));
        assertEquals(Integer.valueOf(1), document.getStatus());
        assertEquals(Long.valueOf(fileContent.length), document.getFileSize());
        assertEquals("pdf", document.getFileType());
        verify(minioClient).putObject(any(PutObjectArgs.class));
        verify(documentMapper).insert(document);
    }

    @Test
    void upload_shouldPreserveExistingStatus() throws Exception {
        Document document = new Document();
        document.setTitle("Test");
        document.setStatus(0);

        byte[] fileContent = "test content".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", fileContent);

        when(minioConfig.getBucketName()).thenReturn("test-bucket");
        when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(mock(ObjectWriteResponse.class));
        when(documentMapper.insert(document)).thenReturn(1);
        when(documentMapper.selectById(document.getId())).thenReturn(document);

        documentService.upload(document, file);

        assertEquals(Integer.valueOf(0), document.getStatus());
    }

    // ==================== batchUpload() ====================

    @Test
    void batchUpload_shouldThrowWhenFilesListEmpty() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> documentService.batchUpload(1L, 1, Collections.<MultipartFile>emptyList()));
        assertTrue(ex.getMessage().contains("上传文件不能为空"));
    }

    @Test
    void batchUpload_shouldThrowWhenFilesListNull() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> documentService.batchUpload(1L, 1, null));
        assertTrue(ex.getMessage().contains("上传文件不能为空"));
    }

    @Test
    void batchUpload_shouldCreateDocForEachFile() throws Exception {
        byte[] content1 = "content1".getBytes();
        byte[] content2 = "content2".getBytes();
        MockMultipartFile file1 = new MockMultipartFile("file", "doc1.pdf", "application/pdf", content1);
        MockMultipartFile file2 = new MockMultipartFile("file", "doc2.pdf", "application/pdf", content2);

        List<MultipartFile> files = Arrays.<MultipartFile>asList(file1, file2);

        when(minioConfig.getBucketName()).thenReturn("test-bucket");
        when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(mock(ObjectWriteResponse.class));
        when(documentMapper.insert(any(Document.class))).thenAnswer(invocation -> {
            Document doc = invocation.getArgument(0);
            doc.setId(doc.getTitle().equals("doc1.pdf") ? 1L : 2L);
            return 1;
        });
        when(documentMapper.selectById(1L)).thenAnswer(invocation -> {
            Document d = new Document();
            d.setId(1L);
            d.setTitle("doc1.pdf");
            return d;
        });
        when(documentMapper.selectById(2L)).thenAnswer(invocation -> {
            Document d = new Document();
            d.setId(2L);
            d.setTitle("doc2.pdf");
            return d;
        });

        List<Document> results = documentService.batchUpload(1L, 1, files);

        assertNotNull(results);
        assertEquals(2, results.size());
        verify(documentMapper, times(2)).insert(any(Document.class));
    }

    // ==================== preview() ====================

    @Test
    void preview_shouldThrowWhenNoFilePath() {
        Document document = new Document();
        document.setId(1L);
        document.setFilePath(null);

        when(documentMapper.selectById(1L)).thenReturn(document);

        BusinessException ex = assertThrows(BusinessException.class, () -> documentService.preview(1L));
        assertTrue(ex.getMessage().contains("文档不存在"));
    }

    @Test
    void preview_shouldReturnInputStreamFromMinIO() throws Exception {
        Document document = new Document();
        document.setId(1L);
        document.setFilePath("documents/123/test.pdf");
        document.setFileType("pdf");

        GetObjectResponse mockResponse = mock(GetObjectResponse.class);

        when(documentMapper.selectById(1L)).thenReturn(document);
        when(minioConfig.getBucketName()).thenReturn("test-bucket");
        when(minioClient.getObject(any(GetObjectArgs.class))).thenReturn(mockResponse);

        InputStream result = documentService.preview(1L);

        assertNotNull(result);
        verify(minioClient).getObject(any(GetObjectArgs.class));
    }

    // ==================== download() ====================

    @Test
    void download_shouldReturnFileDownloadResult() throws Exception {
        Document document = new Document();
        document.setId(1L);
        document.setTitle("TestDoc");
        document.setFilePath("documents/123/test.pdf");
        document.setFileType("pdf");
        document.setFileSize(1024L);

        GetObjectResponse mockResponse = mock(GetObjectResponse.class);

        when(documentMapper.selectById(1L)).thenReturn(document);
        when(minioConfig.getBucketName()).thenReturn("test-bucket");
        when(minioClient.getObject(any(GetObjectArgs.class))).thenReturn(mockResponse);

        FileDownloadResult result = documentService.download(1L);

        assertNotNull(result);
        assertEquals("TestDoc.pdf", result.getFileName());
        assertEquals("application/pdf", result.getContentType());
        assertEquals(Long.valueOf(1024L), result.getFileSize());
    }

    @Test
    void download_shouldUseFilePathNameWhenTitleMissing() throws Exception {
        Document document = new Document();
        document.setId(1L);
        document.setTitle(null);
        document.setFilePath("documents/123/report.pdf");
        document.setFileType("pdf");
        document.setFileSize(512L);

        GetObjectResponse mockResponse = mock(GetObjectResponse.class);

        when(documentMapper.selectById(1L)).thenReturn(document);
        when(minioConfig.getBucketName()).thenReturn("test-bucket");
        when(minioClient.getObject(any(GetObjectArgs.class))).thenReturn(mockResponse);

        FileDownloadResult result = documentService.download(1L);

        assertNotNull(result);
        assertEquals("report.pdf", result.getFileName());
    }

    @Test
    void download_shouldAddExtensionWhenTitleDoesNotHaveIt() throws Exception {
        Document document = new Document();
        document.setId(1L);
        document.setTitle("MyReport");
        document.setFilePath("documents/123/file.pdf");
        document.setFileType("pdf");
        document.setFileSize(256L);

        GetObjectResponse mockResponse = mock(GetObjectResponse.class);

        when(documentMapper.selectById(1L)).thenReturn(document);
        when(minioConfig.getBucketName()).thenReturn("test-bucket");
        when(minioClient.getObject(any(GetObjectArgs.class))).thenReturn(mockResponse);

        FileDownloadResult result = documentService.download(1L);

        assertNotNull(result);
        assertEquals("MyReport.pdf", result.getFileName());
    }

    @Test
    void download_shouldNotDuplicateExtension() throws Exception {
        Document document = new Document();
        document.setId(1L);
        document.setTitle("MyReport.pdf");
        document.setFilePath("documents/123/file.pdf");
        document.setFileType("pdf");
        document.setFileSize(256L);

        GetObjectResponse mockResponse = mock(GetObjectResponse.class);

        when(documentMapper.selectById(1L)).thenReturn(document);
        when(minioConfig.getBucketName()).thenReturn("test-bucket");
        when(minioClient.getObject(any(GetObjectArgs.class))).thenReturn(mockResponse);

        FileDownloadResult result = documentService.download(1L);

        assertNotNull(result);
        assertEquals("MyReport.pdf", result.getFileName());
    }

    @Test
    void download_shouldThrowWhenNoFilePath() {
        Document document = new Document();
        document.setId(1L);
        document.setFilePath(null);

        when(documentMapper.selectById(1L)).thenReturn(document);

        BusinessException ex = assertThrows(BusinessException.class, () -> documentService.download(1L));
        assertTrue(ex.getMessage().contains("文档不存在"));
    }

    // ==================== getByCaseId() ====================

    @Test
    void getByCaseId_shouldReturnListOfDocuments() {
        Document doc1 = new Document();
        doc1.setId(1L);
        doc1.setCaseId(10L);
        doc1.setTitle("Case Doc 1");

        Document doc2 = new Document();
        doc2.setId(2L);
        doc2.setCaseId(10L);
        doc2.setTitle("Case Doc 2");

        List<Document> docs = Arrays.asList(doc1, doc2);
        when(documentMapper.selectByCaseId(10L)).thenReturn(docs);

        List<Document> result = documentService.getByCaseId(10L);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(documentMapper).selectByCaseId(10L);
    }

    @Test
    void getByCaseId_shouldReturnEmptyListWhenNoDocuments() {
        when(documentMapper.selectByCaseId(999L)).thenReturn(Collections.<Document>emptyList());

        List<Document> result = documentService.getByCaseId(999L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
