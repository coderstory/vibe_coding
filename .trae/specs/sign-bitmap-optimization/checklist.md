# Checklist

## 接口变更
- [x] SignService 接口 verifySign 方法添加 activityId 和 duration 参数
- [x] SignService 接口文档更新说明使用 Redis Bitmap 存储方案

## 核心实现
- [x] SignServiceImpl 注入 StringRedisTemplate
- [x] SignServiceImpl 添加 MD5 哈希工具方法（签名字符串 -> long bitOffset）
- [x] SignServiceImpl 使用 opsForValue().getBit() 检查签名状态
- [x] SignServiceImpl 使用 opsForValue().setBit() 标记已使用签名
- [x] SignServiceImpl 使用 expire() 设置活动时长作为过期时间
- [x] Bitmap key 格式：`seckill:sign:bitmap:{activityId}`

## 现有测试修改
- [x] SignServiceTest 中所有 verifySign 调用添加 activityId 和 duration 参数
- [x] tearDown 方法清理 Bitmap key（格式：`seckill:sign:bitmap:{activityId}`）
- [x] addBitmapKey 方法更新为生成 Bitmap key 格式

## 新增测试用例
- [x] 测试不同活动签名隔离（活动 A 签名不影响活动 B 验证结果）
- [x] 测试 Bitmap 过期时间正确设置
- [x] 测试不同签名映射到不同 bit 位置

## 测试验证
- [x] SignServiceTest 所有测试通过
- [x] 所有 67 个测试通过
