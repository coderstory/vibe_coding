package cn.coderstory.springboot.seckill.service.impl;

import cn.coderstory.springboot.seckill.entity.SeckillActivity;
import cn.coderstory.springboot.seckill.mapper.SeckillActivityMapper;
import cn.coderstory.springboot.seckill.service.ActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {
    private final SeckillActivityMapper activityMapper;

    @Override
    public SeckillActivity getActivity(Long activityId) {
        return activityMapper.selectById(activityId);
    }

    @Override
    public boolean startActivity(Long activityId) {
        SeckillActivity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            return false;
        }
        activity.setStatus(1);
        return activityMapper.updateById(activity) > 0;
    }

    @Override
    public boolean endActivity(Long activityId) {
        SeckillActivity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            return false;
        }
        activity.setStatus(2);
        return activityMapper.updateById(activity) > 0;
    }
}