package cn.coderstory.springboot.order.service;

import cn.coderstory.springboot.order.entity.Order;
import java.util.List;

public interface OrderService {
    Order createSeckillOrder(Long userId, Long goodsId, Long activityId, String queueId);
    boolean payOrder(String orderNo);
    boolean cancelOrder(String orderNo);
    List<Order> getUserOrders(Long userId);
}