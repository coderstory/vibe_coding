package cn.coderstory.springboot.seckill.controller;

import cn.coderstory.springboot.sse.SeckillSseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RestController
@RequestMapping("/api/seckill")
@RequiredArgsConstructor
public class SeckillSseController {

    private final SeckillSseService seckillSseService;

    @GetMapping(value = "/subscribe/{queueId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@PathVariable String queueId) {
        log.info("收到 SSE 订阅请求: queueId={}", queueId);
        return seckillSseService.subscribe(queueId);
    }

    @GetMapping("/unsubscribe/{queueId}")
    public void unsubscribe(@PathVariable String queueId) {
        log.info("收到 SSE 取消订阅请求: queueId={}", queueId);
        seckillSseService.unsubscribe(queueId);
    }

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
