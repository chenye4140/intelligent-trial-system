package com.intelligent.trial.auth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.intelligent.trial.auth.dto.DeptDTO;
import com.intelligent.trial.auth.entity.SysDept;
import com.intelligent.trial.auth.vo.DeptTreeVO;
import com.intelligent.trial.auth.vo.UserVO;

import java.util.List;

/**
 * 部门管理服务接口
 */
public interface ISysDeptService extends IService<SysDept> {

    /**
     * 获取部门树
     *
     * @return 部门树列表
     */
    List<DeptTreeVO> getDeptTree();

    /**
     * 新增部门
     *
     * @param dto 部门信息
     */
    void addDept(DeptDTO dto);

    /**
     * 更新部门
     *
     * @param dto 部门信息
     */
    void updateDept(DeptDTO dto);

    /**
     * 删除部门
     *
     * @param id 部门ID
     */
    void deleteDept(Long id);

    /**
     * 根据部门ID获取用户列表
     *
     * @param deptId 部门ID
     * @return 用户VO列表
     */
    List<UserVO> getUsersByDeptId(Long deptId);
}
