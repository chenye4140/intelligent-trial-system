package com.intelligent.trial.document.controller;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.intelligent.trial.common.annotation.RequireLog;
import com.intelligent.trial.common.dto.R;
import com.intelligent.trial.document.client.LlmClient;
import com.intelligent.trial.document.dto.ParseResultDTO;
import com.intelligent.trial.document.entity.CaseSimilarityRecord;
import com.intelligent.trial.document.entity.DocParseTask;
import com.intelligent.trial.document.entity.DocParagraphVector;
import com.intelligent.trial.document.mapper.DocParseTaskMapper;
import com.intelligent.trial.document.service.CaseSimilarityRecordService;
import com.intelligent.trial.document.service.VectorStorageService;
import com.intelligent.trial.document.vo.SimilarParagraphVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 类案推送控制器
 * 基于向量余弦相似度实现类案检索与推送
 *
 * @author intelligent-trial
 */
@Tag(name = "类案推送", description = "基于向量相似度的类案检索与推送接口")
@RestController
@RequestMapping("/api/document/similarity")
public class CaseSimilarityController {

    private static final Logger log = LoggerFactory.getLogger(CaseSimilarityController.class);

    @Autowired
    private VectorStorageService vectorStorageService;

    @Autowired
    private LlmClient llmClient;

    @Autowired
    private DocParseTaskMapper taskMapper;

    @Autowired
    private CaseSimilarityRecordService caseSimilarityRecordService;

    /**
     * 搜索相似案件段落
     *
     * @param request 搜索请求（支持 caseId 或 text 输入）
     * @return 相似段落列表
     */
    @Operation(summary = "搜索相似案件", description = "基于向量余弦相似度搜索相似案件段落，支持按案件ID或文本搜索")
    @PostMapping("/search")
    @RequireLog(module = "类案推送", action = "搜索")
    public R<List<SimilarParagraphVO>> searchSimilar(@Valid @RequestBody SimilarSearchRequest request) {
        try {
            float[] queryVector;
            String sourceCaseId = null;

            // 方式1：基于 caseId 搜索（使用该案件已解析的段落向量）
            if (request.getCaseId() != null) {
                sourceCaseId = String.valueOf(request.getCaseId());
                DocParseTask task = taskMapper.selectOne(
                        new LambdaQueryWrapper<DocParseTask>()
                                .eq(DocParseTask::getId, request.getCaseId())
                                .eq(DocParseTask::getStatus, 2) // 已完成
                );

                if (task == null) {
                    return R.fail("案件不存在或未完成解析");
                }

                // 从任务结果中提取第一段有向量的段落作为查询向量
                queryVector = extractQueryVectorFromTask(task);
                if (queryVector == null) {
                    return R.fail("该案件未生成向量");
                }
            }
            // 方式2：基于输入文本搜索
            else if (request.getText() != null && !request.getText().trim().isEmpty()) {
                List<float[]> vectors = llmClient.generateEmbeddingBatch(
                        Collections.singletonList(request.getText()));
                if (vectors.isEmpty() || vectors.get(0).length == 0) {
                    return R.fail("向量生成失败");
                }
                queryVector = vectors.get(0);
            }
            else {
                return R.fail("请提供 caseId 或 text 参数");
            }

            // 执行相似度搜索
            List<VectorStorageService.SimilarParagraphResult> results =
                    vectorStorageService.searchSimilar(queryVector,
                            request.getLimit() != null ? request.getLimit() : 10,
                            request.getCategory());

            // 转换为 VO 并收集相似度记录
            List<SimilarParagraphVO> voList = new ArrayList<>();
            List<CaseSimilarityRecord> recordsToSave = new ArrayList<>();

            for (VectorStorageService.SimilarParagraphResult r : results) {
                SimilarParagraphVO vo = new SimilarParagraphVO();
                vo.setVectorId(r.getId());
                vo.setCaseId(r.getTaskId());
                vo.setParagraphContent(r.getContent());
                vo.setSimilarity(r.getSimilarity());
                vo.setCategory(r.getCategory());
                vo.setLawLevel(r.getLawLevel());
                vo.setParagraphIndex(r.getParagraphIndex());

                // 从任务中获取文件名
                DocParseTask task = taskMapper.selectById(r.getTaskId());
                if (task != null) {
                    vo.setFileName(task.getFileName());
                }

                voList.add(vo);

                // 构建相似度记录（仅在有源案件ID时保存）
                if (sourceCaseId != null && r.getTaskId() != null) {
                    String similarCaseId = String.valueOf(r.getTaskId());
                    double similarity = r.getSimilarity();

                    CaseSimilarityRecord record = new CaseSimilarityRecord();
                    record.setSourceCaseId(sourceCaseId);
                    record.setSimilarCaseId(similarCaseId);
                    // 相似度保留4位小数
                    record.setSimilarityScore(BigDecimal.valueOf(similarity)
                            .setScale(4, RoundingMode.HALF_UP));
                    // 内容相似度 = 综合相似度（当前仅基于内容向量计算）
                    record.setContentScore(BigDecimal.valueOf(similarity)
                            .setScale(4, RoundingMode.HALF_UP));
                    // 金额和类型相似度暂未计算，默认为0
                    record.setAmountScore(BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP));
                    record.setTypeScore(BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP));

                    recordsToSave.add(record);
                }
            }

