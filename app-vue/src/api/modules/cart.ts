import request from '../request'

export interface Cart {
  id: number;
  userId: number;
  goodsId: number;
  quantity: number;
}

export const cartApi = {
  /**
   * 获取我的购物车列表。
   *
   * @return 购物车列表
   */
  getMyCart() {
    return request.get<Cart[]>('/cart/my')
  },

  /**
   * 从购物车移除商品。
   *
   * @param goodsId 商品 ID
   * @return 移除结果
   */
  removeFromCart(goodsId: number) {
    return request.delete<Boolean>(`/cart/${goodsId}`)
  },

  /**
   * 清空购物车。
   *
   * @return 清空结果
   */
  clearCart() {
    return request.delete('/cart/clear')
  }
}
