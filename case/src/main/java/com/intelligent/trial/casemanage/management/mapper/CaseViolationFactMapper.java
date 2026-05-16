package com.intelligent.trial.casemanage.management.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.intelligent.trial.casemanage.management.entity.CaseViolationFact;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 案件违纪事实 Mapper 接口
 */
@Mapper
public interface CaseViolationFactMapper extends BaseMapper<CaseViolationFact> {

    /**
     * 根据案件ID查询违纪事实列表（按排序字段排序）
     *
     * @param caseId 案件ID
     * @return 违纪事实列表
     */
    List<CaseViolationFact> selectByCaseIdOrderBySort(@Param("caseId") Long caseId);
}
