package cn.coderstory.springboot.seckill.service;

import cn.coderstory.springboot.seckill.dto.SeckillRequest;
import cn.coderstory.springboot.seckill.dto.SeckillResponse;

/**
 * 秒杀服务接口 - 核心业务逻辑
 *
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
 *
 * 秒杀流程：
 * 1. 接收用户抢购请求，验证签名防篡改
 * 2. 检查用户是否在黑名单（风控）
 * 3. 检查请求幂等性（防止重复提交）
 * 4. QPS限流 + 并发限流（流量控制）
 * 5. Redis原子操作扣减库存
 * 6. 发送RocketMQ事务消息
 * 7. 返回排队编号，用户等待异步处理
 * 8. MQ消费者处理消息，创建订单并最终扣减数据库库存
 *
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
     *   - goodsId: 商品ID
     *   - activityId: 活动ID
     *   - sign: 签名（防篡改）
     *   - timestamp: 时间戳
     *   - idempotentKey: 幂等键
     * @param userId 用户ID
     * @return SeckillResponse 包含：
     *   - queueId: 排队编号（用于查询结果）
     *   - status: 状态 (0-排队中 1-成功 2-失败)
     *   - message: 状态消息
     *
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

    /**
     * 获取商品当前库存（从 Redis）
     *
     * @param goodsId 商品ID
     * @return 当前库存数量
     */
    int getStock(Long goodsId);

    /**
     * 预热商品库存到 Redis
     *
     * 功能说明：
     * - 在秒杀开始前调用
     * - 将数据库中的库存数据同步到 Redis
     * - 保证秒杀开始时无需查询数据库
     *
     * @param goodsId 商品ID
     * @param stock 库存数量
     */
    void preloadStock(Long goodsId, int stock);

    /**
     * 回滚 Redis 中的库存
     *
     * 功能说明：
     * - 当订单创建失败或超时取消时调用
     * - 将之前扣减的库存返还到 Redis
     *
     * @param goodsId 商品ID
     * @param quantity 回滚数量
     */
    void rollbackStock(Long goodsId, int quantity);
}
