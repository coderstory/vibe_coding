package cn.coderstory.springboot.stock.consumer;

import cn.coderstory.springboot.stock.service.StockService;
import cn.coderstory.springboot.sse.SeckillSseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(topic = "seckill_stock_deduct", consumerGroup = "seckill_stock_consumer")
public class StockConsumer implements RocketMQListener<String> {
    private final StockService stockService;
    private final SeckillSseService sseService;

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

        boolean success = stockService.deductStock(goodsId, quantity);
        if (success) {
            log.info("库存扣减成功: goodsId={}, quantity={}", goodsId, quantity);
        } else {
            log.error("库存扣减失败: goodsId={}, quantity={}", goodsId, quantity);
            // 通知前端库存扣减失败
            sseService.sendFailed(queueId, "库存不足，抢购失败");
        }
    }
}