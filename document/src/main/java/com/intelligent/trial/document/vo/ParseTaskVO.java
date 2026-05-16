package com.intelligent.trial.document.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 解析任务 VO（返回给前端）
 *
 * @author intelligent-trial
 */
@Data
public class ParseTaskVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务ID
     */
    private Long id;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件访问 URL
     */
    private String fileUrl;

    /**
     * 文件类型
     */
    private String fileType;

    /**
     * 状态：0=待处理, 1=处理中, 2=已完成, 3=失败
     */
    private Integer status;

    /**
     * 状态描述
     */
    private String statusDesc;

    /**
     * 进度（0-100）
     */
    private Integer progress;

    /**
     * 错误信息
     */
    private String errorMsg;

    /**
     * 向量数量
     */
    private Integer vectorCount;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 解析完成时间
     */
    private Date parseTime;

    /**
     * 获取状态描述
     */
    public String getStatusDesc() {
        if (this.status == null) return "未知";
        switch (this.status) {
            case 0: return "待解析";
            case 1: return "解析中";
            case 2: return "已完成";
            case 3: return "解析失败";
            default: return "未知";
        }
    }
}
