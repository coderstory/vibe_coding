package cn.coderstory.springboot.stock.service.impl;

import cn.coderstory.springboot.lock.DistributedLockService;
import cn.coderstory.springboot.lock.impl.DistributedLockServiceImpl;
import cn.coderstory.springboot.stock.entity.Stock;
import cn.coderstory.springboot.stock.mapper.StockMapper;
import cn.coderstory.springboot.stock.service.StockService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 库存服务实现类
 *
 * 功能描述：
 * - 提供商品库存的扣减、回滚和查询功能
 * - 采用多层防护保证库存数据一致性
 * - 支持与 Redis 的缓存同步
 *
 * 核心功能：
 * 1. 库存扣减 - 乐观锁 + 分布式锁双重保证
 * 2. 库存回滚 - 处理订单取消或超时
 * 3. 库存查询 - 支持 Redis 缓存
 *
 * 技术要点：
 * - 使用 MyBatis Plus 的 LambdaUpdateWrapper 构建更新条件
 * - 使用乐观锁（version 字段）防止并发更新冲突
 * - 使用分布式锁保证库存扣减的强一致性
 * - Redis 缓存同步保证数据最终一致性
 *
 * @author system
 * @version 1.0
 * @since 2026-04-20
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {

    // ==================== 静态常量 ====================

    /** Redis 库存 Key 前缀 */
    private static final String STOCK_KEY_PREFIX = "seckill:stock:";

    // ==================== 依赖注入 ====================

    /** 库存 Mapper，用于数据库操作 */
    private final StockMapper stockMapper;

    /** Redis 模板，用于缓存同步 */
    private final StringRedisTemplate redisTemplate;

    /** 分布式锁服务 */
    private final DistributedLockService distributedLockService;

    // ==================== 业务方法 ====================

    /**
     * 扣减库存
     *
     * 功能描述：
     * - 从可用库存中扣减指定数量
     * - 使用乐观锁 + 分布式锁双重保证
     * - 同步更新 Redis 缓存
     *
     * 处理流程：
     * 1. 查询当前库存
     * 2. 检查库存是否充足
     * 3. 使用乐观锁更新库存（检查 version）
     * 4. 同步 Redis 缓存
     *
     * @param goodsId 商品ID
     * @param quantity 扣减数量
     * @return 是否扣减成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deductStock(Long goodsId, Integer quantity) {
        log.info("开始扣减库存: goodsId={}, quantity={}", goodsId, quantity);

        // 使用分布式锁，保证强一致性
        String lockKey = DistributedLockServiceImpl.getStockLockKey(goodsId);

        Boolean result = distributedLockService.executeWithLock(
                lockKey,
                () -> doDeductStock(goodsId, quantity)
        );

        if (Boolean.TRUE.equals(result)) {
            // 同步 Redis 缓存
            redisTemplate.opsForValue().increment(STOCK_KEY_PREFIX + goodsId, -quantity);
            log.info("库存扣减成功: goodsId={}, quantity={}", goodsId, quantity);
            return true;
        }

        log.warn("库存扣减失败: goodsId={}, quantity={}", goodsId, quantity);
        return false;
    }

    /**
     * 执行实际的库存扣减操作
     *
     * @param goodsId 商品ID
     * @param quantity 扣减数量
     * @return 是否成功
     */
    private boolean doDeductStock(Long goodsId, Integer quantity) {
        // 查询当前库存信息
        Stock stock = stockMapper.selectOne(
                new LambdaQueryWrapper<Stock>()
                        .eq(Stock::getGoodsId, goodsId)
        );

        // 检查库存是否存在
        if (stock == null) {
            log.warn("商品库存记录不存在: goodsId={}", goodsId);
            return false;
        }

        // 检查库存是否充足
        if (stock.getAvailableStock() < quantity) {
            log.warn("库存不足: goodsId={}, available={}, required={}",
                    goodsId, stock.getAvailableStock(), quantity);
            return false;
        }

        // 使用乐观锁更新库存
        // 通过 version 字段防止并发更新冲突
        LambdaUpdateWrapper<Stock> updateWrapper = new LambdaUpdateWrapper<Stock>()
                .setSql("available_stock = available_stock - " + quantity)      // 可用库存减少
                .setSql("locked_stock = locked_stock + " + quantity)            // 锁定库存增加
                .setSql("version = version + 1")                                // 版本号递增
                .eq(Stock::getGoodsId, goodsId)                                // 商品ID条件
                .eq(Stock::getVersion, stock.getVersion())                      // 乐观锁条件
                .ge(Stock::getAvailableStock, quantity);                         // 库存充足条件

        int result = stockMapper.update(null, updateWrapper);

        if (result == 0) {
            log.warn("乐观锁更新失败，可能存在并发冲突: goodsId={}", goodsId);
            return false;
        }

        return true;
    }

    /**
     * 回滚库存
     *
     * 功能描述：
     * - 将扣减的库存返还到可用库存
     * - 用于订单取消或超时场景
     *
     * 处理流程：
     * 1. 查询当前库存
     * 2. 使用乐观锁回滚库存
     * 3. 同步 Redis 缓存
     *
     * @param goodsId 商品ID
     * @param quantity 回滚数量
     * @return 是否回滚成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean rollbackStock(Long goodsId, Integer quantity) {
        log.info("开始回滚库存: goodsId={}, quantity={}", goodsId, quantity);

        // 使用分布式锁
        String lockKey = DistributedLockServiceImpl.getStockLockKey(goodsId);

        Boolean result = distributedLockService.executeWithLock(
                lockKey,
                () -> doRollbackStock(goodsId, quantity)
        );

        if (Boolean.TRUE.equals(result)) {
            // 同步 Redis 缓存
            redisTemplate.opsForValue().increment(STOCK_KEY_PREFIX + goodsId, quantity);
            log.info("库存回滚成功: goodsId={}, quantity={}", goodsId, quantity);
            return true;
        }

        log.warn("库存回滚失败: goodsId={}, quantity={}", goodsId, quantity);
        return false;
    }

    /**
     * 执行实际的库存回滚操作
     *
     * @param goodsId 商品ID
     * @param quantity 回滚数量
     * @return 是否成功
     */
    private boolean doRollbackStock(Long goodsId, Integer quantity) {
        // 查询当前库存
        Stock stock = stockMapper.selectOne(
                new LambdaQueryWrapper<Stock>()
                        .eq(Stock::getGoodsId, goodsId)
        );

        if (stock == null) {
            log.warn("商品库存记录不存在，无法回滚: goodsId={}", goodsId);
            return false;
        }

        // 使用乐观锁回滚库存
        LambdaUpdateWrapper<Stock> updateWrapper = new LambdaUpdateWrapper<Stock>()
                .setSql("available_stock = available_stock + " + quantity)      // 可用库存增加
                .setSql("locked_stock = GREATEST(0, locked_stock - " + quantity + ")")  // 锁定库存减少（不能为负）
                .setSql("version = version + 1")                                // 版本号递增
                .eq(Stock::getGoodsId, goodsId)                                // 商品ID条件
                .eq(Stock::getVersion, stock.getVersion());                     // 乐观锁条件

        int result = stockMapper.update(null, updateWrapper);

        return result > 0;
    }

    /**
     * 获取可用库存
     *
     * 功能描述：
     * - 先查询 Redis 缓存
     * - 缓存不存在则查询数据库并回填缓存
     *
     * @param goodsId 商品ID
     * @return 可用库存数量
     */
    @Override
    public int getAvailableStock(Long goodsId) {
        // 先查询 Redis 缓存
        String stockKey = STOCK_KEY_PREFIX + goodsId;
        String cachedStock = redisTemplate.opsForValue().get(stockKey);

        if (cachedStock != null) {
            return Integer.parseInt(cachedStock);
        }

        // 缓存不存在，查询数据库
        Stock dbStock = stockMapper.selectOne(
                new LambdaQueryWrapper<Stock>()
                        .eq(Stock::getGoodsId, goodsId)
        );

        if (dbStock != null) {
            // 回填缓存
            redisTemplate.opsForValue().set(stockKey, String.valueOf(dbStock.getAvailableStock()));
            return dbStock.getAvailableStock();
        }

        return 0;
    }

    /**
     * 初始化商品库存
     *
     * @param goodsId 商品ID
     * @param totalStock 总库存
     * @return 是否初始化成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean initStock(Long goodsId, Integer totalStock) {
        Stock stock = new Stock();
        stock.setGoodsId(goodsId);
        stock.setTotalStock(totalStock);
        stock.setAvailableStock(totalStock);
        stock.setLockedStock(0);
        stock.setVersion(0);

        int result = stockMapper.insert(stock);

        if (result > 0) {
            // 同步 Redis 缓存
            redisTemplate.opsForValue().set(STOCK_KEY_PREFIX + goodsId, String.valueOf(totalStock));
            log.info("库存初始化成功: goodsId={}, totalStock={}", goodsId, totalStock);
            return true;
        }

        return false;
    }
}
