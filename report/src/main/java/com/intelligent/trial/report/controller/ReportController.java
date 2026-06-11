package com.intelligent.trial.report.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.intelligent.trial.common.annotation.RequireLog;
import com.intelligent.trial.common.annotation.RequirePermission;
import com.intelligent.trial.common.dto.PageRequest;
import com.intelligent.trial.common.dto.R;
import com.intelligent.trial.common.exception.BusinessException;
import com.intelligent.trial.report.dto.ReportGenerateDTO;
import com.intelligent.trial.report.entity.ReportRecord;
import com.intelligent.trial.report.entity.ReportTemplate;
import com.intelligent.trial.report.service.IReportService;
import com.intelligent.trial.report.vo.ReportRecordVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文书生成 Controller
 * 提供文书生成、查询、模板列表等接口
 */
@Tag(name = "文书生成", description = "AI 文书生成、模板管理、生成状态查询等接口")
@RestController
@RequestMapping("/api/report")
public class ReportController {

    private static final Logger log = LoggerFactory.getLogger(ReportController.class);

    @Autowired
    private IReportService reportService;

    /**
     * 生成文书
     *
     * @param dto 生成请求（案件ID、模板ID、自定义提示词）
     * @return 文书记录ID
     */
    @RequirePermission("report:generate:execute")
    @Operation(summary = "生成文书", description = "基于 DeepSeek AI 异步生成文书，返回记录ID用于查询进度")
    @PostMapping("/generate")
    @RequireLog(module = "文书生成", action = "生成")
    public R<Long> generateReport(@Validated @RequestBody ReportGenerateDTO dto) {
        log.info("收到文书生成请求: caseId={}, templateId={}", dto.getCaseId(), dto.getTemplateId());

        try {
            String recordIdStr = reportService.generateReport(
                    dto.getCaseId(),
                    dto.getTemplateId(),
                    dto.getCustomPrompt()
            );
            Long recordId = Long.parseLong(recordIdStr);
            return R.ok("文书生成任务已启动，请通过状态接口查询进度", recordId);
        } catch (BusinessException e) {
            log.error("文书生成失败: {}", e.getMessage());
            return R.fail(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("文书生成异常", e);
            return R.fail("文书生成失败: " + e.getMessage());
        }
    }

    /**
     * 获取文书记录详情
     *
     * @param id 记录ID
     * @return 文书记录
     */
    @Operation(summary = "获取文书记录详情", description = "根据记录ID获取文书生成的详细信息")
    @GetMapping("/record/{id}")
    public R<ReportRecord> getReportRecord(@PathVariable Long id) {
        ReportRecord record = reportService.getReportRecord(id);
        if (record == null) {
            return R.fail(404, "文书记录不存在");
        }
        return R.ok(record);
    }

    /**
     * 分页查询文书记录列表
     *
     * @param caseId   案件ID（可选，用于过滤）
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    @Operation(summary = "分页查询文书记录列表", description = "支持按案件ID过滤，返回分页结果")
    @GetMapping("/list")
    public R<com.intelligent.trial.common.dto.PageResult<ReportRecordVO>> listReports(
            @Parameter(description = "案件ID（可选）") @RequestParam(required = false) Long caseId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Integer pageSize) {

        PageRequest pageRequest = new PageRequest();
        pageRequest.setPageNum(pageNum);
        pageRequest.setPageSize(pageSize);

        Page<ReportRecordVO> resultPage = reportService.listReports(caseId, pageRequest);

        return R.ok(com.intelligent.trial.common.dto.PageResult.of(
                resultPage.getTotal(),
                (int) resultPage.getCurrent(),
                (int) resultPage.getSize(),
                resultPage.getRecords()
        ));
    }

    /**
     * 获取可用的文书模板列表
     *
     * @return 模板列表
     */
    @Operation(summary = "获取文书模板列表", description = "返回所有可用的文书模板")
    @GetMapping("/templates")
    public R<List<ReportTemplate>> listTemplates() {
        List<ReportTemplate> templates = reportService.listTemplates();
        return R.ok(templates);
    }

    /**
     * 查询文书生成状态
     *
     * @param id 记录ID
     * @return 状态信息
     */
    @Operation(summary = "查询文书生成状态", description = "根据记录ID查询文书生成的进度和状态信息")
    @GetMapping("/status/{id}")
    public R<Map<String, Object>> getReportStatus(@Parameter(description = "文书记录ID") @PathVariable Long id) {
        ReportRecord record = reportService.getReportRecord(id);
        if (record == null) {
            return R.fail(404, "文书记录不存在");
        }

        Map<String, Object> statusInfo = new HashMap<>();
        statusInfo.put("id", record.getId());
        statusInfo.put("caseCode", record.getCaseCode());
        statusInfo.put("templateCode", record.getTemplateCode());
        statusInfo.put("reportTitle", record.getReportTitle());
        statusInfo.put("status", record.getStatus());
        statusInfo.put("statusDesc", getStatusDesc(record.getStatus()));
        statusInfo.put("errorMessage", record.getErrorMessage());
        statusInfo.put("createTime", record.getCreateTime());
        statusInfo.put("updateTime", record.getUpdateTime());

        // 已完成时附带内容摘要
        if (record.getStatus() != null && record.getStatus() == 1) {
            String content = record.getReportContent();
            if (content != null && content.length() > 200) {
                statusInfo.put("contentPreview", content.substring(0, 200) + "...");
            } else {
                statusInfo.put("contentPreview", content);
            }
            statusInfo.put("contentLength", content != null ? content.length() : 0);
        }

        return R.ok(statusInfo);
    }

    /**
     * 状态描述
     */
    private String getStatusDesc(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "生成中";
            case 1: return "已完成";
            case 2: return "生成失败";
            default: return "未知(" + status + ")";
        }
    }
}
