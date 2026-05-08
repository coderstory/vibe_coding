package cn.coderstory.springboot.service.order;

/**
 * 订单超时取消服务接口。
 * <p>
 * 定期扫描超时未支付的订单，自动执行取消操作并回滚库存。
 * 通过定时任务调度，避免未支付订单长期占用库存。
 *
 * @since 1.7.0
 */
public interface TimeoutCancelService {

    /**
     * 执行订单超时检查。
     * <p>
     * 查询所有超过支付时限且状态为未支付的订单，
     * 逐个执行取消逻辑并回滚对应商品的库存。
     */
    void scheduleOrderTimeoutCheck();
}