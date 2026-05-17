package com.intelligent.trial.auth.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.intelligent.trial.auth.annotation.RequirePermission;
import com.intelligent.trial.auth.service.ISysAuditLogService;
import com.intelligent.trial.auth.vo.AuditLogVO;
import com.intelligent.trial.common.dto.PageResult;
import com.intelligent.trial.common.dto.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "审计日志", description = "操作日志查询等审计日志管理接口")
@RestController
@RequestMapping("/api/system/audit-log")
public class AuditLogController {

    @Autowired private ISysAuditLogService logService;

    @Operation(summary = "分页查询审计日志", description = "多条件分页查询操作日志")
    @GetMapping("/page")
    @RequirePermission("system:audit:list")
    public R<PageResult<AuditLogVO>> page(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "模块名称") @RequestParam(required = false) String module,
            @Parameter(description = "操作类型") @RequestParam(required = false) String action,
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "开始时间") @RequestParam(required = false) String startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) String endTime) {
        Page<AuditLogVO> page = logService.pageLog(pageNum, pageSize, module, action, userId, startTime, endTime);
        return R.ok(PageResult.of(page.getTotal(), pageNum, pageSize, page.getRecords()));
    }
}
