package com.intelligent.trial.promotion.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.intelligent.trial.common.annotation.RequireLog;
import com.intelligent.trial.common.annotation.RequirePermission;
import com.intelligent.trial.common.dto.PageResult;
import com.intelligent.trial.common.dto.R;
import com.intelligent.trial.common.exception.BusinessException;
import com.intelligent.trial.common.util.UserContext;
import com.intelligent.trial.promotion.dto.CasePromotionGenerateDTO;
import com.intelligent.trial.promotion.dto.CasePromotionSearchDTO;
import com.intelligent.trial.promotion.entity.CasePromotion;
import com.intelligent.trial.promotion.service.ICasePromotionService;
import com.intelligent.trial.promotion.vo.CasePromotionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 以案促改 Controller
 * 提供促改分析生成、查询、状态管理等接口
 */
@Tag(name = "以案促改", description = "AI 以案促改分析生成、查询、状态管理等接口")
@RestController
@RequestMapping("/api/promotion")
public class CasePromotionController {

    private static final Logger log = LoggerFactory.getLogger(CasePromotionController.class);

    @Autowired
    private ICasePromotionService casePromotionService;

    /**
     * AI 生成促改分析（异步）
     *
     * @param dto 生成请求（案件ID、分析类型、模板ID）
     * @return 任务ID
     */
    @RequirePermission("promotion:analysis:generate")
    @Operation(summary = "AI生成促改分析", description = "异步生成以案促改分析，返回任务ID用于查询进度")
    @RequireLog(module="以案促改", action="生成分析")
    @PostMapping("/generate")
    public R<Map<String, Object>> generateAnalysis(
            @Parameter(description = "生成请求（案件ID、分析类型、模板ID）") @Validated @RequestBody CasePromotionGenerateDTO dto) {
        log.info("收到促改分析生成请求: caseId={}, analysisType={}", dto.getCaseId(), dto.getAnalysisType());

        // 自动填充当前用户ID（如果前端未传递）
        if (dto.getUserId() == null) {
            Long currentUserId = UserContext.getUserId();
            if (currentUserId != null) {
                dto.setUserId(currentUserId);
            }
        }

        try {
            String taskId = casePromotionService.generateAnalysis(dto);
            Map<String, Object> result = new HashMap<>();
            result.put("taskId", taskId);
            result.put("message", "促改分析任务已启动，请通过任务ID查询进度");
            return R.ok(result);
        } catch (BusinessException e) {
            log.error("促改分析生成失败: {}", e.getMessage());
            return R.fail(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("促改分析异常", e);
            return R.fail("促改分析失败: " + e.getMessage());
        }
    }

    /**
     * 查询促改分析任务状态
     *
     * @param taskId 任务ID
     * @return 任务状态信息
     */
    @Operation(summary = "查询分析任务状态", description = "根据任务ID查询AI促改分析任务的当前状态")
    @GetMapping("/status/{taskId}")
    public R<Map<String, Object>> getAnalysisStatus(@Parameter(description = "任务ID") @PathVariable String taskId) {
        String status = casePromotionService.getAnalysisStatus(taskId);
        if (status == null) {
            return R.fail(404, "任务不存在: " + taskId);
        }

        Map<String, Object> statusInfo = new HashMap<>();
        statusInfo.put("taskId", taskId);
        statusInfo.put("status", status);
        statusInfo.put("statusDesc", getStatusDesc(status));

        return R.ok(statusInfo);
    }

    /**
     * 根据ID查询促改记录
     *
     * @param id 记录ID
     * @return 促改记录
     */
    @Operation(summary = "获取促改记录详情", description = "根据ID查询单条促改记录")
    @GetMapping("/{id}")
    public R<CasePromotion> getById(@Parameter(description = "记录ID") @PathVariable Long id) {
        CasePromotion record = casePromotionService.getById(id);
        if (record == null) {
            return R.fail(404, "促改记录不存在");
        }
        return R.ok(record);
    }

    /**
     * 根据案件ID查询促改记录
     *
     * @param caseId 案件ID
     * @return 促改记录
     */
    @Operation(summary = "按案件查询促改记录", description = "根据案件ID查询该案件的促改记录")
    @GetMapping("/case/{caseId}")
    public R<CasePromotion> getByCaseId(@Parameter(description = "案件ID") @PathVariable String caseId) {
        CasePromotion record = casePromotionService.getByCaseId(caseId);
        if (record == null) {
            return R.fail(404, "该案件暂无促改记录");
        }
        return R.ok(record);
    }

    /**
     * 分页搜索促改记录
     *
     * @param dto 搜索条件
     * @return 分页结果
     */
    @Operation(summary = "分页搜索促改记录", description = "支持多条件搜索和分页查询促改记录")
    @GetMapping("/list")
    public R<PageResult<CasePromotionVO>> list(@Parameter(description = "搜索条件") CasePromotionSearchDTO dto) {
        IPage<CasePromotionVO> resultPage = casePromotionService.search(dto);

        return R.ok(PageResult.of(
                resultPage.getTotal(),
                (int) resultPage.getCurrent(),
                (int) resultPage.getSize(),
                resultPage.getRecords()
        ));
    }

    /**
     * 创建促改记录（手动编辑）
     *
     * @param entity 促改记录
     * @return 创建的记录
     */
    @RequirePermission("promotion:record:add")
    @Operation(summary = "创建促改记录", description = "手动编辑创建促改记录")
    @RequireLog(module="以案促改", action="新增")
    @PostMapping
    public R<CasePromotion> create(@Parameter(description = "促改记录实体") @Valid @RequestBody CasePromotion entity) {
        try {
            CasePromotion created = casePromotionService.create(entity);
            return R.ok("促改记录创建成功", created);
        } catch (Exception e) {
            log.error("创建促改记录异常", e);
            return R.fail("创建失败: " + e.getMessage());
        }
    }

    /**
     * 更新促改记录
     *
     * @param entity 促改记录
     * @return 是否成功
     */
    @RequirePermission("promotion:record:edit")
    @Operation(summary = "更新促改记录", description = "修改已存在的促改记录")
    @RequireLog(module="以案促改", action="编辑")
    @PutMapping
    public R<String> update(@Parameter(description = "促改记录实体") @Valid @RequestBody CasePromotion entity) {
        boolean success = casePromotionService.update(entity);
        if (success) {
            return R.ok("促改记录更新成功");
        }
        return R.fail("更新失败");
    }

    /**
     * 更新促改记录状态
     *
     * @param id     记录ID
     * @param status 新状态（0=草稿, 1=待审核, 2=已通过, 3=已驳回）
     * @return 是否成功
     */
    @Operation(summary = "更新促改记录状态", description = "修改促改记录的审核状态（0=草稿, 1=待审核, 2=已通过, 3=已驳回）")
    @RequireLog(module="以案促改", action="状态变更")
    @PutMapping("/status/{id}")
    public R<String> updateStatus(
            @Parameter(description = "记录ID") @PathVariable Long id,
            @Parameter(description = "新状态（0=草稿, 1=待审核, 2=已通过, 3=已驳回）") @RequestParam Integer status) {
        boolean success = casePromotionService.updateStatus(id, status);
        if (success) {
            return R.ok("状态更新成功");
        }
        return R.fail("状态更新失败");
    }

    /**
     * 删除促改记录
     *
     * @param id 记录ID
     * @return 是否成功
     */
    @RequirePermission("promotion:record:remove")
    @Operation(summary = "删除促改记录", description = "根据ID删除促改记录")
    @RequireLog(module="以案促改", action="删除")
    @DeleteMapping("/{id}")
    public R<String> delete(@Parameter(description = "记录ID") @PathVariable Long id) {
        boolean success = casePromotionService.deleteById(id);
        if (success) {
            return R.ok("促改记录删除成功");
        }
        return R.fail("删除失败");
    }

    /**
     * 状态描述
     */
    private String getStatusDesc(String status) {
        if (status == null) return "未知";
        if ("running".equals(status)) return "生成中";
        if ("completed".equals(status)) return "已完成";
        if ("failed".equals(status)) return "生成失败";
        return "未知(" + status + ")";
    }
}
