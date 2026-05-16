package com.intelligent.trial.document.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 文档解析结果 DTO
 * 用于封装解析后的结构化数据
 *
 * @author intelligent-trial
 */
@Data
public class ParseResultDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 解析出的段落列表
     */
    private List<ParagraphDTO> paragraphs;

    /**
     * 解析元数据
     */
    private MetadataDTO metadata;

    /**
     * 段落 DTO
     */
    @Data
    public static class ParagraphDTO implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 段落文本内容
         */
        private String content;

        /**
         * 段落样式：title(标题), heading(章节标题), text(正文), table(表格), list(列表), footnote(脚注)
         */
        private String style;

        /**
         * 层级（仅对标题有效）：1=篇, 2=章, 3=节, 4=条, 5=款, 6=项
         */
        private Integer level;

        /**
         * 位置信息（页码或段落序号）
         */
        private Integer position;

        /**
         * 分类结果：总则/分则/附则/法律责任/案件事实/处理意见/法律依据
         */
        private String category;

        /**
         * 法规层级标签：篇/章/节/条/款/项
         */
        private String lawLevel;

        /**
         * 向量ID（入库后回填）
         */
        private String vectorId;
    }

    /**
     * 元数据 DTO
     */
    @Data
    public static class MetadataDTO implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 总段落数
         */
        private Integer totalParagraphs;

        /**
         * 总页数（PDF/图片解析时）
         */
        private Integer totalPages;

        /**
         * 文件字符数
         */
        private Integer totalCharacters;

        /**
         * 分类统计：{ "总则": 10, "分则": 20, ... }
         */
        private Object categoryStats;

        /**
         * 解析耗时（毫秒）
         */
        private Long parseDurationMs;
    }
}
