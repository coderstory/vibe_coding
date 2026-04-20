import request from './request'

export interface Order {
  id: number
  orderNo: string
  userId: number
  goodsId: number
  activityId: number
  quantity: number
  price: number
  status: number
  queueId: string
  createTime: string
}

export const orderApi = {
  getMyOrders() {
    return request.get<Order[]>('/api/order/my')
  },
  payOrder(orderNo: string) {
    return request.post<Boolean>(`/api/order/${orderNo}/pay`)
  },
  cancelOrder(orderNo: string) {
    return request.post<Boolean>(`/api/order/${orderNo}/cancel`)
  }
}