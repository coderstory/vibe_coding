package cn.coderstory.springboot.seckill.service;

import lombok.Data;
import java.time.Duration;

public interface SignService {
    SignResult generateSign(Long userId, Long goodsId, String activitySignKey);

    boolean verifySign(String sign, long timestamp, Long activityId, Duration duration);

    @Data
    class SignResult {
        private final String sign;
        private final long timestamp;
    }
}
