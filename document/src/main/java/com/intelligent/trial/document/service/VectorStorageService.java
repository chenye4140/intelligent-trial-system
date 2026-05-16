package com.intelligent.trial.document.service;

import com.alibaba.fastjson2.JSON;
import com.intelligent.trial.document.dto.ParseResultDTO;
import com.intelligent.trial.document.entity.DocParagraphVector;
import com.intelligent.trial.document.mapper.DocParagraphVectorMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 向量存储服务
 * 负责段落向量的存储和相似度检索
 *
 * @author intelligent-trial
 */
@Service
public class VectorStorageService {

    private static final Logger log = LoggerFactory.getLogger(VectorStorageService.class);

    @Autowired
    private DocParagraphVectorMapper vectorMapper;

    /**
     * 批量存储向量
     *
     * @param taskId     解析任务ID
     * @param texts      段落文本列表
     * @param vectors    向量列表
     * @param paragraphs 段落DTO列表（用于回填分类等信息）
     */
    @Transactional(rollbackFor = Exception.class)
    public List<Long> storeVectors(Long taskId,
                                   List<String> texts,
                                   List<float[]> vectors,
                                   List<ParseResultDTO.ParagraphDTO> paragraphs) {
        if (vectors == null || vectors.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> vectorIds = new ArrayList<>();
        int dimension = vectors.get(0).length;

        for (int i = 0; i < vectors.size(); i++) {
            DocParagraphVector entity = new DocParagraphVector();
            entity.setTaskId(taskId);
            entity.setParagraphIndex(i);

            // 文本内容
            String text = (i < texts.size()) ? texts.get(i) : "";
            entity.setContent(text);

            // 分类和层级信息（从 paragraphs 获取）
            if (paragraphs != null && i < paragraphs.size()) {
                ParseResultDTO.ParagraphDTO p = paragraphs.get(i);
                entity.setCategory(p.getCategory());
                entity.setLawLevel(p.getLawLevel());
            }

            // 向量数据转为 JSON 数组
            float[] vector = vectors.get(i);
            entity.setVectorData(vectorToJson(vector));
            entity.setVectorDimension(dimension);

            vectorMapper.insert(entity);
            vectorIds.add(entity.getId());

            // 回填 vectorId
            if (paragraphs != null && i < paragraphs.size()) {
                paragraphs.get(i).setVectorId(String.valueOf(entity.getId()));
            }
        }

        log.info("向量存储完成: taskId={}, count={}, dimension={}", taskId, vectorIds.size(), dimension);
        return vectorIds;
    }

    /**
     * 相似度搜索
     * 基于余弦相似度，在 MySQL 中先获取候选集，然后在 Java 层计算并排序
     *
     * @param queryVector 查询向量
     * @param limit       返回结果数量
     * @param category    分类过滤（可选）
     * @return 带相似度得分的段落列表
     */
    public List<SimilarParagraphResult> searchSimilar(float[] queryVector,
                                                      int limit,
                                                      String category) {
        if (queryVector == null || queryVector.length == 0) {
            return Collections.emptyList();
        }

        // 1. 将查询向量转为 JSON
        String queryVectorJson = vectorToJson(queryVector);

        // 2. 从数据库获取候选段落（取 limit * 5 个候选用于 Java 层排序）
        int candidateLimit = limit * 5;
        List<DocParagraphVector> candidates = vectorMapper.searchSimilar(
                queryVectorJson, candidateLimit, category);

        // 3. 在 Java 层计算余弦相似度并排序
        List<SimilarParagraphResult> results = new ArrayList<>();
        for (DocParagraphVector pv : candidates) {
            float[] storedVector = jsonToVector(pv.getVectorData());
            if (storedVector == null || storedVector.length != queryVector.length) {
                continue;
            }

            double similarity = cosineSimilarity(queryVector, storedVector);
            SimilarParagraphResult result = new SimilarParagraphResult();
            result.setId(pv.getId());
            result.setTaskId(pv.getTaskId());
            result.setParagraphIndex(pv.getParagraphIndex());
            result.setContent(pv.getContent());
            result.setCategory(pv.getCategory());
            result.setLawLevel(pv.getLawLevel());
            result.setSimilarity(similarity);
            result.setCreateTime(pv.getCreateTime());

            results.add(result);
        }

        // 4. 按相似度降序排序，取 top N
        Collections.sort(results, new Comparator<SimilarParagraphResult>() {
            @Override
            public int compare(SimilarParagraphResult o1, SimilarParagraphResult o2) {
                return Double.compare(o2.getSimilarity(), o1.getSimilarity());
            }
        });

        if (results.size() > limit) {
            results = results.subList(0, limit);
        }

        log.info("相似度搜索完成: 候选 {} 个, 返回 {} 个, category={}",
                candidates.size(), results.size(), category);
        return results;
    }

    /**
     * 根据 taskId 获取所有向量段落
     */
    public List<DocParagraphVector> getByTaskId(Long taskId) {
        return vectorMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<DocParagraphVector>()
                        .eq(DocParagraphVector::getTaskId, taskId)
                        .orderByAsc(DocParagraphVector::getParagraphIndex));
    }

    /**
     * 计算余弦相似度
     *
     * @param a 向量 a
     * @param b 向量 b
     * @return 余弦相似度值 [0, 1]
     */
    public static double cosineSimilarity(float[] a, float[] b) {
        if (a == null || b == null || a.length != b.length) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < a.length; i++) {
            dotProduct += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }

        if (normA == 0.0 || normB == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    /**
     * float[] 转为 JSON 数组字符串
     */
    private static String vectorToJson(float[] vector) {
        if (vector == null) {
            return "[]";
        }
        Double[] boxed = new Double[vector.length];
        for (int i = 0; i < vector.length; i++) {
            boxed[i] = (double) vector[i];
        }
        return JSON.toJSONString(boxed);
    }

    /**
     * JSON 数组字符串转为 float[]
     */
    private static float[] jsonToVector(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            List<Double> list = JSON.parseArray(json, Double.class);
            float[] result = new float[list.size()];
            for (int i = 0; i < list.size(); i++) {
                Double val = list.get(i);
                result[i] = (val != null) ? val.floatValue() : 0.0f;
            }
            return result;
        } catch (Exception e) {
            log.error("向量 JSON 解析失败: {}", json, e);
            return null;
        }
    }

    /**
     * 相似度搜索结果 DTO
     */
    public static class SimilarParagraphResult {
        private Long id;
        private Long taskId;
        private Integer paragraphIndex;
        private String content;
        private String category;
        private String lawLevel;
        private double similarity;
        private Date createTime;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getTaskId() { return taskId; }
        public void setTaskId(Long taskId) { this.taskId = taskId; }
        public Integer getParagraphIndex() { return paragraphIndex; }
        public void setParagraphIndex(Integer paragraphIndex) { this.paragraphIndex = paragraphIndex; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public String getLawLevel() { return lawLevel; }
        public void setLawLevel(String lawLevel) { this.lawLevel = lawLevel; }
        public double getSimilarity() { return similarity; }
        public void setSimilarity(double similarity) { this.similarity = similarity; }
        public Date getCreateTime() { return createTime; }
        public void setCreateTime(Date createTime) { this.createTime = createTime; }
    }
}
