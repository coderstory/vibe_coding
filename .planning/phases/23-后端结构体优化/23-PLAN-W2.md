---
phase: 23
name: 后端结构体优化
wave: 2
depends_on: []
requirements: [BAC-09]
autonomous: false
files_modified:
  - springboot/src/main/java/cn/coderstory/springboot/rocketmq/service/impl/RocketMQAdminServiceImpl.java
  - springboot/src/main/java/cn/coderstory/springboot/rocketmq/service/impl/*TopicService.java
  - springboot/src/main/java/cn/coderstory/springboot/rocketmq/service/impl/*ConsumerService.java
  - springboot/src/main/java/cn/coderstory/springboot/rocketmq/service/impl/*MessageService.java
  - springboot/src/main/java/cn/coderstory/springboot/rocketmq/service/impl/*ClusterService.java
---

# Plan: 后端结构体优化 - Wave 2 - 拆分 RocketMQ 上帝类

## Objective

将 RocketMQAdminServiceImpl（1098行）按功能域拆分为 4 个职责单一的类，采用 Facade 模式保持接口不变。

## Tasks

### Task 1: 抽取 TopicService

<read_first>
- springboot/src/main/java/cn/coderstory/springboot/rocketmq/service/impl/RocketMQAdminServiceImpl.java
</read_first>

<action>
从 RocketMQAdminServiceImpl 中抽取 Topic 相关方法到独立类：

**新建类：** `RocketMQTopicServiceImpl`
- 方法：getTopicList, getTopicDetail, createTopic, deleteTopic
- 注入：DefaultMQAdminExt
- 拷贝相关的常量（SYSTEM_TOPIC_PREFIXES）和辅助方法

**原有类：** RocketMQAdminServiceImpl
- 删除上述 4 个方法的实现
- 注入 RocketMQTopicServiceImpl
- 委托调用

编译验证：
```bash
./gradlew.bat build -x test
```
</action>

<acceptance_criteria>
- [ ] 4 个 Topic API 功能正常（getTopicList, getTopicDetail, createTopic, deleteTopic）
- [ ] `./gradlew.bat build -x test` 编译通过
- [ ] RocketMQAdminServiceImpl 行数减少（减去 ~250 行）
</acceptance_criteria>

---

### Task 2: 抽取 ConsumerService

<read_first>
- springboot/src/main/java/cn/coderstory/springboot/rocketmq/service/impl/RocketMQAdminServiceImpl.java
</read_first>

<action>
从 RocketMQAdminServiceImpl 中抽取 Consumer Group 相关方法：

**新建类：** `RocketMQConsumerServiceImpl`
- 方法：getConsumerGroupList, getConsumerGroupDetail, resetConsumerOffset, deleteConsumerGroup
- 注入：DefaultMQAdminExt
- 拷贝相关的常量（SYSTEM_GROUP_PREFIXES）和辅助方法

**原有类：** RocketMQAdminServiceImpl
- 删除上述 4 个方法的实现
- 注入 RocketMQConsumerServiceImpl
- 委托调用

编译验证：
```bash
./gradlew.bat build -x test
```
</action>

<acceptance_criteria>
- [ ] 4 个 Consumer Group API 功能正常
- [ ] `./gradlew.bat build -x test` 编译通过
</acceptance_criteria>

---

### Task 3: 抽取 MessageService

<read_first>
- springboot/src/main/java/cn/coderstory/springboot/rocketmq/service/impl/RocketMQAdminServiceImpl.java
</read_first>

<action>
从 RocketMQAdminServiceImpl 中抽取消息管理相关方法：

**新建类：** `RocketMQMessageServiceImpl`
- 方法：getMessageList, getMessageDetail, getMessageTrace, sendMessage
- 注入：DefaultMQAdminExt, DefaultMQProducer
- 相关的辅助方法

**原有类：** RocketMQAdminServiceImpl
- 删除上述 4 个方法的实现
- 注入 RocketMQMessageServiceImpl
- 委托调用

编译验证：
```bash
./gradlew.bat build -x test
```
</action>

<acceptance_criteria>
- [ ] 4 个 Message API 功能正常
- [ ] `./gradlew.bat build -x test` 编译通过
</acceptance_criteria>

---

### Task 4: 抽取 ClusterService

<read_first>
- springboot/src/main/java/cn/coderstory/springboot/rocketmq/service/impl/RocketMQAdminServiceImpl.java
</read_first>

<action>
从 RocketMQAdminServiceImpl 中抽取监控面板相关方法：

**新建类：** `RocketMQClusterServiceImpl`
- 方法：getClusterOverview, getBrokerStatusList, getTopicBacklogList, getBrokerMetrics
- 注入：DefaultMQAdminExt
- 相关的辅助方法

**原有类：** RocketMQAdminServiceImpl
- 删除上述 4 个方法的实现和所有剩余常量
- 如所有方法已抽取完毕，检查是否还有其他私有辅助方法残留

编译验证：
```bash
./gradlew.bat build -x test
```

最终 RocketMQAdminServiceImpl 应为 ~50 行（仅注入 4 个委托 + Facade 方法转发）。
</action>

<acceptance_criteria>
- [ ] 4 个 Cluster API 功能正常
- [ ] `./gradlew.bat build -x test` 编译通过
- [ ] RocketMQAdminServiceImpl 从 1098 行降至 ~50 行
- [ ] 4 个新类各约 250 行，职责单一
</acceptance_criteria>

## Verification

### must_haves
1. 编译通过
2. RocketMQ 所有 API 端点功能无回归
3. RocketMQAdminServiceImpl 行数 < 100
4. 每个新类职责单一（仅包含单一模块的方法）

### truths
- 拆分后接口不变，Controller 无需修改
- 原有 RocketMQAdminService 接口保持不动
