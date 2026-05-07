package cn.coderstory.springboot.service.rocketmq.impl;

import cn.coderstory.springboot.service.rocketmq.RocketMQAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * RocketMQ Admin 服务 Facade
 * <p>
 * 职责：保持接口不变，将请求委托给 4 个子服务
 */
@Service
@RequiredArgsConstructor
public class RocketMQAdminServiceImpl implements RocketMQAdminService {

    private final RocketMQTopicServiceImpl topicService;
    private final RocketMQConsumerServiceImpl consumerService;
    private final RocketMQMessageServiceImpl messageService;
    private final RocketMQClusterServiceImpl clusterService;

    @Override
    public List<Map<String, Object>> getTopicList(String keyword) {
        return topicService.getTopicList(keyword);
    }

    @Override
    public Map<String, Object> getTopicDetail(String topicName) {
        return topicService.getTopicDetail(topicName);
    }

    @Override
    public void createTopic(String topicName, int queueCount, String perm) {
        topicService.createTopic(topicName, queueCount, perm);
    }

    @Override
    public void deleteTopic(String topicName) {
        topicService.deleteTopic(topicName);
    }

    @Override
    public List<Map<String, Object>> getConsumerGroupList(String keyword) {
        return consumerService.getConsumerGroupList(keyword);
    }

    @Override
    public Map<String, Object> getConsumerGroupDetail(String groupName) {
        return consumerService.getConsumerGroupDetail(groupName);
    }

    @Override
    public void resetConsumerOffset(String topic, String groupName, long timestamp) {
        consumerService.resetConsumerOffset(topic, groupName, timestamp);
    }

    @Override
    public void deleteConsumerGroup(String groupName) {
        consumerService.deleteConsumerGroup(groupName);
    }

    @Override
    public List<Map<String, Object>> getMessageList(String topic, long startTime, long endTime, int maxMsg) {
        return messageService.getMessageList(topic, startTime, endTime, maxMsg);
    }

    @Override
    public Map<String, Object> getMessageDetail(String topic, String msgId) {
        return messageService.getMessageDetail(topic, msgId);
    }

    @Override
    public Map<String, Object> getMessageTrace(String topic, String msgId) {
        return messageService.getMessageTrace(topic, msgId);
    }

    @Override
    public Map<String, Object> sendMessage(String topic, String tags, String keys, String body) {
        return messageService.sendMessage(topic, tags, keys, body);
    }

    @Override
    public Map<String, Object> getClusterOverview() {
        return clusterService.getClusterOverview();
    }

    @Override
    public Map<String, Object> getBrokerStatusList() {
        return clusterService.getBrokerStatusList();
    }

    @Override
    public Map<String, Object> getTopicBacklogList() {
        return clusterService.getTopicBacklogList();
    }

    @Override
    public Map<String, Object> getBrokerMetrics(String brokerName) {
        return clusterService.getBrokerMetrics(brokerName);
    }
}
