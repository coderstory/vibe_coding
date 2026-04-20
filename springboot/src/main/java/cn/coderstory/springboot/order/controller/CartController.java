package cn.coderstory.springboot.order.controller;

import cn.coderstory.springboot.order.entity.Cart;
import cn.coderstory.springboot.order.service.CartService;
import cn.coderstory.springboot.vo.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @GetMapping("/my")
    public ApiResponse<List<Cart>> myCart(@RequestHeader("X-User-Id") Long userId) {
        return ApiResponse.success(cartService.getUserCart(userId));
    }

    @PostMapping("/add")
    public ApiResponse<Void> addToCart(@RequestParam Long goodsId,
                                       @RequestParam(defaultValue = "1") Integer quantity,
                                       @RequestHeader("X-User-Id") Long userId) {
        cartService.addToCart(userId, goodsId, quantity);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{goodsId}")
    public ApiResponse<Boolean> removeFromCart(@PathVariable Long goodsId,
                                               @RequestHeader("X-User-Id") Long userId) {
        return ApiResponse.success(cartService.removeFromCart(userId, goodsId));
    }

    @DeleteMapping("/clear")
    public ApiResponse<Void> clearCart(@RequestHeader("X-User-Id") Long userId) {
        cartService.clearCart(userId);
        return ApiResponse.success(null);
    }
}