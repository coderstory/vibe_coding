package cn.coderstory.springboot.order.controller;

import cn.coderstory.springboot.order.entity.Order;
import cn.coderstory.springboot.order.service.OrderService;
import cn.coderstory.springboot.vo.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @GetMapping("/my")
    public ApiResponse<List<Order>> myOrders(@RequestHeader("X-User-Id") Long userId) {
        return ApiResponse.success(orderService.getUserOrders(userId));
    }

    @PostMapping("/{orderNo}/pay")
    public ApiResponse<Boolean> payOrder(@PathVariable String orderNo) {
        return ApiResponse.success(orderService.payOrder(orderNo));
    }

    @PostMapping("/{orderNo}/cancel")
    public ApiResponse<Boolean> cancelOrder(@PathVariable String orderNo) {
        return ApiResponse.success(orderService.cancelOrder(orderNo));
    }
}