package com.intelligent.trial.auth.service.impl;

import com.intelligent.trial.auth.dto.DeptDTO;
import com.intelligent.trial.auth.entity.SysDept;
import com.intelligent.trial.auth.mapper.SysDeptMapper;
import com.intelligent.trial.auth.mapper.SysUserMapper;
import com.intelligent.trial.auth.vo.DeptTreeVO;
import com.intelligent.trial.auth.vo.UserVO;
import com.intelligent.trial.common.exception.BusinessException;
import com.intelligent.trial.common.exception.ErrorCode;
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
 * DeptServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
class DeptServiceImplTest {

    @InjectMocks
    private DeptServiceImpl deptService;

    @Mock
    private SysDeptMapper baseMapper;

    @Mock
    private SysUserMapper userMapper;

    @Test
    void getDeptTree_shouldReturnTreeStructure() {
        SysDept root = new SysDept();
        root.setId(1L);
        root.setDeptName("总公司");
        root.setParentId(0L);
        root.setSort(1);

        SysDept child1 = new SysDept();
        child1.setId(2L);
        child1.setDeptName("技术部");
        child1.setParentId(1L);
        child1.setSort(1);

        SysDept child2 = new SysDept();
        child2.setId(3L);
        child2.setDeptName("人事部");
        child2.setParentId(1L);
        child2.setSort(2);

        when(baseMapper.selectAllActiveDepts()).thenReturn(Arrays.asList(root, child1, child2));

        List<DeptTreeVO> result = deptService.getDeptTree();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("总公司", result.get(0).getDeptName());
        assertEquals(2, result.get(0).getChildren().size());
        assertEquals("技术部", result.get(0).getChildren().get(0).getDeptName());
        assertEquals("人事部", result.get(0).getChildren().get(1).getDeptName());
    }

