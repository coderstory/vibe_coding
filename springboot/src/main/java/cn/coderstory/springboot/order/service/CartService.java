package cn.coderstory.springboot.order.service;

import cn.coderstory.springboot.order.entity.Cart;
import java.util.List;

public interface CartService {
    void addToCart(Long userId, Long goodsId, Integer quantity);
    List<Cart> getUserCart(Long userId);
    boolean removeFromCart(Long userId, Long goodsId);
    void clearCart(Long userId);
}