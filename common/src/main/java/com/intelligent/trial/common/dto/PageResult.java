package com.intelligent.trial.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 分页响应结果
 *
 * @param <T> 数据类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 当前页码
     */
    private Integer pageNum;

    /**
     * 每页条数
     */
    private Integer pageSize;

    /**
     * 总页数
     */
    private Integer pages;

    /**
     * 数据列表
     */
    private List<T> list;

    /**
     * 是否有上一页
     */
    public boolean hasPrevious() {
        return pageNum > 1;
    }

    /**
     * 是否有下一页
     */
    public boolean hasNext() {
        return pageNum < pages;
    }

    /**
     * 构建分页结果
     */
    public static <T> PageResult<T> of(Long total, Integer pageNum, Integer pageSize, List<T> list) {
        PageResult<T> result = new PageResult<>();
        result.setTotal(total);
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        result.setList(list);
        result.setPages((int) Math.ceil((double) total / pageSize));
        return result;
    }
}
