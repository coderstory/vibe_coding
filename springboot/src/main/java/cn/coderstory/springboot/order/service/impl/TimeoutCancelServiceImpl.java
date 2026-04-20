package cn.coderstory.springboot.order.service.impl;

import cn.coderstory.springboot.order.entity.Order;
import cn.coderstory.springboot.order.mapper.OrderMapper;
import cn.coderstory.springboot.order.service.TimeoutCancelService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimeoutCancelServiceImpl implements TimeoutCancelService {
    private final OrderMapper orderMapper;

    @Override
    @Scheduled(fixedDelay = 60000)
    public void scheduleOrderTimeoutCheck() {
        LocalDateTime expireTime = LocalDateTime.now().minusMinutes(15);
        List<Order> expiredOrders = orderMapper.selectList(
            new LambdaQueryWrapper<Order>()
                .eq(Order::getStatus, 0)
                .lt(Order::getCreateTime, expireTime)
        );

        for (Order order : expiredOrders) {
            order.setStatus(3);
            orderMapper.updateById(order);
            log.info("订单超时取消: {}", order.getOrderNo());
        }
    }
}