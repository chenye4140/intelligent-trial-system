package com.intelligent.trial.document.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.intelligent.trial.common.annotation.RequireLog;
import com.intelligent.trial.common.annotation.RequirePermission;
import com.intelligent.trial.common.dto.PageResult;
import com.intelligent.trial.common.dto.R;
import com.intelligent.trial.document.entity.IncomingDoc;
import com.intelligent.trial.document.service.IIncomingDocService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import javax.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 来文登记控制器
 *
 * @author intelligent-trial
 */
@Tag(name = "来文登记", description = "来文CRUD、状态管理等来文登记接口")
@RestController
@RequestMapping("/api/incoming-doc")
public class IncomingDocController {

    @Autowired
    private IIncomingDocService incomingDocService;

    /**
     * 分页查询来文列表
     */
    @Operation(summary = "分页查询来文", description = "多条件分页查询来文列表")
    @GetMapping("/page")
    public R<PageResult<IncomingDoc>> page(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "标题") @RequestParam(required = false) String title,
            @Parameter(description = "来文单位") @RequestParam(required = false) String fromUnit,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "开始日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        Page<IncomingDoc> page = incomingDocService.pageIncomingDoc(pageNum, pageSize,
                title, fromUnit, status, startDate, endDate);
        return R.ok(PageResult.of(page.getTotal(), pageNum, pageSize, page.getRecords()));
    }

    /**
     * 来文详情
     */
    @Operation(summary = "来文详情", description = "根据ID获取来文详细信息")
    @GetMapping("/{id}")
    public R<IncomingDoc> detail(@Parameter(description = "来文ID") @PathVariable Long id) {
        IncomingDoc doc = incomingDocService.getById(id);
        if (doc == null) {
            throw new com.intelligent.trial.common.exception.BusinessException(
                com.intelligent.trial.common.exception.ErrorCode.INCOMING_DOC_NOT_FOUND.getCode(), "来文不存在");
        }
        return R.ok(doc);
    }

    /**
     * 新增来文登记
     */
    @RequirePermission("document:incoming:add")
    @Operation(summary = "新增来文登记", description = "创建新的来文登记")
    @PostMapping
    @RequireLog(module = "来文登记", action = "新增")
    public R<IncomingDoc> add(@Valid @RequestBody IncomingDoc incomingDoc) {
        incomingDocService.addIncomingDoc(incomingDoc);
        return R.ok(incomingDoc);
    }

    /**
     * 更新来文登记
     */
    @RequirePermission("document:incoming:edit")
    @Operation(summary = "更新来文登记", description = "编辑来文登记信息")
    @PutMapping
    @RequireLog(module = "来文登记", action = "编辑")
    public R<Void> update(@Valid @RequestBody IncomingDoc incomingDoc) {
        incomingDocService.updateIncomingDoc(incomingDoc);
        return R.ok();
    }

    /**
     * 删除来文登记
     */
    @RequirePermission("document:incoming:remove")
    @Operation(summary = "删除来文登记", description = "根据ID删除来文")
    @DeleteMapping("/{id}")
    @RequireLog(module = "来文登记", action = "删除")
    public R<Void> delete(@Parameter(description = "来文ID") @PathVariable Long id) {
        incomingDocService.deleteIncomingDoc(id);
        return R.ok();
    }

    /**
     * 变更来文状态
     */
    @RequirePermission("document:incoming:edit")
    @Operation(summary = "变更来文状态", description = "修改来文的处理状态")
    @PutMapping("/status/{id}")
    @RequireLog(module = "来文登记", action = "状态变更")
    public R<Void> changeStatus(@Parameter(description = "来文ID") @PathVariable Long id, @Parameter(description = "状态值") @RequestParam Integer status) {
        incomingDocService.changeStatus(id, status);
        return R.ok();
    }
}
