package com.intelligent.trial.document.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.intelligent.trial.document.entity.DocParseTask;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文档解析任务 Mapper 接口
 *
 * @author intelligent-trial
 */
@Mapper
public interface DocParseTaskMapper extends BaseMapper<DocParseTask> {
}
