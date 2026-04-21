package cn.coderstory.springboot.monitor.controller;

import cn.coderstory.springboot.vo.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 监控控制器
 *
 * 功能描述：
 * - 提供系统监控指标查询接口
 * - 提供健康检查接口
 *
 * 监控指标：
 * - 当前并发处理数
 * - 请求 QPS
 * - 系统时间戳
 *
 * @author system
 * @version 1.0
 * @since 2026-04-20
 */
@RestController
@RequestMapping("/api/monitor")
@RequiredArgsConstructor
public class MonitorController {

    private final StringRedisTemplate redisTemplate;

    /**
     * 获取系统监控指标
     *
     * 返回当前系统的实时监控数据，包括：
     * - concurrentCount: 当前正在处理的请求数
     * - timestamp: 服务器时间戳
     *
     * @return 监控指标数据
     */
    @GetMapping("/metrics")
    public ApiResponse<Map<String, Object>> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        String qpsKey = "seckill:qps:*";
        String concurrentCount = redisTemplate.opsForValue().get("seckill:processing:count");

        metrics.put("concurrentCount", concurrentCount != null ? concurrentCount : "0");
        metrics.put("timestamp", System.currentTimeMillis());

        return ApiResponse.success(metrics);
    }

    /**
     * 健康检查接口
     *
     * 用于负载均衡器和监控系统的健康探测
     *
     * @return 健康状态
     */
    @GetMapping("/health")
    public ApiResponse<Map<String, String>> health() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        return ApiResponse.success(health);
    }
}
