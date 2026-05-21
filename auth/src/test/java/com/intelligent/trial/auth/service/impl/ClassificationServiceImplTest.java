package com.intelligent.trial.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.intelligent.trial.auth.dto.ClassificationLevelDTO;
import com.intelligent.trial.auth.entity.SysClassificationLevel;
import com.intelligent.trial.auth.entity.SysUser;
import com.intelligent.trial.auth.mapper.SysClassificationLevelMapper;
import com.intelligent.trial.auth.mapper.SysUserMapper;
import com.intelligent.trial.auth.vo.ClassificationAccessVO;
import com.intelligent.trial.auth.vo.ClassificationLevelVO;
import com.intelligent.trial.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ClassificationServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
class ClassificationServiceImplTest {

    @InjectMocks
    private ClassificationServiceImpl classificationService;

    @Mock
    private SysClassificationLevelMapper baseMapper;

    @Mock
    private SysUserMapper userMapper;

    @Test
    void listAll_shouldReturnAllActiveLevels() {
        List<ClassificationLevelVO> expectedLevels = Arrays.asList(
            createLevelVO(1L, "绝密", "TOP_SECRET", 1),
            createLevelVO(2L, "机密", "SECRET", 2),
            createLevelVO(3L, "秘密", "CONFIDENTIAL", 3)
        );

        when(baseMapper.selectAllActiveLevels()).thenReturn(expectedLevels);

        List<ClassificationLevelVO> result = classificationService.listAll();

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("绝密", result.get(0).getLevelName());
    }

    @Test
    void listAll_shouldReturnEmptyWhenNoLevels() {
        when(baseMapper.selectAllActiveLevels()).thenReturn(Collections.emptyList());

        List<ClassificationLevelVO> result = classificationService.listAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getAccessibleLevels_shouldReturnLevelsForValidUser() {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("zhangsan");

        when(userMapper.selectById(1L)).thenReturn(user);

        SysClassificationLevel level1 = new SysClassificationLevel();
        level1.setId(1L);
        level1.setLevelCode("TOP_SECRET");
        level1.setLevelName("绝密");
        level1.setSort(1);
        level1.setStatus(1);

        SysClassificationLevel level2 = new SysClassificationLevel();
        level2.setId(2L);
        level2.setLevelCode("SECRET");
        level2.setLevelName("机密");
        level2.setSort(2);
        level2.setStatus(1);

        LambdaQueryWrapper<SysClassificationLevel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysClassificationLevel::getStatus, 1);
        wrapper.orderByAsc(SysClassificationLevel::getSort);

        when(baseMapper.selectList(any(LambdaQueryWrapper.class)))
            .thenReturn(Arrays.asList(level1, level2));

        ClassificationAccessVO result = classificationService.getAccessibleLevels(1L);

        assertNotNull(result);
        assertEquals(Long.valueOf(1L), result.getUserId());
        assertEquals(2, result.getAccessibleLevels().size());
        assertEquals(2, result.getMaxLevelSort());
        assertEquals("绝密", result.getAccessibleLevels().get(0).getLevelName());
    }

