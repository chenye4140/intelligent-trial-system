package com.intelligent.trial.casemanage.management.controller;

import com.intelligent.trial.common.annotation.RequireLog;
import com.intelligent.trial.common.dto.R;
import com.intelligent.trial.common.exception.BusinessException;
import com.intelligent.trial.repository.entity.Document;
import com.intelligent.trial.repository.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 案件-文档绑定控制器
 * 提供文档与案件的关联/解绑接口
 */
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
    @RequireLog(module = "案件管理", action = "绑定文档", description = "绑定文档")
    @PutMapping("/bind/{caseId}/{documentId}")
    public R<Void> bindDocument(@PathVariable Long caseId, @PathVariable Long documentId) {
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
    @RequireLog(module = "案件管理", action = "解绑文档", description = "解绑文档")
    @PutMapping("/unbind/{documentId}")
    public R<Void> unbindDocument(@PathVariable Long documentId) {
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
    @GetMapping("/{caseId}")
    public R<List<Document>> getCaseDocuments(@PathVariable Long caseId) {
        List<Document> documents = documentService.getByCaseId(caseId);
        return R.ok(documents);
    }
}
