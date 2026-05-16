package com.intelligent.trial.casemanage.management.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 案件信息DTO
 */
@Data
public class CaseInfoDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 案件ID (更新时必填) */
    private Long id;

    /** 案件名称 */
    private String caseName;

    /** 案件类型: 1=违纪, 2=违法, 3=职务犯罪 */
    private Integer caseType;

    /** 案件来源 */
    private String caseSource;

    /** 被调查人姓名 */
    private String respondentName;

    /** 被调查人单位 */
    private String respondentDept;

    /** 被调查人职务 */
    private String respondentPosition;

    /** 密级ID */
    private Long classificationLevelId;

    /** 状态: 0=草稿, 1=审理中, 2=已完结, 3=已归档 */
    private Integer status;

    /** 立案日期 */
    private Date filingDate;

    /** 结案日期 */
    private Date closeDate;

    /** 简要案情 */
    private String briefDescription;

    /** 承办部门ID */
    private Long handlingDeptId;

    /** 承办人ID */
    private Long handlingUserId;
}
