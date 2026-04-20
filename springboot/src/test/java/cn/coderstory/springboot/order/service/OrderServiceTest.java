package cn.coderstory.springboot.order.service;

import cn.coderstory.springboot.SpringbootApplication;
import cn.coderstory.springboot.order.mapper.OrderMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OrderService 集成测试
 *
 * 使用 @SpringBootTest 进行集成测试，连接实际 MySQL
 * 测试数据会在 @AfterEach 中清理
 *
 * @author system
 * @version 1.0
 * @since 2026-04-20
 */
@SpringBootTest(classes = SpringbootApplication.class)
@DisplayName("OrderService 集成测试")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderMapper orderMapper;

    private final Set<Long> testOrderIds = new HashSet<>();
    private final List<String> testOrderNos = new ArrayList<>();

    @AfterEach
    void tearDown() {
        testOrderIds.forEach(orderId -> {
            try {
                orderMapper.deleteById(orderId);
            } catch (Exception ignored) {
            }
        });
        testOrderIds.clear();

        testOrderNos.forEach(orderNo -> {
            try {
                orderMapper.delete(new LambdaQueryWrapper<cn.coderstory.springboot.order.entity.Order>()
                    .eq(cn.coderstory.springboot.order.entity.Order::getOrderNo, orderNo));
            } catch (Exception ignored) {
            }
        });
        testOrderNos.clear();
    }

    @Test
    @Order(1)
    @DisplayName("应能创建秒杀订单")
    void shouldCreateSeckillOrder() {
        String queueId = "test-queue-" + System.currentTimeMillis();

        cn.coderstory.springboot.order.entity.Order result = orderService.createSeckillOrder(1L, 100L, 1L, queueId);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(1L, result.getUserId());

        testOrderIds.add(result.getId());
        testOrderNos.add(result.getOrderNo());
    }

    @Test
    @Order(2)
    @DisplayName("应能支付订单")
    void shouldPayOrder() {
        String queueId = "test-pay-" + System.currentTimeMillis();

        cn.coderstory.springboot.order.entity.Order created = orderService.createSeckillOrder(1L, 100L, 1L, queueId);
        testOrderIds.add(created.getId());
        testOrderNos.add(created.getOrderNo());

        boolean payResult = orderService.payOrder(created.getOrderNo());

        assertTrue(payResult);
    }

    @Test
    @Order(3)
    @DisplayName("应能取消订单")
    void shouldCancelOrder() {
        String queueId = "test-cancel-" + System.currentTimeMillis();

        cn.coderstory.springboot.order.entity.Order created = orderService.createSeckillOrder(1L, 100L, 1L, queueId);
        testOrderIds.add(created.getId());
        testOrderNos.add(created.getOrderNo());

        boolean cancelResult = orderService.cancelOrder(created.getOrderNo());

        assertTrue(cancelResult);
    }

    @Test
    @Order(4)
    @DisplayName("应能获取用户订单列表")
    void shouldGetUserOrders() {
        String queueId = "test-list-" + System.currentTimeMillis();

        cn.coderstory.springboot.order.entity.Order created = orderService.createSeckillOrder(1L, 100L, 1L, queueId);
        testOrderIds.add(created.getId());
        testOrderNos.add(created.getOrderNo());

        List<cn.coderstory.springboot.order.entity.Order> orders = orderService.getUserOrders(1L);

        assertNotNull(orders);
    }
}
