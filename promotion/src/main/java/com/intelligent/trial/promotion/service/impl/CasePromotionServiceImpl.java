package com.intelligent.trial.promotion.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.intelligent.trial.promotion.client.DeepSeekClient;
import com.intelligent.trial.promotion.dto.CasePromotionGenerateDTO;
import com.intelligent.trial.promotion.dto.CasePromotionSearchDTO;
import com.intelligent.trial.promotion.entity.CasePromotion;
import com.intelligent.trial.promotion.mapper.CasePromotionMapper;
import com.intelligent.trial.promotion.service.ICasePromotionService;
import com.intelligent.trial.promotion.vo.CasePromotionVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 以案促改服务实现类
 * 核心功能：基于 DeepSeek AI 模型自动生成案件促改分析报告
 */
@Service
public class CasePromotionServiceImpl implements ICasePromotionService {

    private static final Logger log = LoggerFactory.getLogger(CasePromotionServiceImpl.class);

    @Autowired
    private CasePromotionMapper casePromotionMapper;

    @Autowired
    private DeepSeekClient deepSeekClient;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Redis key 前缀
    private static final String TASK_STATUS_KEY_PREFIX = "promotion:task:";
    private static final long TASK_STATUS_EXPIRE_HOURS = 24;

    // 状态常量
    private static final int STATUS_DRAFT = 0;
    private static final int STATUS_PENDING = 1;
    private static final int STATUS_APPROVED = 2;
    private static final int STATUS_REJECTED = 3;

    @Override
    public CasePromotion getById(Long id) {
        return casePromotionMapper.selectById(id);
    }

