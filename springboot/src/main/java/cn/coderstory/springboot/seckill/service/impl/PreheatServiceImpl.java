package cn.coderstory.springboot.seckill.service.impl;

import cn.coderstory.springboot.seckill.entity.SeckillActivity;
import cn.coderstory.springboot.seckill.entity.SeckillGoods;
import cn.coderstory.springboot.seckill.mapper.SeckillActivityMapper;
import cn.coderstory.springboot.seckill.mapper.SeckillGoodsMapper;
import cn.coderstory.springboot.seckill.mapper.SeckillReservationMapper;
import cn.coderstory.springboot.seckill.service.PreheatService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class PreheatServiceImpl implements PreheatService {

    private static final String STOCK_KEY_PREFIX = "seckill:stock:";
    private static final String ACTIVITY_KEY_PREFIX = "seckill:activity:";
    private static final String RESERVATION_KEY_PREFIX = "seckill:reservation:";

    /** 活动缓存过期时间（活动结束后1小时自动清理） */
    private static final long ACTIVITY_CACHE_EXPIRE_HOURS = 1;

    private final SeckillActivityMapper activityMapper;
    private final SeckillGoodsMapper goodsMapper;
    private final SeckillReservationMapper reservationMapper;
    private final StringRedisTemplate redisTemplate;

    @Override
    public void preheatActivity(Long activityId) {
        SeckillActivity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            log.warn("活动不存在: {}", activityId);
            return;
        }

        LambdaQueryWrapper<SeckillGoods> goodsQuery = new LambdaQueryWrapper<>();
        goodsQuery.eq(SeckillGoods::getActivityId, activityId);
        List<SeckillGoods> goodsList = goodsMapper.selectList(goodsQuery);

        // 预热商品库存
        for (SeckillGoods goods : goodsList) {
            String stockKey = STOCK_KEY_PREFIX + goods.getId();
            redisTemplate.opsForValue().set(stockKey, String.valueOf(goods.getStock()));
            log.info("预热商品库存: goodsId={}, stock={}", goods.getId(), goods.getStock());
        }

        // 预热活动完整信息到 Redis Hash
        String activityKey = ACTIVITY_KEY_PREFIX + activityId;
        Map<String, String> activityData = new HashMap<>();
        activityData.put("id", String.valueOf(activity.getId()));
        activityData.put("name", activity.getName());
        activityData.put("description", activity.getDescription() != null ? activity.getDescription() : "");
        activityData.put("startTime", activity.getStartTime() != null ? activity.getStartTime().toString() : "");
        activityData.put("endTime", activity.getEndTime() != null ? activity.getEndTime().toString() : "");
        activityData.put("status", String.valueOf(activity.getStatus()));
        activityData.put("perLimit", String.valueOf(activity.getPerLimit()));
        activityData.put("enableCaptcha", String.valueOf(activity.getEnableCaptcha()));
        activityData.put("enableIpLimit", String.valueOf(activity.getEnableIpLimit()));
        activityData.put("signKey", activity.getSignKey() != null ? activity.getSignKey() : "");

        redisTemplate.opsForHash().putAll(activityKey, activityData);

        // 计算过期时间：活动结束时间 + 1小时
        long expireSeconds = calculateExpireSeconds(activity.getEndTime(), ACTIVITY_CACHE_EXPIRE_HOURS);
        redisTemplate.expire(activityKey, expireSeconds, TimeUnit.SECONDS);

        log.info("活动预热完成: activityId={}, goodsCount={}, expireSeconds={}",
                activityId, goodsList.size(), expireSeconds);
    }

    /**
     * 计算缓存过期时间
     *
     * @param endTime 活动结束时间
     * @param extraHours 额外增加的小时数
     * @return 过期秒数
     */
    private long calculateExpireSeconds(java.time.LocalDateTime endTime, long extraHours) {
        if (endTime == null) {
            // 默认1小时过期
            return extraHours * 3600;
        }
        long seconds = java.time.Duration.between(
                java.time.LocalDateTime.now(),
                endTime.plusHours(extraHours)
        ).getSeconds();
        return Math.max(seconds, extraHours * 3600); // 至少保留额外小时数
    }

    @Override
    public Map<String, Object> getPreheatStatus(Long activityId) {
        Map<String, Object> status = new HashMap<>();
        status.put("activityId", activityId);

        String activityKey = ACTIVITY_KEY_PREFIX + activityId;
        // 使用 redisTemplate.hasKey() 检查 Hash key 是否存在
        Boolean hasKey = redisTemplate.hasKey(activityKey);
        status.put("activityPreheat", Boolean.TRUE.equals(hasKey));

        LambdaQueryWrapper<SeckillGoods> goodsQuery = new LambdaQueryWrapper<>();
        goodsQuery.eq(SeckillGoods::getActivityId, activityId);
        List<SeckillGoods> goodsList = goodsMapper.selectList(goodsQuery);

        int preheatedGoods = 0;
        for (SeckillGoods goods : goodsList) {
            String stockKey = STOCK_KEY_PREFIX + goods.getId();
            String stock = redisTemplate.opsForValue().get(stockKey);
            if (stock != null) {
                preheatedGoods++;
            }
        }
        status.put("totalGoods", goodsList.size());
        status.put("preheatedGoods", preheatedGoods);

        SeckillActivity activity = activityMapper.selectById(activityId);
        if (activity != null) {
            status.put("activityName", activity.getName());
            status.put("activityStatus", activity.getStatus());
        }

        return status;
    }

    @Override
    public int getActivityStock(Long activityId) {
        LambdaQueryWrapper<SeckillGoods> goodsQuery = new LambdaQueryWrapper<>();
        goodsQuery.eq(SeckillGoods::getActivityId, activityId);
        List<SeckillGoods> goodsList = goodsMapper.selectList(goodsQuery);

        int totalStock = 0;
        for (SeckillGoods goods : goodsList) {
            String stockKey = STOCK_KEY_PREFIX + goods.getId();
            String stock = redisTemplate.opsForValue().get(stockKey);
            if (stock != null) {
                totalStock += Integer.parseInt(stock);
            }
        }
        return totalStock;
    }
}
