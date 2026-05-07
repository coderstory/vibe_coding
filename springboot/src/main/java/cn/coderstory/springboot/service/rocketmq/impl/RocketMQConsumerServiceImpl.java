package cn.coderstory.springboot.service.rocketmq.impl;

import cn.coderstory.springboot.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.remoting.protocol.admin.ConsumeStats;
import org.apache.rocketmq.remoting.protocol.body.SubscriptionGroupWrapper;
import org.apache.rocketmq.remoting.protocol.subscription.SubscriptionGroupConfig;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class RocketMQConsumerServiceImpl implements RocketMQSubService {

    private static final List<String> SYSTEM_GROUP_PREFIXES = List.of(
        "%RETRY%", "%DLQ%", "CID_RMQ_SYS_"
    );

    private final DefaultMQAdminExt defaultMQAdminExt;

    private boolean isSystemGroup(String groupName) {
        return SYSTEM_GROUP_PREFIXES.stream().anyMatch(groupName::startsWith) ||
            groupName.contains("CID_ONSAPI") ||
            groupName.contains("OWNER") ||
            groupName.contains("_BACKUP");
    }

    public List<Map<String, Object>> getConsumerGroupList(String keyword) {
        try {
            String brokerAddr = RocketMQUtils.getFirstBrokerAddr(defaultMQAdminExt);
            if (brokerAddr == null) throw BusinessException.badRequest("未找到可用的 Broker");

            SubscriptionGroupWrapper wrapper = defaultMQAdminExt.getAllSubscriptionGroup(brokerAddr, 3000);
            Set<String> groupSet = wrapper.getSubscriptionGroupTable().keySet();
            List<Map<String, Object>> result = new ArrayList<>();

            for (String group : groupSet) {
                if (isSystemGroup(group)) continue;
                if (keyword != null && !keyword.isEmpty() && !group.toLowerCase().contains(keyword.toLowerCase())) continue;

                Map<String, Object> item = new HashMap<>();
                item.put("group", group);
                try {
                    ConsumeStats stats = defaultMQAdminExt.examineConsumeStats(group);
                    item.put("consumerCount", stats != null && stats.getOffsetTable() != null ? stats.getOffsetTable().size() : 0);
                    item.put("accumulatedDiff", 0L);
                } catch (Exception e) {
                    item.put("consumerCount", 0);
                    item.put("accumulatedDiff", 0L);
                }
                try {
                    SubscriptionGroupConfig config = wrapper.getSubscriptionGroupTable().get(group);
                    if (config != null) {
                        item.put("groupType", config.isConsumeBroadcastEnable() ? "BROADCASTING" : "CLUSTERING");
                    } else {
                        item.put("groupType", "UNKNOWN");
                    }
                } catch (Exception e) {
                    item.put("groupType", "UNKNOWN");
                }
                item.put("status", "OK");
                result.add(item);
            }
            result.sort(Comparator.comparing(a -> (String) a.get("group")));
            return result;
        } catch (Exception e) {
            log.error("获取 Consumer Group 列表失败", e);
            throw BusinessException.badRequest("获取数据失败，请稍后重试");
        }
    }

    public Map<String, Object> getConsumerGroupDetail(String groupName) {
        try {
            Map<String, Object> detail = new HashMap<>();
            detail.put("group", groupName);

            String brokerAddr = RocketMQUtils.getFirstBrokerAddr(defaultMQAdminExt);
            if (brokerAddr == null) throw BusinessException.badRequest("未找到可用的 Broker");

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
            }

            SubscriptionGroupWrapper wrapper = defaultMQAdminExt.getAllSubscriptionGroup(brokerAddr, 3000);
            SubscriptionGroupConfig config = wrapper.getSubscriptionGroupTable().get(groupName);
            if (config != null) {
                detail.put("groupType", config.isConsumeBroadcastEnable() ? "BROADCASTING" : "CLUSTERING");
                detail.put("subscriptions", Collections.emptyList());
            }

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

    public void resetConsumerOffset(String topic, String groupName, long timestamp) {
        try {
            String brokerAddr = RocketMQUtils.getFirstBrokerAddr(defaultMQAdminExt);
            if (brokerAddr == null) throw BusinessException.badRequest("未找到可用的 Broker");

            SubscriptionGroupWrapper wrapper = defaultMQAdminExt.getAllSubscriptionGroup(brokerAddr, 3000);
            SubscriptionGroupConfig config = wrapper.getSubscriptionGroupTable().get(groupName);
            if (config != null && config.isConsumeBroadcastEnable()) {
                throw BusinessException.badRequest("广播模式不支持位点重置");
            }

            defaultMQAdminExt.resetOffsetByTimestamp(topic, groupName, timestamp, true);
            log.info("位点重置成功: topic={}, group={}, timestamp={}", topic, groupName, timestamp);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("位点重置失败: topic={}, group={}", topic, groupName, e);
            throw BusinessException.badRequest("位点重置失败: " + e.getMessage());
        }
    }

    public void deleteConsumerGroup(String groupName) {
        try {
            String brokerAddr = RocketMQUtils.getFirstBrokerAddr(defaultMQAdminExt);
            if (brokerAddr == null) throw BusinessException.badRequest("未找到可用的 Broker");
            defaultMQAdminExt.deleteSubscriptionGroup(brokerAddr, groupName);
            log.info("删除 Consumer Group 成功: group={}", groupName);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("删除 Consumer Group 失败: group={}", groupName, e);
            throw BusinessException.badRequest("删除 Consumer Group 失败: " + e.getMessage());
        }
    }
}
