package cn.coderstory.springboot.controller.monitor;

import cn.coderstory.springboot.dto.ApiResponse;
import cn.coderstory.springboot.service.monitor.MonitorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 系统监控控制器。
 * <p>
 * 提供系统指标查询、商品库存监控和健康检查接口。
 *
 * @since 1.7.0
 */
@RestController
@RequestMapping("/api/monitor")
@RequiredArgsConstructor
public class MonitorController {

    private final MonitorService monitorService;

    /**
     * 获取系统监控指标。
     *
     * @return 系统监控数据，包括 JVM、内存、线程等信息
     */
    @GetMapping("/metrics")
    public ApiResponse<Map<String, Object>> getMetrics() {
        return ApiResponse.success(monitorService.getMetrics());
    }

    /**
     * 获取商品库存信息。
     *
     * @param goodsId 商品 ID
     * @return 商品的库存详情
     */
    @GetMapping("/stock/{goodsId}")
    public ApiResponse<Map<String, Object>> getGoodsStock(@PathVariable Long goodsId) {
        return ApiResponse.success(monitorService.getGoodsStockInfo(goodsId));
    }

    /**
     * 健康检查接口。
     *
     * @return 系统健康状态
     */
    @GetMapping("/health")
    public ApiResponse<Map<String, String>> health() {
        return ApiResponse.success(Map.of("status", "UP"));
    }
}
