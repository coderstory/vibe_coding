package cn.coderstory.springboot.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RocketMQ Admin 配置
 * 用于管理 Topic、Consumer Group 等集群管理操作
 */
@Slf4j
@Configuration
public class RocketMQConfig {

    @Value("${rocketmq.name-server:localhost:9876}")
    private String nameServer;

    /**
     * 创建 RocketMQ Admin 扩展实例
     * 用于执行 Topic 管理、Consumer Group 管理等运维操作
     */
    @Bean
    public DefaultMQAdminExt defaultMQAdminExt() {
        DefaultMQAdminExt admin = new DefaultMQAdminExt();
        admin.setNamesrvAddr(nameServer);
        admin.setInstanceName("RocketMQAdmin-" + System.currentTimeMillis());
        try {
            admin.start();
            log.info("RocketMQ Admin 启动成功, nameserver: {}", nameServer);
        } catch (Exception e) {
            log.error("RocketMQ Admin 启动失败", e);
        }
        return admin;
    }
}
