package com.intelligent.trial.document.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.intelligent.trial.document.entity.IncomingDoc;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;

/**
 * 来文登记 Mapper 接口
 *
 * @author intelligent-trial
 */
@Mapper
public interface IncomingDocMapper extends BaseMapper<IncomingDoc> {

    /**
     * 分页查询来文列表
     *
     * @param page      分页对象
     * @param title     来文标题
     * @param fromUnit  来文单位
     * @param status    状态
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 分页来文列表
     */
    Page<IncomingDoc> selectIncomingDocPage(Page<IncomingDoc> page,
                                            @Param("title") String title,
                                            @Param("fromUnit") String fromUnit,
                                            @Param("status") Integer status,
                                            @Param("startDate") Date startDate,
                                            @Param("endDate") Date endDate);
}
