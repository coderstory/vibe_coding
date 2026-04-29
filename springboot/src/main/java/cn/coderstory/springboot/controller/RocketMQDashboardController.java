package cn.coderstory.springboot.controller;

import cn.coderstory.springboot.service.RocketMQAdminService;
import cn.coderstory.springboot.vo.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * RocketMQ 监控面板控制器
 * 提供集群概览、Broker状态、Topic堆积量等监控数据
 */
@RestController
@RequestMapping("/api/rocketmq/dashboard")
@RequiredArgsConstructor
public class RocketMQDashboardController {

    private final RocketMQAdminService rocketMQAdminService;

    /**
     * 获取集群概览
     * GET /api/rocketmq/dashboard/overview
     */
    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getClusterOverview() {
        Map<String, Object> overview = rocketMQAdminService.getClusterOverview();
        return ResponseEntity.ok(ApiResponse.success(overview));
    }

    /**
     * 获取 Broker 状态列表
     * GET /api/rocketmq/dashboard/brokers
     */
    @GetMapping("/brokers")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getBrokerStatusList() {
        Map<String, Object> result = rocketMQAdminService.getBrokerStatusList();
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 获取 Topic 堆积量列表
     * GET /api/rocketmq/dashboard/topics
     */
    @GetMapping("/topics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTopicBacklogList() {
        Map<String, Object> result = rocketMQAdminService.getTopicBacklogList();
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 获取 Broker 运行时指标
     * GET /api/rocketmq/dashboard/broker/{brokerName}/metrics
     */
    @GetMapping("/broker/{brokerName}/metrics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getBrokerMetrics(@PathVariable String brokerName) {
        Map<String, Object> metrics = rocketMQAdminService.getBrokerMetrics(brokerName);
        return ResponseEntity.ok(ApiResponse.success(metrics));
    }
}