package cn.coderstory.springboot.seckill.service.impl;

import cn.coderstory.springboot.exception.BusinessException;
import cn.coderstory.springboot.seckill.entity.SeckillActivity;
import cn.coderstory.springboot.seckill.mapper.SeckillActivityMapper;
import cn.coderstory.springboot.seckill.service.ActivityService;
import cn.coderstory.springboot.seckill.service.GoodsService;
import cn.coderstory.springboot.seckill.service.PreheatService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {

    private final SeckillActivityMapper activityMapper;
    private final PreheatService preheatService;
    private final GoodsService goodsService;

    @Override
    public SeckillActivity getActivity(Long activityId) {
        SeckillActivity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw BusinessException.notFound("活动不存在");
        }
        return activity;
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
}
