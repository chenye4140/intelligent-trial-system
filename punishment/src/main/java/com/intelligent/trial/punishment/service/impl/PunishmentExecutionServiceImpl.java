package com.intelligent.trial.punishment.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.intelligent.trial.common.exception.BusinessException;
import com.intelligent.trial.common.util.UserContext;
import com.intelligent.trial.punishment.dto.PunishmentExecutionDTO;
import com.intelligent.trial.punishment.dto.PunishmentSearchDTO;
import com.intelligent.trial.punishment.entity.PunishmentExecution;
import com.intelligent.trial.punishment.entity.PunishmentMaterial;
import com.intelligent.trial.punishment.mapper.PunishmentExecutionMapper;
import com.intelligent.trial.punishment.mapper.PunishmentMaterialMapper;
import com.intelligent.trial.punishment.service.IPunishmentExecutionService;
import com.intelligent.trial.punishment.vo.PunishmentExecutionVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 处分执行 Service 实现
 */
@Service
public class PunishmentExecutionServiceImpl implements IPunishmentExecutionService {

    private static final Logger log = LoggerFactory.getLogger(PunishmentExecutionServiceImpl.class);

    /**
     * 状态映射
     */
    private static final Map<Integer, String> STATUS_MAP = new java.util.HashMap<Integer, String>() {{
        put(0, "待执行");
        put(1, "执行中");
        put(2, "已完成");
        put(3, "已撤销");
    }};

    private final PunishmentExecutionMapper executionMapper;
    private final PunishmentMaterialMapper materialMapper;

    public PunishmentExecutionServiceImpl(PunishmentExecutionMapper executionMapper,
                                          PunishmentMaterialMapper materialMapper) {
        this.executionMapper = executionMapper;
        this.materialMapper = materialMapper;
    }

