package com.intelligent.trial.casemanage.management.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.intelligent.trial.casemanage.management.dto.CaseInfoDTO;
import com.intelligent.trial.casemanage.management.dto.CaseSearchDTO;
import com.intelligent.trial.casemanage.management.entity.CaseParty;
import com.intelligent.trial.casemanage.management.entity.CaseViolationFact;
import com.intelligent.trial.casemanage.management.service.ICaseInfoService;
import com.intelligent.trial.casemanage.management.vo.CaseInfoVO;
import com.intelligent.trial.common.annotation.RequireLog;
import com.intelligent.trial.common.dto.PageResult;
import com.intelligent.trial.common.dto.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 案件管理控制器
 */
@RestController
@RequestMapping("/api/case")
public class CaseInfoController {

    @Autowired
    private ICaseInfoService caseInfoService;

    /**
     * 分页查询案件列表
     */
    @PostMapping("/page")
    public R<PageResult<CaseInfoVO>> pageCase(@RequestBody CaseSearchDTO search) {
        Integer pageNum = search.getPageNum() != null ? search.getPageNum() : 1;
        Integer pageSize = search.getPageSize() != null ? search.getPageSize() : 10;
        Page<CaseInfoVO> page = caseInfoService.pageCase(pageNum, pageSize, search);
        return R.ok(PageResult.of(page.getTotal(), pageNum, pageSize, page.getRecords()));
    }

    /**
     * 案件详情
     */
    @GetMapping("/{id}")
    public R<CaseInfoVO> detail(@PathVariable Long id) {
        return R.ok(caseInfoService.getCaseDetail(id));
    }

    /**
     * 创建案件
     */
    @RequireLog(module = "案件管理", action = "新增", description = "新增案件")
    @PostMapping
    public R<Void> addCase(@RequestBody CaseInfoDTO dto) {
        caseInfoService.addCase(dto);
        return R.ok();
    }

    /**
     * 更新案件
     */
    @RequireLog(module = "案件管理", action = "编辑", description = "编辑案件")
    @PutMapping
    public R<Void> updateCase(@RequestBody CaseInfoDTO dto) {
        caseInfoService.updateCase(dto);
        return R.ok();
    }

    /**
     * 删除案件
     */
    @RequireLog(module = "案件管理", action = "删除", description = "删除案件")
    @DeleteMapping("/{id}")
    public R<Void> deleteCase(@PathVariable Long id) {
        caseInfoService.deleteCase(id);
        return R.ok();
    }

    /**
     * 修改案件状态
     */
    @RequireLog(module = "案件管理", action = "状态变更", description = "变更状态")
    @PutMapping("/status/{id}")
    public R<Void> changeStatus(@PathVariable Long id, @RequestParam Integer status) {
        caseInfoService.changeStatus(id, status);
        return R.ok();
    }

    /**
     * 获取当事人列表
     */
    @GetMapping("/{caseId}/parties")
    public R<List<CaseParty>> getParties(@PathVariable Long caseId) {
        return R.ok(caseInfoService.getParties(caseId));
    }

    /**
     * 添加当事人
     */
    @RequireLog(module = "案件管理", action = "新增当事人", description = "新增当事人")
    @PostMapping("/party")
    public R<Void> addParty(@RequestBody CaseParty party) {
        caseInfoService.addParty(party);
        return R.ok();
    }

    /**
     * 删除当事人
     */
    @RequireLog(module = "案件管理", action = "删除当事人", description = "删除当事人")
    @DeleteMapping("/party/{id}")
    public R<Void> deleteParty(@PathVariable Long id) {
        caseInfoService.deleteParty(id);
        return R.ok();
    }

    /**
     * 获取违纪事实列表
     */
    @GetMapping("/{caseId}/violations")
    public R<List<CaseViolationFact>> getViolationFacts(@PathVariable Long caseId) {
        return R.ok(caseInfoService.getViolationFacts(caseId));
    }

    /**
     * 添加违纪事实
     */
    @RequireLog(module = "案件管理", action = "新增违纪事实", description = "新增违纪事实")
    @PostMapping("/violation")
    public R<Void> addViolationFact(@RequestBody CaseViolationFact fact) {
        caseInfoService.addViolationFact(fact);
        return R.ok();
    }

    /**
     * 更新违纪事实
     */
    @RequireLog(module = "案件管理", action = "编辑违纪事实", description = "编辑违纪事实")
    @PutMapping("/violation")
    public R<Void> updateViolationFact(@RequestBody CaseViolationFact fact) {
        caseInfoService.updateViolationFact(fact);
        return R.ok();
    }

    /**
     * 删除违纪事实
     */
    @RequireLog(module = "案件管理", action = "删除违纪事实", description = "删除违纪事实")
    @DeleteMapping("/violation/{id}")
    public R<Void> deleteViolationFact(@PathVariable Long id) {
        caseInfoService.deleteViolationFact(id);
        return R.ok();
    }
}
