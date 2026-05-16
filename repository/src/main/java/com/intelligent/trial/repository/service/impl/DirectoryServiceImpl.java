package com.intelligent.trial.repository.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.intelligent.trial.common.exception.BusinessException;
import com.intelligent.trial.repository.entity.Directory;
import com.intelligent.trial.repository.mapper.DirectoryMapper;
import com.intelligent.trial.repository.service.DirectoryService;
import com.intelligent.trial.repository.vo.DirectoryTreeVO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 目录管理服务实现
 */
@Service
public class DirectoryServiceImpl implements DirectoryService {

    /**
     * 最大目录层级深度
     */
    private static final int MAX_DEPTH = 10;

    private final DirectoryMapper directoryMapper;

    public DirectoryServiceImpl(DirectoryMapper directoryMapper) {
        this.directoryMapper = directoryMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Directory create(Directory directory) {
        // 校验层级深度
        validateDepth(directory);

        // 设置默认值
        if (directory.getSort() == null) {
            directory.setSort(0);
        }
        if (directory.getStatus() == null) {
            directory.setStatus(1);
        }
        if (directory.getParentId() == null) {
            directory.setParentId(0L);
        }

        // 构建路径
        String path = buildPath(directory.getParentId());
        directory.setPath(path);

        directoryMapper.insert(directory);
        return directory;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Directory update(Directory directory) {
        Directory existing = directoryMapper.selectById(directory.getId());
        if (existing == null) {
            throw new BusinessException("目录不存在");
        }

        // 如果修改了父目录，需要校验层级和更新路径
        if (directory.getParentId() != null && !directory.getParentId().equals(existing.getParentId())) {
            validateDepthForUpdate(directory.getId(), directory.getParentId());
            String path = buildPath(directory.getParentId());
            directory.setPath(path);
        }

        // 只更新非空字段
        directoryMapper.updateById(directory);
        return directoryMapper.selectById(directory.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Directory directory = directoryMapper.selectById(id);
        if (directory == null) {
            throw new BusinessException("目录不存在");
        }

        // 级联删除所有子目录
        String pathPrefix = directory.getPath();
        List<Directory> children = directoryMapper.selectByPathPrefix(pathPrefix);
        if (!children.isEmpty()) {
            List<Long> ids = children.stream()
                    .map(Directory::getId)
                    .collect(Collectors.toList());
            directoryMapper.deleteBatchIds(ids);
        }

        // 删除当前目录
        directoryMapper.deleteById(id);
    }

    @Override
    public Directory getById(Long id) {
        Directory directory = directoryMapper.selectById(id);
        if (directory == null) {
            throw new BusinessException("目录不存在");
        }
        return directory;
    }

    @Override
    public List<DirectoryTreeVO> getTree(Integer repoType) {
        // 查询该库类型下所有目录
        LambdaQueryWrapper<Directory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Directory::getRepoType, repoType)
                .eq(Directory::getStatus, 1)
                .orderByAsc(Directory::getSort)
                .orderByAsc(Directory::getCreateTime);
        List<Directory> allDirectories = directoryMapper.selectList(wrapper);

        // 转换为 VO
        List<DirectoryTreeVO> allVOs = allDirectories.stream()
                .map(this::toTreeVO)
                .collect(Collectors.toList());

        // 构建树形结构
        return buildTree(allVOs, 0L);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void move(Long id, Long newParentId) {
        Directory directory = directoryMapper.selectById(id);
        if (directory == null) {
            throw new BusinessException("目录不存在");
        }

        // 不能移动到自己的子目录下
        if (isDescendant(id, newParentId)) {
            throw new BusinessException("不能将目录移动到其子目录下");
        }

        // 校验移动后的层级深度
        validateDepthForUpdate(id, newParentId);

        // 更新父目录和路径
        String newPath = buildPath(newParentId);
        String oldPath = directory.getPath();

        // 更新当前目录
        directory.setParentId(newParentId);
        directory.setPath(newPath);
        directoryMapper.updateById(directory);

        // 更新所有子目录的路径
        List<Directory> children = directoryMapper.selectByPathPrefix(oldPath);
        for (Directory child : children) {
            String childNewPath = child.getPath().replaceFirst("^" + oldPath, newPath);
            child.setPath(childNewPath);
            directoryMapper.updateById(child);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSort(Long id, Integer sort) {
        Directory directory = directoryMapper.selectById(id);
        if (directory == null) {
            throw new BusinessException("目录不存在");
        }
        directory.setSort(sort);
        directoryMapper.updateById(directory);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchImport(Integer repoType, InputStream inputStream) {
        int count = 0;
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            // 跳过表头，从第二行开始读取
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Directory directory = new Directory();
                directory.setRepoType(repoType);
                directory.setName(getCellStringValue(row.getCell(0)));
                directory.setParentId(getCellLongValue(row.getCell(1), 0L));
                directory.setSort(getCellIntValue(row.getCell(2), 0));
                directory.setPermissionScope(getCellStringValue(row.getCell(3)));
                directory.setStatus(1);

                // 构建路径
                String path = buildPath(directory.getParentId());
                directory.setPath(path);

                directoryMapper.insert(directory);
                count++;
            }
        } catch (Exception e) {
            throw new BusinessException("Excel导入失败: " + e.getMessage());
        }
        return count;
    }

    @Override
    public byte[] export(Integer repoType) {
        LambdaQueryWrapper<Directory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Directory::getRepoType, repoType)
                .orderByAsc(Directory::getPath)
                .orderByAsc(Directory::getSort);
        List<Directory> directories = directoryMapper.selectList(wrapper);

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("目录列表");

            // 创建表头
            Row headerRow = sheet.createRow(0);
            String[] headers = {"目录名称", "父目录ID", "排序值", "权限范围", "定密级别ID", "目录路径"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // 填充数据
            for (int i = 0; i < directories.size(); i++) {
                Directory dir = directories.get(i);
                Row dataRow = sheet.createRow(i + 1);
                dataRow.createCell(0).setCellValue(dir.getName());
                dataRow.createCell(1).setCellValue(dir.getParentId());
                dataRow.createCell(2).setCellValue(dir.getSort());
                dataRow.createCell(3).setCellValue(dir.getPermissionScope() != null ? dir.getPermissionScope() : "");
                dataRow.createCell(4).setCellValue(dir.getClassificationLevelId() != null ? dir.getClassificationLevelId() : 0);
                dataRow.createCell(5).setCellValue(dir.getPath() != null ? dir.getPath() : "");
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new BusinessException("Excel导出失败: " + e.getMessage());
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 构建树形结构
     */
    private List<DirectoryTreeVO> buildTree(List<DirectoryTreeVO> allNodes, Long parentId) {
        return allNodes.stream()
                .filter(node -> node.getParentId().equals(parentId))
                .peek(node -> node.setChildren(buildTree(allNodes, node.getId())))
                .collect(Collectors.toList());
    }

    /**
     * Entity 转 TreeVO
     */
    private DirectoryTreeVO toTreeVO(Directory directory) {
        DirectoryTreeVO vo = new DirectoryTreeVO();
        BeanUtils.copyProperties(directory, vo);
        return vo;
    }

    /**
     * 构建目录路径
     */
    private String buildPath(Long parentId) {
        if (parentId == null || parentId == 0L) {
            return "/0/";
        }
        Directory parent = directoryMapper.selectById(parentId);
        if (parent == null) {
            throw new BusinessException("父目录不存在");
        }
        return parent.getPath() + parentId + "/";
    }

    /**
     * 校验创建时的层级深度
     */
    private void validateDepth(Directory directory) {
        Long parentId = directory.getParentId();
        if (parentId == null || parentId == 0L) {
            return; // 根目录
        }

        int depth = 1;
        Long current = parentId;
        while (current != null && current != 0L) {
            Directory parent = directoryMapper.selectById(current);
            if (parent == null) {
                throw new BusinessException("父目录不存在");
            }
            depth++;
            if (depth > MAX_DEPTH) {
                throw new BusinessException("目录层级不能超过" + MAX_DEPTH + "级");
            }
            current = parent.getParentId();
        }

        if (depth >= MAX_DEPTH) {
            throw new BusinessException("目录层级不能超过" + MAX_DEPTH + "级");
        }
    }

    /**
     * 校验更新时的层级深度（移动父目录场景）
     */
    private void validateDepthForUpdate(Long id, Long newParentId) {
        if (newParentId == null || newParentId == 0L) {
            return;
        }
        if (id.equals(newParentId)) {
            throw new BusinessException("不能将目录移动到自己下面");
        }

        // 计算新父目录的深度
        int parentDepth = 1;
        Long current = newParentId;
        while (current != null && current != 0L) {
            Directory parent = directoryMapper.selectById(current);
            if (parent == null) {
                throw new BusinessException("父目录不存在");
            }
            parentDepth++;
            if (parentDepth >= MAX_DEPTH) {
                throw new BusinessException("目录层级不能超过" + MAX_DEPTH + "级");
            }
            current = parent.getParentId();
        }

        // 计算当前目录下最大子树深度
        Directory currentDir = directoryMapper.selectById(id);
        if (currentDir == null) {
            throw new BusinessException("目录不存在");
        }
        List<Directory> children = directoryMapper.selectByPathPrefix(currentDir.getPath());
        int maxChildDepth = 0;
        for (Directory child : children) {
            int childDepth = getPathDepth(child.getPath()) - getPathDepth(currentDir.getPath());
            maxChildDepth = Math.max(maxChildDepth, childDepth);
        }

        if (parentDepth + maxChildDepth > MAX_DEPTH) {
            throw new BusinessException("移动后目录层级将超过" + MAX_DEPTH + "级限制");
        }
    }

    /**
     * 判断 targetId 是否是 sourceId 的后代
     */
    private boolean isDescendant(Long sourceId, Long targetId) {
        if (targetId == null || targetId == 0L) {
            return false;
        }
        Directory source = directoryMapper.selectById(sourceId);
        if (source == null) {
            return false;
        }
        String sourcePath = source.getPath();

        // 获取 target 的路径
        Directory target = directoryMapper.selectById(targetId);
        if (target == null) {
            return false;
        }
        // 如果 target 的路径以 source 的路径开头，说明 source 是 target 的祖先
        return target.getPath().startsWith(sourcePath);
    }

    /**
     * 计算路径深度
     */
    private int getPathDepth(String path) {
        if (path == null || path.isEmpty()) {
            return 0;
        }
        // 路径格式: /0/1/2/ 深度为 3
        return (int) path.chars().filter(ch -> ch == '/').count() - 1;
    }

    /**
     * 获取单元格字符串值
     */
    private String getCellStringValue(Cell cell) {
        if (cell == null) return null;
        cell.setCellType(CellType.STRING);
        String value = cell.getStringCellValue();
        return (value == null || value.trim().isEmpty()) ? null : value.trim();
    }

    /**
     * 获取单元格 Long 值
     */
    private Long getCellLongValue(Cell cell, Long defaultValue) {
        if (cell == null) return defaultValue;
        try {
            cell.setCellType(CellType.NUMERIC);
            return (long) cell.getNumericCellValue();
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * 获取单元格 Integer 值
     */
    private Integer getCellIntValue(Cell cell, Integer defaultValue) {
        if (cell == null) return defaultValue;
        try {
            cell.setCellType(CellType.NUMERIC);
            return (int) cell.getNumericCellValue();
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
