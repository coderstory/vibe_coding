package cn.coderstory.springboot.controller.rocketmq;

import cn.coderstory.springboot.service.rocketmq.RocketMQAdminService;
import cn.coderstory.springboot.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RocketMQ 管理控制器。
 * <p>
 * 提供 Topic 和 Consumer Group 的 RESTful API，包括消息查询、消息轨迹和消息发送功能。
 *
 * @since 1.7.0
 */
@Slf4j
@RestController
@RequestMapping("/api/rocketmq")
@RequiredArgsConstructor
public class RocketMQController {

    private final RocketMQAdminService rocketMQAdminService;

    // ==================== Topic 管理 ====================

    /**
     * 获取 Topic 列表。
     *
     * @param keyword 搜索关键字（可选，按名称模糊匹配）
     * @return Topic 列表及总数
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
     * 根据 Topic 名称获取详细信息。
     *
     * @param topicName Topic 名称
     * @return Topic 详情数据
     */
    @GetMapping("/topics/{topicName}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTopicDetail(
        @PathVariable String topicName) {
        Map<String, Object> detail = rocketMQAdminService.getTopicDetail(topicName);
        return ResponseEntity.ok(ApiResponse.success(detail));
    }

    /**
     * 创建新的 Topic。
     * <p>
     * 支持指定队列数量和读写权限。默认队列数为 8，默认权限为 READ。
     *
     * @param request 包含 topicName、queueCount、perm 的请求体
     * @return 创建成功提示
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
     * 根据 Topic 名称删除指定 Topic。
     *
     * @param topicName 要删除的 Topic 名称
     * @return 删除成功提示
     */
    @DeleteMapping("/topics/{topicName}")
    public ResponseEntity<ApiResponse<Void>> deleteTopic(@PathVariable String topicName) {
        rocketMQAdminService.deleteTopic(topicName);
        return ResponseEntity.ok(ApiResponse.success("Topic 删除成功", null));
    }

    // ==================== Consumer Group 管理 ====================

    /**
     * 获取 Consumer Group 列表。
     *
     * @param keyword 搜索关键字（可选，按名称模糊匹配）
     * @return Consumer Group 列表及总数
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
     * 根据消费组名称获取详细信息。
     *
     * @param group 消费组名称
     * @return Consumer Group 详情数据
     */
    @GetMapping("/consumer-groups/{group}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getConsumerGroupDetail(
        @PathVariable String group) {
        Map<String, Object> detail = rocketMQAdminService.getConsumerGroupDetail(group);
        return ResponseEntity.ok(ApiResponse.success(detail));
    }

    /**
     * 重置 Consumer Group 在指定 Topic 上的消费位点。
     * <p>
     * 根据时间戳将消费位点回退到指定时间位置，用于重新消费历史消息。
     *
     * @param group   消费组名称
     * @param request 包含 topic 和 timestamp 的请求体
     * @return 位点重置成功提示
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

    /**
     * 删除指定 Consumer Group。
     *
     * @param group 要删除的消费组名称
     * @return 删除成功提示
     */
    @DeleteMapping("/consumer-groups/{group}")
    public ResponseEntity<ApiResponse<Void>> deleteConsumerGroup(@PathVariable String group) {
        rocketMQAdminService.deleteConsumerGroup(group);
        return ResponseEntity.ok(ApiResponse.success("Consumer Group 删除成功", null));
    }

    // ==================== 消息管理 ====================

    /**
     * 查询指定 Topic 的消息列表。
     * <p>
     * 支持按时间范围和关键字筛选，默认最多返回 100 条消息。
     *
     * @param topic    Topic 名称
     * @param startTime 起始时间戳（可选，毫秒）
     * @param endTime   结束时间戳（可选，毫秒）
     * @param maxMsg    最大返回消息数，默认 100
     * @param keyword   搜索关键字（可选）
     * @return 消息列表及总数
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
     * 根据消息 ID 查询消息详情。
     *
     * @param topic Topic 名称
     * @param msgId 消息 ID
     * @return 消息详细信息
     */
    @GetMapping("/messages/{topic}/{msgId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMessageDetail(
        @PathVariable String topic,
        @PathVariable String msgId) {
        Map<String, Object> result = rocketMQAdminService.getMessageDetail(topic, msgId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 查询消息轨迹信息。
     *
     * @param topic Topic 名称
     * @param msgId 消息 ID
     * @return 消息轨迹数据
     */
    @GetMapping("/messages/{topic}/{msgId}/trace")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMessageTrace(
        @PathVariable String topic,
        @PathVariable String msgId) {
        Map<String, Object> result = rocketMQAdminService.getMessageTrace(topic, msgId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 发送消息到指定 Topic。
     *
     * @param body 包含 topic、tags、keys、body 的消息体
     * @return 发送结果（包含消息 ID 和发送状态）
     */
    @PostMapping("/messages")
    public ResponseEntity<ApiResponse<Map<String, Object>>> sendMessage(@RequestBody Map<String, Object> body) {
        String topic = (String) body.get("topic");
        String tags = (String) body.get("tags");
        String keys = (String) body.get("keys");
        String msgBody = (String) body.get("body");
        Map<String, Object> result = rocketMQAdminService.sendMessage(topic, tags, keys, msgBody);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
