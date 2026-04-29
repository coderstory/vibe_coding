package cn.coderstory.springboot.service;

import java.util.List;
import java.util.Map;

/**
 * RocketMQ Admin 服务接口
 * 提供 Topic 的查看、创建、删除等管理功能
 */
public interface RocketMQAdminService {

    /**
     * 获取所有 Topic 列表
     * @param keyword 关键字筛选（可选，模糊匹配）
     * @return Topic 列表
     */
    List<Map<String, Object>> getTopicList(String keyword);

    /**
     * 获取 Topic 详情
     * @param topicName Topic 名称
     * @return Topic 详细信息
     */
    Map<String, Object> getTopicDetail(String topicName);

    /**
     * 创建 Topic
     * @param topicName Topic 名称
     * @param queueCount 队列数，默认 8
     * @param perm 权限，READ(4)/WRITE(2)/READ_WRITE(6)，默认 READ
     */
    void createTopic(String topicName, int queueCount, String perm);

    /**
     * 删除 Topic
     * @param topicName Topic 名称
     */
    void deleteTopic(String topicName);

    // ==================== Consumer Group 管理 ====================

    /**
     * 获取所有 Consumer Group 列表
     * @param keyword 关键字筛选（可选，模糊匹配）
     * @return Consumer Group 列表
     */
    List<Map<String, Object>> getConsumerGroupList(String keyword);

    /**
     * 获取 Consumer Group 详情
     * @param groupName Group 名称
     * @return Group 详细信息（消费进度、订阅关系、位点）
     */
    Map<String, Object> getConsumerGroupDetail(String groupName);

    /**
     * 重置消费位点（按时间戳）
     * @param topic Topic 名称
     * @param groupName Group 名称
     * @param timestamp 重置时间戳（毫秒）
     */
    void resetConsumerOffset(String topic, String groupName, long timestamp);

    /**
     * 删除 Consumer Group
     * @param groupName Group 名称
     */
    void deleteConsumerGroup(String groupName);

    // ==================== 消息管理 ====================

    /**
     * 查询消息列表
     * @param topic Topic名称
     * @param startTime 开始时间戳
     * @param endTime 结束时间戳
     * @param maxMsg 最大返回消息数（默认100）
     * @return 消息列表，每条消息包含 msgId, topic, tags, keys, timestamp, queueId, queueOffset, properties
     */
    List<Map<String, Object>> getMessageList(String topic, long startTime, long endTime, int maxMsg);

    /**
     * 查询消息详情
     * @param topic Topic名称
     * @param msgId 消息ID
     * @return 消息详情，包含所有字段和 body
     */
    Map<String, Object> getMessageDetail(String topic, String msgId);

    /**
     * 查询消息轨迹
     * @param topic Topic名称
     * @param msgId 消息ID
     * @return 轨迹数据，包含 producerInfo, consumeTraceList (Consumer Group, status, consumeTime)
     */
    Map<String, Object> getMessageTrace(String topic, String msgId);

    /**
     * 发送消息
     * @param topic Topic名称
     * @param tags Tags（可选）
     * @param keys Keys（可选）
     * @param body 消息内容
     * @return 发送结果，包含 msgId
     */
    Map<String, Object> sendMessage(String topic, String tags, String keys, String body);

    // ==================== 监控面板 ====================

    /**
     * 获取集群概览
     * @return 集群概览信息（Broker数量、Topic数量、Consumer数量、总堆积量）
     */
    Map<String, Object> getClusterOverview();

    /**
     * 获取 Broker 状态列表
     * @return Broker 状态列表
     */
    Map<String, Object> getBrokerStatusList();

    /**
     * 获取 Topic 堆积量列表
     * @return Topic 堆积量列表
     */
    Map<String, Object> getTopicBacklogList();

    /**
     * 获取 Broker 运行时指标
     * @param brokerName Broker 名称
     * @return Broker 运行时指标
     */
    Map<String, Object> getBrokerMetrics(String brokerName);
}
