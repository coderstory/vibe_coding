package cn.coderstory.springboot.monitor.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MonitorService {
    private final StringRedisTemplate redisTemplate;

    public Map<String, Object> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        String concurrentCount = redisTemplate.opsForValue().get("seckill:processing:count");
        metrics.put("concurrentCount", concurrentCount != null ? Integer.parseInt(concurrentCount) : 0);

        Long qpsCount = redisTemplate.keys("seckill:qps:*").stream().count();
        metrics.put("qpsKeys", qpsCount);

        metrics.put("timestamp", System.currentTimeMillis());

        return metrics;
    }

    public Map<String, Object> getGoodsStockInfo(Long goodsId) {
        Map<String, Object> info = new HashMap<>();
        String stockKey = "seckill:stock:" + goodsId;
        String stock = redisTemplate.opsForValue().get(stockKey);
        info.put("goodsId", goodsId);
        info.put("availableStock", stock != null ? Integer.parseInt(stock) : 0);
        return info;
    }
}