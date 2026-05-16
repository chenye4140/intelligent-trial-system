package com.intelligent.trial.repository.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.intelligent.trial.common.exception.BusinessException;
import com.intelligent.trial.repository.entity.Directory;
import com.intelligent.trial.repository.mapper.DirectoryMapper;
import com.intelligent.trial.repository.vo.DirectoryTreeVO;
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
 * DirectoryServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
class DirectoryServiceImplTest {

    @InjectMocks
    private DirectoryServiceImpl directoryService;

    @Mock
    private DirectoryMapper directoryMapper;

    // ==================== create() ====================

    @Test
    void create_shouldSetDefaultValuesForRootDirectory() {
        Directory directory = new Directory();
        directory.setName("Root");
        directory.setRepoType(1);
        directory.setParentId(null);
        directory.setSort(null);
        directory.setStatus(null);

        when(directoryMapper.insert(directory)).thenReturn(1);

        directoryService.create(directory);

        assertEquals(Long.valueOf(0L), directory.getParentId());
        assertEquals(Integer.valueOf(0), directory.getSort());
        assertEquals(Integer.valueOf(1), directory.getStatus());
        assertEquals("/0/", directory.getPath());
        verify(directoryMapper).insert(directory);
    }

    @Test
    void create_shouldBuildPathForChildDirectory() {
        Directory parent = new Directory();
        parent.setId(1L);
        parent.setPath("/0/");
        parent.setParentId(0L);

        Directory directory = new Directory();
        directory.setName("Child");
        directory.setRepoType(1);
        directory.setParentId(1L);

        when(directoryMapper.selectById(1L)).thenReturn(parent);
        when(directoryMapper.insert(directory)).thenReturn(1);

        directoryService.create(directory);

        assertEquals("/0/1/", directory.getPath());
    }

    @Test
    void create_shouldSetParentIdZeroWhenNull() {
        Directory directory = new Directory();
        directory.setName("Root");
        directory.setRepoType(1);
        directory.setParentId(null);

        when(directoryMapper.insert(directory)).thenReturn(1);

        directoryService.create(directory);

        assertEquals(Long.valueOf(0L), directory.getParentId());
        assertEquals("/0/", directory.getPath());
    }

    // ==================== update() ====================

