package com.intelligent.trial.auth.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.intelligent.trial.auth.annotation.RequirePermission;
import com.intelligent.trial.auth.service.ISysAuditLogService;
import com.intelligent.trial.auth.vo.AuditLogVO;
import com.intelligent.trial.common.dto.PageResult;
import com.intelligent.trial.common.dto.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/system/audit-log")
public class AuditLogController {

    @Autowired private ISysAuditLogService logService;

    @GetMapping("/page")
    @RequirePermission("system:audit:list")
    public R<PageResult<AuditLogVO>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        Page<AuditLogVO> page = logService.pageLog(pageNum, pageSize, module, action, userId, startTime, endTime);
        return R.ok(PageResult.of(page.getTotal(), pageNum, pageSize, page.getRecords()));
    }
}
