package cn.coderstory.springboot.seckill.service;

import cn.coderstory.springboot.seckill.entity.SeckillActivity;

public interface ActivityService {
    SeckillActivity getActivity(Long activityId);
    boolean startActivity(Long activityId);
    boolean endActivity(Long activityId);
}