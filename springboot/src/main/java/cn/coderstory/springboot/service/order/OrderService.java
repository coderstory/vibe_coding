package cn.coderstory.springboot.service.order;

import cn.coderstory.springboot.entity.order.Order;

import java.util.List;

/**
 * 订单管理服务接口。
 * <p>
 * 提供秒杀订单的创建、支付、取消和用户订单查询功能。
 * 秒杀订单通过 RocketMQ 异步创建，保证高并发下的最终一致性。
 *
 * @since 1.7.0
 */
public interface OrderService {

    /**
     * 创建秒杀订单。
     * <p>
     * 由 RocketMQ 消费者调用，从队列中取出请求后创建数据库订单。
     * 创建过程包含库存二次校验和幂等处理。
     *
     * @param userId     用户 ID
     * @param goodsId    商品 ID
     * @param activityId 活动 ID
     * @param queueId    排队编号（幂等键）
     * @return 创建的订单
     */
    Order createSeckillOrder(Long userId, Long goodsId, Long activityId, String queueId);

    /**
     * 支付指定订单。
     *
     * @param orderNo 订单编号
     * @return 是否支付成功
     */
    boolean payOrder(String orderNo);

    /**
     * 取消指定订单。
     * <p>
     * 取消后自动回滚商品库存。
     *
     * @param orderNo 订单编号
     * @return 是否取消成功
     */
    boolean cancelOrder(String orderNo);

    /**
     * 获取用户的所有订单列表。
     *
     * @param userId 用户 ID
     * @return 订单列表
     */
    List<Order> getUserOrders(Long userId);
}
