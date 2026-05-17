package com.intelligent.trial.document.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.intelligent.trial.document.entity.IncomingDoc;

import java.util.Date;

/**
 * 来文登记服务接口
 *
 * @author intelligent-trial
 */
public interface IIncomingDocService extends IService<IncomingDoc> {

    /**
     * 分页查询来文列表
     *
     * @param pageNum   页码
     * @param pageSize  每页条数
     * @param title     来文标题
     * @param fromUnit  来文单位
     * @param status    状态
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 分页来文列表
     */
    Page<IncomingDoc> pageIncomingDoc(Integer pageNum, Integer pageSize,
                                      String title, String fromUnit,
                                      Integer status, Date startDate, Date endDate);

    /**
     * 新增来文登记
     *
     * @param incomingDoc 来文信息
     */
    void addIncomingDoc(IncomingDoc incomingDoc);

    /**
     * 更新来文登记
     *
     * @param incomingDoc 来文信息
     */
    void updateIncomingDoc(IncomingDoc incomingDoc);

    /**
     * 删除来文登记
     *
     * @param id 来文ID
     */
    void deleteIncomingDoc(Long id);

    /**
     * 变更来文状态
     *
     * @param id     来文ID
     * @param status 新状态
     */
    void changeStatus(Long id, Integer status);
}
