package com.intelligent.trial.casemanage.management.controller;

import com.intelligent.trial.common.annotation.RequireLog;
import com.intelligent.trial.common.dto.R;
import com.intelligent.trial.common.exception.BusinessException;
import com.intelligent.trial.repository.entity.Document;
import com.intelligent.trial.repository.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 案件-文档绑定控制器
 * 提供文档与案件的关联/解绑接口
 */
@Tag(name = "案件文档", description = "案件与文档关联管理等接口")
@RestController
@RequestMapping("/api/case/document")
public class CaseDocumentController {

    @Autowired
    private DocumentService documentService;

    /**
     * 绑定文档到案件
     * PUT /api/case/document/bind/{caseId}/{documentId}
     *
     * @param caseId     案件ID
     * @param documentId 文档ID
     * @return 操作结果
     */
    @Operation(summary = "绑定文档到案件", description = "将指定文档关联到案件")
    @RequireLog(module = "案件管理", action = "绑定文档", description = "绑定文档")
    @PutMapping("/bind/{caseId}/{documentId}")
    public R<Void> bindDocument(@Parameter(description = "案件ID") @PathVariable Long caseId, @Parameter(description = "文档ID") @PathVariable Long documentId) {
        Document document = documentService.getById(documentId);
        if (document == null) {
            return R.fail("文档不存在");
        }
        document.setCaseId(caseId);
        documentService.update(document);
        return R.ok();
    }

    /**
     * 解绑文档与案件的关联
     * PUT /api/case/document/unbind/{documentId}
     *
     * @param documentId 文档ID
     * @return 操作结果
     */
    @Operation(summary = "解绑文档", description = "解除文档与案件的关联")
    @RequireLog(module = "案件管理", action = "解绑文档", description = "解绑文档")
    @PutMapping("/unbind/{documentId}")
    public R<Void> unbindDocument(@Parameter(description = "文档ID") @PathVariable Long documentId) {
        Document document = documentService.getById(documentId);
        if (document == null) {
            return R.fail("文档不存在");
        }
        document.setCaseId(null);
        documentService.update(document);
        return R.ok();
    }

    /**
     * 获取案件关联的所有文档
     * GET /api/case/document/{caseId}
     *
     * @param caseId 案件ID
     * @return 文档列表
     */
    @Operation(summary = "获取案件文档", description = "获取案件关联的所有文档列表")
    @GetMapping("/{caseId}")
    public R<List<Document>> getCaseDocuments(@Parameter(description = "案件ID") @PathVariable Long caseId) {
        List<Document> documents = documentService.getByCaseId(caseId);
        return R.ok(documents);
    }
}
