package cn.coderstory.springboot.seckill.service.impl;

import cn.coderstory.springboot.seckill.entity.SeckillActivity;
import cn.coderstory.springboot.seckill.entity.SeckillGoods;
import cn.coderstory.springboot.seckill.entity.SeckillReservation;
import cn.coderstory.springboot.seckill.mapper.SeckillActivityMapper;
import cn.coderstory.springboot.seckill.mapper.SeckillGoodsMapper;
import cn.coderstory.springboot.seckill.mapper.SeckillReservationMapper;
import cn.coderstory.springboot.seckill.service.PreheatService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PreheatServiceImpl implements PreheatService {
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

        for (SeckillGoods goods : goodsList) {
            String stockKey = "seckill:stock:" + goods.getId();
            redisTemplate.opsForValue().set(stockKey, String.valueOf(goods.getStock()));
            log.info("预热商品库存: goodsId={}, stock={}", goods.getId(), goods.getStock());
        }

        String activityKey = "seckill:activity:" + activityId;
        redisTemplate.opsForValue().set(activityKey, String.valueOf(activity.getStatus()));

        log.info("活动预热完成: activityId={}, goodsCount={}", activityId, goodsList.size());
    }
}