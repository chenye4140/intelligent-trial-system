package com.intelligent.trial.document.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.intelligent.trial.document.entity.DocParagraphVector;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 段落向量 Mapper
 *
 * @author intelligent-trial
 */
public interface DocParagraphVectorMapper extends BaseMapper<DocParagraphVector> {

    /**
     * 根据向量余弦相似度搜索相似段落
     * MySQL 中使用 JSON_TABLE 展开向量数组后计算余弦相似度
     *
     * @param vectorJson 查询向量的 JSON 字符串
     * @param limit      返回结果数量上限
     * @param category   分类过滤（可选）
     * @return 按相似度降序排列的段落列表（附带相似度得分）
     */
    List<DocParagraphVector> searchSimilar(@Param("vectorJson") String vectorJson,
                                           @Param("limit") int limit,
                                           @Param("category") String category);
}
