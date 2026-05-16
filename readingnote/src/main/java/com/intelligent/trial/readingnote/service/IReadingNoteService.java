package com.intelligent.trial.readingnote.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.intelligent.trial.readingnote.entity.ReadingNote;
import java.util.List;

public interface IReadingNoteService {
    Page<ReadingNote> pageQuery(int pageNum, int pageSize, String caseId, Integer noteType);
    ReadingNote getDetail(Long id);
    ReadingNote create(ReadingNote note);
    void update(ReadingNote note);
    void delete(Long id);
    List<ReadingNote> getByCaseId(String caseId);
    List<ReadingNote> getSharedNotes(String caseId);
    void toggleShared(Long id, Integer isShared);
}
