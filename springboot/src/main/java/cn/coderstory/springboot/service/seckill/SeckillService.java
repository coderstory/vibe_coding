package cn.coderstory.springboot.service.seckill;

import cn.coderstory.springboot.dto.seckill.SeckillRequest;
import cn.coderstory.springboot.dto.seckill.SeckillResponse;

/**
 * 秒杀服务接口 - 核心业务逻辑
 * <p>
 * 工作原理：
 * ┌────────────────────────────────────────────────────────────────────────────┐
 * │                           三层保护机制                                      │
 * ├────────────────────────────────────────────────────────────────────────────┤
 * │ 第一层：Redis原子扣减                                                       │
 * │   - 使用DECR原子操作快速判断库存是否充足                                    │
 * │   - 高性能过滤掉大部分无效请求，避免直接打到数据库                            │
 * │   - 库存预热阶段会将库存数据加载到Redis                                     │
 * │                                                                            │
 * │ 第二层：RocketMQ消息队列                                                   │
 * │   - 削峰填谷，将高并发请求异步化处理                                         │
 * │   - 控制进入数据库的并发量                                                   │
 * │   - 事务消息保证订单创建和库存扣减的最终一致性                                │
 * │                                                                            │
 * │ 第三层：数据库乐观锁                                                        │
 * │   - 最终数据一致性保障                                                      │
 * │   - UPDATE时使用version字段防止超卖                                         │
 * │   - 确保即使MQ出现问题也能保证数据正确                                       │
 * └────────────────────────────────────────────────────────────────────────────┘
 * <p>
 * 秒杀流程：
 * 1. 接收用户抢购请求，验证签名防篡改
 * 2. 检查用户是否在黑名单（风控）
 * 3. 检查请求幂等性（防止重复提交）
 * 4. QPS限流 + 并发限流（流量控制）
 * 5. Redis原子操作扣减库存
 * 6. 发送RocketMQ事务消息
 * 7. 返回排队编号，用户等待异步处理
 * 8. MQ消费者处理消息，创建订单并最终扣减数据库库存
 * <p>
 * 使用场景：
 * - 高并发秒杀活动
 * - 限时抢购场景
 * - 任何需要流量控制的商品销售场景
 *
 * @author system
 * @version 1.1.0
 */
public interface SeckillService {

    /**
     * 执行秒杀操作
     *
     * @param request 秒杀请求参数，包含：
     *                - goodsId: 商品ID
     *                - activityId: 活动ID
     *                - sign: 签名（防篡改）
     *                - timestamp: 时间戳
     *                - idempotentKey: 幂等键
     * @param userId  用户ID
     * @return SeckillResponse 包含：
     * - queueId: 排队编号（用于查询结果）
     * - status: 状态 (0-排队中 1-成功 2-失败)
     * - message: 状态消息
     * <p>
     * 业务逻辑：
     * 1. 验证签名是否正确
     * 2. 检查黑名单
     * 3. 检查幂等性（同一idempotentKey只处理一次）
     * 4. 限流检查
     * 5. Redis原子扣减库存
     * 6. 发送MQ事务消息
     * 7. 返回排队编号
     */
    SeckillResponse seckill(SeckillRequest request, Long userId);

}
