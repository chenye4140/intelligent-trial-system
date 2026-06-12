package com.intelligent.trial.document.sse;

import com.alibaba.fastjson2.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 解析任务 SSE 进度广播器
 * 管理每个任务的 SseEmitter 连接列表，支持多客户端订阅同一任务进度
 * 当异步解析任务更新进度时，通过此广播器实时推送给所有订阅的客户端
 *
 * @author intelligent-trial
 */
@Component
public class ParseProgressBroadcaster {

    private static final Logger log = LoggerFactory.getLogger(ParseProgressBroadcaster.class);

    /**
     * SSE 连接超时时间（5 分钟）
     */
    private static final long SSE_TIMEOUT = 5 * 60 * 1000L;

    /**
     * taskId -> SseEmitter 列表
     */
    private final Map<Long, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    /**
     * 订阅指定任务的进度更新
     *
     * @param taskId 任务ID
     * @return SseEmitter 实例
     */
    public SseEmitter subscribe(Long taskId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        emitters.computeIfAbsent(taskId, k -> new ArrayList<>()).add(emitter);

        // 连接完成时移除
        emitter.onCompletion(() -> removeEmitter(taskId, emitter));
        emitter.onTimeout(() -> removeEmitter(taskId, emitter));
        emitter.onError((e) -> removeEmitter(taskId, emitter));

        log.debug("客户端订阅任务进度: taskId={}, 当前订阅数={}", taskId, getSubscriberCount(taskId));
        return emitter;
    }

    /**
     * 广播任务进度更新
     *
     * @param taskId   任务ID
     * @param progress 进度 (0-100)
     * @param status   状态 (0=待处理, 1=处理中, 2=已完成, 3=失败)
     * @param message  附加消息（可选）
     */
    public void broadcast(Long taskId, Integer progress, Integer status, String message) {
        List<SseEmitter> targetEmitters = emitters.get(taskId);
        if (targetEmitters == null || targetEmitters.isEmpty()) {
            return;
        }

        // 构建 SSE 事件数据
        String data = JSON.toJSONString(new ProgressEvent(progress, status, message));

        List<SseEmitter> deadEmitters = new ArrayList<>();
        for (SseEmitter emitter : targetEmitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("progress")
                        .data(data));
            } catch (IOException e) {
                log.warn("SSE 发送失败，标记为无效: taskId={}", taskId, e);
                deadEmitters.add(emitter);
            }
        }

        // 清理失效的连接
        for (SseEmitter dead : deadEmitters) {
            removeEmitter(taskId, dead);
        }
    }

    /**
     * 广播任务完成事件
     *
     * @param taskId 任务ID
     */
    public void broadcastComplete(Long taskId) {
        broadcast(taskId, 100, 2, "解析完成");
        // 完成后关闭所有连接
        closeAll(taskId);
    }

    /**
     * 广播任务失败事件
     *
     * @param taskId 任务ID
     * @param error  错误信息
     */
    public void broadcastError(Long taskId, String error) {
        broadcast(taskId, 0, 3, error != null ? error : "解析失败");
        closeAll(taskId);
    }

    /**
     * 获取指定任务的订阅数
     */
    public int getSubscriberCount(Long taskId) {
        List<SseEmitter> list = emitters.get(taskId);
        return list != null ? list.size() : 0;
    }

    /**
     * 关闭指定任务的所有 SSE 连接
     */
    private void closeAll(Long taskId) {
        List<SseEmitter> list = emitters.remove(taskId);
        if (list != null) {
            for (SseEmitter emitter : list) {
                try {
                    emitter.complete();
                } catch (Exception e) {
                    // ignore
                }
            }
            log.debug("关闭任务所有 SSE 连接: taskId={}, 连接数={}", taskId, list.size());
        }
    }

    /**
     * 移除单个 Emitter
     */
    private void removeEmitter(Long taskId, SseEmitter emitter) {
        List<SseEmitter> list = emitters.get(taskId);
        if (list != null) {
            list.remove(emitter);
            if (list.isEmpty()) {
                emitters.remove(taskId);
            }
        }
    }

    /**
     * SSE 进度事件数据
     */
    public static class ProgressEvent {
        private Integer progress;
        private Integer status;
        private String message;
        private Long timestamp;

        public ProgressEvent(Integer progress, Integer status, String message) {
            this.progress = progress;
            this.status = status;
            this.message = message;
            this.timestamp = System.currentTimeMillis();
        }

        public Integer getProgress() {
            return progress;
        }

        public void setProgress(Integer progress) {
            this.progress = progress;
        }

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Long timestamp) {
            this.timestamp = timestamp;
        }
    }
}
