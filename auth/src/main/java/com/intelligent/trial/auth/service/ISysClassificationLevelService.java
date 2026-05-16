package com.intelligent.trial.auth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.intelligent.trial.auth.dto.ClassificationLevelDTO;
import com.intelligent.trial.auth.entity.SysClassificationLevel;
import com.intelligent.trial.auth.vo.ClassificationAccessVO;
import com.intelligent.trial.auth.vo.ClassificationLevelVO;

import java.util.List;

/**
 * 密级管理服务接口
 */
public interface ISysClassificationLevelService extends IService<SysClassificationLevel> {

    /**
     * 查询所有密级列表
     *
     * @return 密级VO列表
     */
    List<ClassificationLevelVO> listAll();

    /**
     * 获取用户可访问的密级
     *
     * @param userId 用户ID
     * @return 可访问密级信息
     */
    ClassificationAccessVO getAccessibleLevels(Long userId);

    /**
     * 新增密级
     *
     * @param dto 密级信息
     */
    void addLevel(ClassificationLevelDTO dto);

    /**
     * 更新密级
     *
     * @param dto 密级信息
     */
    void updateLevel(ClassificationLevelDTO dto);

    /**
     * 删除密级
     *
     * @param id 密级ID
     */
    void deleteLevel(Long id);
}
