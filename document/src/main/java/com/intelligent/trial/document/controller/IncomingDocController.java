package com.intelligent.trial.document.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.intelligent.trial.common.annotation.RequireLog;
import com.intelligent.trial.common.dto.PageResult;
import com.intelligent.trial.common.dto.R;
import com.intelligent.trial.document.entity.IncomingDoc;
import com.intelligent.trial.document.service.IIncomingDocService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import javax.validation.Valid;

/**
 * 来文登记控制器
 *
 * @author intelligent-trial
 */
@RestController
@RequestMapping("/api/incoming-doc")
public class IncomingDocController {

    @Autowired
    private IIncomingDocService incomingDocService;

    /**
     * 分页查询来文列表
     */
    @GetMapping("/page")
    public R<PageResult<IncomingDoc>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String fromUnit,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        Page<IncomingDoc> page = incomingDocService.pageIncomingDoc(pageNum, pageSize,
                title, fromUnit, status, startDate, endDate);
        return R.ok(PageResult.of(page.getTotal(), pageNum, pageSize, page.getRecords()));
    }

    /**
     * 来文详情
     */
    @GetMapping("/{id}")
    public R<IncomingDoc> detail(@PathVariable Long id) {
        IncomingDoc doc = incomingDocService.getById(id);
        if (doc == null) {
            return R.fail("来文不存在");
        }
        return R.ok(doc);
    }

    /**
     * 新增来文登记
     */
    @PostMapping
    @RequireLog(module = "来文登记", action = "新增")
    public R<Void> add(@Valid @RequestBody IncomingDoc incomingDoc) {
        incomingDocService.addIncomingDoc(incomingDoc);
        return R.ok();
    }

    /**
     * 更新来文登记
     */
    @PutMapping
    @RequireLog(module = "来文登记", action = "编辑")
    public R<Void> update(@Valid @RequestBody IncomingDoc incomingDoc) {
        incomingDocService.updateIncomingDoc(incomingDoc);
        return R.ok();
    }

    /**
     * 删除来文登记
     */
    @DeleteMapping("/{id}")
    @RequireLog(module = "来文登记", action = "删除")
    public R<Void> delete(@PathVariable Long id) {
        incomingDocService.deleteIncomingDoc(id);
        return R.ok();
    }

    /**
     * 变更来文状态
     */
    @PutMapping("/status/{id}")
    @RequireLog(module = "来文登记", action = "状态变更")
    public R<Void> changeStatus(@PathVariable Long id, @RequestParam Integer status) {
        incomingDocService.changeStatus(id, status);
        return R.ok();
    }
}
