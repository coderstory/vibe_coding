package cn.coderstory.springboot.service.impl;

import cn.coderstory.springboot.exception.BusinessException;
import cn.coderstory.springboot.service.RocketMQAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.TopicConfig;
import org.apache.rocketmq.remoting.protocol.admin.ConsumeStats;
import org.apache.rocketmq.remoting.protocol.body.ClusterInfo;
import org.apache.rocketmq.remoting.protocol.body.SubscriptionGroupWrapper;
import org.apache.rocketmq.remoting.protocol.body.TopicList;
import org.apache.rocketmq.remoting.protocol.route.BrokerData;
import org.apache.rocketmq.remoting.protocol.route.TopicRouteData;
import org.apache.rocketmq.remoting.protocol.subscription.SubscriptionGroupConfig;
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

    private final DefaultMQAdminExt defaultMQAdminExt;

    @Value("${rocketmq.name-server:localhost:9876}")
    private String nameServer;

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
                if (topicName.startsWith("SCHEDULE_TOPIC_") ||
                    topicName.startsWith("TBW_") ||
                    topicName.equals("RMQ_SYS_TRANS_HALF_TOPIC") ||
                    topicName.equals("RMQ_SYS_TRACE_TOPIC") ||
                    topicName.equals("RMQ_SYS_ACL_TOPIC")) {
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
}
