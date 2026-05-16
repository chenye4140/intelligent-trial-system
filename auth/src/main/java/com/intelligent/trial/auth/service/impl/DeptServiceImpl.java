package com.intelligent.trial.auth.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.intelligent.trial.auth.dto.DeptDTO;
import com.intelligent.trial.auth.entity.SysDept;
import com.intelligent.trial.auth.mapper.SysDeptMapper;
import com.intelligent.trial.auth.mapper.SysUserMapper;
import com.intelligent.trial.auth.service.ISysDeptService;
import com.intelligent.trial.auth.vo.DeptTreeVO;
import com.intelligent.trial.auth.vo.UserVO;
import com.intelligent.trial.common.exception.BusinessException;
import com.intelligent.trial.common.exception.ErrorCode;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 部门管理服务实现类
 */
@Service
public class DeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements ISysDeptService {

    @Autowired
    private SysUserMapper userMapper;

    @Override
    public List<DeptTreeVO> getDeptTree() {
        List<SysDept> allDepts = baseMapper.selectAllActiveDepts();
        return buildDeptTree(allDepts, 0L);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addDept(DeptDTO dto) {
        SysDept dept = new SysDept();
        BeanUtils.copyProperties(dto, dept);
        dept.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        dept.setCreateTime(new Date());
        dept.setUpdateTime(new Date());
        baseMapper.insert(dept);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDept(DeptDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "部门ID不能为空");
        }
        SysDept existDept = baseMapper.selectById(dto.getId());
        if (existDept == null) {
            throw new BusinessException(ErrorCode.AUTH_DEPT_NOT_FOUND);
        }
        SysDept dept = new SysDept();
        BeanUtils.copyProperties(dto, dept);
        dept.setUpdateTime(new Date());
        baseMapper.updateById(dept);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDept(Long id) {
        SysDept existDept = baseMapper.selectById(id);
        if (existDept == null) {
            throw new BusinessException(ErrorCode.AUTH_DEPT_NOT_FOUND);
        }
        // 检查是否有子部门
        List<SysDept> children = baseMapper.selectByParentId(id);
        if (children != null && !children.isEmpty()) {
            throw new BusinessException(ErrorCode.AUTH_DEPT_HAS_CHILDREN);
        }
        baseMapper.deleteById(id);
    }

    @Override
    public List<UserVO> getUsersByDeptId(Long deptId) {
        return userMapper.selectUsersByDeptId(deptId);
    }

    /**
     * 构建部门树
     */
    private List<DeptTreeVO> buildDeptTree(List<SysDept> depts, Long parentId) {
        List<DeptTreeVO> tree = new ArrayList<>();
        if (depts == null) return tree;

        for (SysDept dept : depts) {
            if (parentId.equals(dept.getParentId())) {
                DeptTreeVO node = new DeptTreeVO();
                BeanUtils.copyProperties(dept, node);
                node.setChildren(buildDeptTree(depts, dept.getId()));
                tree.add(node);
            }
        }

        // 按 sort 排序
        tree.sort(Comparator.comparing(DeptTreeVO::getSort, Comparator.nullsLast(Comparator.naturalOrder())));
        return tree;
    }
}
