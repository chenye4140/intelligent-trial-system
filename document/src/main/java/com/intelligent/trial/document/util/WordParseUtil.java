package com.intelligent.trial.document.util;

import com.intelligent.trial.document.dto.ParseResultDTO;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.model.StyleDescription;
import org.apache.poi.hwpf.model.StyleSheet;
import org.apache.poi.hwpf.usermodel.*;
import com.intelligent.trial.common.exception.BusinessException;
import com.intelligent.trial.common.exception.ErrorCode;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.xmlbeans.XmlCursor;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTText;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Word 文档解析工具类
 * 使用 Apache POI 解析 .doc 和 .docx 文件
 * 提取文本内容、表格、段落样式，识别并过滤页眉页脚
 *
 * @author intelligent-trial
 */
public class WordParseUtil {

    private static final Logger log = LoggerFactory.getLogger(WordParseUtil.class);

    /**
     * 解析 Word 文档（根据文件类型自动选择解析方式）
     *
     * @param filePath 文件路径
     * @param fileType 文件类型（doc 或 docx）
     * @return 解析结果
     */
    public static ParseResultDTO parseWord(String filePath, String fileType) {
        try (InputStream is = new FileInputStream(filePath)) {
            if ("doc".equalsIgnoreCase(fileType)) {
                return parseDoc(is);
            } else {
                return parseDocx(is);
            }
        } catch (Exception e) {
            log.error("解析Word文档失败: {}", filePath, e);
            throw new BusinessException(ErrorCode.DOC_PARSE_FAILED.getCode(), "解析Word文档失败: " + e.getMessage());
        }
    }

    /**
     * 解析 Word 文档（从输入流）
     *
     * @param inputStream 文件输入流
     * @param fileType    文件类型
     * @return 解析结果
     */
    public static ParseResultDTO parseWord(InputStream inputStream, String fileType) {
        try {
            if ("doc".equalsIgnoreCase(fileType)) {
                return parseDoc(inputStream);
            } else {
                return parseDocx(inputStream);
            }
        } catch (Exception e) {
            log.error("解析Word文档失败", e);
            throw new BusinessException(ErrorCode.DOC_PARSE_FAILED.getCode(), "解析Word文档失败: " + e.getMessage());
        }
    }

    /**
     * 解析 .docx 文件（Office 2007+）
     */
    private static ParseResultDTO parseDocx(InputStream is) throws Exception {
        XWPFDocument document = new XWPFDocument(is);
        List<ParseResultDTO.ParagraphDTO> paragraphs = new ArrayList<>();
        int position = 0;

        // 收集页眉页脚文本用于过滤
        List<String> headerFooterTexts = new ArrayList<>();
        for (XWPFHeader header : document.getHeaderList()) {
            for (XWPFParagraph p : header.getParagraphs()) {
                headerFooterTexts.add(p.getText().trim());
            }
        }
        for (XWPFFooter footer : document.getFooterList()) {
            for (XWPFParagraph p : footer.getParagraphs()) {
                headerFooterTexts.add(p.getText().trim());
            }
        }

        // 解析正文段落
        for (XWPFParagraph xwpfParagraph : document.getParagraphs()) {
            String text = xwpfParagraph.getText().trim();
            if (text.isEmpty()) {
                continue;
            }

            // 过滤页眉页脚内容
            if (headerFooterTexts.contains(text)) {
                continue;
            }

            ParseResultDTO.ParagraphDTO paragraph = new ParseResultDTO.ParagraphDTO();
            paragraph.setContent(text);
            paragraph.setPosition(++position);

            // 识别段落样式
            String style = determineDocxParagraphStyle(xwpfParagraph);
            paragraph.setStyle(style);

            // 识别层级（基于样式名或字体特征）
            Integer level = determineDocxLevel(xwpfParagraph, style);
            paragraph.setLevel(level);

            paragraphs.add(paragraph);
        }

        // 解析表格
        for (XWPFTable table : document.getTables()) {
            ParseResultDTO.ParagraphDTO tableParagraph = new ParseResultDTO.ParagraphDTO();
            StringBuilder tableContent = new StringBuilder();
            for (XWPFTableRow row : table.getRows()) {
                List<String> cellTexts = new ArrayList<>();
                for (XWPFTableCell cell : row.getTableCells()) {
                    cellTexts.add(cell.getText().trim());
                }
                tableContent.append(String.join(" | ", cellTexts)).append("\n");
            }
            tableParagraph.setContent(tableContent.toString().trim());
            tableParagraph.setStyle("table");
            tableParagraph.setPosition(++position);
            paragraphs.add(tableParagraph);
        }

        document.close();

        return buildParseResult(paragraphs);
    }

