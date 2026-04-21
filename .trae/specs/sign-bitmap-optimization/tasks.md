# Tasks

- [x] Task 1: 修改 SignService 接口，添加 activityId 和 duration 参数
  - [x] SubTask 1.1: 在 SignService.java 中修改 verifySign 方法签名
  - [x] SubTask 1.2: 更新 SignResult 内部类文档

- [x] Task 2: 实现 Redis Bitmap 签名存储逻辑
  - [x] SubTask 2.1: 注入 StringRedisTemplate 依赖
  - [x] SubTask 2.2: 添加 MD5 哈希工具方法
  - [x] SubTask 2.3: 实现签名到 bit 位置的映射逻辑
  - [x] SubTask 2.4: 使用 setBit() 标记已使用签名
  - [x] SubTask 2.5: 使用 getBit() 检查签名状态
  - [x] SubTask 2.6: 使用 expire() 设置过期时间

- [x] Task 3: 更新 SignServiceImpl 实现
  - [x] SubTask 3.1: 修改 verifySign 方法，使用 StringRedisTemplate 的 Bitmap 操作
  - [x] SubTask 3.2: 实现活动级 Bitmap key 生成逻辑
  - [x] SubTask 3.3: 首次验证时设置 Bitmap 过期时间

- [x] Task 4: 修改现有单元测试
  - [x] SubTask 4.1: 更新 SignServiceTest 中所有 verifySign 调用，添加 activityId 和 duration 参数
  - [x] SubTask 4.2: 修改 tearDown 清理逻辑，清理 Bitmap key
  - [x] SubTask 4.3: 修改 addBitmapKey 方法，生成 Bitmap key 格式

- [x] Task 5: 新增单元测试用例
  - [x] SubTask 5.1: 新增测试 - 不同活动签名隔离（活动 A 签名不影响活动 B）
  - [x] SubTask 5.2: 新增测试 - Bitmap 过期时间设置验证
  - [x] SubTask 5.3: 新增测试 - MD5 映射不同签名到不同 bit 位置

- [x] Task 6: 验证和审查
  - [x] SubTask 6.1: 运行完整测试套件
  - [x] SubTask 6.2: 检查所有新增测试用例通过
  - [x] SubTask 6.3: 验证 Bitmap key 过期时间正确设置

# Task Dependencies
- Task 2 和 Task 3 可以并行开发
- Task 4 依赖 Task 1、Task 2、Task 3
- Task 5 依赖 Task 4
- Task 6 依赖 Task 5
