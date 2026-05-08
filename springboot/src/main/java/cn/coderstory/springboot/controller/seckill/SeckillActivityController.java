package cn.coderstory.springboot.controller.seckill;

import cn.coderstory.springboot.entity.seckill.SeckillActivity;
import cn.coderstory.springboot.service.seckill.ActivityService;
import cn.coderstory.springboot.vo.seckill.ActivityDetailVO;
import cn.coderstory.springboot.dto.ApiResponse;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 秒杀活动管理控制器。
 * <p>
 * 提供秒杀活动的 CRUD 操作、发布管理、详情查询和库存查询功能。
 *
 * @since 1.7.0
 */
@RestController
@RequestMapping("/api/seckill/activity")
@RequiredArgsConstructor
public class SeckillActivityController {

    private final ActivityService activityService;

    /**
     * 分页查询秒杀活动列表。
     *
     * @param page 页码，默认 1
     * @param size 每页数量，默认 20
     * @return 分页活动列表
     */
    @GetMapping
    public ApiResponse<IPage<SeckillActivity>> getActivityPage(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(activityService.getActivityPage(page, size));
    }

    /**
     * 根据活动 ID 获取活动信息。
     *
     * @param id 活动 ID
     * @return 活动信息
     */
    @GetMapping("/{id}")
    public ApiResponse<SeckillActivity> getActivity(@PathVariable Long id) {
        return ApiResponse.success(activityService.getActivity(id));
    }

    /**
     * 获取活动详情（包含商品列表）
     * <p>
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

    /**
     * 创建秒杀活动。
     *
     * @param activity 活动信息
     * @return 创建的活动
     */
    @PostMapping
    public ApiResponse<SeckillActivity> createActivity(@RequestBody SeckillActivity activity) {
        return ApiResponse.success(activityService.createActivity(activity));
    }

    /**
     * 更新秒杀活动信息。
     *
     * @param id       活动 ID
     * @param activity 活动信息
     * @return 更新后的活动
     */
    @PutMapping("/{id}")
    public ApiResponse<SeckillActivity> updateActivity(@PathVariable Long id, @RequestBody SeckillActivity activity) {
        return ApiResponse.success(activityService.updateActivity(id, activity));
    }

    /**
     * 删除秒杀活动。
     *
     * @param id 活动 ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> deleteActivity(@PathVariable Long id) {
        return ApiResponse.success(activityService.deleteActivity(id));
    }

    /**
     * 发布秒杀活动（上线可见）。
     *
     * @param id 活动 ID
     * @return 发布结果
     */
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
