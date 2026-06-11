package com.intelligent.trial.punishment.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.intelligent.trial.common.annotation.RequireLog;
import com.intelligent.trial.common.annotation.RequirePermission;
import com.intelligent.trial.common.dto.R;
import com.intelligent.trial.punishment.dto.PunishmentExecutionDTO;
import com.intelligent.trial.punishment.dto.PunishmentSearchDTO;
import com.intelligent.trial.punishment.entity.PunishmentMaterial;
import com.intelligent.trial.punishment.service.IPunishmentExecutionService;
import com.intelligent.trial.punishment.vo.PunishmentExecutionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import javax.validation.Valid;

/**
 * 处分执行 Controller
 */
@Tag(name = "处分执行", description = "处分执行记录管理、状态流转、逾期监控、材料管理等接口")
@RestController
@RequestMapping("/api/punishment")
public class PunishmentController {

    private final IPunishmentExecutionService executionService;

    public PunishmentController(IPunishmentExecutionService executionService) {
        this.executionService = executionService;
    }

    // ==================== 处分执行 CRUD ====================

    /**
     * 分页查询处分执行记录
     */
    @Operation(summary = "分页查询处分执行记录", description = "支持多条件搜索，返回分页结果")
    @GetMapping("/page")
    public R<Page<PunishmentExecutionVO>> page(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") int pageSize,
            PunishmentSearchDTO searchDTO) {
        Page<PunishmentExecutionVO> page = executionService.pageQuery(pageNum, pageSize, searchDTO);
        return R.ok(page);
    }

    /**
     * 获取详情（含关联材料）
     */
    @Operation(summary = "获取处分执行详情", description = "根据ID获取详情，包含关联材料信息")
    @GetMapping("/{id}")
    public R<PunishmentExecutionVO> getDetail(@Parameter(description = "处分执行记录ID") @PathVariable Long id) {
        PunishmentExecutionVO vo = executionService.getDetail(id);
        return R.ok(vo);
    }

    /**
     * 创建处分执行记录
     */
    @RequirePermission("punishment:execution:add")
    @Operation(summary = "创建处分执行记录", description = "新增一条处分执行记录")
    @RequireLog(module="处分执行", action="新增")
    @PostMapping
    public R<PunishmentExecutionVO> create(@Valid @RequestBody PunishmentExecutionDTO dto) {
        PunishmentExecutionVO vo = executionService.getDetail(executionService.create(dto).getId());
        return R.ok(vo);
    }

    /**
     * 更新处分执行记录
     */
    @RequirePermission("punishment:execution:edit")
    @Operation(summary = "更新处分执行记录", description = "修改已有的处分执行记录")
    @RequireLog(module="处分执行", action="编辑")
    @PutMapping
    public R<Void> update(@Valid @RequestBody PunishmentExecutionDTO dto) {
        executionService.update(dto);
        return R.ok();
    }

    /**
     * 删除处分执行记录
     */
    @RequirePermission("punishment:execution:remove")
    @Operation(summary = "删除处分执行记录", description = "根据ID删除处分执行记录")
    @RequireLog(module="处分执行", action="删除")
    @DeleteMapping("/{id}")
    public R<Void> delete(@Parameter(description = "处分执行记录ID") @PathVariable Long id) {
        executionService.delete(id);
        return R.ok();
    }

    /**
     * 变更状态
     */
    @RequirePermission("punishment:execution:status")
    @Operation(summary = "变更处分状态", description = "修改处分执行记录的状态")
    @RequireLog(module="处分执行", action="状态变更")
    @PutMapping("/{id}/status")
    public R<Void> changeStatus(
            @Parameter(description = "处分执行记录ID") @PathVariable Long id,
            @Parameter(description = "目标状态值") @RequestParam Integer status) {
        executionService.changeStatus(id, status);
        return R.ok();
    }

    /**
     * 按案件ID查询
     */
    @Operation(summary = "按案件ID查询处分记录", description = "获取指定案件关联的所有处分执行记录")
    @GetMapping("/case/{caseId}")
    public R<List<PunishmentExecutionVO>> getByCaseId(@Parameter(description = "案件ID") @PathVariable String caseId) {
        return R.ok(executionService.getByCaseId(caseId));
    }

    /**
     * 查询逾期记录
     */
    @Operation(summary = "查询逾期处分记录", description = "获取所有已超过执行期限的处分记录")
    @GetMapping("/overdue")
    public R<List<PunishmentExecutionVO>> getOverdue() {
        return R.ok(executionService.getOverdueExecutions());
    }

    /**
     * 统计各状态数量
     */
    @Operation(summary = "统计处分状态数量", description = "返回各状态下处分执行记录的数量统计")
    @GetMapping("/statistics")
    public R<List<Map<String, Object>>> statistics() {
        return R.ok(executionService.countByStatus());
    }

    // ==================== 材料管理 ====================

    /**
     * 上传材料
     */
    @RequirePermission("punishment:material:upload")
    @Operation(summary = "上传处分材料", description = "为指定处分执行记录上传关联材料")
    @RequireLog(module="处分执行", action="上传材料")
    @PostMapping("/material")
    public R<PunishmentMaterial> uploadMaterial(
            @Parameter(description = "处分执行记录ID") @RequestParam Long executionId,
            @Parameter(description = "材料类型") @RequestParam String materialType,
            @Parameter(description = "文件路径") @RequestParam String filePath) {
        PunishmentMaterial material = executionService.uploadMaterial(executionId, materialType, filePath, null);
        return R.ok(material);
    }

    /**
     * 获取材料列表
     */
    @Operation(summary = "获取材料列表", description = "获取指定处分执行记录关联的所有材料")
    @GetMapping("/material/{executionId}")
    public R<List<PunishmentMaterial>> getMaterials(@Parameter(description = "处分执行记录ID") @PathVariable Long executionId) {
        return R.ok(executionService.getMaterials(executionId));
    }

    /**
     * 删除材料
     */
    @RequirePermission("punishment:material:remove")
    @Operation(summary = "删除处分材料", description = "根据材料ID删除关联材料")
    @RequireLog(module="处分执行", action="删除材料")
    @DeleteMapping("/material/{materialId}")
    public R<Void> deleteMaterial(@Parameter(description = "材料ID") @PathVariable Long materialId) {
        executionService.deleteMaterial(materialId);
        return R.ok();
    }
}
