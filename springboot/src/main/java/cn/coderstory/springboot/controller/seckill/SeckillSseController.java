package cn.coderstory.springboot.controller.seckill;

import cn.coderstory.springboot.sse.seckill.SeckillSseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 秒杀 SSE 推送控制器。
 * <p>
 * 提供秒杀结果的 Server-Sent Events 订阅、取消订阅和连接状态查询功能。
 * 客户端通过 SSE 连接实时接收秒杀结果推送。
 *
 * @since 1.7.0
 */
@Slf4j
@RestController
@RequestMapping("/api/seckill")
@RequiredArgsConstructor
public class SeckillSseController {

    private final SeckillSseService seckillSseService;

    /**
     * 订阅秒杀结果推送。
     * <p>
     * 客户端通过 SSE 协议连接到指定排队号的秒杀结果推送通道。
     *
     * @param queueId 排队号
     * @return SSE 连接发射器
     */
    @GetMapping(value = "/subscribe/{queueId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@PathVariable String queueId) {
        log.info("收到 SSE 订阅请求: queueId={}", queueId);
        return seckillSseService.subscribe(queueId);
    }

    /**
     * 取消秒杀结果订阅。
     *
     * @param queueId 排队号
     */
    @GetMapping("/unsubscribe/{queueId}")
    public void unsubscribe(@PathVariable String queueId) {
        log.info("收到 SSE 取消订阅请求: queueId={}", queueId);
        seckillSseService.unsubscribe(queueId);
    }

    /**
     * 查询 SSE 订阅连接状态。
     *
     * @param queueId 排队号
     * @return 连接状态信息（在线或已断开）
     */
    @GetMapping("/subscribe/status/{queueId}")
    public Object getConnectionStatus(@PathVariable String queueId) {
        boolean online = seckillSseService.isOnline(queueId);
        return java.util.Map.of(
            "queueId", queueId,
            "online", online,
            "message", online ? "连接在线" : "连接已断开或不存在"
        );
    }
}
