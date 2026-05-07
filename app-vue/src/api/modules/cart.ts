import request from '../request'

export interface Cart {
  id: number;
  userId: number;
  goodsId: number;
  quantity: number;
}

export const cartApi = {
  getMyCart() {
    return request.get<Cart[]>('/cart/my')
  },
  removeFromCart(goodsId: number) {
    return request.delete<Boolean>(`/cart/${goodsId}`)
  },
  clearCart() {
    return request.delete('/cart/clear')
  }
}
