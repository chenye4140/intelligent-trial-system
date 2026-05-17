package com.intelligent.trial.punishment.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.intelligent.trial.common.annotation.RequireLog;
import com.intelligent.trial.common.dto.R;
import com.intelligent.trial.punishment.dto.PunishmentExecutionDTO;
import com.intelligent.trial.punishment.dto.PunishmentSearchDTO;
import com.intelligent.trial.punishment.entity.PunishmentMaterial;
import com.intelligent.trial.punishment.service.IPunishmentExecutionService;
import com.intelligent.trial.punishment.vo.PunishmentExecutionVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 处分执行 Controller
 */
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
    @GetMapping("/page")
    public R<Page<PunishmentExecutionVO>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            PunishmentSearchDTO searchDTO) {
        Page<PunishmentExecutionVO> page = executionService.pageQuery(pageNum, pageSize, searchDTO);
        return R.ok(page);
    }

    /**
     * 获取详情（含关联材料）
     */
    @GetMapping("/{id}")
    public R<PunishmentExecutionVO> getDetail(@PathVariable Long id) {
        PunishmentExecutionVO vo = executionService.getDetail(id);
        return R.ok(vo);
    }

    /**
     * 创建处分执行记录
     */
    @RequireLog(module="处分执行", action="新增")
    @PostMapping
    public R<PunishmentExecutionVO> create(@RequestBody PunishmentExecutionDTO dto) {
        PunishmentExecutionVO vo = executionService.getDetail(executionService.create(dto).getId());
        return R.ok(vo);
    }

    /**
     * 更新处分执行记录
     */
    @RequireLog(module="处分执行", action="编辑")
    @PutMapping
    public R<Void> update(@RequestBody PunishmentExecutionDTO dto) {
        executionService.update(dto);
        return R.ok();
    }

    /**
     * 删除处分执行记录
     */
    @RequireLog(module="处分执行", action="删除")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        executionService.delete(id);
        return R.ok();
    }

    /**
     * 变更状态
     */
    @RequireLog(module="处分执行", action="状态变更")
    @PutMapping("/{id}/status")
    public R<Void> changeStatus(@PathVariable Long id, @RequestParam Integer status) {
        executionService.changeStatus(id, status);
        return R.ok();
    }

    /**
     * 按案件ID查询
     */
    @GetMapping("/case/{caseId}")
    public R<List<PunishmentExecutionVO>> getByCaseId(@PathVariable String caseId) {
        return R.ok(executionService.getByCaseId(caseId));
    }

    /**
     * 查询逾期记录
     */
    @GetMapping("/overdue")
    public R<List<PunishmentExecutionVO>> getOverdue() {
        return R.ok(executionService.getOverdueExecutions());
    }

    /**
     * 统计各状态数量
     */
    @GetMapping("/statistics")
    public R<List<Map<String, Object>>> statistics() {
        return R.ok(executionService.countByStatus());
    }

    // ==================== 材料管理 ====================

    /**
     * 上传材料
     */
    @RequireLog(module="处分执行", action="上传材料")
    @PostMapping("/material")
    public R<PunishmentMaterial> uploadMaterial(
            @RequestParam Long executionId,
            @RequestParam String materialType,
            @RequestParam String filePath) {
        PunishmentMaterial material = executionService.uploadMaterial(executionId, materialType, filePath, null);
        return R.ok(material);
    }

    /**
     * 获取材料列表
     */
    @GetMapping("/material/{executionId}")
    public R<List<PunishmentMaterial>> getMaterials(@PathVariable Long executionId) {
        return R.ok(executionService.getMaterials(executionId));
    }

    /**
     * 删除材料
     */
    @RequireLog(module="处分执行", action="删除材料")
    @DeleteMapping("/material/{materialId}")
    public R<Void> deleteMaterial(@PathVariable Long materialId) {
        executionService.deleteMaterial(materialId);
        return R.ok();
    }
}