    @Test
    void update_shouldThrowWhenNotFound() {
        Directory directory = new Directory();
        directory.setId(999L);

        when(directoryMapper.selectById(999L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> directoryService.update(directory));
        assertTrue(ex.getMessage().contains("目录不存在"));
    }

    @Test
    void update_shouldUpdateWithoutPathChangeWhenParentUnchanged() {
        Directory existing = new Directory();
        existing.setId(1L);
        existing.setParentId(0L);
        existing.setPath("/0/");

        Directory directory = new Directory();
        directory.setId(1L);
        directory.setName("Updated");
        directory.setParentId(0L);

        when(directoryMapper.selectById(1L)).thenReturn(existing, directory);
        when(directoryMapper.updateById(directory)).thenReturn(1);

        directoryService.update(directory);

        verify(directoryMapper).updateById(directory);
    }

    @Test
    void update_shouldUpdatePathWhenParentChanged() {
        Directory existing = new Directory();
        existing.setId(1L);
        existing.setParentId(0L);
        existing.setPath("/0/");

        Directory newParent = new Directory();
        newParent.setId(2L);
        newParent.setParentId(0L);
        newParent.setPath("/0/");

        Directory directory = new Directory();
        directory.setId(1L);
        directory.setName("Updated");
        directory.setParentId(2L);

        when(directoryMapper.selectById(1L)).thenReturn(existing);
        when(directoryMapper.selectById(2L)).thenReturn(newParent);
        when(directoryMapper.updateById(directory)).thenReturn(1);
        when(directoryMapper.selectById(1L)).thenReturn(existing, directory);

        directoryService.update(directory);

        assertEquals("/0/2/", directory.getPath());
    }

    // ==================== delete() ====================

    @Test
    void delete_shouldThrowWhenNotFound() {
        when(directoryMapper.selectById(999L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> directoryService.delete(999L));
        assertTrue(ex.getMessage().contains("目录不存在"));
    }

    @Test
    void delete_shouldCascadeDeleteChildren() {
        Directory parent = new Directory();
        parent.setId(1L);
        parent.setPath("/0/1/");

        Directory child1 = new Directory();
        child1.setId(2L);
        child1.setPath("/0/1/2/");

        Directory child2 = new Directory();
        child2.setId(3L);
        child2.setPath("/0/1/3/");

        List<Directory> children = Arrays.asList(child1, child2);

        when(directoryMapper.selectById(1L)).thenReturn(parent);
        when(directoryMapper.selectByPathPrefix("/0/1/")).thenReturn(children);
        when(directoryMapper.deleteBatchIds(anyList())).thenReturn(1);
        when(directoryMapper.deleteById(1L)).thenReturn(1);

        directoryService.delete(1L);

        verify(directoryMapper).deleteBatchIds(argThat(ids -> ids.contains(2L) && ids.contains(3L)));
        verify(directoryMapper).deleteById(1L);
    }

    @Test
    void delete_shouldDeleteOnlyDirectoryWhenNoChildren() {
        Directory directory = new Directory();
        directory.setId(1L);
        directory.setPath("/0/1/");

        when(directoryMapper.selectById(1L)).thenReturn(directory);
        when(directoryMapper.selectByPathPrefix("/0/1/")).thenReturn(Collections.<Directory>emptyList());
        when(directoryMapper.deleteById(1L)).thenReturn(1);

        directoryService.delete(1L);

        verify(directoryMapper, never()).deleteBatchIds(anyList());
        verify(directoryMapper).deleteById(1L);
    }

    // ==================== getById() ====================

    @Test
    void getById_shouldThrowWhenNotFound() {
        when(directoryMapper.selectById(999L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> directoryService.getById(999L));
        assertTrue(ex.getMessage().contains("目录不存在"));
    }

    @Test
    void getById_shouldReturnDirectoryWhenFound() {
        Directory directory = new Directory();
        directory.setId(1L);
        directory.setName("Test");

        when(directoryMapper.selectById(1L)).thenReturn(directory);

        Directory result = directoryService.getById(1L);

        assertNotNull(result);
        assertEquals(Long.valueOf(1L), result.getId());
        assertEquals("Test", result.getName());
    }

    // ==================== getTree() ====================

    @Test
    void getTree_shouldBuildTreeStructure() {
        Directory root = new Directory();
        root.setId(1L);
        root.setRepoType(1);
        root.setParentId(0L);
        root.setName("Root");
        root.setSort(1);
        root.setStatus(1);

        Directory child = new Directory();
        child.setId(2L);
        child.setRepoType(1);
        child.setParentId(1L);
        child.setName("Child");
        child.setSort(1);
        child.setStatus(1);

        List<Directory> dirs = Arrays.asList(root, child);
        when(directoryMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(dirs);

        List<DirectoryTreeVO> result = directoryService.getTree(1);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Long.valueOf(1L), result.get(0).getId());
        assertNotNull(result.get(0).getChildren());
        assertEquals(1, result.get(0).getChildren().size());
        assertEquals(Long.valueOf(2L), result.get(0).getChildren().get(0).getId());
    }

    @Test
    void getTree_shouldFilterByRepoTypeAndStatus() {
        when(directoryMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.<Directory>emptyList());

        List<DirectoryTreeVO> result = directoryService.getTree(2);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(directoryMapper).selectList(argThat((LambdaQueryWrapper<Directory> wrapper) -> true));
    }

    @Test
    void getTree_shouldReturnEmptyWhenNoDirectories() {
        when(directoryMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.<Directory>emptyList());

        List<DirectoryTreeVO> result = directoryService.getTree(1);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== move() ====================

    @Test
    void move_shouldThrowWhenDirectoryNotFound() {
        when(directoryMapper.selectById(1L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> directoryService.move(1L, 2L));
        assertTrue(ex.getMessage().contains("目录不存在"));
    }

    @Test
    void move_shouldThrowWhenMovingToDescendant() {
        Directory source = new Directory();
        source.setId(1L);
        source.setPath("/0/1/");

        Directory target = new Directory();
        target.setId(3L);
        target.setPath("/0/1/2/3/");

        when(directoryMapper.selectById(1L)).thenReturn(source);
        when(directoryMapper.selectById(3L)).thenReturn(target);

        BusinessException ex = assertThrows(BusinessException.class, () -> directoryService.move(1L, 3L));
        assertTrue(ex.getMessage().contains("不能将目录移动到其子目录下"));
    }

    @Test
    void move_shouldUpdatePathForDirectoryAndChildren() {
        Directory source = new Directory();
        source.setId(1L);
        source.setPath("/0/2/");
        source.setParentId(2L);

        Directory newParent = new Directory();
        newParent.setId(5L);
        newParent.setPath("/0/");
        newParent.setParentId(0L);

        Directory child = new Directory();
        child.setId(2L);
        child.setPath("/0/2/1/");

        when(directoryMapper.selectById(1L)).thenReturn(source);
        when(directoryMapper.selectById(5L)).thenReturn(newParent);
        when(directoryMapper.selectByPathPrefix("/0/2/")).thenReturn(Arrays.asList(child));
        when(directoryMapper.updateById(any(Directory.class))).thenReturn(1);

        directoryService.move(1L, 5L);

        assertEquals(Long.valueOf(5L), source.getParentId());
        assertEquals("/0/5/", source.getPath());
        // Child path should be updated from /0/2/1/ to /0/5/1/
        verify(directoryMapper, atLeastOnce()).updateById(any(Directory.class));
    }

    // ==================== updateSort() ====================

    @Test
    void updateSort_shouldThrowWhenNotFound() {
        when(directoryMapper.selectById(999L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> directoryService.updateSort(999L, 5));
        assertTrue(ex.getMessage().contains("目录不存在"));
    }

    @Test
    void updateSort_shouldUpdateSortValue() {
        Directory directory = new Directory();
        directory.setId(1L);
        directory.setSort(0);

        when(directoryMapper.selectById(1L)).thenReturn(directory);
        when(directoryMapper.updateById(directory)).thenReturn(1);

        directoryService.updateSort(1L, 5);

        assertEquals(Integer.valueOf(5), directory.getSort());
        verify(directoryMapper).updateById(directory);
    }

    // ==================== validateDepth ====================

    @Test
    void create_shouldThrowWhenDepthExceedsMax() {
        // Build a chain of 10 directories (depth 10)
        Directory parent1 = new Directory();
        parent1.setId(1L);
        parent1.setParentId(0L);
        parent1.setPath("/0/");

        Directory parent2 = new Directory();
        parent2.setId(2L);
        parent2.setParentId(1L);
        parent2.setPath("/0/1/");

        Directory parent3 = new Directory();
        parent3.setId(3L);
        parent3.setParentId(2L);
        parent3.setPath("/0/1/2/");

        Directory parent4 = new Directory();
        parent4.setId(4L);
        parent4.setParentId(3L);
        parent4.setPath("/0/1/2/3/");

        Directory parent5 = new Directory();
        parent5.setId(5L);
        parent5.setParentId(4L);
        parent5.setPath("/0/1/2/3/4/");

        Directory parent6 = new Directory();
        parent6.setId(6L);
        parent6.setParentId(5L);
        parent6.setPath("/0/1/2/3/4/5/");

        Directory parent7 = new Directory();
        parent7.setId(7L);
        parent7.setParentId(6L);
        parent7.setPath("/0/1/2/3/4/5/6/");

        Directory parent8 = new Directory();
        parent8.setId(8L);
        parent8.setParentId(7L);
        parent8.setPath("/0/1/2/3/4/5/6/7/");

        Directory parent9 = new Directory();
        parent9.setId(9L);
        parent9.setParentId(8L);
        parent9.setPath("/0/1/2/3/4/5/6/7/8/");

        Directory parent10 = new Directory();
        parent10.setId(10L);
        parent10.setParentId(9L);
        parent10.setPath("/0/1/2/3/4/5/6/7/8/9/");

        // Create a directory with parentId=10L - this would be depth 11
        Directory directory = new Directory();
        directory.setName("Deep");
        directory.setRepoType(1);
        directory.setParentId(10L);

        when(directoryMapper.selectById(10L)).thenReturn(parent10);
        when(directoryMapper.selectById(9L)).thenReturn(parent9);
        when(directoryMapper.selectById(8L)).thenReturn(parent8);
        when(directoryMapper.selectById(7L)).thenReturn(parent7);
        when(directoryMapper.selectById(6L)).thenReturn(parent6);
        when(directoryMapper.selectById(5L)).thenReturn(parent5);
        when(directoryMapper.selectById(4L)).thenReturn(parent4);
        when(directoryMapper.selectById(3L)).thenReturn(parent3);
        when(directoryMapper.selectById(2L)).thenReturn(parent2);
        when(directoryMapper.selectById(1L)).thenReturn(parent1);

        BusinessException ex = assertThrows(BusinessException.class, () -> directoryService.create(directory));
        assertTrue(ex.getMessage().contains("目录层级不能超过"));
    }

    @Test
    void create_shouldThrowAtMaxDepth() {
        // Build a chain of 9 levels (1->2->3->4->5->6->7->8->9)
        // Trying to add a 10th level should fail since depth reaches MAX_DEPTH(10)
        Directory parent1 = new Directory();
        parent1.setId(1L);
        parent1.setParentId(0L);
        parent1.setPath("/0/");

        Directory parent2 = new Directory();
        parent2.setId(2L);
        parent2.setParentId(1L);
        parent2.setPath("/0/1/");

        Directory parent3 = new Directory();
        parent3.setId(3L);
        parent3.setParentId(2L);
        parent3.setPath("/0/1/2/");

        Directory parent4 = new Directory();
        parent4.setId(4L);
        parent4.setParentId(3L);
        parent4.setPath("/0/1/2/3/");

        Directory parent5 = new Directory();
        parent5.setId(5L);
        parent5.setParentId(4L);
        parent5.setPath("/0/1/2/3/4/");

        Directory parent6 = new Directory();
        parent6.setId(6L);
        parent6.setParentId(5L);
        parent6.setPath("/0/1/2/3/4/5/");

        Directory parent7 = new Directory();
        parent7.setId(7L);
        parent7.setParentId(6L);
        parent7.setPath("/0/1/2/3/4/5/6/");

        Directory parent8 = new Directory();
        parent8.setId(8L);
        parent8.setParentId(7L);
        parent8.setPath("/0/1/2/3/4/5/6/7/");

        Directory parent9 = new Directory();
        parent9.setId(9L);
        parent9.setParentId(8L);
        parent9.setPath("/0/1/2/3/4/5/6/7/8/");

        // Try to create at depth 10 - should be rejected
        Directory directory = new Directory();
        directory.setName("MaxDepth");
        directory.setRepoType(1);
        directory.setParentId(9L);

        when(directoryMapper.selectById(9L)).thenReturn(parent9);
        when(directoryMapper.selectById(8L)).thenReturn(parent8);
        when(directoryMapper.selectById(7L)).thenReturn(parent7);
        when(directoryMapper.selectById(6L)).thenReturn(parent6);
        when(directoryMapper.selectById(5L)).thenReturn(parent5);
        when(directoryMapper.selectById(4L)).thenReturn(parent4);
        when(directoryMapper.selectById(3L)).thenReturn(parent3);
        when(directoryMapper.selectById(2L)).thenReturn(parent2);
        when(directoryMapper.selectById(1L)).thenReturn(parent1);

        BusinessException ex = assertThrows(BusinessException.class, () -> directoryService.create(directory));
        assertTrue(ex.getMessage().contains("目录层级不能超过"));
    }

    @Test
    void create_shouldSucceedAtDepth9() {
        // Build a chain of 8 levels (1->2->3->4->5->6->7->8)
        // Adding a 9th level should succeed (depth=9 < MAX_DEPTH=10)
        Directory parent1 = new Directory();
        parent1.setId(1L);
        parent1.setParentId(0L);
        parent1.setPath("/0/");

        Directory parent2 = new Directory();
        parent2.setId(2L);
        parent2.setParentId(1L);
        parent2.setPath("/0/1/");

        Directory parent3 = new Directory();
        parent3.setId(3L);
        parent3.setParentId(2L);
        parent3.setPath("/0/1/2/");

        Directory parent4 = new Directory();
        parent4.setId(4L);
        parent4.setParentId(3L);
        parent4.setPath("/0/1/2/3/");

        Directory parent5 = new Directory();
        parent5.setId(5L);
        parent5.setParentId(4L);
        parent5.setPath("/0/1/2/3/4/");

        Directory parent6 = new Directory();
        parent6.setId(6L);
        parent6.setParentId(5L);
        parent6.setPath("/0/1/2/3/4/5/");

        Directory parent7 = new Directory();
        parent7.setId(7L);
        parent7.setParentId(6L);
        parent7.setPath("/0/1/2/3/4/5/6/");

        Directory parent8 = new Directory();
        parent8.setId(8L);
        parent8.setParentId(7L);
        parent8.setPath("/0/1/2/3/4/5/6/7/");

        // Create at depth 9 - should succeed
        Directory directory = new Directory();
        directory.setName("Depth9");
        directory.setRepoType(1);
        directory.setParentId(8L);

        when(directoryMapper.selectById(8L)).thenReturn(parent8);
        when(directoryMapper.selectById(7L)).thenReturn(parent7);
        when(directoryMapper.selectById(6L)).thenReturn(parent6);
        when(directoryMapper.selectById(5L)).thenReturn(parent5);
        when(directoryMapper.selectById(4L)).thenReturn(parent4);
        when(directoryMapper.selectById(3L)).thenReturn(parent3);
        when(directoryMapper.selectById(2L)).thenReturn(parent2);
        when(directoryMapper.selectById(1L)).thenReturn(parent1);
        when(directoryMapper.insert(directory)).thenReturn(1);

        directoryService.create(directory);

        assertEquals("/0/1/2/3/4/5/6/7/8/", directory.getPath());
    }

    @Test
    void create_shouldThrowWhenParentNotFound() {
        Directory directory = new Directory();
        directory.setName("Test");
        directory.setRepoType(1);
        directory.setParentId(999L);

        when(directoryMapper.selectById(999L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> directoryService.create(directory));
        assertTrue(ex.getMessage().contains("父目录不存在"));
    }
}
