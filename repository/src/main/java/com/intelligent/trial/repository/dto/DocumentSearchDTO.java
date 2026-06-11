package com.intelligent.trial.repository.dto;

import com.intelligent.trial.common.dto.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 文档搜索条件 DTO
 * 支持多字段模糊搜索 + 分页查询
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "文档搜索请求")
public class DocumentSearchDTO extends PageRequest {

    /**
     * 库类型：1=法规库, 2=资料库, 3=裁判文书库, 4=案例库
     */
    private Integer repoType;

    /**
     * 关键词（匹配标题、文号、发布单位）
     */
    private String keyword;

    /**
     * 目录ID
     */
    @Schema(description = "目录ID")
        private Long directoryId;

    /**
     * 有效性状态：valid=有效, invalid=失效, pending=待生效
     */
    private String validityStatus;

    /**
     * 发布日期开始
     */
    private Date publishDateStart;

    /**
     * 发布日期结束
     */
    private Date publishDateEnd;

    /**
     * 定密级别ID
     */
    private Long classificationLevelId;

    /**
     * 发布单位
     */
    private String publishUnit;

    /**
     * 关联案件ID
     */
    @Schema(description = "案件ID")
        private Long caseId;
}
