package cn.coderstory.springboot.seckill.controller;

import cn.coderstory.springboot.seckill.entity.SeckillActivity;
import cn.coderstory.springboot.seckill.service.ActivityService;
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
}
