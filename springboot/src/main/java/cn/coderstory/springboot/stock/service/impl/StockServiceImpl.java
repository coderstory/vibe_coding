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

@Slf4j
@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {

    private static final String STOCK_CACHE_KEY_PREFIX = "seckill:stock:cache:";

    private final StockMapper stockMapper;
    private final StringRedisTemplate redisTemplate;
    private final DistributedLockService distributedLockService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deductStock(Long goodsId, Integer quantity) {
        log.info("开始扣减库存: goodsId={}, quantity={}", goodsId, quantity);

        String lockKey = DistributedLockServiceImpl.getStockLockKey(goodsId);

        Boolean result = distributedLockService.executeWithLock(
                lockKey,
                () -> doDeductStock(goodsId, quantity)
        );

        if (Boolean.TRUE.equals(result)) {
            redisTemplate.opsForValue().increment(STOCK_CACHE_KEY_PREFIX + goodsId, -quantity);
            log.info("库存扣减成功: goodsId={}, quantity={}", goodsId, quantity);
            return true;
        }

        log.warn("库存扣减失败: goodsId={}, quantity={}", goodsId, quantity);
        return false;
    }

    private boolean doDeductStock(Long goodsId, Integer quantity) {
        Stock stock = stockMapper.selectOne(
                new LambdaQueryWrapper<Stock>()
                        .eq(Stock::getGoodsId, goodsId)
        );

        if (stock == null) {
            log.warn("商品库存记录不存在: goodsId={}", goodsId);
            return false;
        }

        if (stock.getAvailableStock() < quantity) {
            log.warn("库存不足: goodsId={}, available={}, required={}",
                    goodsId, stock.getAvailableStock(), quantity);
            return false;
        }

        LambdaUpdateWrapper<Stock> updateWrapper = new LambdaUpdateWrapper<Stock>()
                .setSql("available_stock = available_stock - " + quantity)
                .setSql("locked_stock = locked_stock + " + quantity)
                .setSql("version = version + 1")
                .eq(Stock::getGoodsId, goodsId)
                .eq(Stock::getVersion, stock.getVersion())
                .ge(Stock::getAvailableStock, quantity);

        int result = stockMapper.update(null, updateWrapper);

        if (result == 0) {
            log.warn("乐观锁更新失败，可能存在并发冲突: goodsId={}", goodsId);
            return false;
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean rollbackStock(Long goodsId, Integer quantity) {
        log.info("开始回滚库存: goodsId={}, quantity={}", goodsId, quantity);

        String lockKey = DistributedLockServiceImpl.getStockLockKey(goodsId);

        Boolean result = distributedLockService.executeWithLock(
                lockKey,
                () -> doRollbackStock(goodsId, quantity)
        );

        if (Boolean.TRUE.equals(result)) {
            redisTemplate.opsForValue().increment(STOCK_CACHE_KEY_PREFIX + goodsId, quantity);
            log.info("库存回滚成功: goodsId={}, quantity={}", goodsId, quantity);
            return true;
        }

        log.warn("库存回滚失败: goodsId={}, quantity={}", goodsId, quantity);
        return false;
    }

    private boolean doRollbackStock(Long goodsId, Integer quantity) {
        Stock stock = stockMapper.selectOne(
                new LambdaQueryWrapper<Stock>()
                        .eq(Stock::getGoodsId, goodsId)
        );

        if (stock == null) {
            log.warn("商品库存记录不存在，无法回滚: goodsId={}", goodsId);
            return false;
        }

        LambdaUpdateWrapper<Stock> updateWrapper = new LambdaUpdateWrapper<Stock>()
                .setSql("available_stock = available_stock + " + quantity)
                .setSql("locked_stock = GREATEST(0, locked_stock - " + quantity + ")")
                .setSql("version = version + 1")
                .eq(Stock::getGoodsId, goodsId)
                .eq(Stock::getVersion, stock.getVersion());

        int result = stockMapper.update(null, updateWrapper);

        return result > 0;
    }

    @Override
    public int getAvailableStock(Long goodsId) {
        String stockKey = STOCK_CACHE_KEY_PREFIX + goodsId;
        String cachedStock = redisTemplate.opsForValue().get(stockKey);

        if (cachedStock != null) {
            return Integer.parseInt(cachedStock);
        }

        Stock dbStock = stockMapper.selectOne(
                new LambdaQueryWrapper<Stock>()
                        .eq(Stock::getGoodsId, goodsId)
        );

        if (dbStock != null) {
            redisTemplate.opsForValue().set(stockKey, String.valueOf(dbStock.getAvailableStock()));
            return dbStock.getAvailableStock();
        }

        return 0;
    }

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
            redisTemplate.opsForValue().set(STOCK_CACHE_KEY_PREFIX + goodsId, String.valueOf(totalStock));
            log.info("库存初始化成功: goodsId={}, totalStock={}", goodsId, totalStock);
            return true;
        }

        return false;
    }
}
