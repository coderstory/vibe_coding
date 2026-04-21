package cn.coderstory.springboot.seckill.service.impl;

import cn.coderstory.springboot.exception.BusinessException;
import cn.coderstory.springboot.seckill.entity.SeckillGoods;
import cn.coderstory.springboot.seckill.mapper.SeckillGoodsMapper;
import cn.coderstory.springboot.seckill.service.GoodsService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GoodsServiceImpl implements GoodsService {

    private final SeckillGoodsMapper goodsMapper;

    @Override
    public IPage<SeckillGoods> getGoodsPage(int page, int size, Long activityId) {
        Page<SeckillGoods> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<SeckillGoods> wrapper = new LambdaQueryWrapper<>();
        if (activityId != null) {
            wrapper.eq(SeckillGoods::getActivityId, activityId);
        }
        wrapper.orderByDesc(SeckillGoods::getCreateTime);
        return goodsMapper.selectPage(pageParam, wrapper);
    }

    @Override
    public SeckillGoods getGoodsById(Long id) {
        SeckillGoods goods = goodsMapper.selectById(id);
        if (goods == null) {
            throw BusinessException.notFound("商品不存在");
        }
        return goods;
    }

    @Override
    @Transactional
    public SeckillGoods createGoods(SeckillGoods goods) {
        goods.setCreateTime(LocalDateTime.now());
        goods.setUpdateTime(LocalDateTime.now());
        goods.setSold(0);
        goodsMapper.insert(goods);
        return goods;
    }

    @Override
    @Transactional
    public SeckillGoods updateGoods(Long id, SeckillGoods goods) {
        SeckillGoods existing = goodsMapper.selectById(id);
        if (existing == null) {
            throw BusinessException.notFound("商品不存在");
        }
        goods.setId(id);
        goods.setUpdateTime(LocalDateTime.now());
        goodsMapper.updateById(goods);
        return goodsMapper.selectById(id);
    }

    @Override
    @Transactional
    public boolean deleteGoods(Long id) {
        return goodsMapper.deleteById(id) > 0;
    }

    @Override
    public long countByActivityId(Long activityId) {
        return goodsMapper.selectCount(new LambdaQueryWrapper<SeckillGoods>()
                .eq(SeckillGoods::getActivityId, activityId));
    }

    @Override
    public List<SeckillGoods> getGoodsListByActivity(LambdaQueryWrapper<SeckillGoods> wrapper) {
        return goodsMapper.selectList(wrapper);
    }
}
