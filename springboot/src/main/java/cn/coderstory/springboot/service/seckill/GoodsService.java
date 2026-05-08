package cn.coderstory.springboot.service.seckill;

import cn.coderstory.springboot.entity.seckill.SeckillGoods;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * 秒杀商品管理服务接口。
 * <p>
 * 提供秒杀商品的 CRUD 操作以及按活动查询商品功能。
 * 每个活动关联一个或多个商品，商品库存独立管理。
 *
 * @since 1.7.0
 */
public interface GoodsService {

    /**
     * 分页查询商品列表，可按活动 ID 筛选。
     *
     * @param page       页码（从 1 开始）
     * @param size       每页条数
     * @param activityId 活动 ID（可选，传 null 查询全部）
     * @return 商品分页结果
     */
    IPage<SeckillGoods> getGoodsPage(int page, int size, Long activityId);

    /**
     * 根据商品 ID 获取商品信息。
     *
     * @param id 商品 ID
     * @return 商品实体，不存在返回 null
     */
    SeckillGoods getGoodsById(Long id);

    /**
     * 创建秒杀商品。
     *
     * @param goods 商品信息（不含 ID）
     * @return 已创建的商品（含生成的 ID）
     */
    SeckillGoods createGoods(SeckillGoods goods);

    /**
     * 更新商品信息。
     *
     * @param id    商品 ID
     * @param goods 更新的商品字段
     * @return 更新后的商品
     */
    SeckillGoods updateGoods(Long id, SeckillGoods goods);

    /**
     * 删除指定商品。
     *
     * @param id 商品 ID
     * @return 是否删除成功
     */
    boolean deleteGoods(Long id);

    /**
     * 统计活动关联的商品数量
     *
     * @param activityId 活动ID
     * @return 商品数量
     */
    long countByActivityId(Long activityId);

    /**
     * 获取活动关联的商品
     * 一个活动只关联一个商品
     *
     * @param activityId 活动ID
     * @return 商品信息，不存在返回 null
     */
    SeckillGoods getGoodsByActivityId(Long activityId);

}
