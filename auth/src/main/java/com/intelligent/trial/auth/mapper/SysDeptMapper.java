package com.intelligent.trial.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.intelligent.trial.auth.entity.SysDept;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 部门 Mapper 接口
 */
@Mapper
public interface SysDeptMapper extends BaseMapper<SysDept> {

    /**
     * 根据父ID查询子部门
     *
     * @param parentId 父部门ID
     * @return 子部门列表
     */
    List<SysDept> selectByParentId(@Param("parentId") Long parentId);

    /**
     * 查询所有正常状态的部门
     *
     * @return 部门列表
     */
    List<SysDept> selectAllActiveDepts();
}
