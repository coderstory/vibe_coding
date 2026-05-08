package cn.coderstory.springboot.controller.rocketmq;

import cn.coderstory.springboot.service.rocketmq.RocketMQAdminService;
import cn.coderstory.springboot.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * RocketMQ 监控面板控制器。
 * <p>
 * 提供集群概览、Broker 状态、Topic 堆积量等监控数据。
 *
 * @since 1.7.0
 */
@RestController
@RequestMapping("/api/rocketmq/dashboard")
@RequiredArgsConstructor
public class RocketMQDashboardController {

    private final RocketMQAdminService rocketMQAdminService;

    /**
     * 获取 RocketMQ 集群概览信息。
     *
     * @return 集群概览数据（含 Broker 数量、Topic 数量、消息总量等）
     */
    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getClusterOverview() {
        Map<String, Object> overview = rocketMQAdminService.getClusterOverview();
        return ResponseEntity.ok(ApiResponse.success(overview));
    }

    /**
     * 获取所有 Broker 的状态列表。
     *
     * @return Broker 状态列表（含运行状态、读写权限等）
     */
    @GetMapping("/brokers")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getBrokerStatusList() {
        Map<String, Object> result = rocketMQAdminService.getBrokerStatusList();
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 获取所有 Topic 的消息堆积量列表。
     *
     * @return Topic 堆积量列表（含未消费消息数、延迟等）
     */
    @GetMapping("/topics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTopicBacklogList() {
        Map<String, Object> result = rocketMQAdminService.getTopicBacklogList();
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 获取指定 Broker 的运行时指标。
     *
     * @param brokerName Broker 名称
     * @return Broker 运行时指标数据（含吞吐量、内存使用等）
     */
    @GetMapping("/broker/{brokerName}/metrics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getBrokerMetrics(@PathVariable String brokerName) {
        Map<String, Object> metrics = rocketMQAdminService.getBrokerMetrics(brokerName);
        return ResponseEntity.ok(ApiResponse.success(metrics));
    }
}
