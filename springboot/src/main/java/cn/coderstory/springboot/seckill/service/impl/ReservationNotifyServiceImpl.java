package cn.coderstory.springboot.seckill.service.impl;

import cn.coderstory.springboot.seckill.entity.SeckillActivity;
import cn.coderstory.springboot.seckill.entity.SeckillReservation;
import cn.coderstory.springboot.seckill.mapper.SeckillActivityMapper;
import cn.coderstory.springboot.seckill.mapper.SeckillReservationMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 预约提醒服务实现
 *
 * 定时任务：
 * - 每分钟执行一次（@Scheduled(fixedDelay = 60000)）
 * - 扫描活动开始前30分钟内的预约
 * - 发送提醒并更新状态
 *
 * 提醒逻辑：
 * 1. 查询所有活动开始时间在 [now, now + 30分钟] 内的活动
 * 2. 查询这些活动的未提醒预约
 * 3. 发送提醒通知（当前是打印日志）
 * 4. 更新预约状态为已提醒
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationNotifyServiceImpl {

    private final SeckillReservationMapper reservationMapper;
    private final SeckillActivityMapper activityMapper;

    /**
     * 提醒提前时间（分钟）
     * 活动开始前多少分钟发送提醒
     */
    private static final int NOTIFY_BEFORE_MINUTES = 30;

    /**
     * 扫描并发送预约提醒
     *
     * 每分钟执行一次，查询即将开始的活动并发送提醒
     */
    @Scheduled(fixedDelay = 60000)
    public void scanAndNotify() {
        log.debug("开始扫描预约提醒...");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime notifyDeadline = now.plusMinutes(NOTIFY_BEFORE_MINUTES);

        // 查询条件：活动开始时间在 [now, now + 30分钟] 内，且状态为未开始
        List<SeckillActivity> upcomingActivities = activityMapper.selectList(
            new LambdaQueryWrapper<SeckillActivity>()
                .gt(SeckillActivity::getStartTime, now)
                .le(SeckillActivity::getStartTime, notifyDeadline)
                .eq(SeckillActivity::getStatus, 0)
        );

        if (upcomingActivities.isEmpty()) {
            log.debug("没有即将开始的活动需要发送提醒");
            return;
        }

        int notifyCount = 0;
        for (SeckillActivity activity : upcomingActivities) {
            // 查询该活动的所有未提醒预约
            List<SeckillReservation> reservations = reservationMapper.selectList(
                new LambdaQueryWrapper<SeckillReservation>()
                    .eq(SeckillReservation::getActivityId, activity.getId())
                    .eq(SeckillReservation::getStatus, 0)
                    .eq(SeckillReservation::getNotified, false)
            );

            for (SeckillReservation reservation : reservations) {
                // 发送提醒
                sendNotification(reservation, activity);

                // 更新提醒状态
                reservation.setNotified(true);
                reservation.setNotifyTime(LocalDateTime.now());
                reservation.setStatus(1); // 1 = 已提醒
                reservationMapper.updateById(reservation);

                notifyCount++;
            }
        }

        if (notifyCount > 0) {
            log.info("发送预约提醒 {} 条", notifyCount);
        }
    }

    /**
     * 发送提醒通知
     *
     * 当前实现只是打印日志。
     * 实际可以扩展为：
     * - 发送站内信通知
     * - 发送短信
     * - 发送邮件
     * - 发送消息队列通知（如 RocketMQ）
     *
     * @param reservation 预约记录
     * @param activity 活动信息
     */
    private void sendNotification(SeckillReservation reservation, SeckillActivity activity) {
        // TODO: 实现实际的提醒通知逻辑
        // 例如：发送邮件、短信、站内信、消息队列等

        log.info("发送预约提醒：用户 {}，活动《{}》，开始时间 {}",
                reservation.getUserId(),
                activity.getName(),
                activity.getStartTime());
    }
}
