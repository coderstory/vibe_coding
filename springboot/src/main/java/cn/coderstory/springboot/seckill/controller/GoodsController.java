package cn.coderstory.springboot.seckill.controller;

import cn.coderstory.springboot.seckill.entity.SeckillGoods;
import cn.coderstory.springboot.seckill.service.GoodsService;
import cn.coderstory.springboot.vo.ApiResponse;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/goods")
@RequiredArgsConstructor
public class GoodsController {

    private final GoodsService goodsService;

    @GetMapping
    public ApiResponse<IPage<SeckillGoods>> getGoodsPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long activityId) {
        return ApiResponse.success(goodsService.getGoodsPage(page, size, activityId));
    }

    @GetMapping("/{id}")
    public ApiResponse<SeckillGoods> getGoodsById(@PathVariable Long id) {
        return ApiResponse.success(goodsService.getGoodsById(id));
    }

    @PostMapping
    public ApiResponse<SeckillGoods> createGoods(@RequestBody SeckillGoods goods) {
        return ApiResponse.success(goodsService.createGoods(goods));
    }

    @PutMapping("/{id}")
    public ApiResponse<SeckillGoods> updateGoods(@PathVariable Long id, @RequestBody SeckillGoods goods) {
        return ApiResponse.success(goodsService.updateGoods(id, goods));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> deleteGoods(@PathVariable Long id) {
        return ApiResponse.success(goodsService.deleteGoods(id));
    }

    @GetMapping("/activity/{activityId}")
    public ApiResponse<IPage<SeckillGoods>> getGoodsByActivity(
            @PathVariable Long activityId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(goodsService.getGoodsPage(page, size, activityId));
    }
}
