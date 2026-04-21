# 秒杀系统管理页面开发计划

## 问题分析

当前秒杀系统缺少完整的管理功能，根据设计文档（2026-04-20-seckill-system-design.md），系统需要以下核心功能：

## 功能清单

### 1. 后端接口

#### 1.1 商品管理接口 (GoodsController)
| 方法 | 路径 | 功能 |
|------|------|------|
| GET | /api/goods | 获取商品列表（分页） |
| GET | /api/goods/{id} | 获取商品详情 |
| POST | /api/goods | 创建商品 |
| PUT | /api/goods/{id} | 更新商品 |
| DELETE | /api/goods/{id} | 删除商品 |
| GET | /api/goods/activity/{activityId} | 获取活动下的商品列表 |

#### 1.2 活动管理接口 (SeckillActivityController)
| 方法 | 路径 | 功能 |
|------|------|------|
| GET | /api/seckill/activity | 获取活动列表（分页） |
| GET | /api/seckill/activity/{id} | 获取活动详情 |
| POST | /api/seckill/activity | 创建活动 |
| PUT | /api/seckill/activity/{id} | 更新活动 |
| DELETE | /api/seckill/activity/{id} | 删除活动 |
| POST | /api/seckill/activity/{id}/publish | 发布活动（触发预热） |

#### 1.3 数据预热接口 (PreheatController)
| 方法 | 路径 | 功能 |
|------|------|------|
| POST | /api/seckill/preheat/{activityId} | 手动触发活动预热 |
| GET | /api/seckill/preheat/status/{activityId} | 获取预热状态 |

### 2. 前端页面

#### 2.1 商品管理
- **GoodsList.vue** - 商品列表（分页、搜索、筛选）
- **GoodsForm.vue** - 商品表单（新增/编辑）

#### 2.2 活动管理
- **ActivityList.vue** - 活动列表（分页、状态筛选）
- **ActivityForm.vue** - 活动表单（新增/编辑、关联商品）

### 3. 路由配置

| 路由 | 组件 | 说明 |
|------|------|------|
| /seckill/goods | GoodsList.vue | 商品管理 |
| /seckill/goods/add | GoodsForm.vue | 新增商品 |
| /seckill/goods/:id | GoodsForm.vue | 编辑商品 |
| /seckill/activity | ActivityList.vue | 活动管理 |
| /seckill/activity/add | ActivityForm.vue | 新增活动 |
| /seckill/activity/:id | ActivityForm.vue | 编辑活动 |

### 4. 菜单数据 (V19)

| 菜单ID | 父菜单 | 菜单名称 | 路由路径 |
|--------|--------|----------|----------|
| 16 | 15 | 商品管理 | /seckill/goods |
| 17 | 15 | 活动管理 | /seckill/activity |

### 5. 现有功能检查

#### 已实现的功能 ✅
- SeckillController - 抢购接口
- SeckillService - 抢购服务
- PreheatService - 数据预热服务
- MonitorController - 监控接口
- SeckillIndex.vue - 秒杀首页
- SeckillDetail.vue - 秒杀详情
- SeckillRecord.vue - 抢购记录
- SeckillCart.vue - 购物车
- OrderList.vue - 订单列表
- MonitorDashboard.vue - 监控大盘

#### 缺少的功能 ❌
- GoodsController - 商品管理接口
- GoodsService - 商品服务
- GoodsMapper - 商品 Mapper
- SeckillActivityController - 完整的活动管理接口
- 商品管理页面（GoodsList、GoodsForm）
- 活动管理页面（ActivityList、ActivityForm）
- 菜单数据（商品管理、活动管理）

## 实施步骤

### Phase 1: 后端接口
1. 创建 SeckillGoodsMapper
2. 创建 GoodsMapper.xml
3. 创建 GoodsService 和 GoodsServiceImpl
4. 创建 GoodsController
5. 扩展 SeckillActivityController（添加完整 CRUD）
6. 检查并完善 PreheatService

### Phase 2: 前端 API
1. 创建 api/goods.ts
2. 更新 api/seckill.ts（添加活动管理接口）

### Phase 3: 前端页面
1. 创建 views/seckill/goods/GoodsList.vue
2. 创建 views/seckill/goods/GoodsForm.vue
3. 创建 views/seckill/activity/ActivityList.vue
4. 创建 views/seckill/activity/ActivityForm.vue

### Phase 4: 路由和菜单
1. 更新 router/index.ts
2. 创建 Flyway V19 迁移脚本（菜单数据）

### Phase 5: 测试
1. 运行单元测试
2. 手动测试功能

## 文件清单

### 后端
```
springboot/src/main/java/cn/coderstory/springboot/seckill/
├── controller/
│   └── GoodsController.java          # 新增
├── service/
│   ├── GoodsService.java             # 新增
│   └── impl/GoodsServiceImpl.java   # 新增
├── mapper/
│   ├── SeckillGoodsMapper.java      # 已有，检查
│   └── SeckillActivityMapper.java   # 已有，检查
└── resources/mapper/
    └── GoodsMapper.xml              # 新增
```

### 前端
```
app-vue/src/
├── api/
│   └── goods.ts                    # 新增
└── views/seckill/
    ├── goods/
    │   ├── GoodsList.vue           # 新增
    │   └── GoodsForm.vue           # 新增
    └── activity/
        ├── ActivityList.vue        # 新增
        └── ActivityForm.vue        # 新增
```

### 数据库
```
springboot/src/main/resources/db/migration/
└── V19__seckill_goods_activity_menu.sql  # 新增菜单
```

## 数据库表检查

确认以下表是否存在：
- `seckill_goods` - 秒杀商品表
- `seckill_activity` - 秒杀活动表
- `seckill_queue` - 排队记录表
- `seckill_order` - 秒杀订单表
- `seckill_reservation` - 预约表
