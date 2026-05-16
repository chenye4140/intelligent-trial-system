package com.intelligent.trial.repository.service;

import com.intelligent.trial.repository.entity.Directory;
import com.intelligent.trial.repository.vo.DirectoryTreeVO;

import java.io.InputStream;
import java.util.List;

/**
 * 目录管理服务接口
 */
public interface DirectoryService {

    /**
     * 创建目录
     *
     * @param directory 目录信息
     * @return 创建后的目录
     */
    Directory create(Directory directory);

    /**
     * 更新目录
     *
     * @param directory 目录信息
     * @return 更新后的目录
     */
    Directory update(Directory directory);

    /**
     * 删除目录（级联删除所有子目录）
     *
     * @param id 目录ID
     */
    void delete(Long id);

    /**
     * 根据ID查询目录
     *
     * @param id 目录ID
     * @return 目录信息
     */
    Directory getById(Long id);

    /**
     * 获取目录树（按库类型）
     *
     * @param repoType 库类型
     * @return 树形结构目录列表
     */
    List<DirectoryTreeVO> getTree(Integer repoType);

    /**
     * 移动目录到新的父目录下
     *
     * @param id       目录ID
     * @param newParentId 新父目录ID（0表示移到根目录）
     */
    void move(Long id, Long newParentId);

    /**
     * 更新目录排序
     *
     * @param id   目录ID
     * @param sort 新排序值
     */
    void updateSort(Long id, Integer sort);

    /**
     * 批量导入目录（从Excel InputStream）
     *
     * @param repoType 库类型
     * @param inputStream Excel 文件流
     * @return 导入成功的数量
     */
    int batchImport(Integer repoType, InputStream inputStream);

    /**
     * 导出目录为Excel（按库类型）
     *
     * @param repoType 库类型
     * @return Excel 文件字节数组
     */
    byte[] export(Integer repoType);
}
