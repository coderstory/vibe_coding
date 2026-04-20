package cn.coderstory.springboot.seckill.service;

import lombok.Data;

public interface SignService {
    SignResult generateSign(Long userId, Long goodsId, String activitySignKey);

    boolean verifySign(String sign, long timestamp);

    @Data
    class SignResult {
        private final String sign;
        private final long timestamp;
    }
}