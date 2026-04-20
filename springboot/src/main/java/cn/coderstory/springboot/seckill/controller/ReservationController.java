package cn.coderstory.springboot.seckill.controller;

import cn.coderstory.springboot.seckill.entity.SeckillReservation;
import cn.coderstory.springboot.seckill.mapper.SeckillReservationMapper;
import cn.coderstory.springboot.vo.ApiResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reservation")
@RequiredArgsConstructor
public class ReservationController {
    private final SeckillReservationMapper reservationMapper;

    @PostMapping("/{activityId}")
    public ApiResponse<Boolean> reserve(@PathVariable Long activityId,
                                        @RequestHeader("X-User-Id") Long userId) {
        SeckillReservation existing = reservationMapper.selectOne(
            new LambdaQueryWrapper<SeckillReservation>()
                .eq(SeckillReservation::getUserId, userId)
                .eq(SeckillReservation::getActivityId, activityId)
        );

        if (existing != null) {
            return ApiResponse.success(false);
        }

        SeckillReservation reservation = new SeckillReservation();
        reservation.setUserId(userId);
        reservation.setActivityId(activityId);
        reservation.setReserveTime(LocalDateTime.now());
        reservation.setStatus(0);
        reservation.setNotified(false);
        reservationMapper.insert(reservation);
        return ApiResponse.success(true);
    }

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