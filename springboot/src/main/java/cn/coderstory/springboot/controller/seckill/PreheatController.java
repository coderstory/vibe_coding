package cn.coderstory.springboot.controller.seckill;

import cn.coderstory.springboot.service.seckill.PreheatService;
import cn.coderstory.springboot.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 秒杀预热控制器。
 * <p>
 * 提供活动数据的预热触发和预热状态查询功能。
 * 预热将活动商品库存加载到 Redis 缓存中。
 *
 * @since 1.7.0
 */
@RestController
@RequestMapping("/api/seckill/preheat")
@RequiredArgsConstructor
public class PreheatController {

    private final PreheatService preheatService;

    /**
     * 触发指定活动的预热操作。
     *
     * @param activityId 活动 ID
     * @return 预热结果（含状态信息）
     */
    @PostMapping("/{activityId}")
    public ApiResponse<Map<String, Object>> preheatActivity(@PathVariable Long activityId) {
        preheatService.preheatActivity(activityId);
        Map<String, Object> result = new HashMap<>();
        result.put("activityId", activityId);
        result.put("status", "preheated");
        result.put("message", "活动数据预热完成");
        return ApiResponse.success(result);
    }

    /**
     * 查询指定活动的预热状态。
     *
     * @param activityId 活动 ID
     * @return 预热状态数据
     */
    @GetMapping("/status/{activityId}")
    public ApiResponse<Map<String, Object>> getPreheatStatus(@PathVariable Long activityId) {
        Map<String, Object> status = preheatService.getPreheatStatus(activityId);
        return ApiResponse.success(status);
    }
}