    @Override
    public Page<PunishmentExecutionVO> pageQuery(int pageNum, int pageSize, PunishmentSearchDTO searchDTO) {
        Page<PunishmentExecution> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<PunishmentExecution> wrapper = new LambdaQueryWrapper<>();

        if (searchDTO != null) {
            if (searchDTO.getCaseId() != null && !searchDTO.getCaseId().isEmpty()) {
                wrapper.eq(PunishmentExecution::getCaseId, searchDTO.getCaseId());
            }
            if (searchDTO.getPunishmentType() != null && !searchDTO.getPunishmentType().isEmpty()) {
                wrapper.eq(PunishmentExecution::getPunishmentType, searchDTO.getPunishmentType());
            }
            if (searchDTO.getStatus() != null) {
                wrapper.eq(PunishmentExecution::getStatus, searchDTO.getStatus());
            }
            if (searchDTO.getIsOverdue() != null) {
                wrapper.eq(PunishmentExecution::getIsOverdue, searchDTO.getIsOverdue());
            }
        }

        wrapper.orderByDesc(PunishmentExecution::getCreateTime);
        Page<PunishmentExecution> resultPage = executionMapper.selectPage(page, wrapper);

        // 转换为 VO
        Page<PunishmentExecutionVO> voPage = new Page<>(pageNum, pageSize, resultPage.getTotal());
        List<PunishmentExecutionVO> voList = new java.util.ArrayList<>();
        for (PunishmentExecution entity : resultPage.getRecords()) {
            voList.add(convertToVO(entity));
        }
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public PunishmentExecutionVO getDetail(Long id) {
        PunishmentExecution execution = executionMapper.selectById(id);
        if (execution == null) {
            throw new BusinessException("处分执行记录不存在");
        }
        PunishmentExecutionVO vo = convertToVO(execution);

        // 查询关联材料
        List<PunishmentMaterial> materials = materialMapper.selectByExecutionId(id);
        vo.setMaterials(materials);

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PunishmentExecution create(PunishmentExecutionDTO dto) {
        if (dto.getCaseId() == null || dto.getCaseId().isEmpty()) {
            throw new BusinessException("案件ID不能为空");
        }
        if (dto.getPunishmentType() == null || dto.getPunishmentType().isEmpty()) {
            throw new BusinessException("处分类型不能为空");
        }

        PunishmentExecution execution = new PunishmentExecution();
        BeanUtils.copyProperties(dto, execution);
        execution.setStatus(dto.getStatus() != null ? dto.getStatus() : 0);
        execution.setReminderFlag(0);
        execution.setIsOverdue(0);

        executionMapper.insert(execution);
        log.info("创建处分执行记录: id={}, caseId={}, type={}", execution.getId(), execution.getCaseId(), execution.getPunishmentType());
        return execution;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(PunishmentExecutionDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("ID不能为空");
        }
        PunishmentExecution existing = executionMapper.selectById(dto.getId());
        if (existing == null) {
            throw new BusinessException("处分执行记录不存在");
        }

        PunishmentExecution execution = new PunishmentExecution();
        BeanUtils.copyProperties(dto, execution);
        executionMapper.updateById(execution);
        log.info("更新处分执行记录: id={}", dto.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        PunishmentExecution existing = executionMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("处分执行记录不存在");
        }

        // 级联删除关联材料
        LambdaQueryWrapper<PunishmentMaterial> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PunishmentMaterial::getExecutionId, id);
        materialMapper.delete(wrapper);

        executionMapper.deleteById(id);
        log.info("删除处分执行记录: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(Long id, Integer status) {
        if (id == null) {
            throw new BusinessException("ID不能为空");
        }
        if (status == null || status < 0 || status > 3) {
            throw new BusinessException("无效的状态值");
        }

        PunishmentExecution execution = executionMapper.selectById(id);
        if (execution == null) {
            throw new BusinessException("处分执行记录不存在");
        }

        execution.setStatus(status);
        // 状态变更为已完成时，设置结束日期（如果没有）
        if (status == 2 && execution.getEndDate() == null) {
            execution.setEndDate(new Date());
        }
        executionMapper.updateById(execution);
        log.info("变更处分执行状态: id={}, newStatus={}", id, status);
    }

    @Override
    public List<PunishmentExecutionVO> getByCaseId(String caseId) {
        List<PunishmentExecution> executions = executionMapper.selectByCaseId(caseId);
        List<PunishmentExecutionVO> voList = new java.util.ArrayList<>();
        for (PunishmentExecution entity : executions) {
            voList.add(convertToVO(entity));
        }
        return voList;
    }

    @Override
    public List<PunishmentExecutionVO> getOverdueExecutions() {
        List<PunishmentExecution> executions = executionMapper.selectOverdueExecutions();
        List<PunishmentExecutionVO> voList = new java.util.ArrayList<>();
        for (PunishmentExecution entity : executions) {
            voList.add(convertToVO(entity));
        }
        return voList;
    }

    @Override
    public List<Map<String, Object>> countByStatus() {
        return executionMapper.countByStatus();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PunishmentMaterial uploadMaterial(Long executionId, String materialType, String filePath, Long uploaderId) {
        PunishmentExecution execution = executionMapper.selectById(executionId);
        if (execution == null) {
            throw new BusinessException("处分执行记录不存在");
        }

        PunishmentMaterial material = new PunishmentMaterial();
        material.setExecutionId(executionId);
        material.setMaterialType(materialType);
        material.setFilePath(filePath);
        material.setUploadTime(new Date());
        material.setUploaderId(uploaderId != null ? uploaderId : UserContext.getUserId());

        materialMapper.insert(material);
        log.info("上传处分材料: executionId={}, type={}, path={}", executionId, materialType, filePath);
        return material;
    }

    @Override
    public List<PunishmentMaterial> getMaterials(Long executionId) {
        return materialMapper.selectByExecutionId(executionId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMaterial(Long materialId) {
        PunishmentMaterial material = materialMapper.selectById(materialId);
        if (material == null) {
            throw new BusinessException("材料不存在");
        }
        materialMapper.deleteById(materialId);
        log.info("删除处分材料: id={}", materialId);
    }

    /**
     * Entity → VO 转换
     */
    private PunishmentExecutionVO convertToVO(PunishmentExecution entity) {
        PunishmentExecutionVO vo = new PunishmentExecutionVO();
        BeanUtils.copyProperties(entity, vo);
        vo.setStatusText(STATUS_MAP.getOrDefault(entity.getStatus(), "未知"));

        // 检查逾期状态（如果结束日期已过且状态不是已完成或已撤销）
        if (entity.getEndDate() != null && entity.getEndDate().before(new Date())
                && entity.getStatus() != null && entity.getStatus() != 2 && entity.getStatus() != 3) {
            vo.setIsOverdue(1);
            // 同步更新数据库
            if (entity.getIsOverdue() == null || entity.getIsOverdue() == 0) {
                entity.setIsOverdue(1);
                executionMapper.updateById(entity);
            }
        }
        return vo;
    }
}
