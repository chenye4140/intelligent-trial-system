package com.intelligent.trial.punishment.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.intelligent.trial.punishment.dto.PunishmentExecutionDTO;
import com.intelligent.trial.punishment.dto.PunishmentSearchDTO;
import com.intelligent.trial.punishment.entity.PunishmentExecution;
import com.intelligent.trial.punishment.entity.PunishmentMaterial;
import com.intelligent.trial.punishment.vo.PunishmentExecutionVO;

import java.util.List;
import java.util.Map;

/**
 * 处分执行 Service 接口
 */
public interface IPunishmentExecutionService {

    /**
     * 分页查询处分执行记录
     */
    Page<PunishmentExecutionVO> pageQuery(int pageNum, int pageSize, PunishmentSearchDTO searchDTO);

    /**
     * 根据ID获取详情（含材料）
     */
    PunishmentExecutionVO getDetail(Long id);

    /**
     * 创建处分执行记录
     */
    PunishmentExecution create(PunishmentExecutionDTO dto);

    /**
     * 更新处分执行记录
     */
    void update(PunishmentExecutionDTO dto);

    /**
     * 删除处分执行记录（级联删除材料）
     */
    void delete(Long id);

    /**
     * 变更状态
     */
    void changeStatus(Long id, Integer status);

    /**
     * 按案件ID查询
     */
    List<PunishmentExecutionVO> getByCaseId(String caseId);

    /**
     * 查询逾期记录
     */
    List<PunishmentExecutionVO> getOverdueExecutions();

    /**
     * 统计各状态数量
     */
    List<Map<String, Object>> countByStatus();

    // ==================== 材料管理 ====================

    /**
     * 上传材料
     */
    PunishmentMaterial uploadMaterial(Long executionId, String materialType, String filePath, Long uploaderId);

    /**
     * 获取材料列表
     */
    List<PunishmentMaterial> getMaterials(Long executionId);

    /**
     * 删除材料
     */
    void deleteMaterial(Long materialId);
}
