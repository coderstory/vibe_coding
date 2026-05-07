package cn.coderstory.springboot.service.order;

import cn.coderstory.springboot.entity.order.Order;

import java.util.List;

public interface OrderService {
    Order createSeckillOrder(Long userId, Long goodsId, Long activityId, String queueId);

    boolean payOrder(String orderNo);

    boolean cancelOrder(String orderNo);

    List<Order> getUserOrders(Long userId);
}
