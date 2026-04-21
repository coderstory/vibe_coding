package cn.coderstory.springboot.stock.consumer;

import cn.coderstory.springboot.sse.SeckillSseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
// @RocketMQMessageListener(topic = "seckill_stock_deduct", consumerGroup = "seckill_stock_consumer")
public class StockConsumer implements RocketMQListener<String> {
    private final SeckillSseService sseService;
    private final StringRedisTemplate redisTemplate;

    /**
     * Redis 扣减库存 Lua 脚本
     * KEYS[1]: 库存 key
     * ARGV[1]: 扣减数量
     * 返回: 扣减后的库存数量，-1 表示库存不存在，-2 表示库存不足
     */
    private static final String DEDUCT_STOCK_LUA = """
        local stock = redis.call('GET', KEYS[1])
        if not stock then return -1 end
        if tonumber(stock) < tonumber(ARGV[1]) then return -2 end
        return redis.call('DECRBY', KEYS[1], ARGV[1])
        """;

    @Override
    public void onMessage(String message) {
        log.info("收到库存扣减消息: {}", message);
        String[] parts = message.split(":");
        if (parts.length != 3) {
            log.error("消息格式错误: {}", message);
            return;
        }

        Long goodsId = Long.parseLong(parts[0]);
        Integer quantity = Integer.parseInt(parts[1]);
        String queueId = parts[2];

        // 直接从 Redis 扣减库存（秒杀专用，不操作数据库）
        String stockKey = "seckill:stock:" + goodsId;
        Long remaining = redisTemplate.execute(
                RedisScript.of(DEDUCT_STOCK_LUA, Long.class),
                Collections.singletonList(stockKey),
                quantity.toString()
        );

        if (remaining == null || remaining < 0) {
            log.error("库存扣减失败: goodsId={}, quantity={}, remaining={}", goodsId, quantity, remaining);
            // 通知前端库存扣减失败
            sseService.sendFailed(queueId, "库存不足，抢购失败");
        } else {
            log.info("库存扣减成功: goodsId={}, quantity={}, remaining={}", goodsId, quantity, remaining);
            // 通知前端抢购成功
            sseService.sendSuccess(queueId, null, "抢购成功");
        }
    }
}