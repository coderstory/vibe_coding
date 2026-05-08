package cn.coderstory.springboot.service.order;

import cn.coderstory.springboot.entity.order.Cart;

import java.util.List;

/**
 * 购物车管理服务接口。
 * <p>
 * 提供购物车的添加、查询、删除和清空功能。
 * 购物车数据以用户维度存储，每个商品在同一用户下唯一。
 *
 * @since 1.7.0
 */
public interface CartService {

    /**
     * 添加商品到购物车。
     * <p>
     * 如果该商品已存在于用户购物车，则累加数量。
     *
     * @param userId   用户 ID
     * @param goodsId  商品 ID
     * @param quantity 添加数量
     */
    void addToCart(Long userId, Long goodsId, Integer quantity);

    /**
     * 获取用户购物车中的商品列表。
     *
     * @param userId 用户 ID
     * @return 购物车商品列表
     */
    List<Cart> getUserCart(Long userId);

    /**
     * 从购物车中移除指定商品。
     *
     * @param userId  用户 ID
     * @param goodsId 商品 ID
     * @return 是否移除成功
     */
    boolean removeFromCart(Long userId, Long goodsId);

    /**
     * 清空用户的整个购物车。
     *
     * @param userId 用户 ID
     */
    void clearCart(Long userId);
}
