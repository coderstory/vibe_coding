package cn.coderstory.springboot.order.service.impl;

import cn.coderstory.springboot.order.entity.Cart;
import cn.coderstory.springboot.order.mapper.CartMapper;
import cn.coderstory.springboot.order.service.CartService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
    private final CartMapper cartMapper;

    @Override
    public void addToCart(Long userId, Long goodsId, Integer quantity) {
        Cart existing = cartMapper.selectOne(
            new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .eq(Cart::getGoodsId, goodsId)
        );

        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + quantity);
            cartMapper.updateById(existing);
        } else {
            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setGoodsId(goodsId);
            cart.setQuantity(quantity);
            cart.setCreateTime(LocalDateTime.now());
            cartMapper.insert(cart);
        }
    }

    @Override
    public List<Cart> getUserCart(Long userId) {
        return cartMapper.selectList(
            new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .orderByDesc(Cart::getCreateTime)
        );
    }

    @Override
    public boolean removeFromCart(Long userId, Long goodsId) {
        Cart cart = cartMapper.selectOne(
            new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .eq(Cart::getGoodsId, goodsId)
        );
        if (cart == null) {
            return false;
        }
        return cartMapper.deleteById(cart.getId()) > 0;
    }

    @Override
    public void clearCart(Long userId) {
        cartMapper.delete(
            new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
        );
    }
}