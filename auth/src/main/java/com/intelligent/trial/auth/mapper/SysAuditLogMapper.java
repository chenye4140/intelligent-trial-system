package com.intelligent.trial.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.intelligent.trial.auth.entity.SysAuditLog;
import com.intelligent.trial.auth.vo.AuditLogVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 审计日志 Mapper 接口
 */
@Mapper
public interface SysAuditLogMapper extends BaseMapper<SysAuditLog> {

    /**
     * 分页查询审计日志
     *
     * @param page      分页对象
     * @param module    模块
     * @param action    操作
     * @param userId    用户ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 分页审计日志VO列表
     */
    Page<AuditLogVO> selectLogPage(Page<AuditLogVO> page,
                                   @Param("module") String module,
                                   @Param("action") String action,
                                   @Param("userId") Long userId,
                                   @Param("startTime") String startTime,
                                   @Param("endTime") String endTime);
}
