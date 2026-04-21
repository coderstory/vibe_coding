package cn.coderstory.springboot.stock.service;

import cn.coderstory.springboot.SpringbootApplication;
import cn.coderstory.springboot.stock.entity.Stock;
import cn.coderstory.springboot.stock.mapper.StockMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = SpringbootApplication.class)
@DisplayName("StockService 集成测试")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class StockServiceTest {

    @Autowired
    private StockService stockService;

    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private final Set<Long> testGoodsIds = new HashSet<>();
    private final Set<String> testRedisKeys = new HashSet<>();

    @AfterEach
    void tearDown() {
        testGoodsIds.forEach(goodsId -> {
            try {
                stockMapper.delete(new LambdaQueryWrapper<Stock>().eq(Stock::getGoodsId, goodsId));
            } catch (Exception ignored) {
            }
        });
        testGoodsIds.clear();

        if (!testRedisKeys.isEmpty()) {
            redisTemplate.delete(testRedisKeys);
            testRedisKeys.clear();
        }
    }

    private Long generateTestGoodsId() {
        long id = System.currentTimeMillis() % 100000;
        testGoodsIds.add(id);
        return id;
    }

    private String addRedisKey(String key) {
        testRedisKeys.add(key);
        return key;
    }

    @Test
    @Order(1)
    @DisplayName("应能正确初始化库存")
    void shouldInitializeStock() {
        Long goodsId = generateTestGoodsId();
        addRedisKey("seckill:stock:cache:" + goodsId);

        boolean result = stockService.initStock(goodsId, 100);

        assertTrue(result);
        assertEquals(100, stockService.getAvailableStock(goodsId));
    }

    @Test
    @Order(2)
    @DisplayName("应能正确获取库存")
    void shouldGetCorrectStock() {
        Long goodsId = generateTestGoodsId();
        addRedisKey("seckill:stock:cache:" + goodsId);

        stockService.initStock(goodsId, 100);

        int availableStock = stockService.getAvailableStock(goodsId);

        assertEquals(100, availableStock);
    }

    @Test
    @Order(3)
    @DisplayName("应能正确扣减库存")
    void shouldDeductStock() {
        Long goodsId = generateTestGoodsId();
        addRedisKey("seckill:stock:cache:" + goodsId);

        stockService.initStock(goodsId, 100);

        boolean result = stockService.deductStock(goodsId, 10);

        assertTrue(result);
        assertEquals(90, stockService.getAvailableStock(goodsId));
    }

    @Test
    @Order(4)
    @DisplayName("库存不足时应扣减失败")
    void shouldFailWhenInsufficientStock() {
        Long goodsId = generateTestGoodsId();
        addRedisKey("seckill:stock:cache:" + goodsId);

        stockService.initStock(goodsId, 5);

        boolean result = stockService.deductStock(goodsId, 10);

        assertFalse(result);
        assertEquals(5, stockService.getAvailableStock(goodsId));
    }

    @Test
    @Order(5)
    @DisplayName("应能正确回滚库存")
    void shouldRollbackStock() {
        Long goodsId = generateTestGoodsId();
        addRedisKey("seckill:stock:cache:" + goodsId);

        stockService.initStock(goodsId, 100);

        boolean deductResult = stockService.deductStock(goodsId, 20);
        assertTrue(deductResult);

        boolean rollbackResult = stockService.rollbackStock(goodsId, 20);
        assertTrue(rollbackResult);

        assertEquals(100, stockService.getAvailableStock(goodsId));
    }

    @Test
    @Order(6)
    @DisplayName("库存为零时应扣减失败")
    void shouldFailWhenStockIsZero() {
        Long goodsId = generateTestGoodsId();
        addRedisKey("seckill:stock:cache:" + goodsId);

        stockService.initStock(goodsId, 0);

        boolean result = stockService.deductStock(goodsId, 1);

        assertFalse(result);
    }
}
