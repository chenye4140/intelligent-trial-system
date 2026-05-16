package com.intelligent.trial.report.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.intelligent.trial.common.dto.PageRequest;
import com.intelligent.trial.common.exception.BusinessException;
import com.intelligent.trial.report.client.DeepSeekClient;
import com.intelligent.trial.report.entity.ReportRecord;
import com.intelligent.trial.report.entity.ReportTemplate;
import com.intelligent.trial.report.mapper.ReportRecordMapper;
import com.intelligent.trial.report.mapper.ReportTemplateMapper;
import com.intelligent.trial.report.service.IReportService;
import com.intelligent.trial.report.vo.ReportRecordVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文书生成服务实现类
 * 核心功能：基于 DeepSeek AI 模型自动生成纪检监察文书
 */
@Service
public class ReportServiceImpl implements IReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportServiceImpl.class);

    @Autowired
    private ReportTemplateMapper reportTemplateMapper;

    @Autowired
    private ReportRecordMapper reportRecordMapper;

    @Autowired
    private DeepSeekClient deepSeekClient;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 状态常量
    private static final int STATUS_GENERATING = 0;
    private static final int STATUS_COMPLETED = 1;
    private static final int STATUS_FAILED = 2;

    // 模板类型常量
    private static final int TEMPLATE_TYPE_SHENLI = 1;    // 审理报告
    private static final int TEMPLATE_TYPE_CHUFEN = 2;    // 处分决定
    private static final int TEMPLATE_TYPE_TANHUA = 3;    // 谈话笔录
    private static final int TEMPLATE_TYPE_CHUHE = 4;     // 初核报告

    @Override
    @Async("reportGenerateExecutor")
    public String generateReport(Long caseId, Long templateId, String customPrompt) {
        log.info("开始生成文书: caseId={}, templateId={}", caseId, templateId);

        // 1. 查询案件信息
        Map<String, Object> caseInfo = queryCaseInfo(caseId);
        if (caseInfo == null) {
            throw new BusinessException("案件不存在: caseId=" + caseId);
        }

        String caseCode = (String) caseInfo.get("case_code");

        // 2. 获取或选择模板
        Long finalTemplateId = templateId;
        if (finalTemplateId == null) {
            Integer caseType = caseInfo.get("case_type") != null
                    ? ((Number) caseInfo.get("case_type")).intValue() : null;
            finalTemplateId = selectBestTemplate(caseType);
            log.info("AI自动选择模板: templateId={}", finalTemplateId);
        }

        ReportTemplate template = reportTemplateMapper.selectById(finalTemplateId);
        if (template == null) {
            throw new BusinessException("文书模板不存在: templateId=" + finalTemplateId);
        }
        if (template.getStatus() != 1) {
            throw new BusinessException("文书模板已禁用: " + template.getTemplateName());
        }

        // 3. 创建文书记录（状态：生成中）
        ReportRecord record = new ReportRecord();
        record.setCaseId(caseId);
        record.setCaseCode(caseCode);
        record.setTemplateId(finalTemplateId);
        record.setTemplateCode(template.getTemplateCode());
        record.setReportTitle(buildReportTitle(template, caseInfo));
        record.setStatus(STATUS_GENERATING);
        record.setGeneratedBy(0L); // TODO: 从上下文获取当前用户ID
        record.setCreateTime(new Date());
        record.setUpdateTime(new Date());
        reportRecordMapper.insert(record);

        Long recordId = record.getId();
        log.info("文书记录已创建: recordId={}", recordId);

        try {
            // 4. 构建 DeepSeek 提示词
            String systemPrompt = buildSystemPrompt(template);
            String userPrompt = buildUserPrompt(template, caseInfo, customPrompt);

            // 5. 调用 DeepSeek API 生成文书内容
            String generatedContent = deepSeekClient.generateContent(systemPrompt, userPrompt);

            // 6. 更新文书记录（状态：已完成）
            ReportRecord updateRecord = new ReportRecord();
            updateRecord.setId(recordId);
            updateRecord.setReportContent(generatedContent);
            updateRecord.setStatus(STATUS_COMPLETED);
            updateRecord.setUpdateTime(new Date());
            reportRecordMapper.updateById(updateRecord);

            log.info("文书生成成功: recordId={}, 内容长度={}", recordId,
                    generatedContent != null ? generatedContent.length() : 0);
        } catch (Exception e) {
            log.error("文书生成失败: recordId={}", recordId, e);
            // 更新状态为失败
            ReportRecord updateRecord = new ReportRecord();
            updateRecord.setId(recordId);
            updateRecord.setStatus(STATUS_FAILED);
            updateRecord.setErrorMessage(e.getMessage());
            updateRecord.setUpdateTime(new Date());
            reportRecordMapper.updateById(updateRecord);
        }

        return String.valueOf(recordId);
    }

    /**
     * 同步版本：直接调用生成并等待完成（用于测试）
     */
    public String generateReportSync(Long caseId, Long templateId, String customPrompt) {
        String recordId = generateReport(caseId, templateId, customPrompt);
        // 由于是 @Async，这里需要等待。
        // 实际使用中应通过 status 接口轮询
        return recordId;
    }

    @Override
    public ReportRecord getReportRecord(Long id) {
        return reportRecordMapper.selectById(id);
    }

    @Override
    public Page<ReportRecordVO> listReports(Long caseId, PageRequest pageRequest) {
        Page<ReportRecord> page = new Page<>(pageRequest.getPageNum(), pageRequest.getPageSize());

        LambdaQueryWrapper<ReportRecord> wrapper = new LambdaQueryWrapper<>();
        if (caseId != null) {
            wrapper.eq(ReportRecord::getCaseId, caseId);
        }
        wrapper.orderByDesc(ReportRecord::getCreateTime);

        Page<ReportRecord> resultPage = reportRecordMapper.selectPage(page, wrapper);

        // 转换为 VO 并填充附加信息
        Page<ReportRecordVO> voPage = new Page<>(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal());
        List<ReportRecordVO> voList = new ArrayList<>();

        for (ReportRecord record : resultPage.getRecords()) {
            ReportRecordVO vo = new ReportRecordVO();
            BeanUtils.copyProperties(record, vo);

            // 填充案件名称
            if (record.getCaseId() != null) {
                Map<String, Object> caseInfo = queryCaseInfo(record.getCaseId());
                if (caseInfo != null) {
                    vo.setCaseName((String) caseInfo.get("case_name"));
                }
            }

            // 填充模板名称
            if (record.getTemplateId() != null) {
                ReportTemplate template = reportTemplateMapper.selectById(record.getTemplateId());
                if (template != null) {
                    vo.setTemplateName(template.getTemplateName());
                }
            }

            voList.add(vo);
        }

        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public List<ReportTemplate> listTemplates() {
        LambdaQueryWrapper<ReportTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReportTemplate::getStatus, 1);
        wrapper.orderByAsc(ReportTemplate::getTemplateType);
        return reportTemplateMapper.selectList(wrapper);
    }

    @Override
    public Long selectBestTemplate(Integer caseType) {
        if (caseType == null) {
            // 默认选择审理报告
            return getDefaultTemplateId(TEMPLATE_TYPE_SHENLI);
        }

        // 根据案件类型智能匹配模板
        switch (caseType) {
            case 1: // 违纪案件
                // 违纪案件主要使用审理报告、处分决定
                Long shenliId = getDefaultTemplateId(TEMPLATE_TYPE_SHENLI);
                if (shenliId != null) return shenliId;
                return getDefaultTemplateId(TEMPLATE_TYPE_CHUFEN);
            case 2: // 违法案件
                // 违法案件可能需要初核报告 + 审理报告
                Long chuheId = getDefaultTemplateId(TEMPLATE_TYPE_CHUHE);
                if (chuheId != null) return chuheId;
                return getDefaultTemplateId(TEMPLATE_TYPE_SHENLI);
            case 3: // 职务犯罪
                // 职务犯罪需要完整的审理报告
                Long shenliId3 = getDefaultTemplateId(TEMPLATE_TYPE_SHENLI);
                if (shenliId3 != null) return shenliId3;
                return getDefaultTemplateId(TEMPLATE_TYPE_CHUHE);
            default:
                return getDefaultTemplateId(TEMPLATE_TYPE_SHENLI);
        }
    }

    // ========================= 私有辅助方法 =========================

    /**
     * 查询案件信息（跨模块查询，使用 JdbcTemplate）
     */
    private Map<String, Object> queryCaseInfo(Long caseId) {
        try {
            String sql = "SELECT id, case_code, case_name, case_type, case_source, " +
                    "respondent_name, respondent_dept, respondent_position, " +
                    "status, filing_date, close_date, brief_description " +
                    "FROM case_info WHERE id = ?";
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, caseId);
            return results.isEmpty() ? null : results.get(0);
        } catch (Exception e) {
            log.error("查询案件信息失败: caseId={}", caseId, e);
            return null;
        }
    }

    /**
     * 构建系统提示词
     */
    private String buildSystemPrompt(ReportTemplate template) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是纪检监察系统的专业文书生成助手，具备丰富的党纪法规知识和公文写作经验。\n\n");
        sb.append("你的任务是：根据提供的案件事实信息，按照给定的文书模板结构，生成规范、严谨的纪检监察文书。\n\n");
        sb.append("要求：\n");
        sb.append("1. 文书语言必须正式、规范、严谨，符合纪检监察公文标准\n");
        sb.append("2. 事实描述要客观、准确，引用法规要精准\n");
        sb.append("3. 结构完整，层次分明，逻辑清晰\n");
        sb.append("4. 处分建议要与违纪违法行为的性质、情节相匹配\n");
        sb.append("5. 使用中文输出，不要使用 markdown 格式\n\n");
        sb.append("文书类型：").append(template.getTemplateName()).append("\n");

        if (template.getContent() != null && !template.getContent().isEmpty()) {
            sb.append("\n模板结构参考：\n").append(template.getContent());
        }

        return sb.toString();
    }

    /**
     * 构建用户提示词
     */
    private String buildUserPrompt(ReportTemplate template, Map<String, Object> caseInfo, String customPrompt) {
        StringBuilder sb = new StringBuilder();
        sb.append("## 案件基本信息\n\n");
        sb.append("- 案件编号：").append(getStr(caseInfo, "case_code")).append("\n");
        sb.append("- 案件名称：").append(getStr(caseInfo, "case_name")).append("\n");
        sb.append("- 案件类型：").append(getCaseTypeDesc(caseInfo)).append("\n");
        sb.append("- 案件来源：").append(getStr(caseInfo, "case_source")).append("\n");
        sb.append("- 立案日期：").append(getDateStr(caseInfo, "filing_date")).append("\n");
        sb.append("- 结案日期：").append(getDateStr(caseInfo, "close_date")).append("\n");
        sb.append("- 当前状态：").append(getCaseStatusDesc(caseInfo)).append("\n\n");

        sb.append("## 被调查人信息\n\n");
        sb.append("- 姓名：").append(getStr(caseInfo, "respondent_name")).append("\n");
        sb.append("- 单位：").append(getStr(caseInfo, "respondent_dept")).append("\n");
        sb.append("- 职务：").append(getStr(caseInfo, "respondent_position")).append("\n\n");

        sb.append("## 简要案情\n\n");
        sb.append(getStr(caseInfo, "brief_description")).append("\n\n");

        sb.append("## 生成要求\n\n");
        sb.append("请根据以上案件信息，生成一份完整的《").append(template.getTemplateName()).append("》。\n");
        sb.append("文书应当结构完整、内容详实、表述规范。\n\n");

        if (customPrompt != null && !customPrompt.isEmpty()) {
            sb.append("## 附加要求\n\n");
            sb.append(customPrompt).append("\n\n");
        }

        sb.append("请开始生成文书内容：\n");

        return sb.toString();
    }

    /**
     * 构建文书标题
     */
    private String buildReportTitle(ReportTemplate template, Map<String, Object> caseInfo) {
        String caseName = getStr(caseInfo, "case_name");
        String respondentName = getStr(caseInfo, "respondent_name");
        String templateName = template.getTemplateName();

        if (caseName != null && !caseName.isEmpty()) {
            return caseName + " - " + templateName;
        } else if (respondentName != null && !respondentName.isEmpty()) {
            return "关于" + respondentName + "同志的" + templateName;
        } else {
            return templateName;
        }
    }

    /**
     * 获取指定类型的默认模板ID
     */
    private Long getDefaultTemplateId(int templateType) {
        LambdaQueryWrapper<ReportTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReportTemplate::getTemplateType, templateType);
        wrapper.eq(ReportTemplate::getStatus, 1);
        wrapper.last("LIMIT 1");
        ReportTemplate template = reportTemplateMapper.selectOne(wrapper);
        return template != null ? template.getId() : null;
    }

    // ========================= 辅助方法 =========================

    private String getStr(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : "未填写";
    }

    private String getDateStr(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Date) {
            return cn.hutool.core.date.DateUtil.formatDate((Date) value);
        }
        return value != null ? value.toString() : "未填写";
    }

    private String getCaseTypeDesc(Map<String, Object> caseInfo) {
        Object typeObj = caseInfo.get("case_type");
        if (typeObj == null) return "未填写";
        int type = ((Number) typeObj).intValue();
        switch (type) {
            case 1: return "违纪案件";
            case 2: return "违法案件";
            case 3: return "职务犯罪案件";
            default: return "未知类型(" + type + ")";
        }
    }

    private String getCaseStatusDesc(Map<String, Object> caseInfo) {
        Object statusObj = caseInfo.get("status");
        if (statusObj == null) return "未填写";
        int status = ((Number) statusObj).intValue();
        switch (status) {
            case 0: return "草稿";
            case 1: return "审理中";
            case 2: return "已完结";
            case 3: return "已归档";
            default: return "未知状态(" + status + ")";
        }
    }
}
