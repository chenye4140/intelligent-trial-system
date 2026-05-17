package com.intelligent.trial.repository;

import com.intelligent.trial.repository.config.MinioConfig;
import com.intelligent.trial.repository.entity.Directory;
import com.intelligent.trial.repository.entity.Document;
import io.minio.MinioClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Repository 模块集成测试 — 使用 H2 内存数据库 + Mock MinIO，
 * 对核心 REST API 进行端到端 HTTP 级别测试。
 *
 * 覆盖 Document 和 Directory 两大控制器的关键端点。
 */
@SpringBootTest(
        classes = RepositoryApplication.class
)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Repository 模块集成测试")
class RepositoryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // Mock MinIO — 集成测试不依赖真实 MinIO 服务
    @MockBean
    private MinioClient minioClient;

    @MockBean
    private MinioConfig minioConfig;

    @BeforeEach
    void setUp() {
        when(minioConfig.getEndpoint()).thenReturn("http://localhost:9000");
        when(minioConfig.getBucketName()).thenReturn("trial-documents");
    }

    // ==================== Directory CRUD 测试 ====================

    @Test
    @DisplayName("POST /api/repository/directory/create - 创建目录成功")
    void createDirectory_success() throws Exception {
        mockMvc.perform(post("/api/repository/directory/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"repoType\":1,\"parentId\":0,\"name\":\"测试目录\",\"sort\":0,\"status\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("测试目录"))
                .andExpect(jsonPath("$.data.repoType").value(1))
                .andExpect(jsonPath("$.data.parentId").value(0));
    }

    @Test
    @DisplayName("GET /api/repository/directory/tree - 获取目录树")
    void getDirectoryTree_success() throws Exception {
        mockMvc.perform(get("/api/repository/directory/tree")
                        .param("repoType", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("GET /api/repository/directory/tree - 获取资料库目录树")
    void getDirectoryTree_materialLibrary() throws Exception {
        mockMvc.perform(get("/api/repository/directory/tree")
                        .param("repoType", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("GET /api/repository/directory/get/{id} - 获取目录详情")
    void getDirectoryById_success() throws Exception {
        mockMvc.perform(get("/api/repository/directory/get/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("党章"));
    }

    @Test
    @DisplayName("PUT /api/repository/directory/update - 更新目录成功")
    void updateDirectory_success() throws Exception {
        mockMvc.perform(put("/api/repository/directory/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":1,\"name\":\"更新后的党内法规\",\"repoType\":1,\"parentId\":0,\"sort\":1,\"status\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("PUT /api/repository/directory/sort/{id} - 更新目录排序")
    void updateDirectorySort_success() throws Exception {
        mockMvc.perform(put("/api/repository/directory/sort/1")
                        .param("sort", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("DELETE /api/repository/directory/delete/{id} - 删除目录（级联删除子目录）")
    void deleteDirectory_cascade() throws Exception {
        // 先创建一个目录
        String createResult = mockMvc.perform(post("/api/repository/directory/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"repoType\":1,\"parentId\":0,\"name\":\"待删除目录\",\"sort\":99,\"status\":1}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // 从响应中提取 ID
        com.fasterxml.jackson.databind.JsonNode node =
                new com.fasterxml.jackson.databind.ObjectMapper().readTree(createResult);
        Long newId = node.get("data").get("id").asLong();

        // 删除该目录
        mockMvc.perform(delete("/api/repository/directory/delete/" + newId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    // ==================== Document CRUD 测试 ====================

    @Test
    @DisplayName("POST /api/repository/document/create - 创建文档成功")
    void createDocument_success() throws Exception {
        mockMvc.perform(post("/api/repository/document/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"测试文档\",\"repoType\":1,\"directoryId\":1,\"status\":0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.title").value("测试文档"))
                .andExpect(jsonPath("$.data.status").value(0));
    }

    @Test
    @DisplayName("GET /api/repository/document/get/{id} - 获取文档详情")
    void getDocumentById_success() throws Exception {
        // 使用 ID 4/5 避免被 batchDelete 测试影响（该测试删除 ID 2/3）
        mockMvc.perform(get("/api/repository/document/get/4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.title").value("某市纪委关于张某违纪违法案的审理报告"))
                .andExpect(jsonPath("$.data.docNo").isEmpty());
    }

    @Test
    @DisplayName("GET /api/repository/document/get/{id} - 文档不存在返回 null")
    void getDocumentById_notFound() throws Exception {
        mockMvc.perform(get("/api/repository/document/get/99999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("PUT /api/repository/document/update - 更新文档成功")
    void updateDocument_success() throws Exception {
        mockMvc.perform(put("/api/repository/document/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":1,\"title\":\"更新后的党章\",\"repoType\":1,\"directoryId\":2,\"status\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("POST /api/repository/document/search - 搜索文档（默认分页）")
    void searchDocument_defaultPagination() throws Exception {
        mockMvc.perform(post("/api/repository/document/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pageNum\":1,\"pageSize\":10}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").isNotEmpty())
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    @DisplayName("POST /api/repository/document/search - 按库类型过滤")
    void searchDocument_filterByRepoType() throws Exception {
        mockMvc.perform(post("/api/repository/document/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"repoType\":1,\"pageNum\":1,\"pageSize\":10}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").isNumber());
    }

    @Test
    @DisplayName("POST /api/repository/document/search - 按标题关键词搜索")
    void searchDocument_filterByTitle() throws Exception {
        mockMvc.perform(post("/api/repository/document/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"党章\",\"pageNum\":1,\"pageSize\":10}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").isNumber());
    }

    @Test
    @DisplayName("DELETE /api/repository/document/delete/{id} - 删除文档")
    void deleteDocument_success() throws Exception {
        // 先创建一个文档（不带文件路径，避免 MinIO 删除）
        String createResult = mockMvc.perform(post("/api/repository/document/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"待删除文档\",\"repoType\":2,\"directoryId\":4,\"status\":0}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // 从响应中提取 ID
        com.fasterxml.jackson.databind.JsonNode node =
                new com.fasterxml.jackson.databind.ObjectMapper().readTree(createResult);
        Long newId = node.get("data").get("id").asLong();

        // 删除该文档
        mockMvc.perform(delete("/api/repository/document/delete/" + newId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("DELETE /api/repository/document/batch-delete - 批量删除文档")
    void batchDeleteDocuments_success() throws Exception {
        mockMvc.perform(delete("/api/repository/document/batch-delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[2,3]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("POST /api/repository/document/search - 按状态过滤")
    void searchDocument_filterByStatus() throws Exception {
        mockMvc.perform(post("/api/repository/document/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":1,\"pageNum\":1,\"pageSize\":10}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("POST /api/repository/document/search - 按目录 ID 过滤")
    void searchDocument_filterByDirectoryId() throws Exception {
        mockMvc.perform(post("/api/repository/document/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"directoryId\":2,\"pageNum\":1,\"pageSize\":10}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("POST /api/repository/document/search - 空搜索条件返回全部")
    void searchDocument_emptySearch() throws Exception {
        mockMvc.perform(post("/api/repository/document/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").isNumber());
    }

    // ==================== 边界测试 ====================

    @Test
    @DisplayName("POST /api/repository/directory/create - 创建同名目录（允许同名不同父目录）")
    void createDirectory_sameNameDifferentParent() throws Exception {
        // 在不同的父目录下创建同名目录
        mockMvc.perform(post("/api/repository/directory/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"repoType\":1,\"parentId\":1,\"name\":\"子目录测试\",\"sort\":0,\"status\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("POST /api/repository/document/search - 分页边界（pageNum=0 返回 400）")
    void searchDocument_zeroPageNum_returns400() throws Exception {
        // pageNum=0 触发 @Valid 校验失败（通常要求 >= 1）
        mockMvc.perform(post("/api/repository/document/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pageNum\":0,\"pageSize\":10}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/repository/directory/tree - 不存在的库类型返回空数组")
    void getDirectoryTree_emptyRepoType() throws Exception {
        mockMvc.perform(get("/api/repository/directory/tree")
                        .param("repoType", "99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }
}
