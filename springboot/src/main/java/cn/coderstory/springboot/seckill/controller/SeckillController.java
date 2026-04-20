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

@RestController
@RequestMapping("/api/seckill")
@RequiredArgsConstructor
public class SeckillController {
    private final SeckillService seckillService;
    private final IdempotentService idempotentService;

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

    @GetMapping("/result/{queueId}")
    public ApiResponse<SeckillResponse> getResult(@PathVariable String queueId) {
        return ApiResponse.success(SeckillResponse.builder()
            .queueId(queueId)
            .status(0)
            .message("排队中")
            .build());
    }

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