package com.intelligent.trial.readingnote.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.intelligent.trial.readingnote.entity.ReadingNote;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ReadingNoteMapper extends BaseMapper<ReadingNote> {
    List<ReadingNote> selectByCaseId(@Param("caseId") String caseId);
    List<ReadingNote> selectSharedNotes(@Param("caseId") String caseId);
}
