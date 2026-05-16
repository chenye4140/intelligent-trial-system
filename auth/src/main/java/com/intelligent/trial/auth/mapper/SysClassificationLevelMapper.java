package com.intelligent.trial.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.intelligent.trial.auth.entity.SysClassificationLevel;
import com.intelligent.trial.auth.vo.ClassificationLevelVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 密级 Mapper 接口
 */
@Mapper
public interface SysClassificationLevelMapper extends BaseMapper<SysClassificationLevel> {

    /**
     * 查询所有正常状态的密级
     *
     * @return 密级VO列表
     */
    List<ClassificationLevelVO> selectAllActiveLevels();

    /**
     * 查询用户可访问的密级列表（根据用户的最高密级）
     *
     * @param maxLevelSort 最高密级排序值
     * @return 密级VO列表
     */
    List<ClassificationLevelVO> selectAccessibleLevels(@Param("maxLevelSort") Integer maxLevelSort);
}
