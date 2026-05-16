package com.intelligent.trial.auth.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.intelligent.trial.auth.entity.SysAuditLog;
import com.intelligent.trial.auth.mapper.SysAuditLogMapper;
import com.intelligent.trial.auth.service.ISysAuditLogService;
import com.intelligent.trial.auth.vo.AuditLogVO;
import org.springframework.stereotype.Service;

/**
 * 审计日志服务实现类
 */
@Service
public class AuditLogServiceImpl extends ServiceImpl<SysAuditLogMapper, SysAuditLog> implements ISysAuditLogService {

    @Override
    public Page<AuditLogVO> pageLog(Integer pageNum, Integer pageSize, String module, String action, Long userId, String startTime, String endTime) {
        Page<AuditLogVO> page = new Page<>(pageNum, pageSize);
        return baseMapper.selectLogPage(page, module, action, userId, startTime, endTime);
    }
}
