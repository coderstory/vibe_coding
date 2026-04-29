package cn.coderstory.springboot.controller;

import cn.coderstory.springboot.service.RocketMQAdminService;
import cn.coderstory.springboot.vo.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RocketMQ 管理控制器
 * 提供 Topic 和 Consumer Group 的 RESTful API
 */
@Slf4j
@RestController
@RequestMapping("/api/rocketmq")
@RequiredArgsConstructor
public class RocketMQController {

    private final RocketMQAdminService rocketMQAdminService;

    // ==================== Topic 管理 ====================

    /**
     * 获取 Topic 列表
     * GET /api/rocketmq/topics?keyword=xxx
     */
    @GetMapping("/topics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTopicList(
            @RequestParam(required = false) String keyword) {
        List<Map<String, Object>> list = rocketMQAdminService.getTopicList(keyword);

        Map<String, Object> data = new HashMap<>();
        data.put("records", list);
        data.put("total", list.size());

        return ResponseEntity.ok(ApiResponse.success(data));
    }

    /**
     * 获取 Topic 详情
     * GET /api/rocketmq/topics/{topicName}
     */
    @GetMapping("/topics/{topicName}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTopicDetail(
            @PathVariable String topicName) {
        Map<String, Object> detail = rocketMQAdminService.getTopicDetail(topicName);
        return ResponseEntity.ok(ApiResponse.success(detail));
    }

    /**
     * 创建 Topic
     * POST /api/rocketmq/topics
     * Body: { "topicName": "xxx", "queueCount": 8, "perm": "READ" }
     */
    @PostMapping("/topics")
    public ResponseEntity<ApiResponse<Void>> createTopic(@RequestBody Map<String, Object> request) {
        String topicName = (String) request.get("topicName");
        Integer queueCount = 8;
        Object queueCountObj = request.get("queueCount");
        if (queueCountObj != null) {
            if (queueCountObj instanceof Number) {
                queueCount = ((Number) queueCountObj).intValue();
            }
        }
        String perm = request.get("perm") != null ? (String) request.get("perm") : "READ";

        rocketMQAdminService.createTopic(topicName, queueCount, perm);

        return ResponseEntity.ok(ApiResponse.success("Topic 创建成功", null));
    }

    /**
     * 删除 Topic
     * DELETE /api/rocketmq/topics/{topicName}
     */
    @DeleteMapping("/topics/{topicName}")
    public ResponseEntity<ApiResponse<Void>> deleteTopic(@PathVariable String topicName) {
        rocketMQAdminService.deleteTopic(topicName);
        return ResponseEntity.ok(ApiResponse.success("Topic 删除成功", null));
    }

    // ==================== Consumer Group 管理 ====================

    /**
     * 获取 Consumer Group 列表
     * GET /api/rocketmq/consumer-groups?keyword=xxx
     */
    @GetMapping("/consumer-groups")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getConsumerGroupList(
            @RequestParam(required = false) String keyword) {
        List<Map<String, Object>> list = rocketMQAdminService.getConsumerGroupList(keyword);

        Map<String, Object> data = new HashMap<>();
        data.put("records", list);
        data.put("total", list.size());

        return ResponseEntity.ok(ApiResponse.success(data));
    }

    /**
     * 获取 Consumer Group 详情
     * GET /api/rocketmq/consumer-groups/{group}
     */
    @GetMapping("/consumer-groups/{group}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getConsumerGroupDetail(
            @PathVariable String group) {
        Map<String, Object> detail = rocketMQAdminService.getConsumerGroupDetail(group);
        return ResponseEntity.ok(ApiResponse.success(detail));
    }

    /**
     * 重置消费位点
     * POST /api/rocketmq/consumer-groups/{group}/reset-offset
     * Body: { "topic": "xxx", "timestamp": 1715000000000 }
     */
    @PostMapping("/consumer-groups/{group}/reset-offset")
    public ResponseEntity<ApiResponse<Void>> resetConsumerOffset(
            @PathVariable String group,
            @RequestBody Map<String, Object> request) {
        String topic = (String) request.get("topic");
        Long timestamp = ((Number) request.get("timestamp")).longValue();

        rocketMQAdminService.resetConsumerOffset(topic, group, timestamp);

        return ResponseEntity.ok(ApiResponse.success("位点重置成功", null));
    }

    // ==================== 消息管理 ====================

    /**
     * 查询消息列表
     * GET /api/rocketmq/messages/{topic}?startTime=xxx&endTime=xxx&maxMsg=100&keyword=xxx
     */
    @GetMapping("/messages/{topic}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMessageList(
            @PathVariable String topic,
            @RequestParam(required = false) Long startTime,
            @RequestParam(required = false) Long endTime,
            @RequestParam(defaultValue = "100") Integer maxMsg,
            @RequestParam(required = false) String keyword) {
        List<Map<String, Object>> list = rocketMQAdminService.getMessageList(topic, startTime != null ? startTime : 0, endTime != null ? endTime : System.currentTimeMillis(), maxMsg);
        Map<String, Object> data = new HashMap<>();
        data.put("records", list);
        data.put("total", list.size());
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    /**
     * 查询消息详情
     * GET /api/rocketmq/messages/{topic}/{msgId}
     */
    @GetMapping("/messages/{topic}/{msgId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMessageDetail(
            @PathVariable String topic,
            @PathVariable String msgId) {
        Map<String, Object> result = rocketMQAdminService.getMessageDetail(topic, msgId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 查询消息轨迹
     * GET /api/rocketmq/messages/{topic}/{msgId}/trace
     */
    @GetMapping("/messages/{topic}/{msgId}/trace")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMessageTrace(
            @PathVariable String topic,
            @PathVariable String msgId) {
        Map<String, Object> result = rocketMQAdminService.getMessageTrace(topic, msgId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 发送消息
     * POST /api/rocketmq/messages
     */
    @PostMapping("/messages")
    public ResponseEntity<ApiResponse<Map<String, Object>>> sendMessage(
            @RequestParam String topic,
            @RequestParam(required = false) String tags,
            @RequestParam(required = false) String keys,
            @RequestBody String body) {
        Map<String, Object> result = rocketMQAdminService.sendMessage(topic, tags, keys, body);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}