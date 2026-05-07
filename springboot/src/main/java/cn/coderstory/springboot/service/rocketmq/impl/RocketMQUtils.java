package cn.coderstory.springboot.service.rocketmq.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.remoting.protocol.body.ClusterInfo;
import org.apache.rocketmq.remoting.protocol.route.BrokerData;
import org.apache.rocketmq.remoting.protocol.route.TopicRouteData;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;

import java.util.Map;

@Slf4j
final class RocketMQUtils {

    private RocketMQUtils() {}

    static String getFirstBrokerAddr(DefaultMQAdminExt adminExt) {
        try {
            ClusterInfo clusterInfo = adminExt.examineBrokerClusterInfo();
            Map<String, BrokerData> brokerAddrTable = clusterInfo.getBrokerAddrTable();
            if (brokerAddrTable != null && !brokerAddrTable.isEmpty()) {
                for (BrokerData brokerData : brokerAddrTable.values()) {
                    String addr = brokerData.selectBrokerAddr();
                    if (addr != null && !addr.isEmpty()) return addr;
                }
            }
        } catch (Exception e) {
            log.error("获取 Broker 地址失败", e);
        }
        return null;
    }

    static String getFirstBrokerAddr(DefaultMQAdminExt adminExt, String topic) {
        try {
            TopicRouteData routeData = adminExt.examineTopicRouteInfo(topic);
            if (routeData != null && routeData.getQueueDatas() != null && !routeData.getQueueDatas().isEmpty()) {
                String brokerName = routeData.getQueueDatas().get(0).getBrokerName();
                ClusterInfo clusterInfo = adminExt.examineBrokerClusterInfo();
                Map<String, BrokerData> brokerAddrTable = clusterInfo.getBrokerAddrTable();
                if (brokerAddrTable != null) {
                    BrokerData brokerData = brokerAddrTable.get(brokerName);
                    if (brokerData != null) {
                        String addr = brokerData.selectBrokerAddr();
                        if (addr != null && !addr.isEmpty()) return addr;
                    }
                }
            }
        } catch (Exception e) {
            log.debug("获取 Topic {} 的 Broker 地址失败: {}", topic, e.getMessage());
        }
        return getFirstBrokerAddr(adminExt);
    }

    static String convertPermToString(int perm) {
        return switch (perm) {
            case 2 -> "WRITE";
            case 6 -> "READ_WRITE";
            default -> "READ";
        };
    }

    static int parsePerm(String perm) {
        if (perm == null || perm.isEmpty()) return 4;
        return switch (perm.toUpperCase()) {
            case "WRITE" -> 2;
            case "READ_WRITE" -> 6;
            default -> 4;
        };
    }
}
