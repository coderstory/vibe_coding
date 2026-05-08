package cn.coderstory.springboot.controller.seckill;

import cn.coderstory.springboot.entity.seckill.SeckillGoods;
import cn.coderstory.springboot.service.seckill.GoodsService;
import cn.coderstory.springboot.dto.ApiResponse;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 秒杀商品管理控制器。
 * <p>
 * 提供秒杀商品的 CRUD 操作和按活动查询商品功能。
 *
 * @since 1.7.0
 */
@RestController
@RequestMapping("/api/goods")
@RequiredArgsConstructor
public class GoodsController {

    private final GoodsService goodsService;

    /**
     * 分页查询秒杀商品列表。
     *
     * @param page       页码，默认 1
     * @param size       每页数量，默认 20
     * @param activityId 活动 ID（可选，按活动筛选）
     * @return 分页商品列表
     */
    @GetMapping
    public ApiResponse<IPage<SeckillGoods>> getGoodsPage(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(required = false) Long activityId) {
        return ApiResponse.success(goodsService.getGoodsPage(page, size, activityId));
    }

    /**
     * 根据商品 ID 获取商品信息。
     *
     * @param id 商品 ID
     * @return 商品信息
     */
    @GetMapping("/{id}")
    public ApiResponse<SeckillGoods> getGoodsById(@PathVariable Long id) {
        return ApiResponse.success(goodsService.getGoodsById(id));
    }

    /**
     * 创建秒杀商品。
     *
     * @param goods 商品信息
     * @return 创建的商品
     */
    @PostMapping
    public ApiResponse<SeckillGoods> createGoods(@RequestBody SeckillGoods goods) {
        return ApiResponse.success(goodsService.createGoods(goods));
    }

    /**
     * 更新秒杀商品信息。
     *
     * @param id    商品 ID
     * @param goods 商品信息
     * @return 更新后的商品
     */
    @PutMapping("/{id}")
    public ApiResponse<SeckillGoods> updateGoods(@PathVariable Long id, @RequestBody SeckillGoods goods) {
        return ApiResponse.success(goodsService.updateGoods(id, goods));
    }

    /**
     * 删除秒杀商品。
     *
     * @param id 商品 ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> deleteGoods(@PathVariable Long id) {
        return ApiResponse.success(goodsService.deleteGoods(id));
    }

    /**
     * 根据活动 ID 分页查询商品列表。
     *
     * @param activityId 活动 ID
     * @param page       页码，默认 1
     * @param size       每页数量，默认 20
     * @return 分页商品列表
     */
    @GetMapping("/activity/{activityId}")
    public ApiResponse<IPage<SeckillGoods>> getGoodsByActivity(
        @PathVariable Long activityId,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(goodsService.getGoodsPage(page, size, activityId));
    }
}
