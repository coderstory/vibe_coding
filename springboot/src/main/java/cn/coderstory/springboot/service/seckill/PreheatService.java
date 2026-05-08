package cn.coderstory.springboot.service.seckill;

import java.util.Map;

/**
 * 秒杀活动库存预热服务接口。
 * <p>
 * 负责将活动商品库存从数据库加载到 Redis，提升秒杀高峰期的库存扣减性能。
 * 预热完成后提供状态查询，用于判断活动是否可接受抢购请求。
 *
 * @since 1.7.0
 */
public interface PreheatService {

    /**
     * 预热指定活动的库存数据。
     * <p>
     * 将活动关联商品的总库存加载到 Redis，初始化 DECR 扣减的初始值。
     * 预热操作应在活动开始前完成。
     *
     * @param activityId 活动 ID
     */
    void preheatActivity(Long activityId);

    /**
     * 获取指定活动的预热状态。
     * <p>
     * 返回预热是否完成、Redis 中剩余库存等信息。
     *
     * @param activityId 活动 ID
     * @return 预热状态信息
     */
    Map<String, Object> getPreheatStatus(Long activityId);

    /**
     * 获取活动总库存（从 Redis 获取所有关联商品的预热库存之和）
     *
     * @param activityId 活动ID
     * @return 总库存数量
     */
    int getActivityStock(Long activityId);

    /**
     * 检查用户是否已预约
     *
     * @param activityId 活动ID
     * @param userId     用户ID
     * @return true=已预约，false=未预约
     */
    boolean isUserReserved(Long activityId, Long userId);

    /**
     * 添加用户到活动的预约集合
     * <p>
     * 当用户预约时，调用此方法将用户ID添加到 Redis Set
     *
     * @param activityId 活动ID
     * @param userId     用户ID
     */
    void addReservation(Long activityId, Long userId);
}