            // 批量保存相似度记录
            if (!recordsToSave.isEmpty()) {
                caseSimilarityRecordService.batchSaveOrUpdate(recordsToSave);
            }

            log.info("类案搜索完成: 返回 {} 条结果, 保存 {} 条记录", voList.size(), recordsToSave.size());
            return R.ok(voList);

        } catch (Exception e) {
            log.error("类案搜索失败", e);
            return R.fail("类案搜索失败: " + e.getMessage());
        }
    }

    /**
     * 从任务结果中提取查询向量
     */
    private float[] extractQueryVectorFromTask(DocParseTask task) {
        // 优先从向量表中取第一个段落向量
        List<DocParagraphVector> vectors = vectorStorageService.getByTaskId(task.getId());
        if (!vectors.isEmpty()) {
            String json = vectors.get(0).getVectorData();
            try {
                List<Double> list = JSON.parseArray(json, Double.class);
                float[] result = new float[list.size()];
                for (int i = 0; i < list.size(); i++) {
                    Double val = list.get(i);
                    result[i] = (val != null) ? val.floatValue() : 0.0f;
                }
                return result;
            } catch (Exception e) {
                log.warn("解析向量数据失败", e);
            }
        }

        // 备用：从 resultJson 中获取
        if (task.getResultJson() != null) {
            try {
                ParseResultDTO result = JSON.parseObject(task.getResultJson(), ParseResultDTO.class);
                if (result != null && result.getParagraphs() != null) {
                    for (ParseResultDTO.ParagraphDTO p : result.getParagraphs()) {
                        if (p.getContent() != null && !p.getContent().trim().isEmpty()) {
                            List<float[]> vectors2 = llmClient.generateEmbeddingBatch(
                                    Collections.singletonList(p.getContent()));
                            if (!vectors2.isEmpty()) {
                                return vectors2.get(0);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("从 resultJson 提取向量失败", e);
            }
        }

        return null;
    }

    /**
     * 搜索请求 DTO
     */
    public static class SimilarSearchRequest {
        /** 案件ID（基于案件内容搜索相似段落） */
        private Long caseId;

        /** 搜索文本（直接输入文本搜索） */
        private String text;

        /** 返回结果数量上限，默认 10 */
        private Integer limit;

        /** 分类过滤 */
        private String category;

        public Long getCaseId() { return caseId; }
        public void setCaseId(Long caseId) { this.caseId = caseId; }
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
        public Integer getLimit() { return limit; }
        public void setLimit(Integer limit) { this.limit = limit; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
    }
}
