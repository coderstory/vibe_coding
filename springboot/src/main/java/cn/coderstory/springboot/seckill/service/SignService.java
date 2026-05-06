package cn.coderstory.springboot.seckill.service;

import java.time.Duration;

public interface SignService {
    SignResult generateSign(Long userId, Long goodsId, String activitySignKey);

    boolean verifySign(String sign, long timestamp, Long activityId, Duration duration);

    record SignResult(String sign, long timestamp) {
    }
}
