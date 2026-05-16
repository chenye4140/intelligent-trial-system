package com.intelligent.trial.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.intelligent.trial.auth.dto.ClassificationLevelDTO;
import com.intelligent.trial.auth.entity.SysClassificationLevel;
import com.intelligent.trial.auth.entity.SysUser;
import com.intelligent.trial.auth.mapper.SysClassificationLevelMapper;
import com.intelligent.trial.auth.mapper.SysUserMapper;
import com.intelligent.trial.auth.service.ISysClassificationLevelService;
import com.intelligent.trial.auth.vo.ClassificationAccessVO;
import com.intelligent.trial.auth.vo.ClassificationLevelVO;
import com.intelligent.trial.common.exception.BusinessException;
import com.intelligent.trial.common.exception.ErrorCode;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 密级管理服务实现类
 */
@Service
public class ClassificationServiceImpl extends ServiceImpl<SysClassificationLevelMapper, SysClassificationLevel> implements ISysClassificationLevelService {

    @Autowired
    private SysUserMapper userMapper;

    @Override
    public List<ClassificationLevelVO> listAll() {
        return baseMapper.selectAllActiveLevels();
    }

    @Override
    public ClassificationAccessVO getAccessibleLevels(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "用户不存在");
        }

        // 这里简化处理：假设用户可访问所有正常状态的密级
        // 实际应该根据用户的密级权限来判断
        LambdaQueryWrapper<SysClassificationLevel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysClassificationLevel::getStatus, 1);
        wrapper.orderByAsc(SysClassificationLevel::getSort);
        List<SysClassificationLevel> levels = baseMapper.selectList(wrapper);

        ClassificationAccessVO vo = new ClassificationAccessVO();
        vo.setUserId(userId);
        vo.setMaxLevelSort(levels.isEmpty() ? 0 : levels.get(levels.size() - 1).getSort());

        List<ClassificationAccessVO.ClassificationLevelInfo> levelInfos = levels.stream()
                .map(level -> {
                    ClassificationAccessVO.ClassificationLevelInfo info = new ClassificationAccessVO.ClassificationLevelInfo();
                    info.setId(level.getId());
                    info.setLevelCode(level.getLevelCode());
                    info.setLevelName(level.getLevelName());
                    info.setSort(level.getSort());
                    return info;
                })
                .collect(Collectors.toList());
        vo.setAccessibleLevels(levelInfos);

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addLevel(ClassificationLevelDTO dto) {
        // 检查密级编码是否已存在
        LambdaQueryWrapper<SysClassificationLevel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysClassificationLevel::getLevelCode, dto.getLevelCode());
        if (baseMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR.getCode(), "密级编码已存在");
        }

        SysClassificationLevel level = new SysClassificationLevel();
        BeanUtils.copyProperties(dto, level);
        level.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        level.setCreateTime(new Date());
        level.setUpdateTime(new Date());
        baseMapper.insert(level);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLevel(ClassificationLevelDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "密级ID不能为空");
        }
        SysClassificationLevel existLevel = baseMapper.selectById(dto.getId());
        if (existLevel == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "密级不存在");
        }

        // 检查密级编码是否被其他密级使用
        LambdaQueryWrapper<SysClassificationLevel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysClassificationLevel::getLevelCode, dto.getLevelCode());
        wrapper.ne(SysClassificationLevel::getId, dto.getId());
        if (baseMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR.getCode(), "密级编码已存在");
        }

        SysClassificationLevel level = new SysClassificationLevel();
        BeanUtils.copyProperties(dto, level);
        level.setUpdateTime(new Date());
        baseMapper.updateById(level);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteLevel(Long id) {
        SysClassificationLevel existLevel = baseMapper.selectById(id);
        if (existLevel == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "密级不存在");
        }
        baseMapper.deleteById(id);
    }
}
