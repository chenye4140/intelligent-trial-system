package com.intelligent.trial.report.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.intelligent.trial.common.dto.PageRequest;
import com.intelligent.trial.report.entity.ReportRecord;
import com.intelligent.trial.report.entity.ReportTemplate;
import com.intelligent.trial.report.vo.ReportRecordVO;

import java.util.List;

/**
 * 文书生成服务接口
 */
public interface IReportService {

    /**
     * 生成文书（异步）
     *
     * @param caseId       案件ID
     * @param templateId   模板ID（为空时AI自动选择）
     * @param customPrompt 自定义提示词
     * @return 文书记录ID
     */
    String generateReport(Long caseId, Long templateId, String customPrompt);

    /**
     * 获取文书记录
     *
     * @param id 记录ID
     * @return 文书记录
     */
    ReportRecord getReportRecord(Long id);

    /**
     * 分页查询文书记录
     *
     * @param caseId      案件ID（可选，用于过滤）
     * @param pageRequest 分页参数
     * @return 分页结果
     */
    Page<ReportRecordVO> listReports(Long caseId, PageRequest pageRequest);

    /**
     * 获取所有可用的文书模板
     *
     * @return 模板列表
     */
    List<ReportTemplate> listTemplates();

    /**
     * 根据案件类型AI自动选择最合适的模板
     *
     * @param caseType 案件类型：1=违纪, 2=违法, 3=职务犯罪
     * @return 最佳模板ID
     */
    Long selectBestTemplate(Integer caseType);
}
