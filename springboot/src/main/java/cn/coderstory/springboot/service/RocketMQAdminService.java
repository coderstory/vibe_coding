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
     * @throws BusinessException 如果是广播模式或消费者不在线
     */
    void resetConsumerOffset(String topic, String groupName, long timestamp);
}
