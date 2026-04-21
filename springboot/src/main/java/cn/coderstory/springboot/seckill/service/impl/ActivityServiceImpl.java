package cn.coderstory.springboot.seckill.service.impl;

import cn.coderstory.springboot.exception.BusinessException;
import cn.coderstory.springboot.lock.DistributedLockService;
import cn.coderstory.springboot.lock.impl.DistributedLockServiceImpl;
import cn.coderstory.springboot.seckill.entity.SeckillActivity;
import cn.coderstory.springboot.seckill.mapper.SeckillActivityMapper;
import cn.coderstory.springboot.seckill.service.ActivityService;
import cn.coderstory.springboot.seckill.service.GoodsService;
import cn.coderstory.springboot.seckill.service.PreheatService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {

    private static final String ACTIVITY_KEY_PREFIX = "seckill:activity:";

    private final SeckillActivityMapper activityMapper;
    private final PreheatService preheatService;
    private final GoodsService goodsService;
    private final DistributedLockService distributedLockService;
    private final StringRedisTemplate redisTemplate;

    @Override
    public SeckillActivity getActivity(Long activityId) {
        // 1. 尝试从 Redis Hash 读取
        SeckillActivity cachedActivity = getActivityFromCache(activityId);
        if (cachedActivity != null) {
            return cachedActivity;
        }

        // 2. 缓存不存在，使用分布式锁防止缓存击穿
        // 只有获取到锁的线程才能查库并回填缓存
        String lockKey = DistributedLockServiceImpl.getActivityLockKey(activityId);
        SeckillActivity result = distributedLockService.executeWithLock(
                lockKey,
                5, TimeUnit.SECONDS,
                () -> {
                    // 双重检查：获取锁后再次检查缓存（可能其他线程已经回填）
                    SeckillActivity cached = getActivityFromCache(activityId);
                    if (cached != null) {
                        return cached;
                    }
                    // 查数据库
                    SeckillActivity activity = activityMapper.selectById(activityId);
                    // 检查数据库返回的数据是否有效
                    if (activity == null || activity.getId() == null) {
                        return null;
                    }
                    // 回填缓存
                    saveActivityToCache(activity);
                    return activity;
                }
        );

        if (result == null) {
            throw BusinessException.notFound("活动不存在");
        }
        return result;
    }

    /**
     * 从 Redis Hash 读取活动信息
     */
    private SeckillActivity getActivityFromCache(Long activityId) {
        String activityKey = ACTIVITY_KEY_PREFIX + activityId;

        try {
            Map<Object, Object> cacheData = redisTemplate.opsForHash().entries(activityKey);

            // 检查缓存是否存在且有有效数据
            if (cacheData.isEmpty()) {
                return null;
            }

            // 检查 id 字段是否有有效值（避免缓存空数据）
            Object idValue = cacheData.get("id");
            if (idValue == null || idValue.toString().isEmpty()) {
                return null;
            }

            SeckillActivity activity = new SeckillActivity();
            activity.setId(parseLong(idValue));
            activity.setName((String) cacheData.get("name"));
            activity.setDescription((String) cacheData.get("description"));
            activity.setStartTime(parseLocalDateTime(cacheData.get("startTime")));
            activity.setEndTime(parseLocalDateTime(cacheData.get("endTime")));
            activity.setStatus(parseInteger(cacheData.get("status")));
            activity.setPerLimit(parseInteger(cacheData.get("perLimit")));
            activity.setEnableCaptcha(parseBoolean(cacheData.get("enableCaptcha")));
            activity.setEnableIpLimit(parseBoolean(cacheData.get("enableIpLimit")));
            activity.setSignKey((String) cacheData.get("signKey"));
            return activity;
        } catch (Exception e) {
            // 处理 key 类型错误（旧数据可能是 String 类型）
            if (e.getMessage() != null && e.getMessage().contains("WRONGTYPE")) {
                log.warn("活动缓存类型错误，删除旧数据: activityId={}", activityId);
                redisTemplate.delete(activityKey);
            } else {
                log.warn("解析活动缓存数据失败: activityId={}, error={}", activityId, e.getMessage());
            }
            return null;
        }
    }

    /**
     * 保存活动信息到 Redis Hash
     */
    private void saveActivityToCache(SeckillActivity activity) {
        String activityKey = ACTIVITY_KEY_PREFIX + activity.getId();
        Map<String, String> cacheData = new java.util.HashMap<>();
        cacheData.put("id", String.valueOf(activity.getId()));
        cacheData.put("name", activity.getName() != null ? activity.getName() : "");
        cacheData.put("description", activity.getDescription() != null ? activity.getDescription() : "");
        cacheData.put("startTime", activity.getStartTime() != null ? activity.getStartTime().toString() : "");
        cacheData.put("endTime", activity.getEndTime() != null ? activity.getEndTime().toString() : "");
        cacheData.put("status", String.valueOf(activity.getStatus()));
        cacheData.put("perLimit", String.valueOf(activity.getPerLimit()));
        cacheData.put("enableCaptcha", String.valueOf(activity.getEnableCaptcha()));
        cacheData.put("enableIpLimit", String.valueOf(activity.getEnableIpLimit()));
        cacheData.put("signKey", activity.getSignKey() != null ? activity.getSignKey() : "");

        redisTemplate.opsForHash().putAll(activityKey, cacheData);

        // 设置过期时间：活动结束时间 + 1小时
        long expireSeconds = calculateExpireSeconds(activity.getEndTime());
        redisTemplate.expire(activityKey, expireSeconds, TimeUnit.SECONDS);
        log.debug("活动信息已回填缓存: activityId={}, expireSeconds={}", activity.getId(), expireSeconds);
    }

    private long calculateExpireSeconds(LocalDateTime endTime) {
        if (endTime == null) {
            return 3600; // 默认1小时
        }
        long seconds = java.time.Duration.between(
                LocalDateTime.now(),
                endTime.plusHours(1)
        ).getSeconds();
        return Math.max(seconds, 3600);
    }

    private Long parseLong(Object value) {
        if (value == null || value.toString().isEmpty()) return null;
        return Long.parseLong(value.toString());
    }

    private Integer parseInteger(Object value) {
        if (value == null || value.toString().isEmpty()) return 0;
        return Integer.parseInt(value.toString());
    }

    private Boolean parseBoolean(Object value) {
        if (value == null || value.toString().isEmpty()) return false;
        return Boolean.parseBoolean(value.toString());
    }

    private LocalDateTime parseLocalDateTime(Object value) {
        if (value == null || value.toString().isEmpty()) return null;
        try {
            return LocalDateTime.parse(value.toString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public IPage<SeckillActivity> getActivityPage(int page, int size) {
        Page<SeckillActivity> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<SeckillActivity> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(SeckillActivity::getCreateTime);
        return activityMapper.selectPage(pageParam, wrapper);
    }

    @Override
    @Transactional
    public SeckillActivity createActivity(SeckillActivity activity) {
        if (activity.getSignKey() == null || activity.getSignKey().isEmpty()) {
            activity.setSignKey(UUID.randomUUID().toString());
        }
        if (activity.getStatus() == null) {
            activity.setStatus(0);
        }
        if (activity.getPerLimit() == null) {
            activity.setPerLimit(1);
        }
        activity.setCreateTime(LocalDateTime.now());
        activity.setUpdateTime(LocalDateTime.now());
        activityMapper.insert(activity);
        return activity;
    }

    @Override
    @Transactional
    public SeckillActivity updateActivity(Long id, SeckillActivity activity) {
        SeckillActivity existing = activityMapper.selectById(id);
        if (existing == null) {
            throw BusinessException.notFound("活动不存在");
        }
        activity.setId(id);
        activity.setUpdateTime(LocalDateTime.now());
        activityMapper.updateById(activity);
        return activityMapper.selectById(id);
    }

    @Override
    @Transactional
    public boolean deleteActivity(Long id) {
        return activityMapper.deleteById(id) > 0;
    }

    @Override
    public boolean startActivity(Long activityId) {
        SeckillActivity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            return false;
        }
        activity.setStatus(1);
        activityMapper.updateById(activity);
        return true;
    }

    @Override
    public boolean endActivity(Long activityId) {
        SeckillActivity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            return false;
        }
        activity.setStatus(2);
        activityMapper.updateById(activity);
        return true;
    }

    @Override
    public boolean publishActivity(Long activityId) {
        SeckillActivity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw BusinessException.notFound("活动不存在");
        }
        // 检查活动是否已关联商品
        long goodsCount = goodsService.countByActivityId(activityId);
        if (goodsCount == 0) {
            throw BusinessException.badRequest("活动必须至少关联一个商品才能发布");
        }
        activity.setStatus(1);
        activityMapper.updateById(activity);
        preheatService.preheatActivity(activityId);
        return true;
    }

    @Override
    public int getActivityStock(Long activityId) {
        // 从 Redis 获取预热的库存数据
        return preheatService.getActivityStock(activityId);
    }
}
