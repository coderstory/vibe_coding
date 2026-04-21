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
}
