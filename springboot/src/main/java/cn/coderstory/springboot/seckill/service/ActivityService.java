package cn.coderstory.springboot.seckill.service;

import cn.coderstory.springboot.seckill.entity.SeckillActivity;
import com.baomidou.mybatisplus.core.metadata.IPage;

public interface ActivityService {
    SeckillActivity getActivity(Long activityId);

    IPage<SeckillActivity> getActivityPage(int page, int size);

    SeckillActivity createActivity(SeckillActivity activity);

    SeckillActivity updateActivity(Long id, SeckillActivity activity);

    boolean deleteActivity(Long id);

    boolean startActivity(Long activityId);

    boolean endActivity(Long activityId);

    boolean publishActivity(Long activityId);
}
