import request from './request'

export interface Cart {
  id: number
  userId: number
  goodsId: number
  quantity: number
}

export const cartApi = {
  getMyCart() {
    return request.get<Cart[]>('/api/cart/my')
  },
  addToCart(goodsId: number, quantity: number = 1) {
    return request.post('/api/cart/add', null, { params: { goodsId, quantity } })
  },
  removeFromCart(goodsId: number) {
    return request.delete<Boolean>(`/api/cart/${goodsId}`)
  },
  clearCart() {
    return request.delete('/api/cart/clear')
  }
}