    /**
     * 解析 .doc 文件（Office 97-2003）
     */
    private static ParseResultDTO parseDoc(InputStream is) throws Exception {
        HWPFDocument document = new HWPFDocument(is);
        List<ParseResultDTO.ParagraphDTO> paragraphs = new ArrayList<>();
        Range range = document.getRange();
        int position = 0;

        // 获取样式表
        StyleSheet styleSheet = document.getStyleSheet();

        // 遍历段落
        int numParagraphs = range.numParagraphs();
        for (int i = 0; i < numParagraphs; i++) {
            Paragraph hwpfParagraph = range.getParagraph(i);
            String text = hwpfParagraph.text().trim();
            if (text.isEmpty()) {
                continue;
            }

            ParseResultDTO.ParagraphDTO paragraph = new ParseResultDTO.ParagraphDTO();
            paragraph.setContent(text);
            paragraph.setPosition(++position);

            // 获取样式
            int styleIndex = hwpfParagraph.getStyleIndex();
            if (styleIndex >= 0 && styleIndex < styleSheet.numStyles()) {
                StyleDescription styleDesc = styleSheet.getStyleDescription(styleIndex);
                if (styleDesc != null) {
                    String styleName = styleDesc.getName();
                    String style = determineDocParagraphStyle(styleName, hwpfParagraph);
                    paragraph.setStyle(style);
                    Integer level = determineDocLevel(styleName, style);
                    paragraph.setLevel(level);
                }
            }

            if (paragraph.getStyle() == null) {
                paragraph.setStyle("text");
            }

            paragraphs.add(paragraph);
        }

        document.close();

        return buildParseResult(paragraphs);
    }

    /**
     * 判断 .docx 段落样式
     */
    private static String determineDocxParagraphStyle(XWPFParagraph paragraph) {
        String styleId = paragraph.getStyleID();
        String styleName = paragraph.getStyle();

        // 根据样式ID判断
        if (styleId != null) {
            if (styleId.toLowerCase().contains("title")) {
                return "title";
            }
            if (styleId.toLowerCase().contains("heading")) {
                return "heading";
            }
            if (styleId.toLowerCase().contains("list")) {
                return "list";
            }
        }

        // 根据样式名判断
        if (styleName != null) {
            String lower = styleName.toLowerCase();
            if (lower.contains("title")) {
                return "title";
            }
            if (lower.contains("heading")) {
                return "heading";
            }
            if (lower.contains("list")) {
                return "list";
            }
        }

        // 根据字体特征判断：加粗 + 字号较大 = 标题
        try {
            if (paragraph.getRuns() != null && !paragraph.getRuns().isEmpty()) {
                XWPFRun firstRun = paragraph.getRuns().get(0);
                if (firstRun.isBold() && firstRun.getFontSize() > 14) {
                    return "heading";
                }
                if (firstRun.isBold()) {
                    return "heading";
                }
            }
        } catch (Exception e) {
            // 忽略字体特征提取异常
        }

        return "text";
    }

