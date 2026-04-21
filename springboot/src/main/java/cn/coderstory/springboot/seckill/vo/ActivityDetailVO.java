package cn.coderstory.springboot.seckill.vo;

import cn.coderstory.springboot.seckill.entity.SeckillActivity;
import cn.coderstory.springboot.seckill.entity.SeckillGoods;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 活动详情 VO（包含活动信息和商品信息）
 *
 * 用于秒杀详情页，一个活动只关联一个商品
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ActivityDetailVO extends SeckillActivity {

    /**
     * 活动关联的商品
     * 一个活动只关联一个商品
     */
    private SeckillGoods goods;
}