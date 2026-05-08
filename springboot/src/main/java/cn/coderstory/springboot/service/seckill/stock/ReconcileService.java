package cn.coderstory.springboot.service.seckill.stock;

/**
 * 库存对账服务接口。
 * <p>
 * 对比 Redis 库存与数据库库存的差异，在不一致时进行修正。
 * 用于补偿因异常（如消息丢失、服务宕机）导致的库存数据不一致。
 *
 * @since 1.7.0
 */
public interface ReconcileService {

    /**
     * 执行库存对账。
     * <p>
     * 遍历所有秒杀商品，比较 Redis 与数据库的库存值，
     * 发现差异时以数据库为准进行修正。
     */
    void reconcileStock();
}