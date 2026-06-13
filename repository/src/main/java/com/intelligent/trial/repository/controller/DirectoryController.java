package com.intelligent.trial.repository.controller;

import com.intelligent.trial.common.annotation.RequireLog;
import com.intelligent.trial.common.annotation.RequirePermission;
import com.intelligent.trial.common.dto.R;
import com.intelligent.trial.repository.entity.Directory;
import com.intelligent.trial.repository.service.DirectoryService;
import com.intelligent.trial.repository.vo.DirectoryTreeVO;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 目录管理 API 控制器
 * 提供目录 CRUD、树形查询、移动、排序、导入/导出等功能
 */
@Tag(name = "目录管理", description = "文档目录树CRUD、导入导出等目录管理接口")
@RestController
@RequestMapping("/api/repository/directory")
public class DirectoryController {

    private static final Logger log = LoggerFactory.getLogger(DirectoryController.class);
    private final DirectoryService directoryService;

    public DirectoryController(DirectoryService directoryService) {
        this.directoryService = directoryService;
    }

    // ==================== 基础 CRUD ====================

    /**
     * 创建目录
     */
    @RequirePermission("repository:directory:add")
    @Operation(summary = "创建目录", description = "创建新的目录节点")
    @RequireLog(module = "目录管理", action = "新增", description = "创建目录")
    @PostMapping("/create")
    public R<Directory> create(@Valid @RequestBody Directory directory) {
        Directory created = directoryService.create(directory);
        return R.ok(created);
    }

    /**
     * 更新目录
     */
    @RequirePermission("repository:directory:edit")
    @Operation(summary = "更新目录", description = "更新目录信息")
    @RequireLog(module = "目录管理", action = "编辑", description = "更新目录")
    @PutMapping("/update")
    public R<Directory> update(@Valid @RequestBody Directory directory) {
        Directory updated = directoryService.update(directory);
        return R.ok(updated);
    }

    /**
     * 删除目录（级联删除子目录）
     */
    @RequirePermission("repository:directory:remove")
    @Operation(summary = "删除目录", description = "删除目录及其所有子目录")
    @RequireLog(module = "目录管理", action = "删除", description = "删除目录")
    @DeleteMapping("/delete/{id}")
    public R<Void> delete(@Parameter(description = "目录ID") @PathVariable Long id) {
        directoryService.delete(id);
        return R.ok();
    }

    /**
     * 根据ID获取目录详情
     */
    @Operation(summary = "获取目录详情", description = "根据ID获取目录详细信息")
    @GetMapping("/get/{id}")
    public R<Directory> getById(@Parameter(description = "目录ID") @PathVariable Long id) {
        return R.ok(directoryService.getById(id));
    }

    // ==================== 树形查询 ====================

    /**
     * 获取目录树（按库类型）
     *
     * @param repoType 库类型：1=法规库, 2=资料库, 3=裁判文书库, 4=案例库
     */
    @Operation(summary = "获取目录树", description = "根据库类型获取目录树形结构")
    @GetMapping("/tree")
    public R<List<DirectoryTreeVO>> getTree(@Parameter(description = "库类型：1=法规库, 2=资料库, 3=裁判文书库, 4=案例库") @RequestParam Integer repoType) {
        return R.ok(directoryService.getTree(repoType));
    }

    // ==================== 移动和排序 ====================

    /**
     * 移动目录到新的父目录下
     *
     * @param id          目录ID
     * @param newParentId 新父目录ID（0表示移到根目录）
     */
    @RequirePermission("repository:directory:move")
    @Operation(summary = "移动目录", description = "将目录移动到新的父目录下")
    @RequireLog(module = "目录管理", action = "移动", description = "移动目录")
    @PutMapping("/move/{id}")
    public R<Void> move(@Parameter(description = "目录ID") @PathVariable Long id, @Parameter(description = "新父目录ID（0=根目录）") @RequestParam Long newParentId) {
        directoryService.move(id, newParentId);
        return R.ok();
    }

    /**
     * 更新目录排序值
     *
     * @param id   目录ID
     * @param sort 排序值
     */
    @RequirePermission("repository:directory:sort")
    @Operation(summary = "更新目录排序", description = "更新目录的排序值")
    @RequireLog(module = "目录管理", action = "排序", description = "排序目录")
    @PutMapping("/sort/{id}")
    public R<Void> updateSort(@Parameter(description = "目录ID") @PathVariable Long id, @Parameter(description = "排序值") @RequestParam Integer sort) {
        directoryService.updateSort(id, sort);
        return R.ok();
    }

    // ==================== 批量导入/导出 ====================

    /**
     * 批量导入目录（从Excel文件）
     *
     * @param repoType 库类型
     * @param file     Excel 文件
     */
    @RequirePermission("repository:directory:import")
    @Operation(summary = "导入目录", description = "从Excel文件批量导入目录")
    @RequireLog(module = "目录管理", action = "导入", description = "导入Excel")
    @PostMapping("/import")
    public R<Integer> batchImport(@Parameter(description = "库类型") @RequestParam Integer repoType,
                                  @Parameter(description = "Excel文件") @RequestParam("file") MultipartFile file) {
        try {
            int count = directoryService.batchImport(repoType, file.getInputStream());
            return R.ok("成功导入 " + count + " 条目录", count);
        } catch (Exception e) {
            log.error("目录导入失败", e);
            return R.fail("导入失败: " + e.getMessage());
        }
    }

    /**
     * 导出目录为Excel文件
     *
     * @param repoType 库类型
     */
    @Operation(summary = "导出目录", description = "将目录导出为Excel文件")
    @GetMapping("/export")
    public ResponseEntity<byte[]> export(@Parameter(description = "库类型") @RequestParam Integer repoType) throws java.io.UnsupportedEncodingException {
        byte[] data = directoryService.export(repoType);
        String fileName = "directory_export_" + repoType + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''" + java.net.URLEncoder.encode(fileName, "UTF-8"))
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .contentLength(data.length)
                .body(data);
    }
}
