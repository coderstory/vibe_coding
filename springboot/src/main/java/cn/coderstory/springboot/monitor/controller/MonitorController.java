package cn.coderstory.springboot.monitor.controller;

import cn.coderstory.springboot.vo.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/monitor")
@RequiredArgsConstructor
public class MonitorController {
    private final StringRedisTemplate redisTemplate;

    @GetMapping("/metrics")
    public ApiResponse<Map<String, Object>> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        String qpsKey = "seckill:qps:*";
        String concurrentCount = redisTemplate.opsForValue().get("seckill:processing:count");

        metrics.put("concurrentCount", concurrentCount != null ? concurrentCount : "0");
        metrics.put("timestamp", System.currentTimeMillis());

        return ApiResponse.success(metrics);
    }

    @GetMapping("/health")
    public ApiResponse<Map<String, String>> health() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        return ApiResponse.success(health);
    }
}