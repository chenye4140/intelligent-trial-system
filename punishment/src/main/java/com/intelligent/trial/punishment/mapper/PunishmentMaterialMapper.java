package com.intelligent.trial.punishment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.intelligent.trial.punishment.entity.PunishmentMaterial;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 处分材料 Mapper
 */
@Mapper
public interface PunishmentMaterialMapper extends BaseMapper<PunishmentMaterial> {

    /**
     * 按执行ID查询材料列表
     */
    List<PunishmentMaterial> selectByExecutionId(@Param("executionId") Long executionId);
}
