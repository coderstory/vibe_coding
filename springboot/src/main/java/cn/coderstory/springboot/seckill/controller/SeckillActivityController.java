package cn.coderstory.springboot.seckill.controller;

import cn.coderstory.springboot.seckill.entity.SeckillActivity;
import cn.coderstory.springboot.seckill.service.ActivityService;
import cn.coderstory.springboot.seckill.vo.ActivityDetailVO;
import cn.coderstory.springboot.vo.ApiResponse;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/seckill/activity")
@RequiredArgsConstructor
public class SeckillActivityController {

    private final ActivityService activityService;

    @GetMapping
    public ApiResponse<IPage<SeckillActivity>> getActivityPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(activityService.getActivityPage(page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<SeckillActivity> getActivity(@PathVariable Long id) {
        return ApiResponse.success(activityService.getActivity(id));
    }

    /**
     * 获取活动详情（包含商品列表）
     *
     * 用于秒杀详情页，返回活动信息及关联的所有商品
     * 用户可以选择要抢购的商品
     *
     * @param id 活动ID
     * @return 活动详情（含商品列表）
     */
    @GetMapping("/{id}/detail")
    public ApiResponse<ActivityDetailVO> getActivityDetail(@PathVariable Long id) {
        return ApiResponse.success(activityService.getActivityDetail(id));
    }

    @PostMapping
    public ApiResponse<SeckillActivity> createActivity(@RequestBody SeckillActivity activity) {
        return ApiResponse.success(activityService.createActivity(activity));
    }

    @PutMapping("/{id}")
    public ApiResponse<SeckillActivity> updateActivity(@PathVariable Long id, @RequestBody SeckillActivity activity) {
        return ApiResponse.success(activityService.updateActivity(id, activity));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> deleteActivity(@PathVariable Long id) {
        return ApiResponse.success(activityService.deleteActivity(id));
    }

    @PostMapping("/{id}/publish")
    public ApiResponse<Boolean> publishActivity(@PathVariable Long id) {
        return ApiResponse.success(activityService.publishActivity(id));
    }

    /**
     * 获取活动总库存
     *
     * @param id 活动ID
     * @return 活动总库存数量
     */
    @GetMapping("/{id}/stock")
    public ApiResponse<Integer> getActivityStock(@PathVariable Long id) {
        return ApiResponse.success(activityService.getActivityStock(id));
    }
}
