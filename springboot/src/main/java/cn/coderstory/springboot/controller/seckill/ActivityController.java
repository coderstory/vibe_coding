package cn.coderstory.springboot.controller.seckill;

import cn.coderstory.springboot.entity.seckill.SeckillActivity;
import cn.coderstory.springboot.service.seckill.ActivityService;
import cn.coderstory.springboot.service.seckill.PreheatService;
import cn.coderstory.springboot.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 活动管理控制器。
 * <p>
 * 提供秒杀活动的启停控制和活动信息查询功能。
 *
 * @since 1.7.0
 */
@RestController
@RequestMapping("/api/activity")
@RequiredArgsConstructor
public class ActivityController {
    private final ActivityService activityService;
    private final PreheatService preheatService;

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
     * 启动指定活动（含预热）。
     *
     * @param id 活动 ID
     * @return 启动结果（true 表示成功）
     */
    @PostMapping("/{id}/start")
    public ApiResponse<Boolean> startActivity(@PathVariable Long id) {
        preheatService.preheatActivity(id);
        return ApiResponse.success(activityService.startActivity(id));
    }

    /**
     * 结束指定活动。
     *
     * @param id 活动 ID
     * @return 结束结果（true 表示成功）
     */
    @PostMapping("/{id}/end")
    public ApiResponse<Boolean> endActivity(@PathVariable Long id) {
        return ApiResponse.success(activityService.endActivity(id));
    }
}