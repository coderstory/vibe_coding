package cn.coderstory.springboot.sse;

import cn.coderstory.springboot.config.SeckillProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * SSE 实时通知服务
 *
 * 功能说明：
 * - 管理 SSE 连接，提供实时消息推送能力
 * - 用于秒杀结果的实时通知
 *
 * 核心功能：
 * 1. 订阅接口 - 建立 SSE 连接
 * 2. 推送消息 - 向指定连接推送消息
 * 3. 取消订阅 - 关闭 SSE 连接
 * 4. 心跳检测 - 保持连接活跃
 *
 * 工作流程：
 * 1. 用户发起抢购请求，获取 queueId
 * 2. 前端建立 SSE 连接，订阅该 queueId
 * 3. 后端异步处理抢购请求
 * 4. 处理完成后，通过 SSE 推送结果给前端
 * 5. 前端收到结果后展示给用户
 *
 * @author system
 * @version 1.0
 * @since 2026-04-20
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SeckillSseService {

    /**
     * SSE 连接池：queueId -> SseEmitter
     * 使用 ConcurrentHashMap 保证线程安全
     */
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    /**
     * 秒杀业务配置
     */
    private final SeckillProperties seckillProperties;

    /**
     * 默认超时时间（毫秒）
     */
    private static final long DEFAULT_TIMEOUT = 300000L;

    /**
     * 建立 SSE 订阅
     *
     * 功能说明：
     * - 为指定的 queueId 建立 SSE 连接
     * - 返回 SseEmitter 对象供 Spring MVC 使用
     *
     * @param queueId 队列ID（唯一标识一个秒杀请求）
     * @return SseEmitter SSE 连接对象
     *
     * @example
     * <pre>
     *     SseEmitter emitter = sseService.subscribe("queue-123");
     *     // 在 Controller 中返回 emitter
     * </pre>
     */
    public SseEmitter subscribe(String queueId) {
        return subscribe(queueId, DEFAULT_TIMEOUT);
    }

    /**
     * 建立 SSE 订阅（自定义超时时间）
     *
     * @param queueId 队列ID
     * @param timeout 超时时间（毫秒）
     * @return SseEmitter SSE 连接对象
     */
    public SseEmitter subscribe(String queueId, long timeout) {
        // 获取配置的超时时间，如果为0或负数则使用默认值
        long effectiveTimeout = seckillProperties.getSse().getTimeout() > 0
                ? seckillProperties.getSse().getTimeout()
                : timeout;

        log.info("建立 SSE 连接: queueId={}, timeout={}ms", queueId, effectiveTimeout);

        // 创建 SSE 发送器
        // - timeout: 连接超时时间
        // - noRetry: 不自动重试
        SseEmitter emitter = new SseEmitter(effectiveTimeout);

        // 设置连接完成回调
        emitter.onCompletion(() -> {
            log.info("SSE 连接完成: queueId={}", queueId);
            emitters.remove(queueId);
        });

        // 设置超时回调
        emitter.onTimeout(() -> {
            log.info("SSE 连接超时: queueId={}", queueId);
            emitters.remove(queueId);
        });

        // 设置错误回调
        emitter.onError(e -> {
            log.error("SSE 连接错误: queueId={}, error={}", queueId, e.getMessage());
            emitters.remove(queueId);
        });

        // 保存到连接池
        emitters.put(queueId, emitter);

        // 发送初始连接成功事件
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("SSE 连接已建立，queueId: " + queueId));
        } catch (IOException e) {
            log.error("发送初始事件失败: queueId={}", queueId, e);
            emitter.complete();
        }

        return emitter;
    }

    /**
     * 推送消息到指定队列
     *
     * 功能说明：
     * - 向指定 queueId 的连接推送消息
     * - 通常用于推送秒杀结果
     *
     * @param queueId 队列ID
     * @param eventName 事件名称
     * @param data 消息数据
     * @return 是否推送成功
     *
     * @example
     * <pre>
     *     // 推送秒杀成功结果
     *     sseService.sendToQueue("queue-123", "seckill_result", "{\"status\": 1, \"message\": \"抢购成功\"}");
     * </pre>
     */
    public boolean sendToQueue(String queueId, String eventName, Object data) {
        SseEmitter emitter = emitters.get(queueId);

        if (emitter == null) {
            log.warn("SSE 连接不存在: queueId={}", queueId);
            return false;
        }

        try {
            emitter.send(SseEmitter.event()
                    .name(eventName)
                    .data(data));

            log.info("SSE 消息推送成功: queueId={}, event={}", queueId, eventName);
            return true;
        } catch (IOException e) {
            log.error("SSE 消息推送失败: queueId={}, event={}", queueId, eventName, e);
            emitters.remove(queueId);
            return false;
        }
    }

    /**
     * 推送秒杀成功结果
     *
     * @param queueId 队列ID
     * @param orderId 订单ID
     * @param message 附加消息
     * @return 是否推送成功
     */
    public boolean sendSuccess(String queueId, Long orderId, String message) {
        Map<String, Object> result = Map.of(
                "status", 1,
                "message", message != null ? message : "抢购成功",
                "orderId", orderId
        );
        return sendToQueue(queueId, "seckill_result", result);
    }

    /**
     * 推送秒杀失败结果
     *
     * @param queueId 队列ID
     * @param reason 失败原因
     * @return 是否推送成功
     */
    public boolean sendFailed(String queueId, String reason) {
        Map<String, Object> result = Map.of(
                "status", 2,
                "message", reason != null ? reason : "抢购失败"
        );
        return sendToQueue(queueId, "seckill_result", result);
    }

    /**
     * 推送排队中状态
     *
     * @param queueId 队列ID
     * @param message 状态消息
     * @return 是否推送成功
     */
    public boolean sendWaiting(String queueId, String message) {
        Map<String, Object> result = Map.of(
                "status", 0,
                "message", message != null ? message : "排队处理中"
        );
        return sendToQueue(queueId, "seckill_status", result);
    }

    /**
     * 发送心跳检测
     *
     * 功能说明：
     * - 定期发送心跳，保持连接活跃
     * - 防止长连接因空闲被中间设备断开
     *
     * @param queueId 队列ID
     * @return 是否发送成功
     */
    public boolean sendHeartbeat(String queueId) {
        return sendToQueue(queueId, "heartbeat", "ping");
    }

    /**
     * 取消订阅
     *
     * 功能说明：
     * - 手动关闭 SSE 连接
     * - 从连接池中移除
     *
     * @param queueId 队列ID
     */
    public void unsubscribe(String queueId) {
        SseEmitter emitter = emitters.remove(queueId);
        if (emitter != null) {
            log.info("取消 SSE 订阅: queueId={}", queueId);
            emitter.complete();
        }
    }

    /**
     * 强制完成连接
     *
     * @param queueId 队列ID
     * @param reason 完成原因
     */
    public void complete(String queueId, String reason) {
        SseEmitter emitter = emitters.remove(queueId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("completed")
                        .data(reason));
                emitter.complete();
                log.info("SSE 连接已完成: queueId={}, reason={}", queueId, reason);
            } catch (IOException e) {
                log.error("SSE 连接完成失败: queueId={}", queueId, e);
            }
        }
    }

    /**
     * 获取当前活跃的连接数
     *
     * @return 活跃连接数
     */
    public int getActiveConnectionCount() {
        return emitters.size();
    }

    /**
     * 检查指定队列是否在线
     *
     * @param queueId 队列ID
     * @return 是否在线
     */
    public boolean isOnline(String queueId) {
        return emitters.containsKey(queueId);
    }
}
