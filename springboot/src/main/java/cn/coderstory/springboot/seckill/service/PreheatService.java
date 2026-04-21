package cn.coderstory.springboot.seckill.service;

import java.util.Map;

public interface PreheatService {
    void preheatActivity(Long activityId);

    Map<String, Object> getPreheatStatus(Long activityId);

    /**
     * 获取活动总库存（从 Redis 获取所有关联商品的预热库存之和）
     *
     * @param activityId 活动ID
     * @return 总库存数量
     */
    int getActivityStock(Long activityId);

    /**
     * 获取活动预约人数
     *
     * @param activityId 活动ID
     * @return 预约人数
     */
    long getReservationCount(Long activityId);

    /**
     * 检查用户是否已预约
     *
     * @param activityId 活动ID
     * @param userId 用户ID
     * @return true=已预约，false=未预约
     */
    boolean isUserReserved(Long activityId, Long userId);

    /**
     * 添加用户到活动的预约集合
     *
     * 当用户预约时，调用此方法将用户ID添加到 Redis Set
     *
     * @param activityId 活动ID
     * @param userId 用户ID
     */
    void addReservation(Long activityId, Long userId);
}
