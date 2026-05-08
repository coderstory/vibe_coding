package cn.coderstory.springboot.service.seckill;

import java.time.Duration;

/**
 * 秒杀签名验证服务接口。
 * <p>
 * 提供签名的生成和验证功能，用于防止秒杀请求被篡改和重放攻击。
 * 签名基于用户 ID、商品 ID 和活动密钥计算，附加时间戳防止重放。
 *
 * @since 1.7.0
 */
public interface SignService {

    /**
     * 生成秒杀请求签名。
     *
     * @param userId         用户 ID
     * @param goodsId        商品 ID
     * @param activitySignKey 活动签名密钥
     * @return 签名结果，包含签名值和生成时间戳
     */
    SignResult generateSign(Long userId, Long goodsId, String activitySignKey);

    /**
     * 验证秒杀请求签名的有效性。
     * <p>
     * 校验签名值是否匹配，同时检查时间戳是否在有效时长内。
     *
     * @param sign      签名值
     * @param timestamp 请求时间戳
     * @param activityId 活动 ID
     * @param duration  签名有效时长
     * @return 签名是否有效
     */
    boolean verifySign(String sign, long timestamp, Long activityId, Duration duration);

    record SignResult(String sign, long timestamp) {
    }
}