    @Test
    void getDeptTree_shouldReturnEmptyWhenNoDepts() {
        when(baseMapper.selectAllActiveDepts()).thenReturn(Collections.emptyList());

        List<DeptTreeVO> result = deptService.getDeptTree();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getDeptTree_shouldReturnNullWhenMapperReturnsNull() {
        when(baseMapper.selectAllActiveDepts()).thenReturn(null);

        List<DeptTreeVO> result = deptService.getDeptTree();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void addDept_shouldCreateDepartment() {
        DeptDTO dto = new DeptDTO();
        dto.setDeptName("新部门");
        dto.setParentId(1L);
        dto.setLeader("李四");
        dto.setSort(1);

        when(baseMapper.insert(any(SysDept.class))).thenReturn(1);

        deptService.addDept(dto);

        verify(baseMapper).insert(argThat((SysDept dept) ->
            "新部门".equals(dept.getDeptName())
                && Long.valueOf(1L).equals(dept.getParentId())
                && Integer.valueOf(1).equals(dept.getStatus())
                && dept.getCreateTime() != null
                && dept.getUpdateTime() != null));
    }

    @Test
    void addDept_shouldUseDefaultStatusWhenNotProvided() {
        DeptDTO dto = new DeptDTO();
        dto.setDeptName("默认状态部门");
        dto.setParentId(0L);
        // status not set

        when(baseMapper.insert(any(SysDept.class))).thenReturn(1);

        deptService.addDept(dto);

        verify(baseMapper).insert(argThat((SysDept dept) ->
            Integer.valueOf(1).equals(dept.getStatus())));
    }

    @Test
    void addDept_shouldUseProvidedStatus() {
        DeptDTO dto = new DeptDTO();
        dto.setDeptName("停用部门");
        dto.setParentId(0L);
        dto.setStatus(0);

        when(baseMapper.insert(any(SysDept.class))).thenReturn(1);

        deptService.addDept(dto);

        verify(baseMapper).insert(argThat((SysDept dept) ->
            Integer.valueOf(0).equals(dept.getStatus())));
    }

    @Test
    void updateDept_shouldUpdateDepartment() {
        DeptDTO dto = new DeptDTO();
        dto.setId(1L);
        dto.setDeptName("更新后部门");
        dto.setLeader("王五");

        SysDept existing = new SysDept();
        existing.setId(1L);
        existing.setDeptName("原部门");

        when(baseMapper.selectById(1L)).thenReturn(existing);
        when(baseMapper.updateById(any(SysDept.class))).thenReturn(1);

        deptService.updateDept(dto);

        verify(baseMapper).updateById(argThat((SysDept dept) ->
            Long.valueOf(1L).equals(dept.getId())
                && "更新后部门".equals(dept.getDeptName())
                && dept.getUpdateTime() != null));
    }

    @Test
    void updateDept_shouldThrowWhenDeptIdNull() {
        DeptDTO dto = new DeptDTO();
        dto.setDeptName("noId");

        BusinessException ex = assertThrows(BusinessException.class, () -> deptService.updateDept(dto));
        assertTrue(ex.getMessage().contains("部门ID不能为空"));
    }

    @Test
    void updateDept_shouldThrowWhenDeptNotFound() {
        DeptDTO dto = new DeptDTO();
        dto.setId(999L);
        dto.setDeptName("ghost");

        when(baseMapper.selectById(999L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> deptService.updateDept(dto));
        assertTrue(ex.getMessage().contains("部门不存在"));
    }

    @Test
    void deleteDept_shouldSucceedForNormalDept() {
        SysDept dept = new SysDept();
        dept.setId(5L);
        dept.setDeptName("可删除部门");

        when(baseMapper.selectById(5L)).thenReturn(dept);
        when(baseMapper.selectByParentId(5L)).thenReturn(Collections.emptyList());
        when(baseMapper.deleteById(5L)).thenReturn(1);

        deptService.deleteDept(5L);

        verify(baseMapper).deleteById(5L);
    }

    @Test
    void deleteDept_shouldFailWhenHasChildren() {
        SysDept dept = new SysDept();
        dept.setId(1L);

        SysDept child = new SysDept();
        child.setId(2L);

        when(baseMapper.selectById(1L)).thenReturn(dept);
        when(baseMapper.selectByParentId(1L)).thenReturn(Arrays.asList(child));

        BusinessException ex = assertThrows(BusinessException.class, () -> deptService.deleteDept(1L));
        assertTrue(ex.getMessage().contains("部门下有子部门"));
        verify(baseMapper, never()).deleteById(any());
    }

    @Test
    void deleteDept_shouldFailWhenNotFound() {
        when(baseMapper.selectById(999L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> deptService.deleteDept(999L));
        assertTrue(ex.getMessage().contains("部门不存在"));
    }

    @Test
    void deleteDept_shouldSucceedWhenChildrenIsNull() {
        SysDept dept = new SysDept();
        dept.setId(5L);

        when(baseMapper.selectById(5L)).thenReturn(dept);
        when(baseMapper.selectByParentId(5L)).thenReturn(null);
        when(baseMapper.deleteById(5L)).thenReturn(1);

        deptService.deleteDept(5L);

        verify(baseMapper).deleteById(5L);
    }

    @Test
    void getUsersByDeptId_shouldReturnUsers() {
        UserVO user1 = new UserVO();
        user1.setId(1L);
        user1.setUsername("zhangsan");

        UserVO user2 = new UserVO();
        user2.setId(2L);
        user2.setUsername("lisi");

        when(userMapper.selectUsersByDeptId(1L)).thenReturn(Arrays.asList(user1, user2));

        List<UserVO> result = deptService.getUsersByDeptId(1L);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("zhangsan", result.get(0).getUsername());
    }

    @Test
    void getDeptTree_shouldSortChildrenBySort() {
        SysDept root = new SysDept();
        root.setId(1L);
        root.setDeptName("总公司");
        root.setParentId(0L);
        root.setSort(0);

        SysDept child2 = new SysDept();
        child2.setId(3L);
        child2.setDeptName("人事部");
        child2.setParentId(1L);
        child2.setSort(2);

        SysDept child1 = new SysDept();
        child1.setId(2L);
        child1.setDeptName("技术部");
        child1.setParentId(1L);
        child1.setSort(1);

        when(baseMapper.selectAllActiveDepts()).thenReturn(Arrays.asList(root, child2, child1));

        List<DeptTreeVO> result = deptService.getDeptTree();

        assertEquals(2, result.get(0).getChildren().size());
        // Children should be sorted by sort ascending
        assertEquals("技术部", result.get(0).getChildren().get(0).getDeptName());
        assertEquals("人事部", result.get(0).getChildren().get(1).getDeptName());
    }
}
