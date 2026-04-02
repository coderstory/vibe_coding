---
name: "wechat-miniprogram"
description: "微信小程序开发专家，提供小程序项目结构、组件开发、API使用、最佳实践等指导。Invoke when user is working on WeChat Mini Program development, asking about WXML/WXSS/JS structure, or needs mini program specific help."
---

# 微信小程序开发专家

## 项目结构

微信小程序标准项目结构：

```
project/
├── app.js              # 小程序逻辑入口
├── app.json            # 全局配置
├── app.wxss            # 全局样式
├── project.config.json # 项目配置
├── sitemap.json        # 站点地图
├── pages/              # 页面目录
│   └── index/
│       ├── index.js    # 页面逻辑
│       ├── index.json  # 页面配置
│       ├── index.wxml  # 页面结构
│       └── index.wxss  # 页面样式
├── components/         # 自定义组件
│   └── my-component/
│       ├── my-component.js
│       ├── my-component.json
│       ├── my-component.wxml
│       └── my-component.wxss
├── utils/              # 工具函数
├── images/             # 图片资源
└── services/           # 服务层
```

## 核心文件说明

### app.json 全局配置
```json
{
  "pages": [
    "pages/index/index",
    "pages/logs/logs"
  ],
  "window": {
    "navigationBarTitleText": "小程序标题",
    "navigationBarBackgroundColor": "#fff",
    "navigationBarTextStyle": "black"
  },
  "tabBar": {
    "list": [
      {
        "pagePath": "pages/index/index",
        "text": "首页",
        "iconPath": "images/home.png",
        "selectedIconPath": "images/home-active.png"
      }
    ]
  }
}
```

### 页面生命周期

```javascript
Page({
  data: {
    message: 'Hello'
  },
  
  // 页面加载时触发
  onLoad(options) {
    console.log('页面加载', options)
  },
  
  // 页面初次渲染完成
  onReady() {
    console.log('页面渲染完成')
  },
  
  // 页面显示时触发
  onShow() {
    console.log('页面显示')
  },
  
  // 页面隐藏时触发
  onHide() {
    console.log('页面隐藏')
  },
  
  // 页面卸载时触发
  onUnload() {
    console.log('页面卸载')
  },
  
  // 下拉刷新
  onPullDownRefresh() {
    wx.stopPullDownRefresh()
  },
  
  // 上拉加载
  onReachBottom() {
    console.log('上拉加载更多')
  }
})
```

## WXML 模板语法

```xml
<!-- 数据绑定 -->
<view>{{message}}</view>

<!-- 条件渲染 -->
<view wx:if="{{condition}}">条件为真</view>
<view wx:elif="{{condition2}}">条件2为真</view>
<view wx:else>其他情况</view>

<!-- 列表渲染 -->
<view wx:for="{{items}}" wx:key="id" wx:for-item="item" wx:for-index="index">
  {{index}}: {{item.name}}
</view>

<!-- 事件绑定 -->
<button bindtap="handleTap">点击</button>
<button catchtap="handleCatch">阻止冒泡</button>

<!-- 模板 -->
<template name="myTemplate">
  <view>{{name}}</view>
</template>
<template is="myTemplate" data="{{name: 'test'}}"/>
```

## 常用 API

### 网络请求
```javascript
wx.request({
  url: 'https://api.example.com/data',
  method: 'GET',
  data: { key: 'value' },
  success: (res) => {
    console.log(res.data)
  },
  fail: (err) => {
    console.error(err)
  }
})
```

### 存储
```javascript
// 同步存储
wx.setStorageSync('key', 'value')
const value = wx.getStorageSync('key')

// 异步存储
wx.setStorage({
  key: 'key',
  data: 'value',
  success: () => {}
})
```

### 路由跳转
```javascript
// 保留当前页面跳转
wx.navigateTo({ url: '/pages/detail/detail?id=123' })

// 关闭当前页面跳转
wx.redirectTo({ url: '/pages/detail/detail' })

// 跳转到 tabBar 页面
wx.switchTab({ url: '/pages/index/index' })

// 返回上一页
wx.navigateBack({ delta: 1 })
```

## 自定义组件

```javascript
// components/my-component/my-component.js
Component({
  // 组件属性
  properties: {
    title: {
      type: String,
      value: '默认标题'
    }
  },
  
  // 组件内部数据
  data: {
    count: 0
  },
  
  // 生命周期
  lifetimes: {
    attached() {
      console.log('组件挂载')
    },
    detached() {
      console.log('组件卸载')
    }
  },
  
  // 方法
  methods: {
    handleClick() {
      this.triggerEvent('myevent', { value: 'test' })
    }
  }
})
```

## 最佳实践

1. **性能优化**
   - 使用 `wx:key` 优化列表渲染
   - 避免频繁调用 `setData`
   - 图片使用适当的尺寸和格式
   - 分包加载优化首屏速度

2. **代码组织**
   - 使用 Component 构造器创建可复用组件
   - 封装通用的工具函数到 utils
   - 使用 Behavior 共享代码逻辑

3. **注意事项**
   - 小程序包大小限制（2MB）
   - 单个分包/主包大小限制（2MB）
   - 所有 JS 代码运行在严格模式下
   - 不支持 DOM 操作
