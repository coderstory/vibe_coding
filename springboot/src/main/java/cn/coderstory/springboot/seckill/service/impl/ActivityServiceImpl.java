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

/**
 * 秒杀活动服务实现类
 *
 * 核心功能：
 * 1. 活动 CRUD 操作
 * 2. 活动详情缓存（Redis Hash 存储）
 * 3. 分布式锁防止缓存击穿
 *
 * 缓存策略说明：
 * - 活动发布时，PreheatService 会将活动信息预热到 Redis Hash
 * - 读取活动时，优先从 Redis 读取，缓存不存在时使用分布式锁查库
 * - 使用分布式锁防止缓存击穿（大量请求同时发现缓存不存在，都去查库）
 *
 * Redis Key 设计：
 * - seckill:activity:{id} - 存储活动详情（Hash 类型）
 *
 * @author seckill-team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {

    /** Redis 中活动缓存的 Key 前缀 */
    private static final String ACTIVITY_KEY_PREFIX = "seckill:activity:";

    private final SeckillActivityMapper activityMapper;
    private final PreheatService preheatService;
    private final GoodsService goodsService;
    private final DistributedLockService distributedLockService;
    private final StringRedisTemplate redisTemplate;

    /**
     * 获取活动详情（带缓存和分布式锁）
     *
     * 读取流程：
     * 1. 先尝试从 Redis Hash 读取活动信息
     * 2. 缓存不存在时，使用分布式锁防止缓存击穿
     * 3. 获取锁后再次检查缓存（双重检查锁定模式）
     * 4. 确认缓存仍不存在，才查数据库并回填缓存
     *
     * @param activityId 活动ID
     * @return 活动详情， null 表示活动不存在
     */
    @Override
    public SeckillActivity getActivity(Long activityId) {
        // 步骤1：尝试从 Redis Hash 读取缓存
        SeckillActivity cachedActivity = getActivityFromCache(activityId);
        if (cachedActivity != null) {
            log.debug("活动 {} 从缓存读取成功", activityId);
            return cachedActivity;
        }

        // 步骤2：缓存不存在，使用分布式锁防止缓存击穿
        // 只有获取到锁的线程才能查库并回填缓存，其他线程需要等待
        String lockKey = DistributedLockServiceImpl.getActivityLockKey(activityId);
        SeckillActivity result = distributedLockService.executeWithLock(
                lockKey,                      // 锁的 key
                5, TimeUnit.SECONDS,         // 锁的持有时间，防止死锁
                () -> {
                    // ===== 双重检查锁定（Double-Check Locking）=====
                    // 获取锁后再次检查缓存，可能其他线程已经回填了缓存
                    SeckillActivity cached = getActivityFromCache(activityId);
                    if (cached != null) {
                        return cached;
                    }

                    // 缓存仍然不存在，查数据库
                    SeckillActivity activity = activityMapper.selectById(activityId);

                    // MyBatis Plus 的 selectById 在数据不存在时返回空对象（字段为null）而非null
                    // 所以需要额外检查 id 是否有效
                    if (activity == null || activity.getId() == null) {
                        log.warn("活动 {} 在数据库中不存在", activityId);
                        return null;
                    }

                    // 查库成功后回填缓存，供后续请求使用
                    saveActivityToCache(activity);
                    log.debug("活动 {} 已从数据库读取并回填缓存", activityId);
                    return activity;
                }
        );

        // 查库后仍为 null，说明活动不存在
        if (result == null) {
            throw BusinessException.notFound("活动不存在");
        }
        return result;
    }

    /**
     * 从 Redis Hash 读取活动缓存
     *
     * Redis 数据结构：
     * - Key: seckill:activity:{id}
     * - Type: Hash
     * - Fields: id, name, description, startTime, endTime, status, perLimit, enableCaptcha, enableIpLimit, signKey
     *
     * @param activityId 活动ID
     * @return 活动对象， null 表示缓存不存在或数据无效
     */
    private SeckillActivity getActivityFromCache(Long activityId) {
        String activityKey = ACTIVITY_KEY_PREFIX + activityId;

        try {
            // 使用 HGETALL 获取 Hash 中的所有字段
            Map<Object, Object> cacheData = redisTemplate.opsForHash().entries(activityKey);

            // 检查缓存是否存在
            if (cacheData.isEmpty()) {
                return null;
            }

            // 检查 id 字段是否有有效值（避免缓存到空数据）
            Object idValue = cacheData.get("id");
            if (idValue == null || idValue.toString().isEmpty()) {
                log.warn("活动 {} 缓存数据无效（id为空），将重新从数据库加载", activityId);
                return null;
            }

            // 从 Hash 中构建活动对象
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
            // 处理 Redis 类型错误（例如旧数据可能是 String 类型）
            if (e.getMessage() != null && e.getMessage().contains("WRONGTYPE")) {
                log.warn("活动 {} 缓存类型错误，删除旧数据让下次重新加载", activityId);
                redisTemplate.delete(activityKey);
            } else {
                log.warn("解析活动 {} 缓存数据失败: {}", activityId, e.getMessage());
            }
            return null;
        }
    }

    /**
     * 保存活动信息到 Redis Hash
     *
     * 使用 Hash 类型的好处是可以单独访问某个字段，
     * 比如只需要活动名称时不需要获取全部数据
     *
     * @param activity 活动对象
     */
    private void saveActivityToCache(SeckillActivity activity) {
        String activityKey = ACTIVITY_KEY_PREFIX + activity.getId();

        // 构建 Hash 的 field-value 映射
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

        // 使用 HSET 保存整个 Hash
        redisTemplate.opsForHash().putAll(activityKey, cacheData);

        // 设置过期时间：活动结束时间 + 1小时（给缓冲时间）
        long expireSeconds = calculateExpireSeconds(activity.getEndTime());
        redisTemplate.expire(activityKey, expireSeconds, TimeUnit.SECONDS);
        log.debug("活动 {} 已缓存，过期时间 {} 秒", activity.getId(), expireSeconds);
    }

    /**
     * 计算缓存过期时间
     *
     * @param endTime 活动结束时间
     * @return 过期秒数（至少1小时）
     */
    private long calculateExpireSeconds(LocalDateTime endTime) {
        if (endTime == null) {
            return 3600; // 默认1小时
        }
        // 计算到活动结束后1小时的秒数
        long seconds = java.time.Duration.between(
                LocalDateTime.now(),
                endTime.plusHours(1)
        ).getSeconds();
        return Math.max(seconds, 3600); // 确保至少1小时
    }

    // ========== 类型转换工具方法 ==========

    /**
     * 安全解析 Long 类型
     * 处理空值和转换异常
     */
    private Long parseLong(Object value) {
        if (value == null || value.toString().isEmpty()) return null;
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 安全解析 Integer 类型
     * 处理空值和转换异常
     */
    private Integer parseInteger(Object value) {
        if (value == null || value.toString().isEmpty()) return 0;
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * 安全解析 Boolean 类型
     * 处理空值和转换异常
     */
    private Boolean parseBoolean(Object value) {
        if (value == null || value.toString().isEmpty()) return false;
        return Boolean.parseBoolean(value.toString());
    }

    /**
     * 安全解析 LocalDateTime 类型
     * 使用 ISO 格式解析，处理异常情况
     */
    private LocalDateTime parseLocalDateTime(Object value) {
        if (value == null || value.toString().isEmpty()) return null;
        try {
            return LocalDateTime.parse(value.toString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception e) {
            return null;
        }
    }

    // ========== CRUD 操作 ==========

    /**
     * 分页查询活动列表
     */
    @Override
    public IPage<SeckillActivity> getActivityPage(int page, int size) {
        Page<SeckillActivity> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<SeckillActivity> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(SeckillActivity::getCreateTime);
        return activityMapper.selectPage(pageParam, wrapper);
    }

    /**
     * 创建活动
     *
     * @param activity 活动信息
     * @return 创建后的活动（含ID）
     */
    @Override
    @Transactional
    public SeckillActivity createActivity(SeckillActivity activity) {
        // 自动生成签名密钥，用于秒杀请求的签名验证
        if (activity.getSignKey() == null || activity.getSignKey().isEmpty()) {
            activity.setSignKey(UUID.randomUUID().toString());
        }
        // 默认状态为未发布（0）
        if (activity.getStatus() == null) {
            activity.setStatus(0);
        }
        // 默认每人限购1件
        if (activity.getPerLimit() == null) {
            activity.setPerLimit(1);
        }
        activity.setCreateTime(LocalDateTime.now());
        activity.setUpdateTime(LocalDateTime.now());
        activityMapper.insert(activity);
        return activity;
    }

    /**
     * 更新活动信息
     */
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

    /**
     * 删除活动
     */
    @Override
    @Transactional
    public boolean deleteActivity(Long id) {
        return activityMapper.deleteById(id) > 0;
    }

    /**
     * 开启活动（手动开始）
     */
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

    /**
     * 结束活动（手动结束）
     */
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

    /**
     * 发布活动
     *
     * 发布流程：
     * 1. 检查活动是否已关联商品（至少要有一个商品才能发布）
     * 2. 更新活动状态为已发布
     * 3. 调用预热服务，将活动和库存数据预热到 Redis
     *
     * @param activityId 活动ID
     * @return true 发布成功
     * @throws BusinessException 如果活动不存在或没有关联商品
     */
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
        // 更新状态为进行中
        activity.setStatus(1);
        activityMapper.updateById(activity);
        // 触发预热：将活动信息和商品库存写入 Redis
        preheatService.preheatActivity(activityId);
        log.info("活动 {} 发布成功，已预热到 Redis", activityId);
        return true;
    }

    /**
     * 获取活动总库存
     *
     * 库存数据来自 Redis（预热时写入），不查数据库
     *
     * @param activityId 活动ID
     * @return 所有商品的库存总和
     */
    @Override
    public int getActivityStock(Long activityId) {
        // 委托给 PreheatService 从 Redis 获取库存
        return preheatService.getActivityStock(activityId);
    }
}
