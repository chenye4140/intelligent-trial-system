package com.intelligent.trial.promotion.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.intelligent.trial.promotion.entity.CasePromotion;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 以案促改 Mapper 接口
 */
public interface CasePromotionMapper extends BaseMapper<CasePromotion> {

    /**
     * 根据案件ID查询促改记录
     *
     * @param caseId 案件ID
     * @return 促改记录
     */
    CasePromotion selectByCaseId(@Param("caseId") String caseId);

    /**
     * 根据状态查询促改记录列表
     *
     * @param status 状态
     * @return 促改记录列表
     */
    List<CasePromotion> selectByStatus(@Param("status") Integer status);
}
