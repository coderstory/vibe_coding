package cn.coderstory.springboot.stock.service;

/**
 * 库存服务接口
 *
 * 功能说明：
 * - 提供商品库存的扣减、回滚、查询和初始化功能
 * - 采用多层防护保证库存数据一致性
 * - 支持与 Redis 的缓存同步
 *
 * 核心功能：
 * 1. 库存扣减 - 乐观锁 + 分布式锁双重保证
 * 2. 库存回滚 - 处理订单取消或超时
 * 3. 库存查询 - 支持 Redis 缓存
 * 4. 库存初始化 - 初始化商品库存
 *
 * @author system
 * @version 1.1.0
 */
public interface StockService {

    /**
     * 扣减库存
     *
     * 功能说明：
     * - 从可用库存中扣减指定数量
     * - 使用乐观锁 + 分布式锁双重保证
     * - 同步更新 Redis 缓存
     *
     * @param goodsId 商品ID
     * @param quantity 扣减数量
     * @return 是否扣减成功
     */
    boolean deductStock(Long goodsId, Integer quantity);

    /**
     * 回滚库存
     *
     * 功能说明：
     * - 将扣减的库存返还到可用库存
     * - 用于订单取消或超时场景
     *
     * @param goodsId 商品ID
     * @param quantity 回滚数量
     * @return 是否回滚成功
     */
    boolean rollbackStock(Long goodsId, Integer quantity);

    /**
     * 获取可用库存
     *
     * 功能说明：
     * - 先查询 Redis 缓存
     * - 缓存不存在则查询数据库并回填缓存
     *
     * @param goodsId 商品ID
     * @return 可用库存数量
     */
    int getAvailableStock(Long goodsId);

    /**
     * 初始化商品库存
     *
     * 功能说明：
     * - 初始化商品的库存数据
     * - 同时初始化 Redis 缓存
     *
     * @param goodsId 商品ID
     * @param totalStock 总库存
     * @return 是否初始化成功
     */
    boolean initStock(Long goodsId, Integer totalStock);
}
