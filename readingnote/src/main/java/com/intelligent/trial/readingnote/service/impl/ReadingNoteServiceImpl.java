package com.intelligent.trial.readingnote.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.intelligent.trial.common.exception.BusinessException;
import com.intelligent.trial.common.exception.ErrorCode;
import com.intelligent.trial.common.util.UserContext;
import com.intelligent.trial.readingnote.entity.ReadingNote;
import com.intelligent.trial.readingnote.mapper.ReadingNoteMapper;
import com.intelligent.trial.readingnote.service.IReadingNoteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReadingNoteServiceImpl implements IReadingNoteService {
    private static final Logger log = LoggerFactory.getLogger(ReadingNoteServiceImpl.class);
    private final ReadingNoteMapper readingNoteMapper;

    public ReadingNoteServiceImpl(ReadingNoteMapper readingNoteMapper) {
        this.readingNoteMapper = readingNoteMapper;
    }

    @Override
    public Page<ReadingNote> pageQuery(int pageNum, int pageSize, String caseId, Integer noteType) {
        Page<ReadingNote> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ReadingNote> wrapper = new LambdaQueryWrapper<>();
        if (caseId != null && !caseId.isEmpty()) wrapper.eq(ReadingNote::getCaseId, caseId);
        if (noteType != null) wrapper.eq(ReadingNote::getNoteType, noteType);
        wrapper.orderByDesc(ReadingNote::getCreateTime);
        return readingNoteMapper.selectPage(page, wrapper);
    }

    @Override
    public ReadingNote getDetail(Long id) {
        ReadingNote note = readingNoteMapper.selectById(id);
        if (note == null) throw new BusinessException(ErrorCode.NOTE_NOT_FOUND);
        return note;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReadingNote create(ReadingNote note) {
        if (note.getCaseId() == null || note.getCaseId().isEmpty()) throw new BusinessException(ErrorCode.NOTE_CASE_ID_EMPTY);
        if (note.getTitle() == null || note.getTitle().isEmpty()) throw new BusinessException(ErrorCode.NOTE_TITLE_EMPTY);
        note.setUserId(UserContext.getUserId());
        note.setIsShared(note.getIsShared() != null ? note.getIsShared() : 0);
        note.setNoteType(note.getNoteType() != null ? note.getNoteType() : 1);
        readingNoteMapper.insert(note);
        log.info("创建阅卷笔记: id={}, caseId={}, title={}", note.getId(), note.getCaseId(), note.getTitle());
        return note;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ReadingNote note) {
        if (note.getId() == null) throw new BusinessException(ErrorCode.NOTE_ID_EMPTY);
        ReadingNote existing = readingNoteMapper.selectById(note.getId());
        if (existing == null) throw new BusinessException(ErrorCode.NOTE_NOT_FOUND);
        readingNoteMapper.updateById(note);
        log.info("更新阅卷笔记: id={}", note.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        ReadingNote existing = readingNoteMapper.selectById(id);
        if (existing == null) throw new BusinessException(ErrorCode.NOTE_NOT_FOUND);
        readingNoteMapper.deleteById(id);
        log.info("删除阅卷笔记: id={}", id);
    }

    @Override
    public List<ReadingNote> getByCaseId(String caseId) {
        return readingNoteMapper.selectByCaseId(caseId);
    }

    @Override
    public List<ReadingNote> getSharedNotes(String caseId) {
        return readingNoteMapper.selectSharedNotes(caseId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleShared(Long id, Integer isShared) {
        ReadingNote note = readingNoteMapper.selectById(id);
        if (note == null) throw new BusinessException(ErrorCode.NOTE_NOT_FOUND);
        note.setIsShared(isShared);
        readingNoteMapper.updateById(note);
    }
}
