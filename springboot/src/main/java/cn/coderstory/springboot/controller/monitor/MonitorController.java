package cn.coderstory.springboot.controller.monitor;

import cn.coderstory.springboot.dto.ApiResponse;
import cn.coderstory.springboot.service.monitor.MonitorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/monitor")
@RequiredArgsConstructor
public class MonitorController {

    private final MonitorService monitorService;

    @GetMapping("/metrics")
    public ApiResponse<Map<String, Object>> getMetrics() {
        return ApiResponse.success(monitorService.getMetrics());
    }

    @GetMapping("/stock/{goodsId}")
    public ApiResponse<Map<String, Object>> getGoodsStock(@PathVariable Long goodsId) {
        return ApiResponse.success(monitorService.getGoodsStockInfo(goodsId));
    }

    @GetMapping("/health")
    public ApiResponse<Map<String, String>> health() {
        return ApiResponse.success(Map.of("status", "UP"));
    }
}
