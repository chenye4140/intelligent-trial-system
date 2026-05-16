package com.intelligent.trial.punishment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.intelligent.trial.punishment.entity.PunishmentExecution;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 处分执行 Mapper
 */
@Mapper
public interface PunishmentExecutionMapper extends BaseMapper<PunishmentExecution> {

    /**
     * 查询逾期未处理的处分执行记录
     */
    List<PunishmentExecution> selectOverdueExecutions();

    /**
     * 按案件ID查询处分执行记录
     */
    List<PunishmentExecution> selectByCaseId(@Param("caseId") String caseId);

    /**
     * 统计各状态数量
     */
    List<Map<String, Object>> countByStatus();
}
