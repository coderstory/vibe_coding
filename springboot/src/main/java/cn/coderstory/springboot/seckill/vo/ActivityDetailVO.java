package cn.coderstory.springboot.seckill.vo;

import cn.coderstory.springboot.seckill.entity.SeckillActivity;
import cn.coderstory.springboot.seckill.entity.SeckillGoods;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 活动详情 VO（包含活动信息和商品列表）
 *
 * 用于秒杀详情页，返回活动详情及关联的所有商品
 * 用户可以查看商品信息并选择要抢购的商品
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ActivityDetailVO extends SeckillActivity {

    /**
     * 活动关联的商品列表
     * 一个活动可以关联多个商品，用户选择其中一个进行抢购
     */
    private List<SeckillGoods> goodsList;
}