    @Override
    public IPage<CasePromotionVO> search(CasePromotionSearchDTO dto) {
        Page<CasePromotion> page = new Page<>(dto.getPageNum(), dto.getPageSize());

        LambdaQueryWrapper<CasePromotion> wrapper = new LambdaQueryWrapper<>();
        if (dto.getCaseId() != null && !dto.getCaseId().isEmpty()) {
            wrapper.eq(CasePromotion::getCaseId, dto.getCaseId());
        }
        if (dto.getStatus() != null) {
            wrapper.eq(CasePromotion::getStatus, dto.getStatus());
        }
        if (dto.getUserId() != null) {
            wrapper.eq(CasePromotion::getUserId, dto.getUserId());
        }
        wrapper.orderByDesc(CasePromotion::getCreateTime);

        Page<CasePromotion> resultPage = casePromotionMapper.selectPage(page, wrapper);

        // 转换为 VO 并填充附加信息
        Page<CasePromotionVO> voPage = new Page<>(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal());
        List<CasePromotionVO> voList = new ArrayList<>();

        for (CasePromotion record : resultPage.getRecords()) {
            CasePromotionVO vo = new CasePromotionVO();
            BeanUtils.copyProperties(record, vo);

            // 填充案件标题
            if (record.getCaseId() != null) {
                String caseTitle = queryCaseTitle(record.getCaseId());
                vo.setCaseTitle(caseTitle);
            }

            // 填充创建人姓名
            if (record.getUserId() != null) {
                String userName = queryUserName(record.getUserId());
                vo.setUserName(userName);
            }

            voList.add(vo);
        }

        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public CasePromotion getByCaseId(String caseId) {
        return casePromotionMapper.selectByCaseId(caseId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CasePromotion create(CasePromotion entity) {
        if (entity.getStatus() == null) {
            entity.setStatus(STATUS_DRAFT);
        }
        if (entity.getCreateTime() == null) {
            entity.setCreateTime(new Date());
        }
        if (entity.getUpdateTime() == null) {
            entity.setUpdateTime(new Date());
        }
        casePromotionMapper.insert(entity);
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(CasePromotion entity) {
        entity.setUpdateTime(new Date());
        return casePromotionMapper.updateById(entity) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteById(Long id) {
        return casePromotionMapper.deleteById(id) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(Long id, Integer status) {
        CasePromotion entity = new CasePromotion();
        entity.setId(id);
        entity.setStatus(status);
        entity.setUpdateTime(new Date());
        return casePromotionMapper.updateById(entity) > 0;
    }

    @Override
    public String generateAnalysis(CasePromotionGenerateDTO dto) {
        // 1. 生成任务ID
        String taskId = UUID.randomUUID().toString().replace("-", "");

        // 2. 存储任务状态为 running
        String redisKey = TASK_STATUS_KEY_PREFIX + taskId;
        redisTemplate.opsForValue().set(redisKey, "running", TASK_STATUS_EXPIRE_HOURS, TimeUnit.HOURS);

        log.info("促改分析任务已创建: taskId={}, caseId={}, analysisType={}",
                taskId, dto.getCaseId(), dto.getAnalysisType());

        // 3. 异步执行分析
        analyzeAndSave(taskId, dto);

        // 4. 返回任务ID
        return taskId;
    }

    @Override
    public String getAnalysisStatus(String taskId) {
        String redisKey = TASK_STATUS_KEY_PREFIX + taskId;
        String status = redisTemplate.opsForValue().get(redisKey);
        return status != null ? status : "not_found";
    }

    /**
     * 异步执行促改分析并保存结果
     */
    @Async("promotionExecutor")
    @Transactional(rollbackFor = Exception.class)
    public void analyzeAndSave(String taskId, CasePromotionGenerateDTO dto) {
        String redisKey = TASK_STATUS_KEY_PREFIX + taskId;
        log.info("开始异步执行促改分析: taskId={}", taskId);

        try {
            // 1. 查询案件信息
            Map<String, Object> caseInfo = queryCaseInfoByCaseId(dto.getCaseId());
            if (caseInfo == null) {
                redisTemplate.opsForValue().set(redisKey, "failed:case_not_found",
                        TASK_STATUS_EXPIRE_HOURS, TimeUnit.HOURS);
                log.error("案件不存在: caseId={}", dto.getCaseId());
                return;
            }

            // 2. 查询违纪事实
            List<Map<String, Object>> violationFacts = queryViolationFacts(dto.getCaseId());

            // 3. 构建系统提示词
            String systemPrompt = buildSystemPrompt(dto.getAnalysisType());

            // 4. 构建用户提示词
            String userPrompt = buildUserPrompt(caseInfo, violationFacts, dto.getAnalysisType());

            // 5. 调用 DeepSeek API 获取分析结果
            String analysisContent = deepSeekClient.chat(systemPrompt, userPrompt);

            // 6. 创建促改记录
            CasePromotion promotion = new CasePromotion();
            promotion.setCaseId(dto.getCaseId());
            promotion.setTemplateId(dto.getTemplateId());
            promotion.setContent(analysisContent);
            promotion.setStatus(STATUS_PENDING);
            promotion.setUserId(dto.getUserId());
            promotion.setCreateTime(new Date());
            promotion.setUpdateTime(new Date());
            casePromotionMapper.insert(promotion);

            // 7. 更新任务状态为 completed
            redisTemplate.opsForValue().set(redisKey, "completed",
                    TASK_STATUS_EXPIRE_HOURS, TimeUnit.HOURS);

            log.info("促改分析完成并保存成功: taskId={}, promotionId={}, 内容长度={}",
                    taskId, promotion.getId(), analysisContent != null ? analysisContent.length() : 0);
        } catch (Exception e) {
            log.error("促改分析失败: taskId={}", taskId, e);
            // 更新任务状态为 failed
            redisTemplate.opsForValue().set(redisKey, "failed:" + e.getMessage(),
                    TASK_STATUS_EXPIRE_HOURS, TimeUnit.HOURS);
        }
    }

    // ========================= 私有辅助方法 =========================

    /**
     * 查询案件信息（通过 caseId 字符串查询，可能是编号或ID）
     */
    private Map<String, Object> queryCaseInfoByCaseId(String caseId) {
        try {
            String sql = "SELECT id, case_code, case_name, case_type, case_source, " +
                    "respondent_name, respondent_dept, respondent_position, " +
                    "status, filing_date, close_date, brief_description, violation_type " +
                    "FROM case_info WHERE case_code = ? OR id = ?";
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, caseId, caseId);
            return results.isEmpty() ? null : results.get(0);
        } catch (Exception e) {
            log.error("查询案件信息失败: caseId={}", caseId, e);
            return null;
        }
    }

    /**
     * 查询案件标题
     */
    private String queryCaseTitle(String caseId) {
        try {
            String sql = "SELECT case_name FROM case_info WHERE case_code = ? OR id = ? LIMIT 1";
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, caseId, caseId);
            if (!results.isEmpty()) {
                Object val = results.get(0).get("case_name");
                return val != null ? val.toString() : null;
            }
        } catch (Exception e) {
            log.error("查询案件标题失败: caseId={}", caseId, e);
        }
        return null;
    }

    /**
     * 查询用户姓名
     */
    private String queryUserName(Long userId) {
        try {
            String sql = "SELECT real_name FROM sys_user WHERE id = ? LIMIT 1";
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, userId);
            if (!results.isEmpty()) {
                Object val = results.get(0).get("real_name");
                return val != null ? val.toString() : null;
            }
        } catch (Exception e) {
            log.error("查询用户姓名失败: userId={}", userId, e);
        }
        return null;
    }

    /**
     * 查询案件违纪事实
     */
    private List<Map<String, Object>> queryViolationFacts(String caseId) {
        try {
            String sql = "SELECT fact_title, fact_content, violation_type, occurred_date, " +
                    "amount, evidence FROM case_violation_fact " +
                    "WHERE case_id = ? OR case_id IN (SELECT id FROM case_info WHERE case_code = ?) " +
                    "ORDER BY sort ASC";
            return jdbcTemplate.queryForList(sql, caseId, caseId);
        } catch (Exception e) {
            log.error("查询违纪事实失败: caseId={}", caseId, e);
            return new ArrayList<>();
        }
    }

    /**
     * 构建系统提示词
     */
    private String buildSystemPrompt(String analysisType) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是纪检监察案件分析专家，擅长以案促改分析。");
        sb.append("你的任务是根据给定的案件信息，深入分析案件反映出的问题，");
        sb.append("提出有针对性的整改建议和防范措施，帮助相关单位完善制度、堵塞漏洞、加强管理。\n\n");
        sb.append("分析要求：\n");
        sb.append("1. 问题剖析要深刻准确，直击本质\n");
        sb.append("2. 原因分析要全面客观，多维度审视\n");
        sb.append("3. 整改建议要具体可行，具有可操作性\n");
        sb.append("4. 防范措施要系统完善，形成长效机制\n");
        sb.append("5. 语言表述要规范严谨，符合纪检监察公文标准\n\n");

        if ("discipline".equals(analysisType)) {
            sb.append("【分析类型：纪律分析】\n");
            sb.append("重点关注：案件反映出的纪律意识淡薄、纪律执行不到位等问题，\n");
            sb.append("提出加强纪律教育、严格纪律执行的具体措施。\n");
        } else if ("management".equals(analysisType)) {
            sb.append("【分析类型：管理分析】\n");
            sb.append("重点关注：案件暴露出的管理漏洞、监督缺失、权力运行不规范等问题，\n");
            sb.append("提出完善管理机制、强化监督制约的具体措施。\n");
        } else if ("system".equals(analysisType)) {
            sb.append("【分析类型：制度分析】\n");
            sb.append("重点关注：案件反映出的制度缺陷、制度执行不力等问题，\n");
            sb.append("提出健全制度体系、强化制度执行的具体措施。\n");
        } else {
            sb.append("【分析类型：综合分析】\n");
            sb.append("请从纪律、管理、制度等多个维度进行全面分析。\n");
        }

        return sb.toString();
    }

    /**
     * 构建用户提示词
     */
    private String buildUserPrompt(Map<String, Object> caseInfo, List<Map<String, Object>> violationFacts, String analysisType) {
        StringBuilder sb = new StringBuilder();

        sb.append("## 案件基本信息\n\n");
        sb.append("- 案件编号：").append(getStr(caseInfo, "case_code")).append("\n");
        sb.append("- 案件名称：").append(getStr(caseInfo, "case_name")).append("\n");
        sb.append("- 案件类型：").append(getCaseTypeDesc(caseInfo)).append("\n");
        sb.append("- 案件来源：").append(getStr(caseInfo, "case_source")).append("\n");
        sb.append("- 立案日期：").append(getDateStr(caseInfo, "filing_date")).append("\n");
        sb.append("- 结案日期：").append(getDateStr(caseInfo, "close_date")).append("\n\n");

        sb.append("## 被调查人信息\n\n");
        sb.append("- 姓名：").append(getStr(caseInfo, "respondent_name")).append("\n");
        sb.append("- 单位：").append(getStr(caseInfo, "respondent_dept")).append("\n");
        sb.append("- 职务：").append(getStr(caseInfo, "respondent_position")).append("\n\n");

        sb.append("## 简要案情\n\n");
        sb.append(getStr(caseInfo, "brief_description")).append("\n\n");

        if (violationFacts != null && !violationFacts.isEmpty()) {
            sb.append("## 违纪事实\n\n");
            for (int i = 0; i < violationFacts.size(); i++) {
                Map<String, Object> fact = violationFacts.get(i);
                sb.append("### 事实").append(i + 1).append("：").append(getStr(fact, "fact_title")).append("\n\n");
                sb.append("内容：").append(getStr(fact, "fact_content")).append("\n");
                sb.append("违纪类型：").append(getStr(fact, "violation_type")).append("\n");
                Object occurredDate = fact.get("occurred_date");
                if (occurredDate != null) {
                    sb.append("发生时间：").append(occurredDate.toString()).append("\n");
                }
                Object amount = fact.get("amount");
                if (amount != null) {
                    sb.append("涉及金额：").append(amount.toString()).append("\n");
                }
                sb.append("证据：").append(getStr(fact, "evidence")).append("\n\n");
            }
        }

        sb.append("## 分析要求\n\n");
        sb.append("请根据以上案件信息，生成一份完整的以案促改分析报告。\n");
        sb.append("报告应包含以下部分：\n");
        sb.append("1. 案件概况\n");
        sb.append("2. 问题剖析\n");
        sb.append("3. 原因分析\n");
        sb.append("4. 整改建议\n");
        sb.append("5. 防范措施\n");

        return sb.toString();
    }

    // ========================= 辅助方法 =========================

    private String getStr(Map<String, Object> map, String key) {
        if (map == null) return "未填写";
        Object value = map.get(key);
        return value != null ? value.toString() : "未填写";
    }

    private String getDateStr(Map<String, Object> map, String key) {
        if (map == null) return "未填写";
        Object value = map.get(key);
        if (value instanceof java.util.Date) {
            return cn.hutool.core.date.DateUtil.formatDate((java.util.Date) value);
        }
        return value != null ? value.toString() : "未填写";
    }

    private String getCaseTypeDesc(Map<String, Object> caseInfo) {
        if (caseInfo == null) return "未填写";
        Object typeObj = caseInfo.get("case_type");
        if (typeObj == null) return "未填写";
        int type = ((Number) typeObj).intValue();
        switch (type) {
            case 1: return "违纪案件";
            case 2: return "违法案件";
            case 3: return "职务犯罪案件";
            default: return "未知类型(" + type + ")";
        }
    }
}
