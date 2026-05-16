package com.intelligent.trial.document.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 相似段落视图对象
 * 用于类案推送接口返回
 *
 * @author intelligent-trial
 */
@Data
public class SimilarParagraphVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 段落向量ID
     */
    private Long vectorId;

    /**
     * 关联案件ID（通过 task 关联）
     */
    private Long caseId;

    /**
     * 案件名称
     */
    private String caseName;

    /**
     * 段落内容
     */
    private String paragraphContent;

    /**
     * 相似度得分（0-1）
     */
    private Double similarity;

    /**
     * 分类：总则/分则/附则/法律责任/案件事实/处理意见/法律依据
     */
    private String category;

    /**
     * 法规层级
     */
    private String lawLevel;

    /**
     * 原始文件名
     */
    private String fileName;

    /**
     * 段落序号
     */
    private Integer paragraphIndex;
}
