package cn.coderstory.springboot.seckill.service;

import cn.coderstory.springboot.seckill.entity.SeckillGoods;
import com.baomidou.mybatisplus.core.metadata.IPage;

public interface GoodsService {
    IPage<SeckillGoods> getGoodsPage(int page, int size, Long activityId);

    SeckillGoods getGoodsById(Long id);

    SeckillGoods createGoods(SeckillGoods goods);

    SeckillGoods updateGoods(Long id, SeckillGoods goods);

    boolean deleteGoods(Long id);

    /**
     * 统计活动关联的商品数量
     *
     * @param activityId 活动ID
     * @return 商品数量
     */
    long countByActivityId(Long activityId);
}
