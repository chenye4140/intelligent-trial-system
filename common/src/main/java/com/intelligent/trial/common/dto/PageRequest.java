package com.intelligent.trial.common.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.io.Serializable;

/**
 * 分页请求参数
 */
@Data
public class PageRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前页码（从1开始）
     */
    @Min(value = 1, message = "页码最小为1")
    private Integer pageNum = 1;

    /**
     * 每页条数
     */
    @Min(value = 1, message = "每页条数最小为1")
    @Max(value = 1000, message = "每页条数最大为1000")
    private Integer pageSize = 10;

    /**
     * 排序字段
     */
    private String orderBy;

    /**
     * 排序方式（asc/desc）
     */
    private String orderType = "desc";

    /**
     * 获取MyBatis-Plus偏移量
     */
    public long getOffset() {
        return (long) (pageNum - 1) * pageSize;
    }
}
