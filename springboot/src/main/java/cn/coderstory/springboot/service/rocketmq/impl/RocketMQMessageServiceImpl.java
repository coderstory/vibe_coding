package cn.coderstory.springboot.service.rocketmq.impl;

import cn.coderstory.springboot.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.QueryResult;
import org.apache.rocketmq.client.consumer.DefaultMQPullConsumer;
import org.apache.rocketmq.client.consumer.PullResult;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.protocol.admin.ConsumeStats;
import org.apache.rocketmq.remoting.protocol.body.SubscriptionGroupWrapper;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class RocketMQMessageServiceImpl implements RocketMQSubService {

    private static final List<String> SYSTEM_GROUP_PREFIXES = List.of(
        "%RETRY%", "%DLQ%", "CID_RMQ_SYS_"
    );

    private final DefaultMQAdminExt defaultMQAdminExt;
    private final DefaultMQProducer messageProducer;

    @Value("${rocketmq.name-server:localhost:9876}")
    private String nameServer;

    private boolean isSystemGroup(String groupName) {
        return SYSTEM_GROUP_PREFIXES.stream().anyMatch(groupName::startsWith) ||
            groupName.contains("CID_ONSAPI") ||
            groupName.contains("OWNER") ||
            groupName.contains("_BACKUP");
    }

    public List<Map<String, Object>> getMessageList(String topic, long startTime, long endTime, int maxMsg) {
        try {
            long sevenDays = 7 * 24 * 60 * 60 * 1000L;
            if (endTime - startTime > sevenDays) {
                throw BusinessException.badRequest("时间范围不能超过 7 天");
            }

            List<Map<String, Object>> result = new ArrayList<>();
            try {
                QueryResult queryResult = defaultMQAdminExt.queryMessage(topic, "*", maxMsg, startTime, endTime);
                if (queryResult != null && queryResult.getMessageList() != null) {
                    for (MessageExt msg : queryResult.getMessageList()) {
                        result.add(buildMessageItem(msg));
                    }
                }
            } catch (Exception e) {
                log.warn("queryMessage 查询失败，尝试遍历队列方式: {}", e.getMessage());
                return getMessageListByQueue(topic, startTime, endTime, maxMsg);
            }

            result.sort((a, b) -> Long.compare((Long) b.get("timestamp"), (Long) a.get("timestamp")));
            return result;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("查询消息列表失败: topic={}", topic, e);
            throw BusinessException.badRequest("查询消息失败: " + e.getMessage());
        }
    }

    public Map<String, Object> getMessageDetail(String topic, String msgId) {
        try {
            MessageExt msg = defaultMQAdminExt.viewMessage(topic, msgId);
            if (msg == null) throw BusinessException.notFound("未找到消息: " + msgId);

            Map<String, Object> result = buildMessageItem(msg);
            result.put("body", new String(msg.getBody(), java.nio.charset.StandardCharsets.UTF_8));
            return result;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("查询消息详情失败: topic={}, msgId={}", topic, msgId, e);
            throw BusinessException.badRequest("查询消息详情失败: " + e.getMessage());
        }
    }

    public Map<String, Object> getMessageTrace(String topic, String msgId) {
        try {
            Map<String, Object> result = new HashMap<>();
            result.put("msgId", msgId);
            result.put("topic", topic);

            List<Map<String, Object>> consumeTraceList = new ArrayList<>();
            String brokerAddr = RocketMQUtils.getFirstBrokerAddr(defaultMQAdminExt, topic);
            if (brokerAddr != null) {
                try {
                    SubscriptionGroupWrapper wrapper = defaultMQAdminExt.getAllSubscriptionGroup(brokerAddr, 3000);
                    for (String group : wrapper.getSubscriptionGroupTable().keySet()) {
                        if (isSystemGroup(group)) continue;
                        try {
                            ConsumeStats stats = defaultMQAdminExt.examineConsumeStats(group);
                            if (stats != null && stats.getOffsetTable() != null && !stats.getOffsetTable().isEmpty()) {
                                Map<String, Object> trace = new HashMap<>();
                                trace.put("consumerGroup", group);
                                trace.put("status", "已消费");
                                trace.put("consumeTime", System.currentTimeMillis());
                                consumeTraceList.add(trace);
                            }
                        } catch (Exception ignored) {}
                    }
                } catch (Exception e) {
                    log.debug("获取订阅组列表失败: {}", e.getMessage());
                }
            }
            result.put("consumeTraceList", consumeTraceList);
            return result;
        } catch (Exception e) {
            log.error("查询消息轨迹失败: topic={}, msgId={}", topic, msgId, e);
            throw BusinessException.badRequest("查询消息轨迹失败: " + e.getMessage());
        }
    }

    public Map<String, Object> sendMessage(String topic, String tags, String keys, String body) {
        try {
            defaultMQAdminExt.examineTopicRouteInfo(topic);
            org.apache.rocketmq.common.message.Message message = new org.apache.rocketmq.common.message.Message(
                topic, tags != null && !tags.isEmpty() ? tags : "*",
                keys != null && !keys.isEmpty() ? keys : "",
                body.getBytes(java.nio.charset.StandardCharsets.UTF_8)
            );
            message.setWaitStoreMsgOK(false);

            org.apache.rocketmq.client.producer.SendResult sendResult = messageProducer.send(message, 3000);

            Map<String, Object> result = new HashMap<>();
            result.put("msgId", sendResult.getMsgId());
            result.put("topic", topic);
            result.put("tags", tags != null ? tags : "");
            result.put("keys", keys != null ? keys : "");
            result.put("sendStatus", sendResult.getSendStatus().toString());
            result.put("queueId", sendResult.getMessageQueue().getQueueId());
            result.put("queueOffset", sendResult.getQueueOffset());
            result.put("timestamp", System.currentTimeMillis());
            log.info("消息发送成功: topic={}, msgId={}", topic, sendResult.getMsgId());
            return result;
        } catch (Exception e) {
            log.error("发送消息失败: topic={}", topic, e);
            throw BusinessException.badRequest("发送消息失败: " + e.getMessage());
        }
    }

    private List<Map<String, Object>> getMessageListByQueue(String topic, long startTime, long endTime, int maxMsg) {
        List<Map<String, Object>> result = new ArrayList<>();
        DefaultMQPullConsumer consumer = null;
        String consumerGroup = "pull_consumer_" + topic + "_" + System.currentTimeMillis();
        try {
            consumer = new DefaultMQPullConsumer(consumerGroup);
            consumer.setNamesrvAddr(nameServer);
            consumer.setInstanceName("PullConsumer-" + topic + "-" + System.currentTimeMillis());
            consumer.start();

            Set<MessageQueue> mqs = consumer.fetchSubscribeMessageQueues(topic);
            for (MessageQueue mq : mqs) {
                if (result.size() >= maxMsg) break;
                try {
                    long minOffset = consumer.minOffset(mq);
                    long maxOffset = consumer.maxOffset(mq);
                    if (maxOffset <= minOffset) continue;

                    long offset = Math.max(minOffset, maxOffset - 100);
                    int pullCount = 0;
                    while (offset < maxOffset && result.size() < maxMsg) {
                        try {
                            PullResult pullResult = consumer.pullBlockIfNotFound(mq, null, offset, 32);
                            if (pullResult.getMsgFoundList() == null || pullResult.getMsgFoundList().isEmpty()) break;

                            for (MessageExt msg : pullResult.getMsgFoundList()) {
                                if (result.size() >= maxMsg) break;
                                long storeTime = msg.getStoreTimestamp();
                                if (storeTime >= startTime && storeTime <= endTime) {
                                    result.add(buildMessageItem(msg));
                                }
                            }
                            offset = pullResult.getNextBeginOffset();
                            if (++pullCount > 1000) break;
                        } catch (Exception e) {
                            break;
                        }
                    }
                } catch (Exception e) {
                    log.debug("遍历队列 {} 失败: {}", mq, e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("遍历队列方式查询消息失败: topic={}", topic, e);
        } finally {
            if (consumer != null) {
                consumer.shutdown();
                try {
                    String brokerAddr = RocketMQUtils.getFirstBrokerAddr(defaultMQAdminExt, topic);
                    if (brokerAddr != null) {
                        defaultMQAdminExt.deleteSubscriptionGroup(brokerAddr, consumerGroup);
                    }
                } catch (Exception e) {
                    log.debug("删除临时消费者组失败: {}", e.getMessage());
                }
            }
        }
        return result;
    }

    private Map<String, Object> buildMessageItem(MessageExt msg) {
        Map<String, Object> item = new HashMap<>();
        item.put("msgId", msg.getMsgId());
        item.put("topic", msg.getTopic());
        item.put("tags", msg.getTags() != null ? msg.getTags() : "");
        item.put("keys", msg.getKeys() != null ? msg.getKeys() : "");
        item.put("timestamp", msg.getStoreTimestamp());
        item.put("queueId", msg.getQueueId());
        item.put("queueOffset", msg.getQueueOffset());
        item.put("properties", msg.getProperties());
        return item;
    }
}
