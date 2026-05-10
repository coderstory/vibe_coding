package cn.coderstory.springboot.sse.monitor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 硬件监控 SSE 广播服务。
 * <p>
 * 使用广播模式向所有连接的客户端推送硬件指标数据。
 * 每个客户端使用 UUID 作为唯一连接标识，连接池使用 ConcurrentHashMap 保证线程安全。
 * 广播时遍历所有 emitter 逐个发送，发送失败自动移除断线连接。
 * SseEmitter 使用 0L（永不超时），超时由底层 Tomcat 连接超时控制。
 *
 * @since 1.8.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HardwareSseService {

    /** SSE 连接池：clientId -> SseEmitter */
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    /**
     * 创建 SSE 连接。
     * <p>
     * 生成 UUID clientId，创建 SseEmitter 并注册回调。
     * 发送初始连接事件后返回 emitter。
     *
     * @return SseEmitter SSE 连接发射器
     */
    public SseEmitter createEmitter() {
        String clientId = UUID.randomUUID().toString();
        // 0L 表示 SseEmitter 不主动超时，由底层 Tomcat 连接超时控制
        SseEmitter emitter = new SseEmitter(0L);

        emitter.onCompletion(() -> {
            log.debug("SSE completed: {}", clientId);
            emitters.remove(clientId);
        });
        emitter.onTimeout(() -> {
            log.debug("SSE timeout: {}", clientId);
            emitters.remove(clientId);
        });
        emitter.onError(e -> {
            log.warn("SSE client {} error: {}", clientId, e.getMessage());
            emitters.remove(clientId);
        });

        emitters.put(clientId, emitter);

        try {
            emitter.send(SseEmitter.event()
                .name("connected")
                .data("{\"status\":\"connected\"}"));
        } catch (IOException e) {
            log.warn("SSE initial event failed for {}: {}", clientId, e.getMessage());
            emitter.complete();
        }

        log.debug("SSE connected: {} (total: {})", clientId, emitters.size());
        return emitter;
    }

    /**
     * 广播事件到所有 SSE 客户端。
     * <p>
     * 遍历所有 emitter 发送事件数据，发送失败时自动移除断线连接。
     * 连接池为空时直接返回，不遍历空池。
     *
     * @param eventName 事件名称
     * @param data      事件数据（自动 JSON 序列化）
     */
    public void broadcast(String eventName, Object data) {
        if (emitters.isEmpty()) {
            return;
        }

        emitters.forEach((id, emitter) -> {
            try {
                emitter.send(SseEmitter.event()
                    .name(eventName)
                    .data(data, org.springframework.http.MediaType.APPLICATION_JSON));
            } catch (IOException e) {
                log.debug("SSE broadcast failed for {}: {}", id, e.getMessage());
                emitters.remove(id);
            }
        });
    }

    /**
     * 获取当前活跃连接数。
     *
     * @return 活跃连接数
     */
    public int getActiveConnectionCount() {
        return emitters.size();
    }

    /**
     * 取消订阅。
     * <p>
     * 移除指定 clientId 的连接并调用 complete() 关闭连接。
     *
     * @param clientId 客户端 ID
     */
    public void unsubscribe(String clientId) {
        SseEmitter emitter = emitters.remove(clientId);
        if (emitter != null) {
            emitter.complete();
            log.debug("SSE unsubscribed: {}", clientId);
        }
    }
}
