package com.intelligent.trial.casemanage.management.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 案件查询DTO
 */
@Data
@Schema(description = "案件搜索请求")
public class CaseSearchDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 页码 */
    private Integer pageNum;

    /** 每页条数 */
    private Integer pageSize;

    /** 案件编号 */
    private String caseCode;

    /** 案件名称 */
    @Schema(description = "案件名称")
        private String caseName;

    /** 案件类型: 1=违纪, 2=违法, 3=职务犯罪 */
    @Schema(description = "案件类型")
        private Integer caseType;

    /** 状态: 0=草稿, 1=审理中, 2=已完结, 3=已归档 */
    @Schema(description = "状态")
        private Integer status;

    /** 被调查人姓名 */
    private String respondentName;

    /** 承办部门ID */
    private Long handlingDeptId;

    /** 开始日期 */
    @Schema(description = "开始日期")
        private Date startDate;

    /** 结束日期 */
    @Schema(description = "结束日期")
        private Date endDate;
}
