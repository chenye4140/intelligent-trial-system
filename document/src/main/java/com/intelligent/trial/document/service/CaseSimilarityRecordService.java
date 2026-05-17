package com.intelligent.trial.document.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.intelligent.trial.document.entity.CaseSimilarityRecord;

import java.util.List;

/**
 * 类案相似度记录服务接口
 *
 * @author intelligent-trial
 */
public interface CaseSimilarityRecordService extends IService<CaseSimilarityRecord> {

    /**
     * 保存或更新单条相似度记录
     * 如果 source_case_id + similar_case_id 已存在，则更新分数
     *
     * @param record 相似度记录
     * @return 是否成功
     */
    boolean saveOrUpdateRecord(CaseSimilarityRecord record);

    /**
     * 批量保存或更新相似度记录
     *
     * @param records 相似度记录列表
     * @return 是否成功
     */
    boolean batchSaveOrUpdate(List<CaseSimilarityRecord> records);

    /**
     * 根据源案件ID查询相似度记录
     *
     * @param sourceCaseId 源案件ID
     * @return 相似度记录列表
     */
    List<CaseSimilarityRecord> listBySourceCaseId(String sourceCaseId);
}
