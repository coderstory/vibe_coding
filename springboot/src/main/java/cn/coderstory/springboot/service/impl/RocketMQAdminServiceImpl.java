package cn.coderstory.springboot.service.impl;

import cn.coderstory.springboot.exception.BusinessException;
import cn.coderstory.springboot.service.RocketMQAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.TopicConfig;
import org.apache.rocketmq.remoting.protocol.body.TopicList;
import org.apache.rocketmq.remoting.protocol.route.TopicRouteData;
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

                // 获取 Topic 配置信息
                try {
                    TopicConfig topicConfig = defaultMQAdminExt.examineTopicConfig(nameServer, topicName);
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

            // Topic 配置
            TopicConfig topicConfig = defaultMQAdminExt.examineTopicConfig(nameServer, topicName);
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

            // 检查 Topic 是否已存在
            try {
                TopicConfig existing = defaultMQAdminExt.examineTopicConfig(nameServer, topicName);
                if (existing != null) {
                    throw BusinessException.conflict("Topic 已存在: " + topicName);
                }
            } catch (BusinessException e) {
                throw e;
            } catch (Exception e) {
                // Topic 不存在，可以创建
            }

            TopicConfig topicConfig = new TopicConfig();
            topicConfig.setTopicName(topicName);
            topicConfig.setWriteQueueNums(queueCount);
            topicConfig.setReadQueueNums(queueCount);
            topicConfig.setPerm(parsePerm(perm));

            defaultMQAdminExt.createAndUpdateTopicConfig(nameServer, topicConfig);
            log.info("创建 Topic 成功: {}, queueCount: {}, perm: {}", topicName, queueCount, perm);
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
            // 检查 Topic 是否存在
            TopicConfig existing = defaultMQAdminExt.examineTopicConfig(nameServer, topicName);
            if (existing == null) {
                throw BusinessException.notFound("Topic 不存在: " + topicName);
            }

            // 使用正确的 API: deleteTopicInBroker(Set<String> addrs, String topic)
            Set<String> brokerAddrs = new HashSet<>();
            brokerAddrs.add(nameServer);
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
}
