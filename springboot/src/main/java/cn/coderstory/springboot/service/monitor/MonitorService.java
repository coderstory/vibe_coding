package cn.coderstory.springboot.service.monitor;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
/**
 * 系统监控服务。
 * <p>
 * 提供系统运行状态和性能指标查询功能。
 * 包括秒杀系统并发量、QPS 和商品库存等实时数据。
 *
 * @since 1.7.0
 */
public class MonitorService {
    private final StringRedisTemplate redisTemplate;

    /**
     * 获取系统性能指标。
     * <p>
     * 包括秒杀并发处理数、QPS 键数量和当前时间戳。
     *
     * @return 性能指标 Map
     */
    public Map<String, Object> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        String concurrentCount = redisTemplate.opsForValue().get("seckill:processing:count");
        metrics.put("concurrentCount", concurrentCount != null ? Integer.parseInt(concurrentCount) : 0);

        Long qpsCount = redisTemplate.keys("seckill:qps:*").stream().count();
        metrics.put("qpsKeys", qpsCount);

        metrics.put("timestamp", System.currentTimeMillis());

        return metrics;
    }

    /**
     * 获取商品库存信息。
     *
     * @param goodsId 商品 ID
     * @return 库存信息，包含商品 ID 和可用库存
     */
    public Map<String, Object> getGoodsStockInfo(Long goodsId) {
        Map<String, Object> info = new HashMap<>();
        String stockKey = "seckill:stock:" + goodsId;
        String stock = redisTemplate.opsForValue().get(stockKey);
        info.put("goodsId", goodsId);
        info.put("availableStock", stock != null ? Integer.parseInt(stock) : 0);
        return info;
    }
}
