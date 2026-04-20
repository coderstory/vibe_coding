package cn.coderstory.springboot.stock.service;

import cn.coderstory.springboot.SpringbootApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * StockService 集成测试
 *
 * 使用 @SpringBootTest 进行集成测试
 *
 * @author system
 * @version 1.0
 * @since 2026-04-20
 */
@SpringBootTest(classes = SpringbootApplication.class)
@DisplayName("StockService 集成测试")
class StockServiceTest {

    @Autowired
    private StockService stockService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private Long testGoodsId = 200L;

    @BeforeEach
    void setUp() {
        // 清理测试数据
        redisTemplate.delete("seckill:stock:" + testGoodsId);
    }

    /**
     * 测试用例：初始化库存
     */
    @Test
    @DisplayName("应能正确初始化库存")
    void shouldInitializeStock() {
        boolean result = stockService.initStock(testGoodsId, 100);

        assertTrue(result);
        assertEquals(100, stockService.getAvailableStock(testGoodsId));
    }

    /**
     * 测试用例：获取库存
     */
    @Test
    @DisplayName("应能正确获取库存")
    void shouldGetCorrectStock() {
        stockService.initStock(testGoodsId, 100);

        int availableStock = stockService.getAvailableStock(testGoodsId);

        assertEquals(100, availableStock);
    }

    /**
     * 测试用例：扣减库存
     */
    @Test
    @DisplayName("应能正确扣减库存")
    void shouldDeductStock() {
        stockService.initStock(testGoodsId, 100);

        boolean result = stockService.deductStock(testGoodsId, 10);

        assertTrue(result);
        assertEquals(90, stockService.getAvailableStock(testGoodsId));
    }

    /**
     * 测试用例：库存不足扣减失败
     */
    @Test
    @DisplayName("库存不足时应扣减失败")
    void shouldFailWhenInsufficientStock() {
        stockService.initStock(testGoodsId, 5);

        boolean result = stockService.deductStock(testGoodsId, 10);

        assertFalse(result);
        assertEquals(5, stockService.getAvailableStock(testGoodsId));
    }

    /**
     * 测试用例：回滚库存
     */
    @Test
    @DisplayName("应能正确回滚库存")
    void shouldRollbackStock() {
        stockService.initStock(testGoodsId, 100);

        boolean deductResult = stockService.deductStock(testGoodsId, 20);
        assertTrue(deductResult);

        boolean rollbackResult = stockService.rollbackStock(testGoodsId, 20);
        assertTrue(rollbackResult);

        assertEquals(100, stockService.getAvailableStock(testGoodsId));
    }

    /**
     * 测试用例：库存归零
     */
    @Test
    @DisplayName("库存为零时应扣减失败")
    void shouldFailWhenStockIsZero() {
        stockService.initStock(testGoodsId, 0);

        boolean result = stockService.deductStock(testGoodsId, 1);

        assertFalse(result);
    }
}
