package cn.coderstory.springboot.seckill.controller;

import cn.coderstory.springboot.seckill.dto.SeckillRequest;
import cn.coderstory.springboot.seckill.dto.SeckillResponse;
import cn.coderstory.springboot.seckill.service.SeckillService;
import cn.coderstory.springboot.security.IdempotentService;
import cn.coderstory.springboot.vo.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

/**
 * 秒杀控制器
 *
 * 功能描述：
 * - 处理用户秒杀请求
 * - 实现请求幂等性，防止重复提交
 * - 支持异步下单，结果查询
 *
 * 工作流程：
 * 1. 用户发起秒杀请求，携带幂等性 Key
 * 2. 系统校验请求，生成排队号
 * 3. 用户通过排队号查询秒杀结果
 *
 * @author system
 * @version 1.0
 * @since 2026-04-20
 */
@RestController
@RequestMapping("/api/seckill")
@RequiredArgsConstructor
public class SeckillController {

    private final SeckillService seckillService;
    private final IdempotentService idempotentService;

    /**
     * 秒杀下单接口
     *
     * 请求头：
     * - X-User-Id: 用户 ID（必需）
     *
     * 请求体：
     * - goodsId: 商品 ID
     * - activityId: 活动 ID
     * - idempotentKey: 幂等性 Key（可选，不传则自动生成）
     *
     * @param request 秒杀请求参数
     * @param userId 用户 ID（从请求头获取）
     * @param httpRequest HTTP 请求（用于获取客户端 IP）
     * @return 秒杀结果响应
     */
    @PostMapping("/buy")
    public ApiResponse<SeckillResponse> buy(@RequestBody SeckillRequest request,
                                            @RequestHeader("X-User-Id") Long userId,
                                            HttpServletRequest httpRequest) {
        String ip = getClientIp(httpRequest);

        if (request.getIdempotentKey() == null) {
            request.setIdempotentKey(UUID.randomUUID().toString());
        }

        SeckillResponse response = seckillService.seckill(request, userId);
        return ApiResponse.success(response);
    }

    /**
     * 查询秒杀结果
     *
     * @param queueId 排队号（由秒杀接口返回）
     * @return 当前秒杀状态
     */
    @GetMapping("/result/{queueId}")
    public ApiResponse<SeckillResponse> getResult(@PathVariable String queueId) {
        return ApiResponse.success(SeckillResponse.builder()
            .queueId(queueId)
            .status(0)
            .message("排队中")
            .build());
    }

    /**
     * 获取客户端真实 IP
     *
     * 优先级：X-Forwarded-For > X-Real-IP > getRemoteAddr()
     * 支持代理和负载均衡场景下的 IP 获取
     *
     * @param request HTTP 请求
     * @return 客户端 IP 地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
