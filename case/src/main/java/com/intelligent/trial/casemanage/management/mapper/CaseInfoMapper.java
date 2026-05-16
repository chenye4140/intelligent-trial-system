package com.intelligent.trial.casemanage.management.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.intelligent.trial.casemanage.management.entity.CaseInfo;
import com.intelligent.trial.casemanage.management.vo.CaseInfoVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 案件信息 Mapper 接口
 */
@Mapper
public interface CaseInfoMapper extends BaseMapper<CaseInfo> {

    /**
     * 分页查询案件列表
     *
     * @param page           分页对象
     * @param caseCode       案件编号
     * @param caseName       案件名称
     * @param caseType       案件类型
     * @param status         状态
     * @param respondentName 被调查人姓名
     * @param handlingDeptId 承办部门ID
     * @param startDate      开始日期
     * @param endDate        结束日期
     * @return 分页案件VO列表
     */
    Page<CaseInfoVO> selectCasePage(Page<CaseInfoVO> page,
                                    @Param("caseCode") String caseCode,
                                    @Param("caseName") String caseName,
                                    @Param("caseType") Integer caseType,
                                    @Param("status") Integer status,
                                    @Param("respondentName") String respondentName,
                                    @Param("handlingDeptId") Long handlingDeptId,
                                    @Param("startDate") java.util.Date startDate,
                                    @Param("endDate") java.util.Date endDate);

    /**
     * 根据ID查询案件详情
     *
     * @param id 案件ID
     * @return 案件VO
     */
    CaseInfoVO selectCaseDetailById(@Param("id") Long id);
}
