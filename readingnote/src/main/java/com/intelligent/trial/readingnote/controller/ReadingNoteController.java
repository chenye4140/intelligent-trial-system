package com.intelligent.trial.readingnote.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.intelligent.trial.common.annotation.RequireLog;
import com.intelligent.trial.common.dto.R;
import com.intelligent.trial.readingnote.entity.ReadingNote;
import com.intelligent.trial.readingnote.service.IReadingNoteService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reading-note")
public class ReadingNoteController {
    private final IReadingNoteService readingNoteService;

    public ReadingNoteController(IReadingNoteService readingNoteService) {
        this.readingNoteService = readingNoteService;
    }

    @GetMapping("/page")
    public R<Page<ReadingNote>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String caseId,
            @RequestParam(required = false) Integer noteType) {
        return R.ok(readingNoteService.pageQuery(pageNum, pageSize, caseId, noteType));
    }

    @GetMapping("/{id}")
    public R<ReadingNote> getDetail(@PathVariable Long id) {
        return R.ok(readingNoteService.getDetail(id));
    }

    @RequireLog(module="阅卷笔记", action="新增")
    @PostMapping
    public R<ReadingNote> create(@RequestBody ReadingNote note) {
        return R.ok(readingNoteService.create(note));
    }

    @RequireLog(module="阅卷笔记", action="编辑")
    @PutMapping
    public R<Void> update(@RequestBody ReadingNote note) {
        readingNoteService.update(note);
        return R.ok();
    }

    @RequireLog(module="阅卷笔记", action="删除")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        readingNoteService.delete(id);
        return R.ok();
    }

    @GetMapping("/case/{caseId}")
    public R<List<ReadingNote>> getByCaseId(@PathVariable String caseId) {
        return R.ok(readingNoteService.getByCaseId(caseId));
    }

    @GetMapping("/shared/{caseId}")
    public R<List<ReadingNote>> getSharedNotes(@PathVariable String caseId) {
        return R.ok(readingNoteService.getSharedNotes(caseId));
    }

    @RequireLog(module="阅卷笔记", action="共享设置")
    @PutMapping("/{id}/share")
    public R<Void> toggleShared(@PathVariable Long id, @RequestParam Integer isShared) {
        readingNoteService.toggleShared(id, isShared);
        return R.ok();
    }
}
