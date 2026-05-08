package cn.coderstory.springboot.controller.order;

import cn.coderstory.springboot.entity.order.Cart;
import cn.coderstory.springboot.service.order.CartService;
import cn.coderstory.springboot.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 购物车管理控制器。
 * <p>
 * 提供购物车的查询、添加商品、移除商品和清空购物车功能。
 *
 * @since 1.7.0
 */
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    /**
     * 获取当前用户的购物车列表。
     *
     * @param userId 用户 ID（从请求头获取）
     * @return 购物车商品列表
     */
    @GetMapping("/my")
    public ApiResponse<List<Cart>> myCart(@RequestHeader("X-User-Id") Long userId) {
        return ApiResponse.success(cartService.getUserCart(userId));
    }

    /**
     * 添加商品到购物车。
     *
     * @param goodsId  商品 ID
     * @param quantity 商品数量（默认为 1）
     * @param userId   用户 ID（从请求头获取）
     * @return 操作结果
     */
    @PostMapping("/add")
    public ApiResponse<Void> addToCart(@RequestParam Long goodsId,
                                       @RequestParam(defaultValue = "1") Integer quantity,
                                       @RequestHeader("X-User-Id") Long userId) {
        cartService.addToCart(userId, goodsId, quantity);
        return ApiResponse.success(null);
    }

    /**
     * 从购物车移除指定商品。
     *
     * @param goodsId 商品 ID
     * @param userId  用户 ID（从请求头获取）
     * @return 移除结果
     */
    @DeleteMapping("/{goodsId}")
    public ApiResponse<Boolean> removeFromCart(@PathVariable Long goodsId,
                                               @RequestHeader("X-User-Id") Long userId) {
        return ApiResponse.success(cartService.removeFromCart(userId, goodsId));
    }

    /**
     * 清空当前用户的购物车。
     *
     * @param userId 用户 ID（从请求头获取）
     * @return 操作结果
     */
    @DeleteMapping("/clear")
    public ApiResponse<Void> clearCart(@RequestHeader("X-User-Id") Long userId) {
        cartService.clearCart(userId);
        return ApiResponse.success(null);
    }
}
