package com.intelligent.trial.document.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.intelligent.trial.common.annotation.RequireLog;
import com.intelligent.trial.common.annotation.RequirePermission;
import com.intelligent.trial.common.dto.R;
import com.intelligent.trial.document.entity.DocParseTask;
import com.intelligent.trial.document.service.DocumentParseService;
import com.intelligent.trial.document.util.MinioUtil;
import com.intelligent.trial.document.sse.ParseProgressBroadcaster;
import com.intelligent.trial.document.vo.ParseTaskVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 文档解析 Controller
 * 提供文件上传、解析任务管理、状态查询等接口
 *
 * @author intelligent-trial
 */
@Tag(name = "文档解析", description = "文件上传解析、解析任务管理等接口")
@RestController
@RequestMapping("/api/document/parse")
public class DocumentParseController {

    private static final Logger log = LoggerFactory.getLogger(DocumentParseController.class);

    @Autowired
    private DocumentParseService documentParseService;

    @Autowired
    private MinioUtil minioUtil;

    @Autowired
    private ParseProgressBroadcaster progressBroadcaster;

    /**
     * 上传文件并启动解析任务
     *
     * @param file 上传的文件（支持 .doc, .docx, .pdf, 图片）
     * @return 任务ID
     */
    @RequirePermission("document:parse:upload")
    @Operation(summary = "上传文件并解析", description = "上传文件并启动解析任务，支持 doc/docx/pdf/图片")
    @PostMapping("/upload")
    @RequireLog(module = "文档解析", action = "上传")
    public R<Long> uploadFile(@Parameter(description = "上传的文件") @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return R.fail("请选择要上传的文件");
        }

        log.info("收到文件上传请求: fileName={}, size={}",
                file.getOriginalFilename(), file.getSize());

