package cn.coderstory.springboot.seckill.service;

import java.util.Map;

public interface PreheatService {
    void preheatActivity(Long activityId);

    Map<String, Object> getPreheatStatus(Long activityId);
}
