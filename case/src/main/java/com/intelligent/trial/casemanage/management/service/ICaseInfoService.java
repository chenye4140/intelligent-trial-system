package com.intelligent.trial.casemanage.management.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.intelligent.trial.casemanage.management.dto.CaseInfoDTO;
import com.intelligent.trial.casemanage.management.dto.CaseSearchDTO;
import com.intelligent.trial.casemanage.management.entity.CaseInfo;
import com.intelligent.trial.casemanage.management.entity.CaseParty;
import com.intelligent.trial.casemanage.management.entity.CaseViolationFact;
import com.intelligent.trial.casemanage.management.vo.CaseInfoVO;

import java.util.List;

/**
 * 案件管理服务接口
 */
public interface ICaseInfoService extends IService<CaseInfo> {

    /**
     * 分页查询案件列表
     *
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @param search   查询条件
     * @return 分页案件VO
     */
    Page<CaseInfoVO> pageCase(Integer pageNum, Integer pageSize, CaseSearchDTO search);

    /**
     * 根据ID查询案件详情
     *
     * @param id 案件ID
     * @return 案件VO
     */
    CaseInfoVO getCaseDetail(Long id);

    /**
     * 新增案件
     *
     * @param dto 案件信息
     */
    void addCase(CaseInfoDTO dto);

    /**
     * 更新案件
     *
     * @param dto 案件信息
     */
    void updateCase(CaseInfoDTO dto);

    /**
     * 删除案件
     *
     * @param id 案件ID
     */
    void deleteCase(Long id);

    /**
     * 修改案件状态
     *
     * @param id     案件ID
     * @param status 状态
     */
    void changeStatus(Long id, Integer status);

    /**
     * 获取案件当事人列表
     *
     * @param caseId 案件ID
     * @return 当事人列表
     */
    List<CaseParty> getParties(Long caseId);

    /**
     * 添加当事人
     *
     * @param party 当事人信息
     */
    void addParty(CaseParty party);

    /**
     * 删除当事人
     *
     * @param partyId 当事人ID
     */
    void deleteParty(Long partyId);

    /**
     * 获取案件违纪事实列表
     *
     * @param caseId 案件ID
     * @return 违纪事实列表
     */
    List<CaseViolationFact> getViolationFacts(Long caseId);

    /**
     * 添加违纪事实
     *
     * @param fact 违纪事实信息
     */
    void addViolationFact(CaseViolationFact fact);

    /**
     * 更新违纪事实
     *
     * @param fact 违纪事实信息
     */
    void updateViolationFact(CaseViolationFact fact);

    /**
     * 删除违纪事实
     *
     * @param factId 违纪事实ID
     */
    void deleteViolationFact(Long factId);
}
