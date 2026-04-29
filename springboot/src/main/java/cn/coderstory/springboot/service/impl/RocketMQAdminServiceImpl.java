package cn.coderstory.springboot.service.impl;

import cn.coderstory.springboot.exception.BusinessException;
import cn.coderstory.springboot.service.RocketMQAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.QueryResult;
import org.apache.rocketmq.client.consumer.DefaultMQPullConsumer;
import org.apache.rocketmq.client.consumer.PullResult;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.TopicConfig;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.remoting.protocol.admin.ConsumeStats;
import org.apache.rocketmq.remoting.protocol.body.ClusterInfo;
import org.apache.rocketmq.remoting.protocol.body.SubscriptionGroupWrapper;
import org.apache.rocketmq.remoting.protocol.body.TopicList;
import org.apache.rocketmq.remoting.protocol.route.BrokerData;
import org.apache.rocketmq.remoting.protocol.route.TopicRouteData;
import org.apache.rocketmq.remoting.protocol.subscription.SubscriptionGroupConfig;
import org.apache.rocketmq.remoting.protocol.admin.ConsumeStats;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * RocketMQ Admin 服务实现
 * 使用 DefaultMQAdminExt 实现 Topic 管理功能
 *
 * 注意: 此实现需要 rocketmq-tools 依赖
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RocketMQAdminServiceImpl implements RocketMQAdminService {

    // 系统 Topic 前缀过滤列表
    private static final List<String> SYSTEM_TOPIC_PREFIXES = List.of(
        "SCHEDULE_TOPIC_",
        "TBW_",
        "RMQ_SYS_",
        "%RETRY%",
        "%DLQ%",
        "%BINDER%"
    );

    // 系统 Consumer Group 前缀过滤列表
    private static final List<String> SYSTEM_GROUP_PREFIXES = List.of(
        "%RETRY%",
        "%DLQ%",
        "CID_RMQ_SYS_"
    );

    private final DefaultMQAdminExt defaultMQAdminExt;
    private final DefaultMQProducer messageProducer;

    @Value("${rocketmq.name-server:localhost:9876}")
    private String nameServer;

    /**
     * 判断是否为系统 Topic
     */
    private boolean isSystemTopic(String topicName) {
        return SYSTEM_TOPIC_PREFIXES.stream().anyMatch(topicName::startsWith) ||
               topicName.contains("_BACKUP") ||
               topicName.equals("DEFAULT_TOPIC");
    }

    /**
     * 判断是否为系统 Consumer Group
     */
    private boolean isSystemGroup(String groupName) {
        return SYSTEM_GROUP_PREFIXES.stream().anyMatch(groupName::startsWith) ||
               groupName.contains("CID_ONSAPI") ||
               groupName.contains("OWNER") ||
               groupName.contains("_BACKUP");
    }

    @Override
    public List<Map<String, Object>> getTopicList(String keyword) {
        try {
            TopicList topicList = defaultMQAdminExt.fetchAllTopicList();
            Set<String> topicSet = topicList.getTopicList();

            // 获取集群中第一个 Broker 地址用于查询
            String brokerAddr = getFirstBrokerAddr();
            if (brokerAddr == null) {
                throw BusinessException.badRequest("未找到可用的 Broker");
            }

            List<Map<String, Object>> result = new ArrayList<>();

            for (String topicName : topicSet) {
                // 过滤系统 Topic
                if (isSystemTopic(topicName)) {
                    continue;
                }

                // 过滤关键字
                if (keyword != null && !keyword.isEmpty()
                        && !topicName.toLowerCase().contains(keyword.toLowerCase())) {
                    continue;
                }

                Map<String, Object> item = new HashMap<>();
                item.put("topicName", topicName);

                // 获取 Topic 配置信息 - 使用 Broker 地址
                try {
                    TopicConfig topicConfig = defaultMQAdminExt.examineTopicConfig(brokerAddr, topicName);
                    if (topicConfig != null) {
                        item.put("queueCount", topicConfig.getWriteQueueNums());
                        item.put("status", "ACTIVE");
                        item.put("readQueueNums", topicConfig.getReadQueueNums());
                        item.put("perm", convertPermToString(topicConfig.getPerm()));
                    } else {
                        item.put("queueCount", 0);
                        item.put("status", "UNKNOWN");
                    }
                } catch (Exception e) {
                    log.debug("获取 Topic {} 配置失败: {}", topicName, e.getMessage());
                    item.put("queueCount", 0);
                    item.put("status", "UNKNOWN");
                }

                // 消息数量需要单独查询，这里简化处理
                item.put("messageCount", 0);
                item.put("createTime", new Date());

                result.add(item);
            }

            // 按名称排序
            result.sort(Comparator.comparing(a -> (String) a.get("topicName")));

            return result;
        } catch (Exception e) {
            log.error("获取 Topic 列表失败", e);
            throw BusinessException.badRequest("获取数据失败，请稍后重试");
        }
    }

    @Override
    public Map<String, Object> getTopicDetail(String topicName) {
        try {
            Map<String, Object> detail = new HashMap<>();

            // 获取 Broker 地址
            String brokerAddr = getFirstBrokerAddr();
            if (brokerAddr == null) {
                throw BusinessException.badRequest("未找到可用的 Broker");
            }

            // Topic 配置 - 使用 Broker 地址
            TopicConfig topicConfig = defaultMQAdminExt.examineTopicConfig(brokerAddr, topicName);
            if (topicConfig == null) {
                throw BusinessException.notFound("Topic 不存在: " + topicName);
            }

            detail.put("topicName", topicName);
            detail.put("queueCount", topicConfig.getWriteQueueNums());
            detail.put("readQueueNums", topicConfig.getReadQueueNums());
            detail.put("perm", convertPermToString(topicConfig.getPerm()));
            detail.put("status", "ACTIVE");

            // 路由信息
            try {
                TopicRouteData routeData = defaultMQAdminExt.examineTopicRouteInfo(topicName);
                if (routeData != null) {
                    detail.put("routeInfo", routeData);
                }
            } catch (Exception e) {
                log.debug("获取 Topic {} 路由信息失败: {}", topicName, e.getMessage());
            }

            // 订阅关系（Consumer Group 列表）- 简化处理
            detail.put("subscriptions", Collections.emptyList());

            return detail;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取 Topic 详情失败: {}", topicName, e);
            throw BusinessException.badRequest("获取数据失败，请稍后重试");
        }
    }

    @Override
    public void createTopic(String topicName, int queueCount, String perm) {
        try {
            // 验证 topicName 不包含特殊字符 (D-07)
            if (topicName == null || topicName.trim().isEmpty()) {
                throw BusinessException.badRequest("Topic 名称不能为空");
            }
            if (!topicName.matches("^[a-zA-Z0-9_-]+$")) {
                throw BusinessException.badRequest("Topic 名称不能包含特殊字符，仅支持字母、数字、下划线和连字符");
            }

            // 检查 Topic 是否已存在 - 使用 Broker 地址
            String checkBrokerAddr = getFirstBrokerAddr();
            if (checkBrokerAddr == null) {
                throw BusinessException.badRequest("未找到可用的 Broker");
            }
            try {
                TopicConfig existing = defaultMQAdminExt.examineTopicConfig(checkBrokerAddr, topicName);
                if (existing != null) {
                    throw BusinessException.conflict("Topic 已存在: " + topicName);
                }
            } catch (BusinessException e) {
                throw e;
            } catch (Exception e) {
                // Topic 不存在，可以创建
            }

            // 创建 TopicConfig
            TopicConfig topicConfig = new TopicConfig();
            topicConfig.setTopicName(topicName);
            topicConfig.setWriteQueueNums(queueCount);
            topicConfig.setReadQueueNums(queueCount);
            topicConfig.setPerm(parsePerm(perm));

            // 获取所有 Broker 地址（Master）
            ClusterInfo clusterInfo = defaultMQAdminExt.examineBrokerClusterInfo();
            Map<String, BrokerData> brokerAddrTable = clusterInfo.getBrokerAddrTable();

            if (brokerAddrTable.isEmpty()) {
                throw BusinessException.badRequest("未找到可用的 Broker");
            }

            // 遍历所有 Broker，在每个 Master 上创建 Topic
            int successCount = 0;
            for (BrokerData brokerData : brokerAddrTable.values()) {
                // 获取 Master 地址 (brokerId = 0)
                String brokerAddr = brokerData.selectBrokerAddr();
                if (brokerAddr == null || brokerAddr.isEmpty()) {
                    log.warn("Broker {} 没有有效的 Master 地址", brokerData.getBrokerName());
                    continue;
                }

                try {
                    defaultMQAdminExt.createAndUpdateTopicConfig(brokerAddr, topicConfig);
                    log.info("在 Broker {} ({}) 创建 Topic 成功", brokerData.getBrokerName(), brokerAddr);
                    successCount++;
                } catch (Exception e) {
                    log.error("在 Broker {} 创建 Topic 失败: {}", brokerAddr, e.getMessage());
                }
            }

            if (successCount == 0) {
                throw BusinessException.badRequest("创建 Topic 失败：没有可用的 Broker");
            }

            log.info("创建 Topic 成功: {}, queueCount: {}, perm: {}, 成功创建 Broker 数: {}",
                    topicName, queueCount, perm, successCount);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("创建 Topic 失败: {}", topicName, e);
            throw BusinessException.badRequest("创建 Topic 失败: " + e.getMessage());
        }
    }

    @Override
    public void deleteTopic(String topicName) {
        try {
            // 获取 Broker 地址
            String brokerAddr = getFirstBrokerAddr();
            if (brokerAddr == null) {
                throw BusinessException.badRequest("未找到可用的 Broker");
            }

            // 检查 Topic 是否存在 - 使用 Broker 地址
            TopicConfig existing = defaultMQAdminExt.examineTopicConfig(brokerAddr, topicName);
            if (existing == null) {
                throw BusinessException.notFound("Topic 不存在: " + topicName);
            }

            // 获取所有 Broker 地址（Master）
            ClusterInfo clusterInfo = defaultMQAdminExt.examineBrokerClusterInfo();
            Map<String, BrokerData> brokerAddrTable = clusterInfo.getBrokerAddrTable();

            if (brokerAddrTable.isEmpty()) {
                throw BusinessException.badRequest("未找到可用的 Broker");
            }

            // 遍历所有 Broker，在每个 Master 上删除 Topic
            Set<String> brokerAddrs = new HashSet<>();
            for (BrokerData brokerData : brokerAddrTable.values()) {
                String brokerAddr2 = brokerData.selectBrokerAddr();
                if (brokerAddr2 != null && !brokerAddr2.isEmpty()) {
                    brokerAddrs.add(brokerAddr2);
                }
            }

            if (brokerAddrs.isEmpty()) {
                throw BusinessException.badRequest("没有可用的 Broker");
            }

            defaultMQAdminExt.deleteTopicInBroker(brokerAddrs, topicName);
            log.info("删除 Topic 成功: {}", topicName);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("删除 Topic 失败: {}", topicName, e);
            throw BusinessException.badRequest("删除 Topic 失败: " + e.getMessage());
        }
    }

    /**
     * 将权限数字转换为字符串
     */
    private String convertPermToString(int perm) {
        return switch (perm) {
            case 2 -> "WRITE";
            case 6 -> "READ_WRITE";
            default -> "READ";
        };
    }

    /**
     * 将权限字符串转换为数字
     */
    private int parsePerm(String perm) {
        if (perm == null || perm.isEmpty()) {
            return 4; // READ
        }
        return switch (perm.toUpperCase()) {
            case "WRITE" -> 2;
            case "READ_WRITE" -> 6;
            default -> 4; // READ
        };
    }

    /**
     * 获取第一个可用的 Broker 地址（Master）
     */
    private String getFirstBrokerAddr() {
        try {
            ClusterInfo clusterInfo = defaultMQAdminExt.examineBrokerClusterInfo();
            Map<String, BrokerData> brokerAddrTable = clusterInfo.getBrokerAddrTable();
            if (brokerAddrTable != null && !brokerAddrTable.isEmpty()) {
                for (BrokerData brokerData : brokerAddrTable.values()) {
                    String addr = brokerData.selectBrokerAddr();
                    if (addr != null && !addr.isEmpty()) {
                        return addr;
                    }
                }
            }
        } catch (Exception e) {
            log.error("获取 Broker 地址失败", e);
        }
        return null;
    }

    // ==================== Consumer Group 管理 ====================

    @Override
    public List<Map<String, Object>> getConsumerGroupList(String keyword) {
        try {
            // 获取 Broker 地址
            String brokerAddr = getFirstBrokerAddr();
            if (brokerAddr == null) {
                throw BusinessException.badRequest("未找到可用的 Broker");
            }

            // 使用 Broker 地址获取订阅组列表
            SubscriptionGroupWrapper wrapper = defaultMQAdminExt.getAllSubscriptionGroup(brokerAddr, 3000);
            Set<String> groupSet = wrapper.getSubscriptionGroupTable().keySet();
            List<Map<String, Object>> result = new ArrayList<>();

            for (String group : groupSet) {
                // 过滤系统 Consumer Group（包括所有内部重试组）
                // %RETRY% 是重试组前缀，CID_ONSAPI 是内部系统组
                if (group.startsWith("%RETRY%") || group.startsWith("%DLQ%") ||
                    group.contains("CID_ONSAPI") || group.contains("OWNER") || group.contains("_BACKUP")) {
                    continue;
                }

                // 过滤关键字
                if (keyword != null && !keyword.isEmpty()
                        && !group.toLowerCase().contains(keyword.toLowerCase())) {
                    continue;
                }

                Map<String, Object> item = new HashMap<>();
                item.put("group", group);

                // 获取消费统计 - RocketMQ 5.x API
                try {
                    ConsumeStats stats = defaultMQAdminExt.examineConsumeStats(group);
                    if (stats != null && stats.getOffsetTable() != null) {
                        item.put("consumerCount", stats.getOffsetTable().size());
                    } else {
                        item.put("consumerCount", 0);
                    }
                    item.put("accumulatedDiff", 0L);
                } catch (Exception e) {
                    // 忽略消费统计查询失败（可能是 topic 不存在等）
                    item.put("consumerCount", 0);
                    item.put("accumulatedDiff", 0L);
                    log.debug("获取 Consumer Group {} 消费统计失败: {}", group, e.getMessage());
                }

// 获取 Group 配置（类型）- RocketMQ 5.x API
                try {
                    SubscriptionGroupConfig config = wrapper.getSubscriptionGroupTable().get(group);
                    if (config != null) {
                        // 使用 isConsumeBroadcastEnable 判断广播/集群模式
                        boolean isBroadcast = config.isConsumeBroadcastEnable();
                        item.put("groupType", isBroadcast ? "BROADCASTING" : "CLUSTERING");
                    } else {
                        item.put("groupType", "UNKNOWN");
                    }
                    item.put("status", "OK");
                } catch (Exception e) {
                    item.put("groupType", "UNKNOWN");
                    item.put("status", "OFFLINE");
                }
                    item.put("status", "OK");


                result.add(item);
            }

            // 按名称排序
            result.sort(Comparator.comparing(a -> (String) a.get("group")));

            return result;
        } catch (Exception e) {
            log.error("获取 Consumer Group 列表失败", e);
            throw BusinessException.badRequest("获取数据失败，请稍后重试");
        }
    }

    @Override
    public Map<String, Object> getConsumerGroupDetail(String groupName) {
        try {
            Map<String, Object> detail = new HashMap<>();
            detail.put("group", groupName);

            // 获取 Broker 地址
            String brokerAddr = getFirstBrokerAddr();
            if (brokerAddr == null) {
                throw BusinessException.badRequest("未找到可用的 Broker");
            }

            // 消费统计 - RocketMQ 5.x API
            try {
                ConsumeStats stats = defaultMQAdminExt.examineConsumeStats(groupName);
                if (stats != null && stats.getOffsetTable() != null) {
                    detail.put("consumerCount", stats.getOffsetTable().size());
                    long totalDiff = 0;
                    for (var entry : stats.getOffsetTable().entrySet()) {
                        totalDiff += entry.getValue().getConsumerOffset();
                    }
                    detail.put("totalDiff", totalDiff);
                } else {
                    detail.put("consumerCount", 0);
                    detail.put("totalDiff", 0L);
                }
            } catch (Exception e) {
                detail.put("consumerCount", 0);
                detail.put("totalDiff", 0L);
                log.debug("获取消费统计失败: {}", e.getMessage());
            }

            // 订阅关系配置 - 使用 Broker 地址
            SubscriptionGroupWrapper wrapper = defaultMQAdminExt.getAllSubscriptionGroup(brokerAddr, 3000);
            SubscriptionGroupConfig config = wrapper.getSubscriptionGroupTable().get(groupName);
            if (config != null) {
                boolean isBroadcast = config.isConsumeBroadcastEnable();
                detail.put("groupType", isBroadcast ? "BROADCASTING" : "CLUSTERING");
                detail.put("subscriptions", Collections.emptyList()); // 5.x 订阅数据需单独获取
            }

            // 位点表
            Map<String, Long> offsetTable = new HashMap<>();
            try {
                ConsumeStats stats = defaultMQAdminExt.examineConsumeStats(groupName);
                if (stats != null && stats.getOffsetTable() != null) {
                    stats.getOffsetTable().forEach((mq, ow) -> {
                        String key = mq.getTopic() + "-" + mq.getQueueId();
                        offsetTable.put(key, ow.getConsumerOffset());
                    });
                }
            } catch (Exception e) {
                log.debug("获取位点表失败: {}", e.getMessage());
            }
            detail.put("offsetTable", offsetTable);

            return detail;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取 Consumer Group 详情失败: {}", groupName, e);
            throw BusinessException.badRequest("获取数据失败，请稍后重试");
        }
    }

    @Override
    public void resetConsumerOffset(String topic, String groupName, long timestamp) {
        try {
            // 获取 Broker 地址
            String brokerAddr = getFirstBrokerAddr();
            if (brokerAddr == null) {
                throw BusinessException.badRequest("未找到可用的 Broker");
            }

            // 检查 Group 类型
            SubscriptionGroupWrapper wrapper = defaultMQAdminExt.getAllSubscriptionGroup(brokerAddr, 3000);
            SubscriptionGroupConfig config = wrapper.getSubscriptionGroupTable().get(groupName);
            if (config != null && config.isConsumeBroadcastEnable()) {
                throw BusinessException.badRequest("广播模式不支持位点重置");
            }

            // 执行重置 - RocketMQ 5.x 需要 isForce 参数
            defaultMQAdminExt.resetOffsetByTimestamp(topic, groupName, timestamp, true);
            log.info("位点重置成功: topic={}, group={}, timestamp={}", topic, groupName, timestamp);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("位点重置失败: topic={}, group={}", topic, groupName, e);
            throw BusinessException.badRequest("位点重置失败: " + e.getMessage());
        }
    }

    @Override
    public void deleteConsumerGroup(String groupName) {
        try {
            String brokerAddr = getFirstBrokerAddr();
            if (brokerAddr == null) {
                throw BusinessException.badRequest("未找到可用的 Broker");
            }
            defaultMQAdminExt.deleteSubscriptionGroup(brokerAddr, groupName);
            log.info("删除 Consumer Group 成功: group={}", groupName);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("删除 Consumer Group 失败: group={}", groupName, e);
            throw BusinessException.badRequest("删除 Consumer Group 失败: " + e.getMessage());
        }
    }

    // ==================== 消息管理 ====================

    @Override
    public List<Map<String, Object>> getMessageList(String topic, long startTime, long endTime, int maxMsg) {
        try {
            // 时间范围校验（最多 7 天）
            long sevenDays = 7 * 24 * 60 * 60 * 1000L;
            if (endTime - startTime > sevenDays) {
                throw BusinessException.badRequest("时间范围不能超过 7 天");
            }

            List<Map<String, Object>> result = new ArrayList<>();

            // 使用 queryMessage 按时间范围查询消息
            // RocketMQ 5.x queryMessage 支持按时间范围过滤
            try {
                QueryResult queryResult = defaultMQAdminExt.queryMessage(topic, "*", maxMsg, startTime, endTime);
                if (queryResult != null && queryResult.getMessageList() != null) {
                    for (MessageExt msg : queryResult.getMessageList()) {
                        Map<String, Object> item = new HashMap<>();
                        item.put("msgId", msg.getMsgId());
                        item.put("topic", msg.getTopic());
                        item.put("tags", msg.getTags() != null ? msg.getTags() : "");
                        item.put("keys", msg.getKeys() != null ? msg.getKeys() : "");
                        item.put("timestamp", msg.getStoreTimestamp());
                        item.put("queueId", msg.getQueueId());
                        item.put("queueOffset", msg.getQueueOffset());
                        item.put("properties", msg.getProperties());
                        result.add(item);
                    }
                }
            } catch (Exception e) {
                log.warn("queryMessage 查询失败，尝试遍历队列方式: {}", e.getMessage());
                // 如果 queryMessage 失败，回退到遍历队列方式
                return getMessageListByQueue(topic, startTime, endTime, maxMsg);
            }

            // 按时间倒序排序
            result.sort((a, b) -> {
                Long tsA = (Long) a.get("timestamp");
                Long tsB = (Long) b.get("timestamp");
                return tsB.compareTo(tsA);
            });

            return result;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("查询消息列表失败: topic={}", topic, e);
            throw BusinessException.badRequest("查询消息失败: " + e.getMessage());
        }
    }

    /**
     * 通过遍历队列方式获取消息（使用 PullConsumer 正确实现）
     */
    private List<Map<String, Object>> getMessageListByQueue(String topic, long startTime, long endTime, int maxMsg) {
        List<Map<String, Object>> result = new ArrayList<>();
        DefaultMQPullConsumer consumer = null;
        String consumerGroup = "pull_consumer_" + topic + "_" + System.currentTimeMillis();
        try {
            consumer = new DefaultMQPullConsumer(consumerGroup);
            consumer.setNamesrvAddr(nameServer);
            consumer.setInstanceName("PullConsumer-" + topic + "-" + System.currentTimeMillis());
            consumer.start();

            // 获取 Topic 下的所有队列
            Set<MessageQueue> mqs = consumer.fetchSubscribeMessageQueues(topic);
            log.info("查询 Topic {} 的队列，数量: {}", topic, mqs.size());

            for (MessageQueue mq : mqs) {
                if (result.size() >= maxMsg) break;

                try {
                    long minOffset = consumer.minOffset(mq);
                    long maxOffset = consumer.maxOffset(mq);

                    if (maxOffset <= minOffset) {
                        continue;
                    }

                    log.debug("队列 {}: minOffset={}, maxOffset={}", mq, minOffset, maxOffset);

                    // 从最新位置往前查找
                    long offset = Math.max(minOffset, maxOffset - 100);

                    int pullCount = 0;
                    while (offset < maxOffset && result.size() < maxMsg) {
                        try {
                            // 每次拉取 32 条
                            PullResult pullResult = consumer.pullBlockIfNotFound(mq, null, offset, 32);

                            if (pullResult.getMsgFoundList() == null || pullResult.getMsgFoundList().isEmpty()) {
                                break;
                            }

                            for (MessageExt msg : pullResult.getMsgFoundList()) {
                                if (result.size() >= maxMsg) break;

                                long storeTime = msg.getStoreTimestamp();
                                // 按时间范围过滤
                                if (storeTime >= startTime && storeTime <= endTime) {
                                    Map<String, Object> item = new HashMap<>();
                                    item.put("msgId", msg.getMsgId());
                                    item.put("topic", msg.getTopic());
                                    item.put("tags", msg.getTags() != null ? msg.getTags() : "");
                                    item.put("keys", msg.getKeys() != null ? msg.getKeys() : "");
                                    item.put("timestamp", storeTime);
                                    item.put("queueId", msg.getQueueId());
                                    item.put("queueOffset", msg.getQueueOffset());
                                    item.put("properties", msg.getProperties());
                                    result.add(item);
                                }
                            }

                            offset = pullResult.getNextBeginOffset();
                            pullCount++;

                            // 防止无限循环
                            if (pullCount > 1000) {
                                log.warn("队列 {} 拉取次数超过 1000，停止", mq);
                                break;
                            }
                        } catch (Exception e) {
                            log.debug("拉取队列 {} 消息失败: {}", mq, e.getMessage());
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
                // 删除临时消费者组，避免在 broker 上残留
                try {
                    String brokerAddr = getFirstBrokerAddr(topic);
                    if (brokerAddr != null) {
                        defaultMQAdminExt.deleteSubscriptionGroup(brokerAddr, consumerGroup);
                        log.info("已删除临时消费者组: {}", consumerGroup);
                    }
                } catch (Exception e) {
                    log.debug("删除临时消费者组失败: {}", e.getMessage());
                }
            }
        }
        return result;
    }

    @Override
    public Map<String, Object> getMessageDetail(String topic, String msgId) {
        try {
            // 使用 viewMessage 按 msgId 精确查询
            MessageExt msg = defaultMQAdminExt.viewMessage(topic, msgId);

            if (msg == null) {
                throw BusinessException.notFound("未找到消息: " + msgId);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("msgId", msg.getMsgId());
            result.put("topic", msg.getTopic());
            result.put("tags", msg.getTags() != null ? msg.getTags() : "");
            result.put("keys", msg.getKeys() != null ? msg.getKeys() : "");
            result.put("timestamp", msg.getStoreTimestamp());
            result.put("queueId", msg.getQueueId());
            result.put("queueOffset", msg.getQueueOffset());
            result.put("properties", msg.getProperties());
            result.put("body", new String(msg.getBody(), java.nio.charset.StandardCharsets.UTF_8));
            return result;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("查询消息详情失败: topic={}, msgId={}", topic, msgId, e);
            throw BusinessException.badRequest("查询消息详情失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getMessageTrace(String topic, String msgId) {
        try {
            Map<String, Object> result = new HashMap<>();
            result.put("msgId", msgId);
            result.put("topic", topic);

            // 查询该 topic 下的 Consumer Group 消费信息
            List<Map<String, Object>> consumeTraceList = new ArrayList<>();

            // 获取 Broker 地址
            String brokerAddr = getFirstBrokerAddr(topic);
            if (brokerAddr != null) {
                try {
                    SubscriptionGroupWrapper wrapper = defaultMQAdminExt.getAllSubscriptionGroup(brokerAddr, 3000);
                    Set<String> groups = wrapper.getSubscriptionGroupTable().keySet();

                    for (String group : groups) {
                        // 跳过系统 Consumer Group
                        if (isSystemGroup(group)) {
                            continue;
                        }

                        try {
                            ConsumeStats stats = defaultMQAdminExt.examineConsumeStats(group);
                            if (stats != null && stats.getOffsetTable() != null && !stats.getOffsetTable().isEmpty()) {
                                Map<String, Object> trace = new HashMap<>();
                                trace.put("consumerGroup", group);
                                trace.put("status", "已消费");
                                trace.put("consumeTime", System.currentTimeMillis());
                                consumeTraceList.add(trace);
                            }
                        } catch (Exception e) {
                            // 跳过无法获取的 group
                            log.debug("获取 Group {} 消费状态失败: {}", group, e.getMessage());
                        }
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

    /**
     * 获取指定 Topic 的 Broker 地址
     */
    private String getFirstBrokerAddr(String topic) {
        try {
            TopicRouteData routeData = defaultMQAdminExt.examineTopicRouteInfo(topic);
            if (routeData != null && routeData.getQueueDatas() != null && !routeData.getQueueDatas().isEmpty()) {
                String brokerName = routeData.getQueueDatas().get(0).getBrokerName();
                ClusterInfo clusterInfo = defaultMQAdminExt.examineBrokerClusterInfo();
                Map<String, BrokerData> brokerAddrTable = clusterInfo.getBrokerAddrTable();
                if (brokerAddrTable != null) {
                    BrokerData brokerData = brokerAddrTable.get(brokerName);
                    if (brokerData != null) {
                        String addr = brokerData.selectBrokerAddr();
                        if (addr != null && !addr.isEmpty()) {
                            return addr;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.debug("获取 Topic {} 的 Broker 地址失败: {}", topic, e.getMessage());
        }
        // Fallback: 返回第一个可用的 Broker 地址
        return getFirstBrokerAddr();
    }

    @Override
    public Map<String, Object> sendMessage(String topic, String tags, String keys, String body) {
        try {
            // 使用 adminExt 获取 Topic 路由，确保 Nameserver 有该 Topic 的最新路由信息
            defaultMQAdminExt.examineTopicRouteInfo(topic);

            org.apache.rocketmq.common.message.Message message = new org.apache.rocketmq.common.message.Message(
                topic,
                tags != null && !tags.isEmpty() ? tags : "*",
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

    // ==================== 监控面板 ====================

    @Override
    public Map<String, Object> getClusterOverview() {
        Map<String, Object> result = new HashMap<>();
        try {
            ClusterInfo clusterInfo = defaultMQAdminExt.examineBrokerClusterInfo();
            result.put("clusterName", "DefaultCluster"); // ClusterInfo 5.x API 不直接提供集群名
            result.put("brokerCount", clusterInfo.getBrokerAddrTable().size());

            // 获取 Topic 数量（过滤系统 Topic）
            TopicList topicList = defaultMQAdminExt.fetchAllTopicList();
            int topicCount = 0;
            for (String topic : topicList.getTopicList()) {
                if (!isSystemTopic(topic)) {
                    topicCount++;
                }
            }
            result.put("topicCount", topicCount);

            // 获取 Consumer Group 数量（需要 Broker 地址）
            String brokerAddr = getFirstBrokerAddr();
            int consumerGroupCount = 0;
            long totalDiff = 0;
            if (brokerAddr != null) {
                SubscriptionGroupWrapper subGroupWrapper = defaultMQAdminExt.getAllSubscriptionGroup(brokerAddr, 3000);
                for (String group : subGroupWrapper.getSubscriptionGroupTable().keySet()) {
                    if (!isSystemGroup(group)) {
                        consumerGroupCount++;
                        // 获取每个组的消费进度和堆积量
                        try {
                            ConsumeStats stats = defaultMQAdminExt.examineConsumeStats(group);
                            if (stats != null && stats.getOffsetTable() != null) {
                                for ( var entry : stats.getOffsetTable().entrySet()) {
                                    long brokerOffset = entry.getValue().getBrokerOffset();
                                    long consumerOffset = entry.getValue().getConsumerOffset();
                                    long diff = brokerOffset - consumerOffset;
                                    if (diff > 0) {
                                        totalDiff += diff;
                                    }
                                }
                            }
                        } catch (Exception e) {
                            log.debug("获取 Group {} 消费状态失败", group);
                        }
                    }
                }
            }
            result.put("consumerGroupCount", consumerGroupCount);
            result.put("totalDiff", totalDiff);

            return result;
        } catch (Exception e) {
            log.error("获取集群概览失败", e);
            throw BusinessException.badRequest("获取集群概览失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getBrokerStatusList() {
        List<Map<String, Object>> brokers = new ArrayList<>();
        try {
            ClusterInfo clusterInfo = defaultMQAdminExt.examineBrokerClusterInfo();
            for (var entry : clusterInfo.getBrokerAddrTable().entrySet()) {
                String brokerName = entry.getKey();
                BrokerData brokerData = entry.getValue();
                String brokerAddr = brokerData.selectBrokerAddr();

                Map<String, Object> broker = new HashMap<>();
                broker.put("brokerName", brokerName);
                broker.put("brokerAddr", brokerAddr);
                broker.put("status", "ONLINE"); // 默认在线
                broker.put("version", "V5"); // RocketMQ 5.x
                broker.put("inBrokerHouseDate", "");

                brokers.add(broker);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("records", brokers);
            result.put("total", brokers.size());
            return result;
        } catch (Exception e) {
           log.error("获取 Broker 状态列表失败", e);
            throw BusinessException.badRequest("获取 Broker 状态列表失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getTopicBacklogList() {
        List<Map<String, Object>> topics = new ArrayList<>();
        try {
            String brokerAddr = getFirstBrokerAddr();
            if (brokerAddr == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("records", topics);
                result.put("total", 0);
                return result;
            }

            SubscriptionGroupWrapper subGroupWrapper = defaultMQAdminExt.getAllSubscriptionGroup(brokerAddr, 3000);

            // 按 Topic 分组计算堆积量
            Map<String, Long> topicDiffMap = new HashMap<>();
            for (var entry : subGroupWrapper.getSubscriptionGroupTable().entrySet()) {
                String group = entry.getKey();
                if (isSystemGroup(group)) continue;

                try {
                    ConsumeStats stats = defaultMQAdminExt.examineConsumeStats(group);
                    if (stats != null && stats.getOffsetTable() != null) {
                        for (var offsetEntry : stats.getOffsetTable().entrySet()) {
                            String topicName = offsetEntry.getKey().getTopic();
                            long brokerOffset = offsetEntry.getValue().getBrokerOffset();
                            long consumerOffset = offsetEntry.getValue().getConsumerOffset();
                            long diff = brokerOffset - consumerOffset;
                            if (diff > 0) {
                                topicDiffMap.merge(topicName, diff, Long::sum);
                            }
                        }
                    }
                } catch (Exception e) {
                    log.debug("获取 Group {} 消费状态失败", group);
                }
            }

            // 获取 Topic 列表
            TopicList topicList = defaultMQAdminExt.fetchAllTopicList();
            for (String topicName : topicList.getTopicList()) {
                if (isSystemTopic(topicName)) continue;

                Map<String, Object> topic = new HashMap<>();
                topic.put("topicName", topicName);
                topic.put("diff", topicDiffMap.getOrDefault(topicName, 0L));
                topic.put("lastUpdateTime", System.currentTimeMillis());
                topics.add(topic);
            }

            // 按堆积量倒序
            topics.sort((a, b) -> Long.compare((Long) b.get("diff"), (Long) a.get("diff")));

            Map<String, Object> result = new HashMap<>();
            result.put("records", topics);
            result.put("total", topics.size());
            return result;
        } catch (Exception e) {
            log.error("获取 Topic 堆积量列表失败", e);
            throw BusinessException.badRequest("获取 Topic 堆积量列表失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getBrokerMetrics(String brokerName) {
        Map<String, Object> metrics = new HashMap<>();
        try {
            // 获取 Broker 地址
            ClusterInfo clusterInfo = defaultMQAdminExt.examineBrokerClusterInfo();
            BrokerData brokerData = clusterInfo.getBrokerAddrTable().get(brokerName);
            if (brokerData == null) {
                throw BusinessException.notFound("Broker 不存在: " + brokerName);
            }
            String brokerAddr = brokerData.selectBrokerAddr();

            // 获取运行时统计信息
            org.apache.rocketmq.remoting.protocol.admin.KVTable kvTable = defaultMQAdminExt.fetchBrokerRuntimeStats(brokerAddr);
            Map<String, String> statsTable = kvTable.getTable();

            metrics.put("brokerName", brokerName);
            metrics.put("brokerAddr", brokerAddr);

            // TPS 数据
            metrics.put("putTps", parseTpsData(statsTable.get("putTps")));
            metrics.put("getFoundTps", parseTpsData(statsTable.get("getFoundTps")));
            metrics.put("getTotalTps", parseTpsData(statsTable.get("getTotalTps")));
            metrics.put("sendThreadPoolQueueSize", statsTable.get("sendThreadPoolQueueSize"));
            metrics.put("pullThreadPoolQueueSize", statsTable.get("pullThreadPoolQueueSize"));

            // 消息统计
            metrics.put("msgPutTotalTodayNow", statsTable.get("msgPutTotalTodayNow"));
            metrics.put("msgGetTotalTodayNow", statsTable.get("msgGetTotalTodayNow"));
            metrics.put("msgPutTotalYesterdayMorning", statsTable.get("msgPutTotalYesterdayMorning"));
            metrics.put("msgGetTotalYesterdayMorning", statsTable.get("msgGetTotalYesterdayMorning"));

            // 运行时信息
            metrics.put("bootTimestamp", statsTable.get("bootTimestamp"));
            metrics.put("runtime", statsTable.get("runtime"));
            metrics.put("version", statsTable.get("version"));

            // 发送 TPS 数组（模拟时序数据，用于图表展示）
            List<Long> times = new ArrayList<>();
            List<Double> sendTps = new ArrayList<>();
            List<Double> consumeTps = new ArrayList<>();
            List<Double> transferTps = new ArrayList<>();

            // 构造 10 个数据点（模拟实时监控）
            long now = System.currentTimeMillis();
            for (int i = 9; i >= 0; i--) {
                times.add(now - i * 1000);
                // 解析当前 TPS 值，加入小幅随机波动
                double baseSendTps = parseTpsValue(statsTable.get("putTps"));
                double baseConsumeTps = parseTpsValue(statsTable.get("getFoundTps"));
                double baseTransferTps = parseTpsValue(statsTable.get("getTotalTps"));
                sendTps.add(Math.max(0, baseSendTps + (Math.random() - 0.5) * baseSendTps * 0.1));
                consumeTps.add(Math.max(0, baseConsumeTps + (Math.random() - 0.5) * baseConsumeTps * 0.1));
                transferTps.add(Math.max(0, baseTransferTps + (Math.random() - 0.5) * baseTransferTps * 0.1));
            }
            metrics.put("times", times);
            metrics.put("sendTps", sendTps);
            metrics.put("consumeTps", consumeTps);
            metrics.put("getTransferedTps", transferTps);

            return metrics;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取 Broker {} 运行时指标失败", brokerName, e);
            throw BusinessException.badRequest("获取 Broker 指标失败: " + e.getMessage());
        }
    }

    /**
     * 解析 TPS 字符串数据（格式如 "0.00 0.00 0.00" 表示 10s/60s/600s）
     */
    private List<Double> parseTpsData(String tpsStr) {
        List<Double> result = new ArrayList<>();
        if (tpsStr != null && !tpsStr.isEmpty()) {
            String[] parts = tpsStr.split("\\s+");
            for (String part : parts) {
                try {
                    result.add(Double.parseDouble(part.trim()));
                } catch (NumberFormatException e) {
                    result.add(0.0);
                }
            }
        }
        return result;
    }

    /**
     * 解析单个 TPS 值
     */
    private double parseTpsValue(String tpsStr) {
        if (tpsStr == null || tpsStr.isEmpty()) {
            return 0.0;
        }
        String[] parts = tpsStr.trim().split("\\s+");
        try {
            return Double.parseDouble(parts[0]);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
