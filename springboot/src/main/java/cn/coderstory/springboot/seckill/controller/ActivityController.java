package cn.coderstory.springboot.seckill.controller;

import cn.coderstory.springboot.seckill.entity.SeckillActivity;
import cn.coderstory.springboot.seckill.service.ActivityService;
import cn.coderstory.springboot.seckill.service.PreheatService;
import cn.coderstory.springboot.vo.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/activity")
@RequiredArgsConstructor
public class ActivityController {
    private final ActivityService activityService;
    private final PreheatService preheatService;

    @GetMapping("/{id}")
    public ApiResponse<SeckillActivity> getActivity(@PathVariable Long id) {
        return ApiResponse.success(activityService.getActivity(id));
    }

    @PostMapping("/{id}/start")
    public ApiResponse<Boolean> startActivity(@PathVariable Long id) {
        preheatService.preheatActivity(id);
        return ApiResponse.success(activityService.startActivity(id));
    }

    @PostMapping("/{id}/end")
    public ApiResponse<Boolean> endActivity(@PathVariable Long id) {
        return ApiResponse.success(activityService.endActivity(id));
    }
}