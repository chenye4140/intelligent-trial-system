package com.intelligent.trial.document.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.intelligent.trial.document.entity.CaseSimilarityRecord;
import com.intelligent.trial.document.mapper.CaseSimilarityRecordMapper;
import com.intelligent.trial.document.service.CaseSimilarityRecordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 类案相似度记录服务实现类
 *
 * @author intelligent-trial
 */
@Service
public class CaseSimilarityRecordServiceImpl extends ServiceImpl<CaseSimilarityRecordMapper, CaseSimilarityRecord>
        implements CaseSimilarityRecordService {

    private static final Logger log = LoggerFactory.getLogger(CaseSimilarityRecordServiceImpl.class);

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveOrUpdateRecord(CaseSimilarityRecord record) {
        int rows = baseMapper.insertOrUpdate(record);
        log.info("保存相似度记录: source={}, similar={}, rows={}",
                record.getSourceCaseId(), record.getSimilarCaseId(), rows);
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchSaveOrUpdate(List<CaseSimilarityRecord> records) {
        if (records == null || records.isEmpty()) {
            return true;
        }
        int rows = baseMapper.batchInsertOrUpdate(records);
        log.info("批量保存相似度记录: count={}, rows={}", records.size(), rows);
        return rows > 0;
    }

    @Override
    public List<CaseSimilarityRecord> listBySourceCaseId(String sourceCaseId) {
        LambdaQueryWrapper<CaseSimilarityRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CaseSimilarityRecord::getSourceCaseId, sourceCaseId)
                .orderByDesc(CaseSimilarityRecord::getSimilarityScore);
        return list(wrapper);
    }
}
