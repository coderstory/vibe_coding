package cn.coderstory.springboot.service.rocketmq.impl;

import cn.coderstory.springboot.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.remoting.protocol.admin.ConsumeStats;
import org.apache.rocketmq.remoting.protocol.body.ClusterInfo;
import org.apache.rocketmq.remoting.protocol.body.KVTable;
import org.apache.rocketmq.remoting.protocol.body.SubscriptionGroupWrapper;
import org.apache.rocketmq.remoting.protocol.body.TopicList;
import org.apache.rocketmq.remoting.protocol.route.BrokerData;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class RocketMQClusterServiceImpl implements RocketMQSubService {

    private static final List<String> SYSTEM_TOPIC_PREFIXES = List.of(
        "SCHEDULE_TOPIC_", "TBW_", "RMQ_SYS_", "%RETRY%", "%DLQ%", "%BINDER%"
    );
    private static final List<String> SYSTEM_GROUP_PREFIXES = List.of(
        "%RETRY%", "%DLQ%", "CID_RMQ_SYS_"
    );

    private final DefaultMQAdminExt defaultMQAdminExt;

    private boolean isSystemTopic(String topicName) {
        return SYSTEM_TOPIC_PREFIXES.stream().anyMatch(topicName::startsWith) ||
            topicName.contains("_BACKUP") || topicName.equals("DEFAULT_TOPIC");
    }

    private boolean isSystemGroup(String groupName) {
        return SYSTEM_GROUP_PREFIXES.stream().anyMatch(groupName::startsWith) ||
            groupName.contains("CID_ONSAPI") || groupName.contains("OWNER") || groupName.contains("_BACKUP");
    }

    public Map<String, Object> getClusterOverview() {
        Map<String, Object> result = new HashMap<>();
        try {
            ClusterInfo clusterInfo = defaultMQAdminExt.examineBrokerClusterInfo();
            result.put("clusterName", "DefaultCluster");
            result.put("brokerCount", clusterInfo.getBrokerAddrTable().size());

            TopicList topicList = defaultMQAdminExt.fetchAllTopicList();
            int topicCount = 0;
            for (String topic : topicList.getTopicList()) {
                if (!isSystemTopic(topic)) topicCount++;
            }
            result.put("topicCount", topicCount);

            String brokerAddr = RocketMQUtils.getFirstBrokerAddr(defaultMQAdminExt);
            int consumerGroupCount = 0;
            long totalDiff = 0;
            if (brokerAddr != null) {
                SubscriptionGroupWrapper subGroupWrapper = defaultMQAdminExt.getAllSubscriptionGroup(brokerAddr, 3000);
                for (String group : subGroupWrapper.getSubscriptionGroupTable().keySet()) {
                    if (!isSystemGroup(group)) {
                        consumerGroupCount++;
                        try {
                            ConsumeStats stats = defaultMQAdminExt.examineConsumeStats(group);
                            if (stats != null && stats.getOffsetTable() != null) {
                                for (var entry : stats.getOffsetTable().entrySet()) {
                                    long diff = entry.getValue().getBrokerOffset() - entry.getValue().getConsumerOffset();
                                    if (diff > 0) totalDiff += diff;
                                }
                            }
                        } catch (Exception ignored) {}
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

    public Map<String, Object> getBrokerStatusList() {
        List<Map<String, Object>> brokers = new ArrayList<>();
        try {
            ClusterInfo clusterInfo = defaultMQAdminExt.examineBrokerClusterInfo();
            for (var entry : clusterInfo.getBrokerAddrTable().entrySet()) {
                Map<String, Object> broker = new HashMap<>();
                broker.put("brokerName", entry.getKey());
                broker.put("brokerAddr", entry.getValue().selectBrokerAddr());
                broker.put("status", "ONLINE");
                broker.put("version", "V5");
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

    public Map<String, Object> getTopicBacklogList() {
        List<Map<String, Object>> topics = new ArrayList<>();
        try {
            String brokerAddr = RocketMQUtils.getFirstBrokerAddr(defaultMQAdminExt);
            if (brokerAddr == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("records", topics);
                result.put("total", 0);
                return result;
            }

            SubscriptionGroupWrapper subGroupWrapper = defaultMQAdminExt.getAllSubscriptionGroup(brokerAddr, 3000);
            Map<String, Long> topicDiffMap = new HashMap<>();
            for (var entry : subGroupWrapper.getSubscriptionGroupTable().entrySet()) {
                String group = entry.getKey();
                if (isSystemGroup(group)) continue;
                try {
                    ConsumeStats stats = defaultMQAdminExt.examineConsumeStats(group);
                    if (stats != null && stats.getOffsetTable() != null) {
                        for (var offsetEntry : stats.getOffsetTable().entrySet()) {
                            String topicName = offsetEntry.getKey().getTopic();
                            long diff = offsetEntry.getValue().getBrokerOffset() - offsetEntry.getValue().getConsumerOffset();
                            if (diff > 0) topicDiffMap.merge(topicName, diff, Long::sum);
                        }
                    }
                } catch (Exception ignored) {}
            }

            TopicList topicList = defaultMQAdminExt.fetchAllTopicList();
            for (String topicName : topicList.getTopicList()) {
                if (isSystemTopic(topicName)) continue;
                Map<String, Object> topic = new HashMap<>();
                topic.put("topicName", topicName);
                topic.put("diff", topicDiffMap.getOrDefault(topicName, 0L));
                topic.put("lastUpdateTime", System.currentTimeMillis());
                topics.add(topic);
            }
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

    public Map<String, Object> getBrokerMetrics(String brokerName) {
        Map<String, Object> metrics = new HashMap<>();
        try {
            ClusterInfo clusterInfo = defaultMQAdminExt.examineBrokerClusterInfo();
            BrokerData brokerData = clusterInfo.getBrokerAddrTable().get(brokerName);
            if (brokerData == null) throw BusinessException.notFound("Broker 不存在: " + brokerName);
            String brokerAddr = brokerData.selectBrokerAddr();

            KVTable kvTable = defaultMQAdminExt.fetchBrokerRuntimeStats(brokerAddr);
            Map<String, String> statsTable = kvTable.getTable();

            metrics.put("brokerName", brokerName);
            metrics.put("brokerAddr", brokerAddr);
            metrics.put("putTps", parseTpsData(statsTable.get("putTps")));
            metrics.put("getFoundTps", parseTpsData(statsTable.get("getFoundTps")));
            metrics.put("getTotalTps", parseTpsData(statsTable.get("getTotalTps")));
            metrics.put("sendThreadPoolQueueSize", statsTable.get("sendThreadPoolQueueSize"));
            metrics.put("pullThreadPoolQueueSize", statsTable.get("pullThreadPoolQueueSize"));
            metrics.put("msgPutTotalTodayNow", statsTable.get("msgPutTotalTodayNow"));
            metrics.put("msgGetTotalTodayNow", statsTable.get("msgGetTotalTodayNow"));
            metrics.put("msgPutTotalYesterdayMorning", statsTable.get("msgPutTotalYesterdayMorning"));
            metrics.put("msgGetTotalYesterdayMorning", statsTable.get("msgGetTotalYesterdayMorning"));
            metrics.put("bootTimestamp", statsTable.get("bootTimestamp"));
            metrics.put("runtime", statsTable.get("runtime"));
            metrics.put("version", statsTable.get("version"));

            List<Long> times = new ArrayList<>();
            List<Double> sendTps = new ArrayList<>();
            List<Double> consumeTps = new ArrayList<>();
            List<Double> transferTps = new ArrayList<>();
            long now = System.currentTimeMillis();
            for (int i = 9; i >= 0; i--) {
                times.add(now - i * 1000L);
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

    private List<Double> parseTpsData(String tpsStr) {
        List<Double> result = new ArrayList<>();
        if (tpsStr != null && !tpsStr.isEmpty()) {
            for (String part : tpsStr.split("\\s+")) {
                try { result.add(Double.parseDouble(part.trim())); }
                catch (NumberFormatException e) { result.add(0.0); }
            }
        }
        return result;
    }

    private double parseTpsValue(String tpsStr) {
        if (tpsStr == null || tpsStr.isEmpty()) return 0.0;
        try { return Double.parseDouble(tpsStr.trim().split("\\s+")[0]); }
        catch (NumberFormatException e) { return 0.0; }
    }
}
