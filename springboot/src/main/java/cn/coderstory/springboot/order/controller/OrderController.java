package cn.coderstory.springboot.order.controller;

import cn.coderstory.springboot.order.entity.Order;
import cn.coderstory.springboot.order.service.OrderService;
import cn.coderstory.springboot.vo.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 订单控制器
 *
 * 功能描述：
 * - 提供订单查询、支付、取消等操作接口
 * - 支持用户查看自己的订单列表
 *
 * 接口列表：
 * - GET /api/order/my: 获取当前用户的订单列表
 * - POST /api/order/{orderNo}/pay: 支付订单
 * - POST /api/order/{orderNo}/cancel: 取消订单
 *
 * @author system
 * @version 1.0
 * @since 2026-04-20
 */
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 获取当前用户的订单列表
     *
     * @param userId 用户 ID（从请求头获取）
     * @return 订单列表（按创建时间倒序）
     */
    @GetMapping("/my")
    public ApiResponse<List<Order>> myOrders(@RequestHeader("X-User-Id") Long userId) {
        return ApiResponse.success(orderService.getUserOrders(userId));
    }

    /**
     * 支付订单
     *
     * @param orderNo 订单号
     * @return 支付结果
     */
    @PostMapping("/{orderNo}/pay")
    public ApiResponse<Boolean> payOrder(@PathVariable String orderNo) {
        return ApiResponse.success(orderService.payOrder(orderNo));
    }

    /**
     * 取消订单
     *
     * 只能取消状态为"待支付"的订单
     *
     * @param orderNo 订单号
     * @return 取消结果
     */
    @PostMapping("/{orderNo}/cancel")
    public ApiResponse<Boolean> cancelOrder(@PathVariable String orderNo) {
        return ApiResponse.success(orderService.cancelOrder(orderNo));
    }
}
