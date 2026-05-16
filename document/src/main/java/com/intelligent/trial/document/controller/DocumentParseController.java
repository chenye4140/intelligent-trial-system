package com.intelligent.trial.document.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.intelligent.trial.common.dto.R;
import com.intelligent.trial.document.entity.DocParseTask;
import com.intelligent.trial.document.service.DocumentParseService;
import com.intelligent.trial.document.util.MinioUtil;
import com.intelligent.trial.document.vo.ParseTaskVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 文档解析 Controller
 * 提供文件上传、解析任务管理、状态查询等接口
 *
 * @author intelligent-trial
 */
@RestController
@RequestMapping("/api/document/parse")
public class DocumentParseController {

    private static final Logger log = LoggerFactory.getLogger(DocumentParseController.class);

    @Autowired
    private DocumentParseService documentParseService;

    @Autowired
    private MinioUtil minioUtil;

    /**
     * 上传文件并启动解析任务
     *
     * @param file 上传的文件（支持 .doc, .docx, .pdf, 图片）
     * @return 任务ID
     */
    @PostMapping("/upload")
    public R<Long> uploadFile(@RequestParam("file") MultipartFile file) {
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
            return R.fail("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 批量上传文件
     *
     * @param files 上传的文件列表
     * @return 任务ID列表
     */
    @PostMapping("/upload/batch")
    public R<List<Long>> uploadFiles(@RequestParam("files") MultipartFile[] files) {
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
                errors.add(file.getOriginalFilename() + ": " + e.getMessage());
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
    @GetMapping("/task/{taskId}")
    public R<ParseTaskVO> getTaskStatus(@PathVariable Long taskId) {
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
     * 分页查询任务列表
     *
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @param status   状态筛选（可选）
     * @param fileType 文件类型筛选（可选）
     * @return 分页结果
     */
    @GetMapping("/tasks")
    public R<com.intelligent.trial.common.dto.PageResult<ParseTaskVO>> listTasks(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String fileType) {

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
    @GetMapping("/task/{taskId}/result")
    public R<String> getTaskResult(@PathVariable Long taskId) {
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
    @DeleteMapping("/task/{taskId}")
    public R<Void> deleteTask(@PathVariable Long taskId) {
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
    @PostMapping("/task/{taskId}/retry")
    public R<Void> retryTask(@PathVariable Long taskId) {
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
