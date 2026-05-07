package cn.coderstory.springboot.service.rocketmq.impl;

import cn.coderstory.springboot.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.TopicConfig;
import org.apache.rocketmq.remoting.protocol.body.ClusterInfo;
import org.apache.rocketmq.remoting.protocol.body.TopicList;
import org.apache.rocketmq.remoting.protocol.route.BrokerData;
import org.apache.rocketmq.remoting.protocol.route.TopicRouteData;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class RocketMQTopicServiceImpl implements RocketMQSubService {

    private static final List<String> SYSTEM_TOPIC_PREFIXES = List.of(
        "SCHEDULE_TOPIC_", "TBW_", "RMQ_SYS_", "%RETRY%", "%DLQ%", "%BINDER%"
    );

    private final DefaultMQAdminExt defaultMQAdminExt;

    private boolean isSystemTopic(String topicName) {
        return SYSTEM_TOPIC_PREFIXES.stream().anyMatch(topicName::startsWith) ||
            topicName.contains("_BACKUP") ||
            topicName.equals("DEFAULT_TOPIC");
    }

    public List<Map<String, Object>> getTopicList(String keyword) {
        try {
            TopicList topicList = defaultMQAdminExt.fetchAllTopicList();
            Set<String> topicSet = topicList.getTopicList();

            String brokerAddr = RocketMQUtils.getFirstBrokerAddr(defaultMQAdminExt);
            if (brokerAddr == null) {
                throw BusinessException.badRequest("未找到可用的 Broker");
            }

            List<Map<String, Object>> result = new ArrayList<>();
            for (String topicName : topicSet) {
                if (isSystemTopic(topicName)) continue;
                if (keyword != null && !keyword.isEmpty() && !topicName.toLowerCase().contains(keyword.toLowerCase())) continue;

                Map<String, Object> item = new HashMap<>();
                item.put("topicName", topicName);
                try {
                    TopicConfig topicConfig = defaultMQAdminExt.examineTopicConfig(brokerAddr, topicName);
                    if (topicConfig != null) {
                        item.put("queueCount", topicConfig.getWriteQueueNums());
                        item.put("status", "ACTIVE");
                        item.put("readQueueNums", topicConfig.getReadQueueNums());
                        item.put("perm", RocketMQUtils.convertPermToString(topicConfig.getPerm()));
                    } else {
                        item.put("queueCount", 0);
                        item.put("status", "UNKNOWN");
                    }
                } catch (Exception e) {
                    log.debug("获取 Topic {} 配置失败: {}", topicName, e.getMessage());
                    item.put("queueCount", 0);
                    item.put("status", "UNKNOWN");
                }
                item.put("messageCount", 0);
                item.put("createTime", new Date());
                result.add(item);
            }
            result.sort(Comparator.comparing(a -> (String) a.get("topicName")));
            return result;
        } catch (Exception e) {
            log.error("获取 Topic 列表失败", e);
            throw BusinessException.badRequest("获取数据失败，请稍后重试");
        }
    }

    public Map<String, Object> getTopicDetail(String topicName) {
        try {
            Map<String, Object> detail = new HashMap<>();
            String brokerAddr = RocketMQUtils.getFirstBrokerAddr(defaultMQAdminExt);
            if (brokerAddr == null) {
                throw BusinessException.badRequest("未找到可用的 Broker");
            }
            TopicConfig topicConfig = defaultMQAdminExt.examineTopicConfig(brokerAddr, topicName);
            if (topicConfig == null) {
                throw BusinessException.notFound("Topic 不存在: " + topicName);
            }
            detail.put("topicName", topicName);
            detail.put("queueCount", topicConfig.getWriteQueueNums());
            detail.put("readQueueNums", topicConfig.getReadQueueNums());
            detail.put("perm", RocketMQUtils.convertPermToString(topicConfig.getPerm()));
            detail.put("status", "ACTIVE");
            try {
                TopicRouteData routeData = defaultMQAdminExt.examineTopicRouteInfo(topicName);
                if (routeData != null) detail.put("routeInfo", routeData);
            } catch (Exception e) {
                log.debug("获取 Topic {} 路由信息失败: {}", topicName, e.getMessage());
            }
            detail.put("subscriptions", Collections.emptyList());
            return detail;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取 Topic 详情失败: {}", topicName, e);
            throw BusinessException.badRequest("获取数据失败，请稍后重试");
        }
    }

    public void createTopic(String topicName, int queueCount, String perm) {
        try {
            if (topicName == null || topicName.trim().isEmpty()) {
                throw BusinessException.badRequest("Topic 名称不能为空");
            }
            if (!topicName.matches("^[a-zA-Z0-9_-]+$")) {
                throw BusinessException.badRequest("Topic 名称不能包含特殊字符，仅支持字母、数字、下划线和连字符");
            }
            String checkBrokerAddr = RocketMQUtils.getFirstBrokerAddr(defaultMQAdminExt);
            if (checkBrokerAddr == null) {
                throw BusinessException.badRequest("未找到可用的 Broker");
            }
            try {
                TopicConfig existing = defaultMQAdminExt.examineTopicConfig(checkBrokerAddr, topicName);
                if (existing != null) throw BusinessException.conflict("Topic 已存在: " + topicName);
            } catch (BusinessException e) {
                throw e;
            } catch (Exception ignored) {}

            TopicConfig topicConfig = new TopicConfig();
            topicConfig.setTopicName(topicName);
            topicConfig.setWriteQueueNums(queueCount);
            topicConfig.setReadQueueNums(queueCount);
            topicConfig.setPerm(RocketMQUtils.parsePerm(perm));

            ClusterInfo clusterInfo = defaultMQAdminExt.examineBrokerClusterInfo();
            Map<String, BrokerData> brokerAddrTable = clusterInfo.getBrokerAddrTable();
            if (brokerAddrTable.isEmpty()) throw BusinessException.badRequest("未找到可用的 Broker");

            int successCount = 0;
            for (BrokerData brokerData : brokerAddrTable.values()) {
                String brokerAddr = brokerData.selectBrokerAddr();
                if (brokerAddr == null || brokerAddr.isEmpty()) continue;
                try {
                    defaultMQAdminExt.createAndUpdateTopicConfig(brokerAddr, topicConfig);
                    successCount++;
                } catch (Exception e) {
                    log.error("在 Broker {} 创建 Topic 失败: {}", brokerAddr, e.getMessage());
                }
            }
            if (successCount == 0) throw BusinessException.badRequest("创建 Topic 失败：没有可用的 Broker");
            log.info("创建 Topic 成功: {}, queueCount: {}, perm: {}", topicName, queueCount, perm);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("创建 Topic 失败: {}", topicName, e);
            throw BusinessException.badRequest("创建 Topic 失败: " + e.getMessage());
        }
    }

    public void deleteTopic(String topicName) {
        try {
            String brokerAddr = RocketMQUtils.getFirstBrokerAddr(defaultMQAdminExt);
            if (brokerAddr == null) throw BusinessException.badRequest("未找到可用的 Broker");

            TopicConfig existing = defaultMQAdminExt.examineTopicConfig(brokerAddr, topicName);
            if (existing == null) throw BusinessException.notFound("Topic 不存在: " + topicName);

            ClusterInfo clusterInfo = defaultMQAdminExt.examineBrokerClusterInfo();
            Set<String> brokerAddrs = new HashSet<>();
            for (BrokerData brokerData : clusterInfo.getBrokerAddrTable().values()) {
                String addr = brokerData.selectBrokerAddr();
                if (addr != null && !addr.isEmpty()) brokerAddrs.add(addr);
            }
            if (brokerAddrs.isEmpty()) throw BusinessException.badRequest("没有可用的 Broker");
            defaultMQAdminExt.deleteTopicInBroker(brokerAddrs, topicName);
            log.info("删除 Topic 成功: {}", topicName);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("删除 Topic 失败: {}", topicName, e);
            throw BusinessException.badRequest("删除 Topic 失败: " + e.getMessage());
        }
    }
}
