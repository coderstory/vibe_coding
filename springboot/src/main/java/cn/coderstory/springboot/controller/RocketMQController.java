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
 * RocketMQ Topic 管理控制器
 * 提供 Topic 的查看、创建、删除等 RESTful API
 */
@Slf4j
@RestController
@RequestMapping("/api/rocketmq/topics")
@RequiredArgsConstructor
public class RocketMQController {

    private final RocketMQAdminService rocketMQAdminService;

    /**
     * 获取 Topic 列表
     * GET /api/rocketmq/topics?keyword=xxx
     */
    @GetMapping
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
    @GetMapping("/{topicName}")
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
    @PostMapping
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
    @DeleteMapping("/{topicName}")
    public ResponseEntity<ApiResponse<Void>> deleteTopic(@PathVariable String topicName) {
        rocketMQAdminService.deleteTopic(topicName);
        return ResponseEntity.ok(ApiResponse.success("Topic 删除成功", null));
    }
}
