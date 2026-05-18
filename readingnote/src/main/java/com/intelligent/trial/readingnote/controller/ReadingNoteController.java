package com.intelligent.trial.readingnote.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.intelligent.trial.common.annotation.RequireLog;
import com.intelligent.trial.common.dto.R;
import com.intelligent.trial.readingnote.entity.ReadingNote;
import com.intelligent.trial.readingnote.service.IReadingNoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import javax.validation.Valid;

@Tag(name = "阅卷笔记", description = "阅卷笔记CRUD、共享协作、按案件查询等接口")
@RestController
@RequestMapping("/api/reading-note")
public class ReadingNoteController {
    private final IReadingNoteService readingNoteService;

    public ReadingNoteController(IReadingNoteService readingNoteService) {
        this.readingNoteService = readingNoteService;
    }

    @Operation(summary = "分页查询阅卷笔记", description = "支持按案件ID和笔记类型筛选")
    @GetMapping("/page")
    public R<Page<ReadingNote>> page(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int pageSize,
            @Parameter(description = "案件ID") @RequestParam(required = false) String caseId,
            @Parameter(description = "笔记类型") @RequestParam(required = false) Integer noteType) {
        return R.ok(readingNoteService.pageQuery(pageNum, pageSize, caseId, noteType));
    }

    @Operation(summary = "获取阅卷笔记详情", description = "根据ID查询单条阅卷笔记")
    @GetMapping("/{id}")
    public R<ReadingNote> getDetail(@Parameter(description = "笔记ID") @PathVariable Long id) {
        return R.ok(readingNoteService.getDetail(id));
    }

    @Operation(summary = "创建阅卷笔记", description = "新增一条阅卷笔记")
    @RequireLog(module="阅卷笔记", action="新增")
    @PostMapping
    public R<ReadingNote> create(@Valid @RequestBody ReadingNote note) {
        return R.ok(readingNoteService.create(note));
    }

    @Operation(summary = "更新阅卷笔记", description = "修改已存在的阅卷笔记")
    @RequireLog(module="阅卷笔记", action="编辑")
    @PutMapping
    public R<Void> update(@Valid @RequestBody ReadingNote note) {
        readingNoteService.update(note);
        return R.ok();
    }

    @Operation(summary = "删除阅卷笔记", description = "根据ID删除阅卷笔记")
    @RequireLog(module="阅卷笔记", action="删除")
    @DeleteMapping("/{id}")
    public R<Void> delete(@Parameter(description = "笔记ID") @PathVariable Long id) {
        readingNoteService.delete(id);
        return R.ok();
    }

    @Operation(summary = "按案件查询阅卷笔记", description = "查询指定案件下的所有阅卷笔记")
    @GetMapping("/case/{caseId}")
    public R<List<ReadingNote>> getByCaseId(@Parameter(description = "案件ID") @PathVariable String caseId) {
        return R.ok(readingNoteService.getByCaseId(caseId));
    }

    @Operation(summary = "查询共享阅卷笔记", description = "查询指定案件下已共享的阅卷笔记")
    @GetMapping("/shared/{caseId}")
    public R<List<ReadingNote>> getSharedNotes(@Parameter(description = "案件ID") @PathVariable String caseId) {
        return R.ok(readingNoteService.getSharedNotes(caseId));
    }

    @Operation(summary = "切换共享状态", description = "设置阅卷笔记的共享/取消共享状态")
    @RequireLog(module="阅卷笔记", action="共享设置")
    @PutMapping("/{id}/share")
    public R<Void> toggleShared(
            @Parameter(description = "笔记ID") @PathVariable Long id,
            @Parameter(description = "共享状态(0=取消共享, 1=共享)") @RequestParam Integer isShared) {
        readingNoteService.toggleShared(id, isShared);
        return R.ok();
    }
}
