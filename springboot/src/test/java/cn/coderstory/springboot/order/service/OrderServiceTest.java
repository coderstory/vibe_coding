package cn.coderstory.springboot.order.service;

import cn.coderstory.springboot.SpringbootApplication;
import cn.coderstory.springboot.order.entity.Order;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OrderService 集成测试
 *
 * 使用 @SpringBootTest 进行集成测试
 *
 * @author system
 * @version 1.0
 * @since 2026-04-20
 */
@SpringBootTest(classes = SpringbootApplication.class)
@DisplayName("OrderService 集成测试")
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    /**
     * 测试用例：创建秒杀订单
     */
    @Test
    @DisplayName("应能创建秒杀订单")
    void shouldCreateSeckillOrder() {
        String queueId = "test-queue-" + System.currentTimeMillis();

        Order result = orderService.createSeckillOrder(1L, 100L, 1L, queueId);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(1L, result.getUserId());
    }

    /**
     * 测试用例：支付订单
     */
    @Test
    @DisplayName("应能支付订单")
    void shouldPayOrder() {
        String queueId = "test-pay-" + System.currentTimeMillis();

        Order created = orderService.createSeckillOrder(1L, 100L, 1L, queueId);

        boolean payResult = orderService.payOrder(created.getOrderNo());

        assertTrue(payResult);
    }

    /**
     * 测试用例：取消订单
     */
    @Test
    @DisplayName("应能取消订单")
    void shouldCancelOrder() {
        String queueId = "test-cancel-" + System.currentTimeMillis();

        Order created = orderService.createSeckillOrder(1L, 100L, 1L, queueId);

        boolean cancelResult = orderService.cancelOrder(created.getOrderNo());

        assertTrue(cancelResult);
    }

    /**
     * 测试用例：获取用户订单列表
     */
    @Test
    @DisplayName("应能获取用户订单列表")
    void shouldGetUserOrders() {
        List<Order> orders = orderService.getUserOrders(1L);

        assertNotNull(orders);
    }
}
