package com.intelligent.trial.promotion.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.intelligent.trial.promotion.dto.CasePromotionGenerateDTO;
import com.intelligent.trial.promotion.dto.CasePromotionSearchDTO;
import com.intelligent.trial.promotion.entity.CasePromotion;
import com.intelligent.trial.promotion.vo.CasePromotionVO;

/**
 * 以案促改服务接口
 */
public interface ICasePromotionService {

    /**
     * 根据ID查询促改记录
     *
     * @param id 主键ID
     * @return 促改记录
     */
    CasePromotion getById(Long id);

    /**
     * 分页搜索促改记录
     *
     * @param dto 搜索条件
     * @return 分页结果
     */
    IPage<CasePromotionVO> search(CasePromotionSearchDTO dto);

    /**
     * 根据案件ID查询促改记录
     *
     * @param caseId 案件ID
     * @return 促改记录
     */
    CasePromotion getByCaseId(String caseId);

    /**
     * 创建促改记录
     *
     * @param entity 促改记录
     * @return 创建的记录
     */
    CasePromotion create(CasePromotion entity);

    /**
     * 更新促改记录
     *
     * @param entity 促改记录
     * @return 是否成功
     */
    boolean update(CasePromotion entity);

    /**
     * 根据ID删除促改记录
     *
     * @param id 主键ID
     * @return 是否成功
     */
    boolean deleteById(Long id);

    /**
     * 更新促改记录状态
     *
     * @param id     主键ID
     * @param status 新状态
     * @return 是否成功
     */
    boolean updateStatus(Long id, Integer status);

    /**
     * AI生成促改分析（异步）
     *
     * @param dto 生成请求参数
     * @return 任务ID
     */
    String generateAnalysis(CasePromotionGenerateDTO dto);

    /**
     * 获取生成任务状态
     *
     * @param taskId 任务ID
     * @return 任务状态: running/completed/failed
     */
    String getAnalysisStatus(String taskId);
}
