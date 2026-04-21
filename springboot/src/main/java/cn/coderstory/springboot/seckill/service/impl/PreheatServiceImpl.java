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

/**
 * 秒杀活动预热服务
 *
 * 核心功能：
 * 在活动发布前，将活动信息和商品库存预先加载到 Redis
 * 这样秒杀开始后，系统可以直接从 Redis 读取，不用查数据库
 *
 * 设计理念："秒杀过程不操作数据库"
 * - 预热阶段：数据库 -> Redis
 * - 秒杀阶段：全部读写 Redis，不碰数据库
 * - 数据库只在活动发布和结算时使用
 *
 * Redis 数据结构设计：
 * | Key 前缀              | 类型   | 说明                          |
 * | seckill:activity:{id} | Hash   | 活动详情（预热时写入）         |
 * | seckill:stock:{goodsId} | String | 商品库存（预热时写入）         |
 * | seckill:reservation:{activityId} | Set | 预约用户ID集合               |
 *
 * 预热时机：
 * - 管理员在后台发布活动时调用 /api/seckill/activity/{id}/publish
 * - publishActivity() 会自动调用 preheatActivity() 进行预热
 *
 * @author seckill-team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PreheatServiceImpl implements PreheatService {

    /** Redis 中商品库存的 Key 前缀，格式：seckill:stock:{goodsId} */
    private static final String STOCK_KEY_PREFIX = "seckill:stock:";

    /** Redis 中活动信息的 Key 前缀，格式：seckill:activity:{activityId} */
    private static final String ACTIVITY_KEY_PREFIX = "seckill:activity:";

    /** Redis 中预约信息的 Key 前缀，格式：seckill:reservation:{activityId} */
    private static final String RESERVATION_KEY_PREFIX = "seckill:reservation:";

    /** 活动缓存过期时间基数：活动结束后额外保留1小时 */
    private static final long ACTIVITY_CACHE_EXPIRE_HOURS = 1;

    private final SeckillActivityMapper activityMapper;
    private final SeckillGoodsMapper goodsMapper;
    private final SeckillReservationMapper reservationMapper;
    private final StringRedisTemplate redisTemplate;

    /**
     * 预热活动数据和商品库存到 Redis
     *
     * 预热流程：
     * 1. 从数据库读取活动信息
     * 2. 查询该活动关联的所有商品
     * 3. 将每个商品的库存写入 Redis（String 类型）
     * 4. 将活动详情写入 Redis（Hash 类型）
     * 5. 设置过期时间
     *
     * 为什么分开存储？
     * - 库存需要频繁读写（每次抢购都要扣减），用 String 类型更方便用 DECR 命令
     * - 活动详情只需要读取，用 Hash 可以按字段获取，且方便管理
     *
     * @param activityId 活动ID
     */
    @Override
    public void preheatActivity(Long activityId) {
        // 1. 从数据库读取活动信息
        SeckillActivity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            log.warn("预热失败：活动 {} 不存在", activityId);
            return;
        }

        // 2. 查询该活动关联的所有商品
        LambdaQueryWrapper<SeckillGoods> goodsQuery = new LambdaQueryWrapper<>();
        goodsQuery.eq(SeckillGoods::getActivityId, activityId);
        List<SeckillGoods> goodsList = goodsMapper.selectList(goodsQuery);

        // 3. 预热商品库存到 Redis（String 类型，用 INCR/DECR 原子操作扣减）
        for (SeckillGoods goods : goodsList) {
            String stockKey = STOCK_KEY_PREFIX + goods.getId();
            // 使用 SET 而不是 INCR，因为预热时是设置初始库存，不是累加
            redisTemplate.opsForValue().set(stockKey, String.valueOf(goods.getStock()));
            log.info("预热商品库存: goodsId={}, stock={}", goods.getId(), goods.getStock());
        }

        // 4. 预热活动详情到 Redis（Hash 类型）
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

        // 一次性写入整个 Hash
        redisTemplate.opsForHash().putAll(activityKey, activityData);

        // 5. 设置过期时间：活动结束时间 + 1小时
        // 这样活动结束后，缓存还会保留1小时，用于查询已结束的活动
        long expireSeconds = calculateExpireSeconds(activity.getEndTime(), ACTIVITY_CACHE_EXPIRE_HOURS);
        redisTemplate.expire(activityKey, expireSeconds, TimeUnit.SECONDS);

        log.info("活动 {} 预热完成: {} 个商品, 缓存有效期 {} 秒", activityId, goodsList.size(), expireSeconds);
    }

    /**
     * 计算缓存过期时间
     *
     * 过期时间 = 活动结束时间 - 当前时间 + 额外缓冲时间
     * 最短保留下限：extraHours * 3600 秒
     *
     * @param endTime 活动结束时间
     * @param extraHours 活动结束后额外保留的小时数
     * @return 过期秒数（保证至少等于 extraHours * 3600）
     */
    private long calculateExpireSeconds(java.time.LocalDateTime endTime, long extraHours) {
        if (endTime == null) {
            // 没有结束时间时，使用固定的额外时间
            return extraHours * 3600;
        }
        // 计算到活动结束后 extraHours 小时的秒数
        long seconds = java.time.Duration.between(
                java.time.LocalDateTime.now(),
                endTime.plusHours(extraHours)
        ).getSeconds();
        // 确保至少有 extraHours 小时，防止负数或过短的有效期
        return Math.max(seconds, extraHours * 3600);
    }

    /**
     * 获取预热状态
     *
     * 用于管理后台查看活动是否已预热、预热了多少商品等
     *
     * @param activityId 活动ID
     * @return 预热状态信息
     */
    @Override
    public Map<String, Object> getPreheatStatus(Long activityId) {
        Map<String, Object> status = new HashMap<>();
        status.put("activityId", activityId);

        // 检查活动缓存是否存在
        String activityKey = ACTIVITY_KEY_PREFIX + activityId;
        Boolean hasKey = redisTemplate.hasKey(activityKey);
        status.put("activityPreheat", Boolean.TRUE.equals(hasKey));

        // 检查每个商品的库存是否已预热
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

        // 补充活动基本信息（从数据库读取）
        SeckillActivity activity = activityMapper.selectById(activityId);
        if (activity != null) {
            status.put("activityName", activity.getName());
            status.put("activityStatus", activity.getStatus());
        }

        return status;
    }

    /**
     * 获取活动的总库存
     *
     * 从 Redis 读取所有关联商品的库存并求和
     * 这是秒杀详情页显示库存的接口
     *
     * 注意：这里只计算已预热的商品库存
     * 如果 Redis 中没有某个商品的库存数据，说明还没预热，不计入
     *
     * @param activityId 活动ID
     * @return 所有商品的库存总和
     */
    @Override
    public int getActivityStock(Long activityId) {
        // 查询该活动关联的所有商品
        LambdaQueryWrapper<SeckillGoods> goodsQuery = new LambdaQueryWrapper<>();
        goodsQuery.eq(SeckillGoods::getActivityId, activityId);
        List<SeckillGoods> goodsList = goodsMapper.selectList(goodsQuery);

        int totalStock = 0;
        for (SeckillGoods goods : goodsList) {
            // 从 Redis 读取库存
            String stockKey = STOCK_KEY_PREFIX + goods.getId();
            String stock = redisTemplate.opsForValue().get(stockKey);
            if (stock != null) {
                try {
                    totalStock += Integer.parseInt(stock);
                } catch (NumberFormatException e) {
                    log.warn("商品 {} 库存数据异常: {}", goods.getId(), stock);
                }
            }
        }
        return totalStock;
    }
}