    /**
     * 判断 .docx 段落层级
     */
    private static Integer determineDocxLevel(XWPFParagraph paragraph, String style) {
        if (!"heading".equals(style) && !"title".equals(style)) {
            return null;
        }

        String styleId = paragraph.getStyleID();
        if (styleId != null && styleId.toLowerCase().contains("heading")) {
            try {
                // 提取 heading1, heading2, ... 中的数字
                String numStr = styleId.replaceAll("(?i)[^\\d]", "");
                if (!numStr.isEmpty()) {
                    return Integer.parseInt(numStr);
                }
            } catch (Exception e) {
                // 忽略解析异常
            }
        }

        // 根据字号判断层级
        try {
            if (paragraph.getRuns() != null && !paragraph.getRuns().isEmpty()) {
                int fontSize = paragraph.getRuns().get(0).getFontSize();
                if (fontSize > 20) return 1;  // 篇/章级别
                if (fontSize > 16) return 2;  // 节级别
                if (fontSize > 14) return 3;  // 条级别
                return 4;  // 款级别
            }
        } catch (Exception e) {
            // 忽略
        }

        return "title".equals(style) ? 1 : 2;
    }

    /**
     * 判断 .doc 段落样式
     */
    private static String determineDocParagraphStyle(String styleName, Paragraph paragraph) {
        if (styleName != null) {
            String lower = styleName.toLowerCase();
            if (lower.contains("title") || lower.contains("标题")) {
                return "title";
            }
            if (lower.contains("heading") || lower.contains("head") || lower.contains("标题")) {
                return "heading";
            }
            if (lower.contains("list") || lower.contains("列表")) {
                return "list";
            }
        }

        // 根据格式特征判断
        // TODO: POI API 限制，暂时简化判断
        if (styleName != null && (styleName.startsWith("•") || styleName.startsWith("-") || styleName.startsWith("*"))) {
            return "list";
        }

        return "text";
    }

    /**
     * 判断 .doc 段落层级
     */
    private static Integer determineDocLevel(String styleName, String style) {
        if (!"heading".equals(style) && !"title".equals(style)) {
            return null;
        }

        if (styleName != null && styleName.toLowerCase().contains("heading")) {
            try {
                String numStr = styleName.replaceAll("(?i)[^\\d]", "");
                if (!numStr.isEmpty()) {
                    return Integer.parseInt(numStr);
                }
            } catch (Exception e) {
                // 忽略
            }
        }

        return "title".equals(style) ? 1 : 2;
    }

    /**
     * 构建解析结果
     */
    private static ParseResultDTO buildParseResult(List<ParseResultDTO.ParagraphDTO> paragraphs) {
        ParseResultDTO result = new ParseResultDTO();
        result.setParagraphs(paragraphs);

        // 构建元数据
        ParseResultDTO.MetadataDTO metadata = new ParseResultDTO.MetadataDTO();
        metadata.setTotalParagraphs(paragraphs.size());
        int totalChars = 0;
        for (ParseResultDTO.ParagraphDTO p : paragraphs) {
            totalChars += p.getContent() != null ? p.getContent().length() : 0;
        }
        metadata.setTotalCharacters(totalChars);
        result.setMetadata(metadata);

        return result;
    }

    /**
     * 根据文本内容智能判断段落类型（用于辅助分类）
     *
     * @param content 段落内容
     * @return 样式类型
     */
    public static String inferStyleFromContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            return "text";
        }

        String trimmed = content.trim();

        // 短文本 + 无标点 = 可能是标题
        if (trimmed.length() < 30 && !trimmed.contains("。") && !trimmed.contains("，")) {
            // 检查是否包含章节编号模式
            if (trimmed.matches(".*[第][一二三四五六七八九十百千0-9]+[篇章章节条款项目].*")) {
                return "heading";
            }
            return "title";
        }

        // 列表模式
        if (trimmed.matches("^[（(]\\d+[）)].*") || trimmed.matches("^[一二三四五六七八九十]+[、].*")) {
            return "list";
        }

        return "text";
    }
}
