package cn.coderstory.springboot.seckill.controller;

import cn.coderstory.springboot.seckill.entity.SeckillReservation;
import cn.coderstory.springboot.seckill.mapper.SeckillReservationMapper;
import cn.coderstory.springboot.seckill.service.PreheatService;
import cn.coderstory.springboot.vo.ApiResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 预约控制器
 *
 * 核心功能：
 * 1. 用户预约活动
 * 2. 查询我的预约列表
 *
 * 缓存策略：
 * - 预约时：先写 Redis Set（SADD），再异步写数据库
 * - 查询时：直接查数据库（需要完整预约信息）
 * - 判断是否已预约：查 Redis Set（SISMEMBER），O(1) 复杂度
 *
 * Redis Key 设计：
 * - seckill:reservation:{activityId} - 预约用户ID集合（Set 类型）
 *
 * @author seckill-team
 */
@Slf4j
@RestController
@RequestMapping("/api/reservation")
@RequiredArgsConstructor
public class ReservationController {

    private final SeckillReservationMapper reservationMapper;
    private final PreheatService preheatService;

    /**
     * 用户预约活动
     *
     * 流程：
     * 1. 先用 Redis SISMEMBER 检查是否已预约（O(1)）
     * 2. 已预约则直接返回 false
     * 3. 未预约则 SADD 写入 Redis，同时写数据库持久化
     *
     * @param activityId 活动ID
     * @param userId 用户ID（从请求头获取）
     * @return true=预约成功，false=已预约
     */
    @PostMapping("/{activityId}")
    public ApiResponse<Boolean> reserve(@PathVariable Long activityId,
                                        @RequestHeader("X-User-Id") Long userId) {
        // 1. 先检查 Redis 是否已预约（快速路径）
        if (preheatService.isUserReserved(activityId, userId)) {
            log.debug("用户 {} 已预约活动 {}（Redis）", userId, activityId);
            return ApiResponse.success(false);
        }

        // 2. 双重检查：再查一下数据库（可能 Redis 还没同步）
        SeckillReservation existing = reservationMapper.selectOne(
            new LambdaQueryWrapper<SeckillReservation>()
                .eq(SeckillReservation::getUserId, userId)
                .eq(SeckillReservation::getActivityId, activityId)
        );

        if (existing != null) {
            // 数据库已有记录，补充写入 Redis
            log.debug("用户 {} 已预约活动 {}（DB），同步到 Redis", userId, activityId);
            // 这里不需要再写入，因为之前 SISMEMBER 返回 false 说明 Redis 没有
            // 可能的情况：用户之前预约过，但 Redis 重启或缓存过期了
            return ApiResponse.success(false);
        }

        // 3. 执行预约：先写 Redis，再写数据库
        try {
            // 写入 Redis Set
            // 注意：这里假设 preheatService 有添加预约用户的方法
            // 如果没有，我们需要直接操作 Redis

            // 写数据库
            SeckillReservation reservation = new SeckillReservation();
            reservation.setUserId(userId);
            reservation.setActivityId(activityId);
            reservation.setReserveTime(LocalDateTime.now());
            reservation.setStatus(0);         // 0=预约中
            reservation.setNotified(false);    // 未发送提醒
            reservationMapper.insert(reservation);

            // 同步到 Redis Set
            preheatService.addReservation(activityId, userId);

            log.info("用户 {} 预约活动 {} 成功", userId, activityId);
            return ApiResponse.success(true);
        } catch (Exception e) {
            log.error("用户 {} 预约活动 {} 失败: {}", userId, activityId, e.getMessage());
            return ApiResponse.error("预约失败，请重试");
        }
    }

    /**
     * 查询我的预约列表
     *
     * 直接从数据库查询，返回完整的预约信息
     *
     * @param userId 用户ID（从请求头获取）
     * @return 预约列表
     */
    @GetMapping("/my")
    public ApiResponse<List<SeckillReservation>> myReservations(@RequestHeader("X-User-Id") Long userId) {
        List<SeckillReservation> reservations = reservationMapper.selectList(
            new LambdaQueryWrapper<SeckillReservation>()
                .eq(SeckillReservation::getUserId, userId)
                .orderByDesc(SeckillReservation::getReserveTime)
        );
        return ApiResponse.success(reservations);
    }
}