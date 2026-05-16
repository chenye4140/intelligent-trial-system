package com.intelligent.trial.punishment.service.impl;

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
import com.intelligent.trial.punishment.vo.PunishmentExecutionVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * PunishmentExecutionServiceImpl 单元测试
 * 覆盖 CRUD、状态变更、逾期检测、材料管理等核心方法及异常路径
 */
@ExtendWith(MockitoExtension.class)
class PunishmentExecutionServiceImplTest {

    @Mock
    private PunishmentExecutionMapper executionMapper;

    @Mock
    private PunishmentMaterialMapper materialMapper;

    @InjectMocks
    private PunishmentExecutionServiceImpl service;

    @BeforeEach
    void setUp() {
        UserContext.clear();
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    // ======================== 辅助方法 ========================

    private PunishmentExecution createExecution(Long id, String caseId, String type, Integer status, Date endDate) {
        PunishmentExecution execution = new PunishmentExecution();
        execution.setId(id);
        execution.setCaseId(caseId);
        execution.setPunishmentType(type);
        execution.setStatus(status);
        execution.setEndDate(endDate);
        execution.setReminderFlag(0);
        execution.setIsOverdue(0);
        execution.setCreateTime(new Date());
        execution.setUpdateTime(new Date());
        return execution;
    }

    private PunishmentExecutionDTO createDTO(String caseId, String type) {
        PunishmentExecutionDTO dto = new PunishmentExecutionDTO();
        dto.setCaseId(caseId);
        dto.setPunishmentType(type);
        return dto;
    }

    // ======================== pageQuery 测试 ========================

    @Nested
    @DisplayName("pageQuery - 分页查询")
    class PageQueryTests {

        @Test
        @DisplayName("正常分页查询 - 无搜索条件")
        void testPageQuery_NoConditions() {
            // Arrange
            PunishmentExecution exec = createExecution(1L, "CASE001", "警告", 0, null);
            Page<PunishmentExecution> entityPage = new Page<>(1, 10, 1);
            entityPage.setRecords(Collections.singletonList(exec));

            when(executionMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                    .thenReturn(entityPage);

            // Act
            Page<PunishmentExecutionVO> result = service.pageQuery(1, 10, null);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getTotal());
            assertEquals(1, result.getRecords().size());
            assertEquals("CASE001", result.getRecords().get(0).getCaseId());
            assertEquals("警告", result.getRecords().get(0).getPunishmentType());
            assertEquals("待执行", result.getRecords().get(0).getStatusText());
        }

        @Test
        @DisplayName("分页查询 - 带搜索条件（caseId + status）")
        void testPageQuery_WithSearchConditions() {
            // Arrange
            PunishmentSearchDTO searchDTO = new PunishmentSearchDTO();
            searchDTO.setCaseId("CASE001");
            searchDTO.setStatus(1);

            PunishmentExecution exec = createExecution(1L, "CASE001", "记过", 1, null);
            Page<PunishmentExecution> entityPage = new Page<>(1, 10, 1);
            entityPage.setRecords(Collections.singletonList(exec));

            when(executionMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                    .thenReturn(entityPage);

            // Act
            Page<PunishmentExecutionVO> result = service.pageQuery(1, 10, searchDTO);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getTotal());
            assertEquals(1, result.getRecords().get(0).getId());
        }

        @Test
        @DisplayName("分页查询 - 带所有搜索条件")
        void testPageQuery_AllSearchConditions() {
            // Arrange
            PunishmentSearchDTO searchDTO = new PunishmentSearchDTO();
            searchDTO.setCaseId("CASE001");
            searchDTO.setPunishmentType("降级");
            searchDTO.setStatus(2);
            searchDTO.setIsOverdue(1);

            Page<PunishmentExecution> entityPage = new Page<>(1, 10, 0);
            entityPage.setRecords(Collections.emptyList());

            when(executionMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                    .thenReturn(entityPage);

            // Act
            Page<PunishmentExecutionVO> result = service.pageQuery(1, 10, searchDTO);

            // Assert
            assertNotNull(result);
            assertEquals(0, result.getTotal());
            assertTrue(result.getRecords().isEmpty());
        }

        @Test
        @DisplayName("分页查询 - 空结果")
        void testPageQuery_EmptyResult() {
            // Arrange
            Page<PunishmentExecution> entityPage = new Page<>(1, 10, 0);
            entityPage.setRecords(Collections.emptyList());

            when(executionMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                    .thenReturn(entityPage);

            // Act
            Page<PunishmentExecutionVO> result = service.pageQuery(2, 20, new PunishmentSearchDTO());

            // Assert
            assertNotNull(result);
            assertEquals(0, result.getTotal());
            assertEquals(0, result.getRecords().size());
        }

        @Test
        @DisplayName("分页查询 - 状态文本映射正确")
        void testPageQuery_StatusTextMapping() {
            // Arrange
            List<PunishmentExecution> records = new ArrayList<>();
            records.add(createExecution(1L, "C1", "警告", 0, null));
            records.add(createExecution(2L, "C2", "记过", 1, null));
            records.add(createExecution(3L, "C3", "降级", 2, null));
            records.add(createExecution(4L, "C4", "撤职", 3, null));

            Page<PunishmentExecution> entityPage = new Page<>(1, 10, 4);
            entityPage.setRecords(records);

            when(executionMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                    .thenReturn(entityPage);

            // Act
            Page<PunishmentExecutionVO> result = service.pageQuery(1, 10, null);

            // Assert
            List<PunishmentExecutionVO> voList = result.getRecords();
            assertEquals("待执行", voList.get(0).getStatusText());
            assertEquals("执行中", voList.get(1).getStatusText());
            assertEquals("已完成", voList.get(2).getStatusText());
            assertEquals("已撤销", voList.get(3).getStatusText());
        }
    }

    // ======================== getDetail 测试 ========================

    @Nested
    @DisplayName("getDetail - 获取详情")
    class GetDetailTests {

        @Test
        @DisplayName("正常获取详情 - 含材料")
        void testGetDetail_Success() {
            // Arrange
            PunishmentExecution exec = createExecution(1L, "CASE001", "警告", 0, null);
            when(executionMapper.selectById(1L)).thenReturn(exec);

            List<PunishmentMaterial> materials = new ArrayList<>();
            PunishmentMaterial mat = new PunishmentMaterial();
            mat.setId(10L);
            mat.setExecutionId(1L);
            mat.setMaterialType("决定书");
            materials.add(mat);
            when(materialMapper.selectByExecutionId(1L)).thenReturn(materials);

            // Act
            PunishmentExecutionVO vo = service.getDetail(1L);

            // Assert
            assertNotNull(vo);
            assertEquals(1L, vo.getId());
            assertEquals("CASE001", vo.getCaseId());
            assertEquals("警告", vo.getPunishmentType());
            assertNotNull(vo.getMaterials());
            assertEquals(1, vo.getMaterials().size());
            assertEquals("决定书", vo.getMaterials().get(0).getMaterialType());
        }

        @Test
        @DisplayName("获取详情 - 记录不存在抛出异常")
        void testGetDetail_NotFound() {
            // Arrange
            when(executionMapper.selectById(999L)).thenReturn(null);

            // Act & Assert
            BusinessException ex = assertThrows(BusinessException.class, () -> service.getDetail(999L));
            assertEquals("处分执行记录不存在", ex.getMessage());
        }

        @Test
        @DisplayName("获取详情 - 无关联材料")
        void testGetDetail_NoMaterials() {
            // Arrange
            PunishmentExecution exec = createExecution(1L, "CASE001", "开除", 2, new Date());
            when(executionMapper.selectById(1L)).thenReturn(exec);
            when(materialMapper.selectByExecutionId(1L)).thenReturn(Collections.emptyList());

            // Act
            PunishmentExecutionVO vo = service.getDetail(1L);

            // Assert
            assertNotNull(vo);
            assertNotNull(vo.getMaterials());
            assertTrue(vo.getMaterials().isEmpty());
        }
    }

    // ======================== create 测试 ========================

    @Nested
    @DisplayName("create - 创建处分执行记录")
    class CreateTests {

        @Test
        @DisplayName("正常创建 - 默认状态")
        void testCreate_DefaultStatus() {
            // Arrange
            PunishmentExecutionDTO dto = createDTO("CASE001", "警告");
            when(executionMapper.insert(any(PunishmentExecution.class))).thenAnswer(invocation -> {
                PunishmentExecution exec = invocation.getArgument(0);
                exec.setId(100L);
                return 1;
            });

            // Act
            PunishmentExecution result = service.create(dto);

            // Assert
            assertNotNull(result);
            assertEquals(100L, result.getId());
            assertEquals("CASE001", result.getCaseId());
            assertEquals("警告", result.getPunishmentType());
            assertEquals(0, result.getStatus());
            assertEquals(0, result.getReminderFlag());
            assertEquals(0, result.getIsOverdue());
            verify(executionMapper).insert(any(PunishmentExecution.class));
        }

        @Test
        @DisplayName("正常创建 - 指定状态")
        void testCreate_WithStatus() {
            // Arrange
            PunishmentExecutionDTO dto = createDTO("CASE001", "记过");
            dto.setStatus(1);
            when(executionMapper.insert(any(PunishmentExecution.class))).thenAnswer(invocation -> {
                PunishmentExecution exec = invocation.getArgument(0);
                exec.setId(101L);
                return 1;
            });

            // Act
            PunishmentExecution result = service.create(dto);

            // Assert
            assertEquals(1, result.getStatus());
        }

        @Test
        @DisplayName("创建失败 - caseId为空")
        void testCreate_EmptyCaseId() {
            // Arrange
            PunishmentExecutionDTO dto = createDTO(null, "警告");

            // Act & Assert
            BusinessException ex = assertThrows(BusinessException.class, () -> service.create(dto));
            assertEquals("案件ID不能为空", ex.getMessage());
        }

        @Test
        @DisplayName("创建失败 - caseId为空字符串")
        void testCreate_BlankCaseId() {
            // Arrange
            PunishmentExecutionDTO dto = createDTO("", "警告");

            // Act & Assert
            BusinessException ex = assertThrows(BusinessException.class, () -> service.create(dto));
            assertEquals("案件ID不能为空", ex.getMessage());
        }

        @Test
        @DisplayName("创建失败 - punishmentType为空")
        void testCreate_NullPunishmentType() {
            // Arrange
            PunishmentExecutionDTO dto = createDTO("CASE001", null);

            // Act & Assert
            BusinessException ex = assertThrows(BusinessException.class, () -> service.create(dto));
            assertEquals("处分类型不能为空", ex.getMessage());
        }

        @Test
        @DisplayName("创建失败 - punishmentType为空字符串")
        void testCreate_BlankPunishmentType() {
            // Arrange
            PunishmentExecutionDTO dto = createDTO("CASE001", "");

            // Act & Assert
            BusinessException ex = assertThrows(BusinessException.class, () -> service.create(dto));
            assertEquals("处分类型不能为空", ex.getMessage());
        }
    }

    // ======================== update 测试 ========================

    @Nested
    @DisplayName("update - 更新处分执行记录")
    class UpdateTests {

        @Test
        @DisplayName("正常更新")
        void testUpdate_Success() {
            // Arrange
            PunishmentExecutionDTO dto = new PunishmentExecutionDTO();
            dto.setId(1L);
            dto.setCaseId("CASE001");
            dto.setPunishmentType("降级");
            dto.setStatus(1);

            PunishmentExecution existing = createExecution(1L, "CASE001", "警告", 0, null);
            when(executionMapper.selectById(1L)).thenReturn(existing);
            when(executionMapper.updateById(any(PunishmentExecution.class))).thenReturn(1);

            // Act
            service.update(dto);

            // Assert
            verify(executionMapper).selectById(1L);
            verify(executionMapper).updateById(any(PunishmentExecution.class));
        }

        @Test
        @DisplayName("更新失败 - id为空")
        void testUpdate_NullId() {
            // Arrange
            PunishmentExecutionDTO dto = new PunishmentExecutionDTO();
            dto.setId(null);
            dto.setCaseId("CASE001");

            // Act & Assert
            BusinessException ex = assertThrows(BusinessException.class, () -> service.update(dto));
            assertEquals("ID不能为空", ex.getMessage());
        }

        @Test
        @DisplayName("更新失败 - 记录不存在")
        void testUpdate_NotFound() {
            // Arrange
            PunishmentExecutionDTO dto = new PunishmentExecutionDTO();
            dto.setId(999L);
            dto.setCaseId("CASE001");

            when(executionMapper.selectById(999L)).thenReturn(null);

            // Act & Assert
            BusinessException ex = assertThrows(BusinessException.class, () -> service.update(dto));
            assertEquals("处分执行记录不存在", ex.getMessage());
            verify(executionMapper, never()).updateById(any());
        }
    }

    // ======================== delete 测试 ========================

    @Nested
    @DisplayName("delete - 删除处分执行记录")
    class DeleteTests {

        @Test
        @DisplayName("正常删除 - 级联删除材料")
        void testDelete_Success() {
            // Arrange
            PunishmentExecution existing = createExecution(1L, "CASE001", "警告", 0, null);
            when(executionMapper.selectById(1L)).thenReturn(existing);
            when(materialMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(2);
            when(executionMapper.deleteById(1L)).thenReturn(1);

            // Act
            service.delete(1L);

            // Assert
            verify(executionMapper).selectById(1L);
            verify(materialMapper).delete(any(LambdaQueryWrapper.class));
            verify(executionMapper).deleteById(1L);
        }

        @Test
        @DisplayName("删除失败 - 记录不存在")
        void testDelete_NotFound() {
            // Arrange
            when(executionMapper.selectById(999L)).thenReturn(null);

            // Act & Assert
            BusinessException ex = assertThrows(BusinessException.class, () -> service.delete(999L));
            assertEquals("处分执行记录不存在", ex.getMessage());
            verify(executionMapper, never()).deleteById(any());
            verify(materialMapper, never()).delete(any());
        }
    }

    // ======================== changeStatus 测试 ========================

    @Nested
    @DisplayName("changeStatus - 状态变更")
    class ChangeStatusTests {

        @Test
        @DisplayName("正常变更状态 - 待执行 -> 执行中")
        void testChangeStatus_ToExecuting() {
            // Arrange
            PunishmentExecution exec = createExecution(1L, "CASE001", "警告", 0, null);
            when(executionMapper.selectById(1L)).thenReturn(exec);
            when(executionMapper.updateById(any(PunishmentExecution.class))).thenReturn(1);

            // Act
            service.changeStatus(1L, 1);

            // Assert
            ArgumentCaptor<PunishmentExecution> captor = ArgumentCaptor.forClass(PunishmentExecution.class);
            verify(executionMapper).updateById(captor.capture());
            assertEquals(1, captor.getValue().getStatus());
        }

        @Test
        @DisplayName("变更状态为已完成 - 自动设置结束日期")
        void testChangeStatus_ToCompleted_SetsEndDate() {
            // Arrange
            PunishmentExecution exec = createExecution(1L, "CASE001", "警告", 1, null);
            when(executionMapper.selectById(1L)).thenReturn(exec);
            when(executionMapper.updateById(any(PunishmentExecution.class))).thenReturn(1);

            // Act
            service.changeStatus(1L, 2);

            // Assert
            ArgumentCaptor<PunishmentExecution> captor = ArgumentCaptor.forClass(PunishmentExecution.class);
            verify(executionMapper).updateById(captor.capture());
            assertEquals(2, captor.getValue().getStatus());
            assertNotNull(captor.getValue().getEndDate());
        }

        @Test
        @DisplayName("变更状态为已完成 - 已有结束日期不覆盖")
        void testChangeStatus_ToCompleted_ExistingEndDate() {
            // Arrange
            Date existingEnd = new Date(System.currentTimeMillis() - 86400000);
            PunishmentExecution exec = createExecution(1L, "CASE001", "警告", 1, existingEnd);
            when(executionMapper.selectById(1L)).thenReturn(exec);
            when(executionMapper.updateById(any(PunishmentExecution.class))).thenReturn(1);

            // Act
            service.changeStatus(1L, 2);

            // Assert
            ArgumentCaptor<PunishmentExecution> captor = ArgumentCaptor.forClass(PunishmentExecution.class);
            verify(executionMapper).updateById(captor.capture());
            assertEquals(existingEnd, captor.getValue().getEndDate());
        }

        @Test
        @DisplayName("变更状态失败 - id为空")
        void testChangeStatus_NullId() {
            // Act & Assert
            BusinessException ex = assertThrows(BusinessException.class, () -> service.changeStatus(null, 1));
            assertEquals("ID不能为空", ex.getMessage());
        }

        @Test
        @DisplayName("变更状态失败 - status为null")
        void testChangeStatus_NullStatus() {
            // Act & Assert
            BusinessException ex = assertThrows(BusinessException.class, () -> service.changeStatus(1L, null));
            assertEquals("无效的状态值", ex.getMessage());
        }

        @Test
        @DisplayName("变更状态失败 - status超出范围（负数）")
        void testChangeStatus_NegativeStatus() {
            // Act & Assert
            BusinessException ex = assertThrows(BusinessException.class, () -> service.changeStatus(1L, -1));
            assertEquals("无效的状态值", ex.getMessage());
        }

        @Test
        @DisplayName("变更状态失败 - status超出范围（大于3）")
        void testChangeStatus_OutOfRangeStatus() {
            // Act & Assert
            BusinessException ex = assertThrows(BusinessException.class, () -> service.changeStatus(1L, 5));
            assertEquals("无效的状态值", ex.getMessage());
        }

        @Test
        @DisplayName("变更状态失败 - 记录不存在")
        void testChangeStatus_RecordNotFound() {
            // Arrange
            when(executionMapper.selectById(999L)).thenReturn(null);

            // Act & Assert
            BusinessException ex = assertThrows(BusinessException.class, () -> service.changeStatus(999L, 1));
            assertEquals("处分执行记录不存在", ex.getMessage());
        }

        @Test
        @DisplayName("边界状态值 - status=0（待执行）有效")
        void testChangeStatus_StatusZeroValid() {
            // Arrange
            PunishmentExecution exec = createExecution(1L, "CASE001", "警告", 1, null);
            when(executionMapper.selectById(1L)).thenReturn(exec);
            when(executionMapper.updateById(any(PunishmentExecution.class))).thenReturn(1);

            // Act
            service.changeStatus(1L, 0);

            // Assert
            ArgumentCaptor<PunishmentExecution> captor = ArgumentCaptor.forClass(PunishmentExecution.class);
            verify(executionMapper).updateById(captor.capture());
            assertEquals(0, captor.getValue().getStatus());
        }

        @Test
        @DisplayName("边界状态值 - status=3（已撤销）有效")
        void testChangeStatus_StatusThreeValid() {
            // Arrange
            PunishmentExecution exec = createExecution(1L, "CASE001", "警告", 1, null);
            when(executionMapper.selectById(1L)).thenReturn(exec);
            when(executionMapper.updateById(any(PunishmentExecution.class))).thenReturn(1);

            // Act
            service.changeStatus(1L, 3);

            // Assert
            ArgumentCaptor<PunishmentExecution> captor = ArgumentCaptor.forClass(PunishmentExecution.class);
            verify(executionMapper).updateById(captor.capture());
            assertEquals(3, captor.getValue().getStatus());
        }
    }

    // ======================== getByCaseId 测试 ========================

    @Nested
    @DisplayName("getByCaseId - 按案件ID查询")
    class GetByCaseIdTests {

        @Test
        @DisplayName("正常查询 - 多条记录")
        void testGetByCaseId_MultipleRecords() {
            // Arrange
            List<PunishmentExecution> executions = new ArrayList<>();
            executions.add(createExecution(1L, "CASE001", "警告", 0, null));
            executions.add(createExecution(2L, "CASE001", "记过", 1, null));
            when(executionMapper.selectByCaseId("CASE001")).thenReturn(executions);

            // Act
            List<PunishmentExecutionVO> result = service.getByCaseId("CASE001");

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals("警告", result.get(0).getPunishmentType());
            assertEquals("记过", result.get(1).getPunishmentType());
        }

        @Test
        @DisplayName("查询无结果 - 空列表")
        void testGetByCaseId_EmptyList() {
            // Arrange
            when(executionMapper.selectByCaseId("CASE999")).thenReturn(Collections.emptyList());

            // Act
            List<PunishmentExecutionVO> result = service.getByCaseId("CASE999");

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    // ======================== getOverdueExecutions 测试 ========================

    @Nested
    @DisplayName("getOverdueExecutions - 查询逾期记录")
    class GetOverdueExecutionsTests {

        @Test
        @DisplayName("正常查询逾期记录")
        void testGetOverdueExecutions() {
            // Arrange
            List<PunishmentExecution> overdueList = new ArrayList<>();
            overdueList.add(createExecution(1L, "CASE001", "警告", 1, new Date(System.currentTimeMillis() - 86400000)));
            when(executionMapper.selectOverdueExecutions()).thenReturn(overdueList);

            // Act
            List<PunishmentExecutionVO> result = service.getOverdueExecutions();

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("CASE001", result.get(0).getCaseId());
        }

        @Test
        @DisplayName("无逾期记录")
        void testGetOverdueExecutions_None() {
            // Arrange
            when(executionMapper.selectOverdueExecutions()).thenReturn(Collections.emptyList());

            // Act
            List<PunishmentExecutionVO> result = service.getOverdueExecutions();

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    // ======================== countByStatus 测试 ========================

    @Nested
    @DisplayName("countByStatus - 统计各状态数量")
    class CountByStatusTests {

        @Test
        @DisplayName("正常统计各状态数量")
        void testCountByStatus() {
            // Arrange
            List<Map<String, Object>> counts = new ArrayList<>();
            Map<String, Object> map1 = new HashMap<>();
            map1.put("status", 0);
            map1.put("count", 5);
            counts.add(map1);

            Map<String, Object> map2 = new HashMap<>();
            map2.put("status", 1);
            map2.put("count", 3);
            counts.add(map2);

            when(executionMapper.countByStatus()).thenReturn(counts);

            // Act
            List<Map<String, Object>> result = service.countByStatus();

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals(0, result.get(0).get("status"));
            assertEquals(5, result.get(0).get("count"));
            assertEquals(1, result.get(1).get("status"));
            assertEquals(3, result.get(1).get("count"));
        }

        @Test
        @DisplayName("统计结果为空")
        void testCountByStatus_Empty() {
            // Arrange
            when(executionMapper.countByStatus()).thenReturn(Collections.emptyList());

            // Act
            List<Map<String, Object>> result = service.countByStatus();

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    // ======================== uploadMaterial 测试 ========================

    @Nested
    @DisplayName("uploadMaterial - 上传材料")
    class UploadMaterialTests {

        @Test
        @DisplayName("正常上传材料 - 指定上传人")
        void testUploadMaterial_WithUploader() {
            // Arrange
            PunishmentExecution exec = createExecution(1L, "CASE001", "警告", 0, null);
            when(executionMapper.selectById(1L)).thenReturn(exec);
            when(materialMapper.insert(any(PunishmentMaterial.class))).thenAnswer(invocation -> {
                PunishmentMaterial mat = invocation.getArgument(0);
                mat.setId(50L);
                return 1;
            });

            // Act
            PunishmentMaterial result = service.uploadMaterial(1L, "决定书", "/files/decision.pdf", 100L);

            // Assert
            assertNotNull(result);
            assertEquals(50L, result.getId());
            assertEquals(1L, result.getExecutionId());
            assertEquals("决定书", result.getMaterialType());
            assertEquals("/files/decision.pdf", result.getFilePath());
            assertEquals(100L, result.getUploaderId());
            assertNotNull(result.getUploadTime());
        }

        @Test
        @DisplayName("上传材料 - uploaderId为null时从UserContext获取")
        void testUploadMaterial_FromUserContext() {
            // Arrange
            UserContext.setUserId(200L);
            PunishmentExecution exec = createExecution(1L, "CASE001", "警告", 0, null);
            when(executionMapper.selectById(1L)).thenReturn(exec);
            when(materialMapper.insert(any(PunishmentMaterial.class))).thenAnswer(invocation -> {
                PunishmentMaterial mat = invocation.getArgument(0);
                mat.setId(51L);
                return 1;
            });

            // Act
            PunishmentMaterial result = service.uploadMaterial(1L, "送达回证", "/files/receipt.pdf", null);

            // Assert
            assertNotNull(result);
            assertEquals(200L, result.getUploaderId());
        }

        @Test
        @DisplayName("上传材料失败 - 执行记录不存在")
        void testUploadMaterial_ExecutionNotFound() {
            // Arrange
            when(executionMapper.selectById(999L)).thenReturn(null);

            // Act & Assert
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.uploadMaterial(999L, "决定书", "/files/x.pdf", 1L));
            assertEquals("处分执行记录不存在", ex.getMessage());
            verify(materialMapper, never()).insert(any());
        }
    }

    // ======================== getMaterials 测试 ========================

    @Nested
    @DisplayName("getMaterials - 获取材料列表")
    class GetMaterialsTests {

        @Test
        @DisplayName("正常获取材料列表")
        void testGetMaterials() {
            // Arrange
            List<PunishmentMaterial> materials = new ArrayList<>();
            PunishmentMaterial mat1 = new PunishmentMaterial();
            mat1.setId(10L);
            mat1.setExecutionId(1L);
            mat1.setMaterialType("决定书");
            mat1.setFilePath("/files/1.pdf");

            PunishmentMaterial mat2 = new PunishmentMaterial();
            mat2.setId(11L);
            mat2.setExecutionId(1L);
            mat2.setMaterialType("执行报告");
            mat2.setFilePath("/files/2.pdf");

            materials.add(mat1);
            materials.add(mat2);

            when(materialMapper.selectByExecutionId(1L)).thenReturn(materials);

            // Act
            List<PunishmentMaterial> result = service.getMaterials(1L);

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals("决定书", result.get(0).getMaterialType());
            assertEquals("执行报告", result.get(1).getMaterialType());
        }

        @Test
        @DisplayName("获取空材料列表")
        void testGetMaterials_Empty() {
            // Arrange
            when(materialMapper.selectByExecutionId(1L)).thenReturn(Collections.emptyList());

            // Act
            List<PunishmentMaterial> result = service.getMaterials(1L);

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    // ======================== deleteMaterial 测试 ========================

    @Nested
    @DisplayName("deleteMaterial - 删除材料")
    class DeleteMaterialTests {

        @Test
        @DisplayName("正常删除材料")
        void testDeleteMaterial_Success() {
            // Arrange
            PunishmentMaterial material = new PunishmentMaterial();
            material.setId(10L);
            material.setExecutionId(1L);
            material.setMaterialType("决定书");
            when(materialMapper.selectById(10L)).thenReturn(material);
            when(materialMapper.deleteById(10L)).thenReturn(1);

            // Act
            service.deleteMaterial(10L);

            // Assert
            verify(materialMapper).selectById(10L);
            verify(materialMapper).deleteById(10L);
        }

        @Test
        @DisplayName("删除材料失败 - 材料不存在")
        void testDeleteMaterial_NotFound() {
            // Arrange
            when(materialMapper.selectById(999L)).thenReturn(null);

            // Act & Assert
            BusinessException ex = assertThrows(BusinessException.class, () -> service.deleteMaterial(999L));
            assertEquals("材料不存在", ex.getMessage());
            verify(materialMapper, never()).deleteById(any());
        }
    }

    // ======================== 逾期检测（convertToVO内部逻辑）测试 ========================

    @Nested
    @DisplayName("逾期检测逻辑（通过 pageQuery/getDetail 触发）")
    class OverdueDetectionTests {

        @Test
        @DisplayName("逾期检测 - 结束日期已过且状态非已完成/已撤销，自动标记逾期并更新DB")
        void testOverdueDetection_MarksAsOverdue() {
            // Arrange
            Date pastDate = new Date(System.currentTimeMillis() - 86400000 * 2); // 2天前
            PunishmentExecution exec = createExecution(1L, "CASE001", "警告", 1, pastDate);
            exec.setIsOverdue(0);

            Page<PunishmentExecution> entityPage = new Page<>(1, 10, 1);
            entityPage.setRecords(Collections.singletonList(exec));

            when(executionMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                    .thenReturn(entityPage);
            when(executionMapper.updateById(any(PunishmentExecution.class))).thenReturn(1);

            // Act
            Page<PunishmentExecutionVO> result = service.pageQuery(1, 10, null);

            // Assert
            assertEquals(1, result.getRecords().get(0).getIsOverdue());
            verify(executionMapper).updateById(argThat(e -> e.getIsOverdue() != null && e.getIsOverdue() == 1));
        }

        @Test
        @DisplayName("逾期检测 - 已完成状态即使日期过期也不标记逾期")
        void testOverdueDetection_CompletedNotOverdue() {
            // Arrange
            Date pastDate = new Date(System.currentTimeMillis() - 86400000 * 5);
            PunishmentExecution exec = createExecution(1L, "CASE001", "警告", 2, pastDate);
            exec.setIsOverdue(0);

            Page<PunishmentExecution> entityPage = new Page<>(1, 10, 1);
            entityPage.setRecords(Collections.singletonList(exec));

            when(executionMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                    .thenReturn(entityPage);

            // Act
            Page<PunishmentExecutionVO> result = service.pageQuery(1, 10, null);

            // Assert
            assertEquals(0, result.getRecords().get(0).getIsOverdue());
            // 已完成状态不应触发 updateById
            verify(executionMapper, never()).updateById(any(PunishmentExecution.class));
        }

        @Test
        @DisplayName("逾期检测 - 已撤销状态即使日期过期也不标记逾期")
        void testOverdueDetection_RevokedNotOverdue() {
            // Arrange
            Date pastDate = new Date(System.currentTimeMillis() - 86400000 * 5);
            PunishmentExecution exec = createExecution(1L, "CASE001", "警告", 3, pastDate);
            exec.setIsOverdue(0);

            Page<PunishmentExecution> entityPage = new Page<>(1, 10, 1);
            entityPage.setRecords(Collections.singletonList(exec));

            when(executionMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                    .thenReturn(entityPage);

            // Act
            Page<PunishmentExecutionVO> result = service.pageQuery(1, 10, null);

            // Assert
            assertEquals(0, result.getRecords().get(0).getIsOverdue());
            verify(executionMapper, never()).updateById(any(PunishmentExecution.class));
        }

        @Test
        @DisplayName("逾期检测 - 无结束日期不标记逾期")
        void testOverdueDetection_NoEndDate() {
            // Arrange
            PunishmentExecution exec = createExecution(1L, "CASE001", "警告", 1, null);
            exec.setIsOverdue(0);

            Page<PunishmentExecution> entityPage = new Page<>(1, 10, 1);
            entityPage.setRecords(Collections.singletonList(exec));

            when(executionMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                    .thenReturn(entityPage);

            // Act
            Page<PunishmentExecutionVO> result = service.pageQuery(1, 10, null);

            // Assert
            assertEquals(0, result.getRecords().get(0).getIsOverdue());
            verify(executionMapper, never()).updateById(any(PunishmentExecution.class));
        }

        @Test
        @DisplayName("逾期检测 - 已标记逾期的记录不再重复更新")
        void testOverdueDetection_AlreadyOverdue_NoUpdate() {
            // Arrange
            Date pastDate = new Date(System.currentTimeMillis() - 86400000 * 2);
            PunishmentExecution exec = createExecution(1L, "CASE001", "警告", 1, pastDate);
            exec.setIsOverdue(1); // 已标记

            Page<PunishmentExecution> entityPage = new Page<>(1, 10, 1);
            entityPage.setRecords(Collections.singletonList(exec));

            when(executionMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                    .thenReturn(entityPage);

            // Act
            Page<PunishmentExecutionVO> result = service.pageQuery(1, 10, null);

            // Assert
            assertEquals(1, result.getRecords().get(0).getIsOverdue());
            // 已经标记为逾期，不应再次更新
            verify(executionMapper, never()).updateById(any(PunishmentExecution.class));
        }
    }

    // ======================== 集成场景测试 ========================

    @Nested
    @DisplayName("集成场景测试")
    class IntegrationTests {

        @Test
        @DisplayName("完整流程：创建 -> 查询 -> 状态变更 -> 上传材料 -> 查看详情 -> 删除")
        void testFullLifecycle() {
            // 1. Create
            PunishmentExecutionDTO createDTO = createDTO("CASE-LIFECYCLE", "警告");
            when(executionMapper.insert(any(PunishmentExecution.class))).thenAnswer(invocation -> {
                PunishmentExecution exec = invocation.getArgument(0);
                exec.setId(500L);
                return 1;
            });
            PunishmentExecution created = service.create(createDTO);
            assertEquals(500L, created.getId());

            // 2. Change status
            PunishmentExecution execForStatus = createExecution(500L, "CASE-LIFECYCLE", "警告", 0, null);
            when(executionMapper.selectById(500L)).thenReturn(execForStatus);
            when(executionMapper.updateById(any(PunishmentExecution.class))).thenReturn(1);
            service.changeStatus(500L, 1);

            // 3. Upload material
            when(materialMapper.insert(any(PunishmentMaterial.class))).thenAnswer(invocation -> {
                PunishmentMaterial mat = invocation.getArgument(0);
                mat.setId(600L);
                return 1;
            });
            PunishmentMaterial material = service.uploadMaterial(500L, "决定书", "/files/doc.pdf", 99L);
            assertEquals(600L, material.getId());

            // 4. Get detail
            when(materialMapper.selectByExecutionId(500L)).thenReturn(Collections.singletonList(material));
            PunishmentExecutionVO detail = service.getDetail(500L);
            assertEquals(500L, detail.getId());
            assertEquals(1, detail.getMaterials().size());

            // 5. Delete
            when(executionMapper.deleteById(500L)).thenReturn(1);
            when(materialMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(1);
            service.delete(500L);

            // Verify all operations happened
            verify(executionMapper).insert(any());
            verify(executionMapper, atLeastOnce()).selectById(500L);
            verify(executionMapper).updateById(any());
            verify(materialMapper).insert(any());
            verify(materialMapper).selectByExecutionId(500L);
            verify(materialMapper).delete(any());
            verify(executionMapper).deleteById(500L);
        }
    }
}
