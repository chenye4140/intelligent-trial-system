package com.intelligent.trial.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.intelligent.trial.repository.entity.Directory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 目录 Mapper 接口
 */
@Mapper
public interface DirectoryMapper extends BaseMapper<Directory> {

    /**
     * 根据库类型和父ID查询目录列表（按排序字段升序）
     *
     * @param repoType 库类型
     * @param parentId 父目录ID
     * @return 目录列表
     */
    List<Directory> selectByRepoTypeAndParentId(@Param("repoType") Integer repoType,
                                                 @Param("parentId") Long parentId);

    /**
     * 根据路径前缀查询所有子孙目录
     *
     * @param pathPrefix 路径前缀
     * @return 目录列表
     */
    List<Directory> selectByPathPrefix(@Param("pathPrefix") String pathPrefix);

    /**
     * 查询指定目录的所有子目录数量
     *
     * @param directoryId 目录ID
     * @return 子目录数量
     */
    int countChildren(@Param("directoryId") Long directoryId);
}
