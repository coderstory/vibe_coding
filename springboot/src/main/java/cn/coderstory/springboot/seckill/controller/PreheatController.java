package cn.coderstory.springboot.seckill.controller;

import cn.coderstory.springboot.seckill.service.PreheatService;
import cn.coderstory.springboot.vo.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/seckill/preheat")
@RequiredArgsConstructor
public class PreheatController {

    private final PreheatService preheatService;

    @PostMapping("/{activityId}")
    public ApiResponse<Map<String, Object>> preheatActivity(@PathVariable Long activityId) {
        preheatService.preheatActivity(activityId);
        Map<String, Object> result = new HashMap<>();
        result.put("activityId", activityId);
        result.put("status", "preheated");
        result.put("message", "活动数据预热完成");
        return ApiResponse.success(result);
    }

    @GetMapping("/status/{activityId}")
    public ApiResponse<Map<String, Object>> getPreheatStatus(@PathVariable Long activityId) {
        Map<String, Object> status = preheatService.getPreheatStatus(activityId);
        return ApiResponse.success(status);
    }
}
