package cn.coderstory.springboot.order.service.impl;

import cn.coderstory.springboot.order.entity.Order;
import cn.coderstory.springboot.order.mapper.OrderMapper;
import cn.coderstory.springboot.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderMapper orderMapper;

    @Override
    @Transactional
    public Order createSeckillOrder(Long userId, Long goodsId, Long activityId, String queueId) {
        Order order = new Order();
        order.setOrderNo(UUID.randomUUID().toString().replace("-", ""));
        order.setUserId(userId);
        order.setGoodsId(goodsId);
        order.setActivityId(activityId);
        order.setQueueId(queueId);
        order.setQuantity(1);
        order.setStatus(0);
        order.setCreateTime(LocalDateTime.now());
        orderMapper.insert(order);
        return order;
    }

    @Override
    @Transactional
    public boolean payOrder(String orderNo) {
        Order order = orderMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Order>()
                .eq(Order::getOrderNo, orderNo)
        );
        if (order == null || order.getStatus() != 0) {
            return false;
        }
        order.setStatus(1);
        order.setPaymentTime(LocalDateTime.now());
        return orderMapper.updateById(order) > 0;
    }

    @Override
    @Transactional
    public boolean cancelOrder(String orderNo) {
        Order order = orderMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Order>()
                .eq(Order::getOrderNo, orderNo)
        );
        if (order == null || order.getStatus() != 0) {
            return false;
        }
        order.setStatus(2);
        return orderMapper.updateById(order) > 0;
    }

    @Override
    public List<Order> getUserOrders(Long userId) {
        return orderMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId)
                .orderByDesc(Order::getCreateTime)
        );
    }
}