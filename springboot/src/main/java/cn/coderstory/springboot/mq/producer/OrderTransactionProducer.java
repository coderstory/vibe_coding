package cn.coderstory.springboot.mq.producer;

import cn.coderstory.springboot.order.entity.Order;
import cn.coderstory.springboot.order.mapper.OrderMapper;
import cn.coderstory.springboot.seckill.entity.SeckillGoods;
import cn.coderstory.springboot.seckill.mapper.SeckillGoodsMapper;
import cn.coderstory.springboot.sse.SeckillSseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import java.util.UUID;

@Slf4j
@Component
public class OrderTransactionProducer {

    private final RocketMQTemplate rocketMQTemplate;
    private final OrderMapper orderMapper;
    private final SeckillGoodsMapper goodsMapper;
    private final SeckillSseService sseService;
    private TransactionMQProducer transactionProducer;

    @Value("${rocketmq.name-server}")
    private String nameServer;

    public OrderTransactionProducer(RocketMQTemplate rocketMQTemplate, OrderMapper orderMapper, SeckillGoodsMapper goodsMapper, SeckillSseService sseService) {
        this.rocketMQTemplate = rocketMQTemplate;
        this.orderMapper = orderMapper;
        this.goodsMapper = goodsMapper;
        this.sseService = sseService;
    }

    @PostConstruct
    public void init() {
        transactionProducer = new TransactionMQProducer("seckill_order_producer");
        transactionProducer.setNamesrvAddr(nameServer);
        transactionProducer.setTransactionListener(new TransactionListener() {
            @Override
            public org.apache.rocketmq.client.producer.LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
                byte[] payload = msg.getBody();
                String message = new String(payload);
                log.info("执行本地事务: {}", message);

                try {
                    String[] parts = message.split(":");
                    if (parts.length != 4) {
                        return org.apache.rocketmq.client.producer.LocalTransactionState.ROLLBACK_MESSAGE;
                    }

                    Long userId = Long.parseLong(parts[0]);
                    Long goodsId = Long.parseLong(parts[1]);
                    Long activityId = Long.parseLong(parts[2]);
                    String queueId = parts[3];

                    Order order = new Order();
                    order.setOrderNo(UUID.randomUUID().toString().replace("-", ""));
                    order.setUserId(userId);
                    order.setGoodsId(goodsId);
                    order.setActivityId(activityId);
                    order.setQueueId(queueId);
                    order.setQuantity(1);

                    // 查询商品价格
                    SeckillGoods goods = goodsMapper.selectById(goodsId);
                    if (goods != null) {
                        order.setPrice(goods.getSeckillPrice());
                    }

                    order.setStatus(0);
                    order.setCreateTime(java.time.LocalDateTime.now());
                    orderMapper.insert(order);

                    // 订单创建成功，通知前端抢购成功
                    sseService.sendSuccess(queueId, order.getId(), "抢购成功");

                    return org.apache.rocketmq.client.producer.LocalTransactionState.COMMIT_MESSAGE;
                } catch (Exception e) {
                    log.error("本地事务执行失败", e);
                    return org.apache.rocketmq.client.producer.LocalTransactionState.ROLLBACK_MESSAGE;
                }
            }

            @Override
            public org.apache.rocketmq.client.producer.LocalTransactionState checkLocalTransaction(MessageExt msg) {
                byte[] payload = msg.getBody();
                String message = new String(payload);
                log.info("检查本地事务状态: {}", message);
                return org.apache.rocketmq.client.producer.LocalTransactionState.COMMIT_MESSAGE;
            }
        });

        // 启动生产者
        try {
            transactionProducer.start();
            log.info("TransactionMQProducer 启动成功");
        } catch (Exception e) {
            log.error("TransactionMQProducer 启动失败", e);
        }
    }

    private static final String ORDER_CREATE_TOPIC = "seckill_order_create";
    private static final String STOCK_DEDUCT_TOPIC = "seckill_stock_deduct";

    public void sendOrderCreateMsg(Long userId, Long goodsId, Long activityId, String queueId) throws Exception {
        String message = userId + ":" + goodsId + ":" + activityId + ":" + queueId;
        log.info("发送订单创建消息: {}", message);

        Message msg = new Message(ORDER_CREATE_TOPIC, message.getBytes());
        transactionProducer.sendMessageInTransaction(msg, null);
    }

    public void sendStockDeductMsg(Long goodsId, Integer quantity, String queueId) {
        // 不再发送库存扣减消息，因为秒杀时已经扣减了 Redis 库存
        log.info("跳过库存扣减消息: goodsId={}, quantity={}, queueId={} (Redis 库存已在秒杀时扣减)", goodsId, quantity, queueId);
    }
}