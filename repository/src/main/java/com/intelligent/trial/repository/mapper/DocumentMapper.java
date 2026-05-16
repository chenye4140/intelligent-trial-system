package com.intelligent.trial.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.intelligent.trial.repository.entity.Document;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 文档 Mapper 接口
 */
@Mapper
public interface DocumentMapper extends BaseMapper<Document> {

    /**
     * 多条件搜索文档（分页）
     * 支持关键词模糊匹配标题、文号、发布单位
     *
     * @param page                 分页对象
     * @param repoType             库类型
     * @param keyword              搜索关键词
     * @param directoryId          目录ID
     * @param validityStatus       有效性状态
     * @param publishDateStart     发布日期起始
     * @param publishDateEnd       发布日期结束
     * @param classificationLevelId 定密级别ID
     * @param publishUnit          发布单位
     * @return 分页文档结果
     */
    IPage<Document> searchDocuments(Page<Document> page,
                                    @Param("repoType") Integer repoType,
                                    @Param("keyword") String keyword,
                                    @Param("directoryId") Long directoryId,
                                    @Param("validityStatus") String validityStatus,
                                    @Param("publishDateStart") java.util.Date publishDateStart,
                                    @Param("publishDateEnd") java.util.Date publishDateEnd,
                                    @Param("classificationLevelId") Long classificationLevelId,
                                    @Param("publishUnit") String publishUnit);
}