    @Test
    void getAccessibleLevels_shouldThrowWhenUserNotFound() {
        when(userMapper.selectById(999L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
            () -> classificationService.getAccessibleLevels(999L));
        assertTrue(ex.getMessage().contains("用户不存在"));
    }

    @Test
    void getAccessibleLevels_shouldHandleEmptyLevels() {
        SysUser user = new SysUser();
        user.setId(1L);

        when(userMapper.selectById(1L)).thenReturn(user);
        when(baseMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        ClassificationAccessVO result = classificationService.getAccessibleLevels(1L);

        assertNotNull(result);
        assertEquals(0, result.getMaxLevelSort());
        assertTrue(result.getAccessibleLevels().isEmpty());
    }

    @Test
    void addLevel_shouldCreateClassificationLevel() {
        ClassificationLevelDTO dto = new ClassificationLevelDTO();
        dto.setLevelCode("INTERNAL");
        dto.setLevelName("内部");
        dto.setSort(4);

        when(baseMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(baseMapper.insert(any(SysClassificationLevel.class))).thenReturn(1);

        classificationService.addLevel(dto);

        verify(baseMapper).insert(argThat((SysClassificationLevel level) ->
            "INTERNAL".equals(level.getLevelCode())
                && "内部".equals(level.getLevelName())
                && Integer.valueOf(1).equals(level.getStatus())
                && level.getCreateTime() != null));
    }

    @Test
    void addLevel_shouldThrowWhenCodeExists() {
        ClassificationLevelDTO dto = new ClassificationLevelDTO();
        dto.setLevelCode("TOP_SECRET");
        dto.setLevelName("绝密");

        when(baseMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        BusinessException ex = assertThrows(BusinessException.class, () -> classificationService.addLevel(dto));
        assertTrue(ex.getMessage().contains("密级编码已存在"));
        verify(baseMapper, never()).insert(any());
    }

    @Test
    void addLevel_shouldUseProvidedStatus() {
        ClassificationLevelDTO dto = new ClassificationLevelDTO();
        dto.setLevelCode("PUBLIC");
        dto.setLevelName("公开");
        dto.setSort(5);
        dto.setStatus(0);

        when(baseMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(baseMapper.insert(any(SysClassificationLevel.class))).thenReturn(1);

        classificationService.addLevel(dto);

        verify(baseMapper).insert(argThat((SysClassificationLevel level) ->
            Integer.valueOf(0).equals(level.getStatus())));
    }

    @Test
    void updateLevel_shouldUpdateClassificationLevel() {
        ClassificationLevelDTO dto = new ClassificationLevelDTO();
        dto.setId(1L);
        dto.setLevelCode("SECRET_V2");
        dto.setLevelName("机密V2");

        SysClassificationLevel existing = new SysClassificationLevel();
        existing.setId(1L);
        existing.setLevelCode("SECRET");
        existing.setLevelName("机密");

        when(baseMapper.selectById(1L)).thenReturn(existing);
        when(baseMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(baseMapper.updateById(any(SysClassificationLevel.class))).thenReturn(1);

        classificationService.updateLevel(dto);

        verify(baseMapper).updateById(argThat((SysClassificationLevel level) ->
            Long.valueOf(1L).equals(level.getId())
                && "SECRET_V2".equals(level.getLevelCode())
                && level.getUpdateTime() != null));
    }

    @Test
    void updateLevel_shouldThrowWhenLevelIdNull() {
        ClassificationLevelDTO dto = new ClassificationLevelDTO();
        dto.setLevelCode("NO_ID");

        BusinessException ex = assertThrows(BusinessException.class, () -> classificationService.updateLevel(dto));
        assertTrue(ex.getMessage().contains("密级ID不能为空"));
    }

    @Test
    void updateLevel_shouldThrowWhenLevelNotFound() {
        ClassificationLevelDTO dto = new ClassificationLevelDTO();
        dto.setId(999L);
        dto.setLevelCode("GHOST");

        when(baseMapper.selectById(999L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> classificationService.updateLevel(dto));
        assertTrue(ex.getMessage().contains("密级不存在"));
    }

    @Test
    void updateLevel_shouldThrowWhenCodeExistsForOtherLevel() {
        ClassificationLevelDTO dto = new ClassificationLevelDTO();
        dto.setId(1L);
        dto.setLevelCode("SECRET");
        dto.setLevelName("机密改名");

        SysClassificationLevel existing = new SysClassificationLevel();
        existing.setId(1L);
        existing.setLevelCode("TOP_SECRET");

        when(baseMapper.selectById(1L)).thenReturn(existing);
        // Another level already uses "SECRET" code
        when(baseMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        BusinessException ex = assertThrows(BusinessException.class, () -> classificationService.updateLevel(dto));
        assertTrue(ex.getMessage().contains("密级编码已存在"));
        verify(baseMapper, never()).updateById(any());
    }

    @Test
    void deleteLevel_shouldSucceedForNormalLevel() {
        SysClassificationLevel level = new SysClassificationLevel();
        level.setId(5L);
        level.setLevelName("内部");

        when(baseMapper.selectById(5L)).thenReturn(level);
        when(baseMapper.deleteById(5L)).thenReturn(1);

        classificationService.deleteLevel(5L);

        verify(baseMapper).deleteById(5L);
    }

    @Test
    void deleteLevel_shouldFailWhenNotFound() {
        when(baseMapper.selectById(999L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> classificationService.deleteLevel(999L));
        assertTrue(ex.getMessage().contains("密级不存在"));
    }

    private ClassificationLevelVO createLevelVO(Long id, String name, String code, int sort) {
        ClassificationLevelVO vo = new ClassificationLevelVO();
        vo.setId(id);
        vo.setLevelName(name);
        vo.setLevelCode(code);
        vo.setSort(sort);
        return vo;
    }
}