        try {
            Long taskId = documentParseService.uploadAndCreateTask(file);
            return R.ok("文件上传成功，解析任务已启动", taskId);
        } catch (IllegalArgumentException e) {
            return R.fail(400, e.getMessage());
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return R.fail("文件上传失败，请稍后重试");
        }
    }

    /**
     * 批量上传文件
     *
     * @param files 上传的文件列表
     * @return 任务ID列表
     */
    @RequirePermission("document:parse:batchUpload")
    @Operation(summary = "批量上传文件", description = "批量上传多个文件并启动解析任务")
    @PostMapping("/upload/batch")
    @RequireLog(module = "文档解析", action = "批量上传")
    public R<List<Long>> uploadFiles(@Parameter(description = "文件列表") @RequestParam("files") MultipartFile[] files) {
        if (files == null || files.length == 0) {
            return R.fail("请选择要上传的文件");
        }

        log.info("收到批量文件上传请求: count={}", files.length);
        List<Long> taskIds = new java.util.ArrayList<>();
        List<String> errors = new java.util.ArrayList<>();

        for (MultipartFile file : files) {
            try {
                Long taskId = documentParseService.uploadAndCreateTask(file);
                taskIds.add(taskId);
            } catch (Exception e) {
                log.error("文件上传失败: {}", file.getOriginalFilename(), e);
                if (e instanceof IllegalArgumentException) {
                    errors.add(file.getOriginalFilename() + ": " + e.getMessage());
                } else {
                    errors.add(file.getOriginalFilename() + ": 上传失败，请稍后重试");
                }
            }
        }

        if (errors.isEmpty()) {
            return R.ok("全部上传成功", taskIds);
        } else if (taskIds.isEmpty()) {
            return R.fail("全部上传失败: " + String.join("; ", errors));
        } else {
            return R.ok("部分上传成功，失败: " + String.join("; ", errors), taskIds);
        }
    }

    /**
     * 查询任务状态
     *
     * @param taskId 任务ID
     * @return 任务详情
     */
    @Operation(summary = "查询任务状态", description = "根据任务ID查询解析任务状态和进度")
    @GetMapping("/task/{taskId}")
    public R<ParseTaskVO> getTaskStatus(@Parameter(description = "任务ID") @PathVariable Long taskId) {
        DocParseTask task = documentParseService.getTaskById(taskId);
        if (task == null) {
            return R.fail(404, "任务不存在");
        }

        ParseTaskVO vo = convertToVO(task);
        // 附加文件访问 URL
        if (task.getFilePath() != null) {
            try {
                vo.setFileUrl(minioUtil.getFileUrl(task.getFilePath(), 60));
            } catch (Exception e) {
                log.warn("获取文件URL失败: {}", task.getFilePath(), e);
            }
        }

        return R.ok(vo);
    }

    /**
     * SSE 实时订阅任务进度
     * 客户端通过 EventSource 连接此端点，实时接收解析进度更新
     * 事件格式：event: progress\ndata: {"progress":50,"status":1,"message":"解析中","timestamp":...}
     *
     * @param taskId 任务ID
     * @return SseEmitter 流
     */
    @Operation(summary = "SSE 订阅任务进度", description = "通过 Server-Sent Events 实时接收解析任务进度更新")
    @GetMapping(value = "/task/{taskId}/progress/stream", produces = org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeProgress(@Parameter(description = "任务ID") @PathVariable Long taskId) {
        DocParseTask task = documentParseService.getTaskById(taskId);
        if (task == null) {
            throw new com.intelligent.trial.common.exception.BusinessException(
                    com.intelligent.trial.common.exception.ErrorCode.DOC_PARSE_TASK_NOT_FOUND.getCode(),
                    "任务不存在");
        }

        log.info("客户端订阅 SSE 进度: taskId={}, 当前状态={}", taskId, task.getStatus());

        SseEmitter emitter = progressBroadcaster.subscribe(taskId);

        // 立即发送当前状态作为初始值
        try {
            String initialData = com.alibaba.fastjson2.JSON.toJSONString(
                    new ParseProgressBroadcaster.ProgressEvent(
                            task.getProgress(), task.getStatus(), getStatusDesc(task.getStatus())));
            emitter.send(SseEmitter.event()
                    .name("progress")
                    .data(initialData));
        } catch (Exception e) {
            log.warn("发送初始 SSE 状态失败: taskId={}", taskId, e);
        }

        return emitter;
    }

    /**
     * 分页查询任务列表
     *
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @param status   状态筛选（可选）
     * @param fileType 文件类型筛选（可选）
     * @return 分页结果
     */
    @Operation(summary = "分页查询任务列表", description = "分页查询解析任务列表，支持按状态和文件类型筛选")
    @GetMapping("/tasks")
    public R<com.intelligent.trial.common.dto.PageResult<ParseTaskVO>> listTasks(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "任务状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "文件类型") @RequestParam(required = false) String fileType) {

        Page<DocParseTask> resultPage = documentParseService.listTasks(pageNum, pageSize, status, fileType);

        List<ParseTaskVO> voList = resultPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return R.ok(com.intelligent.trial.common.dto.PageResult.of(
                resultPage.getTotal(),
                (int) resultPage.getCurrent(),
                (int) resultPage.getSize(),
                voList
        ));
    }

    /**
     * 获取任务解析结果详情
     *
     * @param taskId 任务ID
     * @return 解析结果 JSON
     */
    @Operation(summary = "获取任务解析结果", description = "获取指定任务的完整解析结果JSON")
    @GetMapping("/task/{taskId}/result")
    public R<String> getTaskResult(@Parameter(description = "任务ID") @PathVariable Long taskId) {
        DocParseTask task = documentParseService.getTaskById(taskId);
        if (task == null) {
            return R.fail(404, "任务不存在");
        }
        if (task.getStatus() != 2) {
            return R.fail("解析尚未完成，当前状态: " + getStatusDesc(task.getStatus()));
        }
        return R.ok(task.getResultJson());
    }

    /**
     * 删除解析任务
     *
     * @param taskId 任务ID
     * @return 操作结果
     */
    @RequirePermission("document:parse:delete")
    @Operation(summary = "删除解析任务", description = "删除指定的解析任务")
    @DeleteMapping("/task/{taskId}")
    @RequireLog(module = "文档解析", action = "删除任务")
    public R<Void> deleteTask(@Parameter(description = "任务ID") @PathVariable Long taskId) {
        DocParseTask task = documentParseService.getTaskById(taskId);
        if (task == null) {
            return R.fail(404, "任务不存在");
        }

        documentParseService.deleteTaskById(taskId);
        return R.ok();
    }

    /**
     * 重试失败的解析任务
     *
     * @param taskId 任务ID
     * @return 操作结果
     */
    @RequirePermission("document:parse:retry")
    @Operation(summary = "重试解析任务", description = "重新执行失败的解析任务")
    @PostMapping("/task/{taskId}/retry")
    @RequireLog(module = "文档解析", action = "重试")
    public R<Void> retryTask(@Parameter(description = "任务ID") @PathVariable Long taskId) {
        DocParseTask task = documentParseService.getTaskById(taskId);
        if (task == null) {
            return R.fail(404, "任务不存在");
        }
        if (task.getStatus() != 3) {
            return R.fail("只有失败的任务才能重试");
        }

        // 重置状态并重新触发解析
        task.setStatus(0);
        task.setProgress(0);
        task.setErrorMsg(null);
        task.setParseTime(null);
        task.setVectorCount(0);
        task.setUpdateTime(new java.util.Date());
        documentParseService.retryTask(task.getId());
        return R.ok();
    }

    // ========================= 辅助方法 =========================

    /**
     * 将实体转换为 VO
     */
    private ParseTaskVO convertToVO(DocParseTask task) {
        ParseTaskVO vo = new ParseTaskVO();
        BeanUtils.copyProperties(task, vo);
        return vo;
    }

    /**
     * 获取状态描述
     */
    private String getStatusDesc(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "待解析";
            case 1: return "解析中";
            case 2: return "已完成";
            case 3: return "解析失败";
            default: return "未知";
        }
    }
}
