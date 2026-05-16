package com.intelligent.trial.casemanage.management.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.intelligent.trial.casemanage.management.entity.CaseParty;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 案件当事人 Mapper 接口
 */
@Mapper
public interface CasePartyMapper extends BaseMapper<CaseParty> {

    /**
     * 根据案件ID查询当事人列表
     *
     * @param caseId 案件ID
     * @return 当事人列表
     */
    List<CaseParty> selectByCaseId(@Param("caseId") Long caseId);
}
