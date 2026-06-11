package com.intelligent.trial.casemanage.management.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.intelligent.trial.casemanage.management.dto.CaseInfoDTO;
import com.intelligent.trial.casemanage.management.dto.CaseSearchDTO;
import com.intelligent.trial.casemanage.management.entity.CaseParty;
import com.intelligent.trial.casemanage.management.entity.CaseViolationFact;
import com.intelligent.trial.casemanage.management.service.ICaseInfoService;
import com.intelligent.trial.casemanage.management.vo.CaseInfoVO;
import com.intelligent.trial.common.annotation.RequireLog;
import com.intelligent.trial.common.annotation.RequirePermission;
import com.intelligent.trial.common.dto.PageResult;
import com.intelligent.trial.common.dto.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import javax.validation.Valid;

/**
 * 案件管理控制器
 */
@Tag(name = "案件管理", description = "案件CRUD、搜索分页、当事人/违纪事实管理等案件管理接口")
@RestController
@RequestMapping("/api/case")
public class CaseInfoController {

    @Autowired
    private ICaseInfoService caseInfoService;

    /**
     * 分页查询案件列表
     */
    @Operation(summary = "分页查询案件", description = "多条件分页查询案件列表")
    @PostMapping("/page")
    public R<PageResult<CaseInfoVO>> pageCase(@Valid @RequestBody CaseSearchDTO search) {
        Integer pageNum = search.getPageNum() != null ? search.getPageNum() : 1;
        Integer pageSize = search.getPageSize() != null ? search.getPageSize() : 10;
        Page<CaseInfoVO> page = caseInfoService.pageCase(pageNum, pageSize, search);
        return R.ok(PageResult.of(page.getTotal(), pageNum, pageSize, page.getRecords()));
    }

    /**
     * 案件详情
     */
    @Operation(summary = "案件详情", description = "根据ID获取案件完整信息")
    @GetMapping("/{id}")
    public R<CaseInfoVO> detail(@Parameter(description = "案件ID") @PathVariable Long id) {
        return R.ok(caseInfoService.getCaseDetail(id));
    }

    /**
     * 创建案件
     */
    @RequirePermission("case:info:add")
    @Operation(summary = "创建案件", description = "新增案件信息")
    @RequireLog(module = "案件管理", action = "新增", description = "新增案件")
    @PostMapping
    public R<Void> addCase(@Valid @RequestBody CaseInfoDTO dto) {
        caseInfoService.addCase(dto);
        return R.ok();
    }

    /**
     * 更新案件
     */
    @RequirePermission("case:info:edit")
    @Operation(summary = "更新案件", description = "编辑案件信息")
    @RequireLog(module = "案件管理", action = "编辑", description = "编辑案件")
    @PutMapping
    public R<Void> updateCase(@Valid @RequestBody CaseInfoDTO dto) {
        caseInfoService.updateCase(dto);
        return R.ok();
    }

    /**
     * 删除案件
     */
    @RequirePermission("case:info:remove")
    @Operation(summary = "删除案件", description = "根据ID删除案件")
    @RequireLog(module = "案件管理", action = "删除", description = "删除案件")
    @DeleteMapping("/{id}")
    public R<Void> deleteCase(@Parameter(description = "案件ID") @PathVariable Long id) {
        caseInfoService.deleteCase(id);
        return R.ok();
    }

    /**
     * 修改案件状态
     */
    @Operation(summary = "修改案件状态", description = "变更案件的处理状态")
    @RequireLog(module = "案件管理", action = "状态变更", description = "变更状态")
    @PutMapping("/status/{id}")
    public R<Void> changeStatus(@Parameter(description = "案件ID") @PathVariable Long id, @Parameter(description = "状态值") @RequestParam Integer status) {
        caseInfoService.changeStatus(id, status);
        return R.ok();
    }

    /**
     * 获取当事人列表
     */
    @Operation(summary = "获取当事人列表", description = "查询案件的所有当事人")
    @GetMapping("/{caseId}/parties")
    public R<List<CaseParty>> getParties(@Parameter(description = "案件ID") @PathVariable Long caseId) {
        return R.ok(caseInfoService.getParties(caseId));
    }

    /**
     * 添加当事人
     */
    @RequirePermission("case:party:add")
    @Operation(summary = "添加当事人", description = "为案件新增当事人")
    @RequireLog(module = "案件管理", action = "新增当事人", description = "新增当事人")
    @PostMapping("/party")
    public R<Void> addParty(@Valid @RequestBody CaseParty party) {
        caseInfoService.addParty(party);
        return R.ok();
    }

    /**
     * 删除当事人
     */
    @RequirePermission("case:party:remove")
    @Operation(summary = "删除当事人", description = "删除案件的当事人")
    @RequireLog(module = "案件管理", action = "删除当事人", description = "删除当事人")
    @DeleteMapping("/party/{id}")
    public R<Void> deleteParty(@Parameter(description = "当事人ID") @PathVariable Long id) {
        caseInfoService.deleteParty(id);
        return R.ok();
    }

    /**
     * 获取违纪事实列表
     */
    @Operation(summary = "获取违纪事实列表", description = "查询案件的违纪事实")
    @GetMapping("/{caseId}/violations")
    public R<List<CaseViolationFact>> getViolationFacts(@Parameter(description = "案件ID") @PathVariable Long caseId) {
        return R.ok(caseInfoService.getViolationFacts(caseId));
    }

    /**
     * 添加违纪事实
     */
    @RequirePermission("case:violation:add")
    @Operation(summary = "添加违纪事实", description = "为案件新增违纪事实")
    @RequireLog(module = "案件管理", action = "新增违纪事实", description = "新增违纪事实")
    @PostMapping("/violation")
    public R<Void> addViolationFact(@Valid @RequestBody CaseViolationFact fact) {
        caseInfoService.addViolationFact(fact);
        return R.ok();
    }

    /**
     * 更新违纪事实
     */
    @RequirePermission("case:violation:edit")
    @Operation(summary = "更新违纪事实", description = "编辑违纪事实信息")
    @RequireLog(module = "案件管理", action = "编辑违纪事实", description = "编辑违纪事实")
    @PutMapping("/violation")
    public R<Void> updateViolationFact(@Valid @RequestBody CaseViolationFact fact) {
        caseInfoService.updateViolationFact(fact);
        return R.ok();
    }

    /**
     * 删除违纪事实
     */
    @RequirePermission("case:violation:remove")
    @Operation(summary = "删除违纪事实", description = "删除案件的违纪事实")
    @RequireLog(module = "案件管理", action = "删除违纪事实", description = "删除违纪事实")
    @DeleteMapping("/violation/{id}")
    public R<Void> deleteViolationFact(@Parameter(description = "违纪事实ID") @PathVariable Long id) {
        caseInfoService.deleteViolationFact(id);
        return R.ok();
    }
}
