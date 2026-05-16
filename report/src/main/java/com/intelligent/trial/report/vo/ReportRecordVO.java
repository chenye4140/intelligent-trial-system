package com.intelligent.trial.report.vo;

import com.intelligent.trial.report.entity.ReportRecord;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文书记录响应 VO
 * 在 ReportRecord 基础上附加案件名称和模板名称
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ReportRecordVO extends ReportRecord {

    private static final long serialVersionUID = 1L;

    /**
     * 案件名称（来自 case_info 表）
     */
    private String caseName;

    /**
     * 模板名称（来自 report_template 表）
     */
    private String templateName;
}
