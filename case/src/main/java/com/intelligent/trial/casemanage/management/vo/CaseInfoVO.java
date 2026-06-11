package com.intelligent.trial.casemanage.management.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 案件信息VO
 */
@Data
@Schema(description = "案件信息")
public class CaseInfoVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "案件ID")
        private Long id;

    /** 案件编号 */
    private String caseCode;

    /** 案件名称 */
    @Schema(description = "案件名称")
        private String caseName;

    /** 案件类型: 1=违纪, 2=违法, 3=职务犯罪 */
    @Schema(description = "案件类型")
        private Integer caseType;

    /** 案件来源 */
    @Schema(description = "案件来源")
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
    @Schema(description = "状态")
        private Integer status;

    /** 立案日期 */
    @Schema(description = "立案日期")
        private Date filingDate;

    /** 结案日期 */
    @Schema(description = "结案日期")
        private Date closeDate;

    /** 简要案情 */
    private String briefDescription;

    /** 承办部门ID */
    private Long handlingDeptId;

    /** 承办人ID */
    private Long handlingUserId;

    /** 承办部门名称 */
    private String handlingDeptName;

    /** 承办人姓名 */
    private String handlingUserName;

    /** 密级名称 */
    private String classificationLevelName;

    @Schema(description = "创建时间")
        private Date createTime;

    @Schema(description = "更新时间")
        private Date updateTime;
}
