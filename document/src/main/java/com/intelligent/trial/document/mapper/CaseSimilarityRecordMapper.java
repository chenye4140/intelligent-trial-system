package com.intelligent.trial.document.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.intelligent.trial.document.entity.CaseSimilarityRecord;

import java.util.List;

/**
 * 类案相似度记录 Mapper
 *
 * @author intelligent-trial
 */
public interface CaseSimilarityRecordMapper extends BaseMapper<CaseSimilarityRecord> {

    /**
     * 插入或更新相似度记录（ON DUPLICATE KEY UPDATE）
     * 当 source_case_id + similar_case_id 已存在时，更新各维度相似度得分
     *
     * @param record 相似度记录
     * @return 受影响行数
     */
    int insertOrUpdate(CaseSimilarityRecord record);

    /**
     * 批量插入或更新相似度记录
     *
     * @param records 相似度记录列表
     * @return 受影响行数
     */
    int batchInsertOrUpdate(List<CaseSimilarityRecord> records);
}
