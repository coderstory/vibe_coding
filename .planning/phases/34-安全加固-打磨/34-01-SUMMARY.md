# Plan 01 Summary: 安全加固 + 页面状态打磨

## Completed

### Wave 1: Backend — API 权限控制 (TDD)
- JwtTokenProvider: generateToken 添加 role claim，新增 getRoleFromToken
- JwtAuthenticationFilter: 解析 JWT role 设置 ROLE_ADMIN/ROLE_USER
- SecurityConfig: /api/monitor/hardware/** 限制 ADMIN 角色
- AuthService: login/refreshToken 查 role_code 注入 JWT
- JwtTokenProviderTest: 5 个单元测试

### Wave 2: Frontend — 页面状态处理
- HardwareMonitorPage: el-skeleton 初始加载骨架态
- SSE 断连时顶部 el-alert 警告提示（"正在重连，n 秒后..."）
- 数据错误时 el-alert 错误提示
- composable 测试扩展：断连 error 格式 + 重连后清空

## Verification

- JwtTokenProviderTest: 5/5 PASS
- vitest useHardwareMetrics: 7/7 PASS (含重连测试)
- npm run build: BUILD SUCCESS
