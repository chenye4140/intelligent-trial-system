package com.intelligent.trial.repository.controller;

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

/**
 * 目录管理 API 控制器
 * 提供目录 CRUD、树形查询、移动、排序、导入/导出等功能
 */
@RestController
@RequestMapping("/api/repository/directory")
public class DirectoryController {

    private final DirectoryService directoryService;

    public DirectoryController(DirectoryService directoryService) {
        this.directoryService = directoryService;
    }

    // ==================== 基础 CRUD ====================

    /**
     * 创建目录
     */
    @PostMapping("/create")
    public R<Directory> create(@RequestBody Directory directory) {
        Directory created = directoryService.create(directory);
        return R.ok(created);
    }

    /**
     * 更新目录
     */
    @PutMapping("/update")
    public R<Directory> update(@RequestBody Directory directory) {
        Directory updated = directoryService.update(directory);
        return R.ok(updated);
    }

    /**
     * 删除目录（级联删除子目录）
     */
    @DeleteMapping("/delete/{id}")
    public R<Void> delete(@PathVariable Long id) {
        directoryService.delete(id);
        return R.ok();
    }

    /**
     * 根据ID获取目录详情
     */
    @GetMapping("/get/{id}")
    public R<Directory> getById(@PathVariable Long id) {
        return R.ok(directoryService.getById(id));
    }

    // ==================== 树形查询 ====================

    /**
     * 获取目录树（按库类型）
     *
     * @param repoType 库类型：1=法规库, 2=资料库, 3=裁判文书库, 4=案例库
     */
    @GetMapping("/tree")
    public R<List<DirectoryTreeVO>> getTree(@RequestParam Integer repoType) {
        return R.ok(directoryService.getTree(repoType));
    }

    // ==================== 移动和排序 ====================

    /**
     * 移动目录到新的父目录下
     *
     * @param id          目录ID
     * @param newParentId 新父目录ID（0表示移到根目录）
     */
    @PutMapping("/move/{id}")
    public R<Void> move(@PathVariable Long id, @RequestParam Long newParentId) {
        directoryService.move(id, newParentId);
        return R.ok();
    }

    /**
     * 更新目录排序值
     *
     * @param id   目录ID
     * @param sort 排序值
     */
    @PutMapping("/sort/{id}")
    public R<Void> updateSort(@PathVariable Long id, @RequestParam Integer sort) {
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
    @PostMapping("/import")
    public R<Integer> batchImport(@RequestParam Integer repoType,
                                  @RequestParam("file") MultipartFile file) {
        try {
            int count = directoryService.batchImport(repoType, file.getInputStream());
            return R.ok("成功导入 " + count + " 条目录", count);
        } catch (Exception e) {
            return R.fail("导入失败: " + e.getMessage());
        }
    }

    /**
     * 导出目录为Excel文件
     *
     * @param repoType 库类型
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> export(@RequestParam Integer repoType) {
        byte[] data = directoryService.export(repoType);
        String fileName = "directory_export_" + repoType + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''" + java.net.URLEncoder.encode(fileName, StandardCharsets.UTF_8))
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .contentLength(data.length)
                .body(data);
    }
}
