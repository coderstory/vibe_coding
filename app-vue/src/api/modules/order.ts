import request from '../request'

export interface Order {
  id: number;
  orderNo: string;
  userId: number;
  goodsId: number;
  activityId: number;
  quantity: number;
  price: number;
  status: number;
  queueId: string;
  createTime: string;
}

export const orderApi = {
  /**
   * 获取我的订单列表。
   *
   * @return 订单列表
   */
  getMyOrders() {
    return request.get<Order[]>('/order/my')
  },

  /**
   * 支付订单。
   *
   * @param orderNo 订单编号
   * @return 支付结果
   */
  payOrder(orderNo: string) {
    return request.post<Boolean>(`/order/${orderNo}/pay`)
  },

  /**
   * 取消订单。
   *
   * @param orderNo 订单编号
   * @return 取消结果
   */
  cancelOrder(orderNo: string) {
    return request.post<Boolean>(`/order/${orderNo}/cancel`)
  }
}
