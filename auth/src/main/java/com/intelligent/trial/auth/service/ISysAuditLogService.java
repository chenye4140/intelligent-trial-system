package com.intelligent.trial.auth.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.intelligent.trial.auth.entity.SysAuditLog;
import com.intelligent.trial.auth.vo.AuditLogVO;

/**
 * 审计日志服务接口
 */
public interface ISysAuditLogService extends IService<SysAuditLog> {

    /**
     * 分页查询审计日志
     *
     * @param pageNum   页码
     * @param pageSize  每页条数
     * @param module    模块
     * @param action    操作
     * @param userId    用户ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 分页审计日志VO
     */
    Page<AuditLogVO> pageLog(Integer pageNum, Integer pageSize, String module, String action, Long userId, String startTime, String endTime);
}
