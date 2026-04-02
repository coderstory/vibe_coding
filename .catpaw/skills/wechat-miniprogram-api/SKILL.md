---
name: "wechat-miniprogram-api"
description: "微信小程序 API 使用指南，包括网络请求、存储、媒体、位置、设备信息、开放能力等常用 API。Invoke when user needs to use WeChat Mini Program APIs like network requests, storage, media, location, device info, or open capabilities."
---

# 微信小程序 API 使用指南

## 网络请求

### wx.request
```javascript
// 基础 GET 请求
wx.request({
  url: 'https://api.example.com/data',
  method: 'GET',
  data: { key: 'value' },
  header: {
    'content-type': 'application/json'
  },
  success: (res) => {
    console.log(res.data)
  },
  fail: (err) => {
    console.error('请求失败', err)
  },
  complete: () => {
    console.log('请求完成')
  }
})

// POST 请求
wx.request({
  url: 'https://api.example.com/submit',
  method: 'POST',
  data: {
    name: 'test',
    value: 123
  },
  header: {
    'content-type': 'application/json'
  },
  success: (res) => {
    if (res.statusCode === 200) {
      console.log('提交成功', res.data)
    }
  }
})

// 封装请求工具
const request = (options) => {
  return new Promise((resolve, reject) => {
    wx.request({
      ...options,
      success: (res) => {
        if (res.statusCode >= 200 && res.statusCode < 300) {
          resolve(res.data)
        } else {
          reject(new Error(`HTTP ${res.statusCode}`))
        }
      },
      fail: reject
    })
  })
}

// 使用 Promise
request({
  url: 'https://api.example.com/data',
  method: 'GET'
}).then(data => {
  console.log(data)
}).catch(err => {
  console.error(err)
})
```

### 下载文件
```javascript
wx.downloadFile({
  url: 'https://example.com/file.pdf',
  success: (res) => {
    const filePath = res.tempFilePath
    // 保存文件或预览
    wx.openDocument({
      filePath: filePath,
      success: () => {
        console.log('打开文档成功')
      }
    })
  }
})
```

### 上传文件
```javascript
wx.chooseImage({
  success: (res) => {
    const tempFilePaths = res.tempFilePaths
    wx.uploadFile({
      url: 'https://api.example.com/upload',
      filePath: tempFilePaths[0],
      name: 'file',
      formData: {
        user: 'test'
      },
      success: (res) => {
        const data = JSON.parse(res.data)
        console.log('上传成功', data)
      }
    })
  }
})
```

## 数据存储

### 同步存储
```javascript
// 存储数据
wx.setStorageSync('userInfo', { name: '张三', age: 25 })
wx.setStorageSync('token', 'abc123')

// 读取数据
const userInfo = wx.getStorageSync('userInfo')
const token = wx.getStorageSync('token')

// 删除数据
wx.removeStorageSync('token')

// 清空所有数据
wx.clearStorageSync()
```

### 异步存储
```javascript
// 存储
wx.setStorage({
  key: 'userInfo',
  data: { name: '张三', age: 25 },
  success: () => {
    console.log('存储成功')
  }
})

// 读取
wx.getStorage({
  key: 'userInfo',
  success: (res) => {
    console.log('读取成功', res.data)
  }
})

// 删除
wx.removeStorage({
  key: 'userInfo',
  success: () => {
    console.log('删除成功')
  }
})

// 获取所有缓存信息
wx.getStorageInfo({
  success: (res) => {
    console.log('当前占用空间', res.currentSize)
    console.log('限制空间', res.limitSize)
    console.log('所有key', res.keys)
  }
})
```

## 路由导航

```javascript
// 保留当前页面，跳转到应用内的某个页面
wx.navigateTo({
  url: '/pages/detail/detail?id=123&name=test',
  success: () => {
    console.log('跳转成功')
  }
})

// 关闭当前页面，跳转到应用内的某个页面
wx.redirectTo({
  url: '/pages/detail/detail'
})

// 跳转到 tabBar 页面，并关闭其他所有非 tabBar 页面
wx.switchTab({
  url: '/pages/index/index'
})

// 关闭所有页面，打开到应用内的某个页面
wx.reLaunch({
  url: '/pages/index/index'
})

// 关闭当前页面，返回上一页面或多级页面
wx.navigateBack({
  delta: 1 // 返回的页面数，如果 delta 大于现有页面数，则返回到首页
})

// 获取当前页面栈
const pages = getCurrentPages()
const currentPage = pages[pages.length - 1]
console.log('当前页面路径', currentPage.route)
console.log('页面参数', currentPage.options)
```

## 媒体相关

### 图片
```javascript
// 选择图片
wx.chooseImage({
  count: 9, // 最多选择数量
  sizeType: ['original', 'compressed'], // 原图或压缩图
  sourceType: ['album', 'camera'], // 相册或相机
  success: (res) => {
    const tempFilePaths = res.tempFilePaths
    console.log('选择的图片', tempFilePaths)
  }
})

// 预览图片
wx.previewImage({
  current: 'https://example.com/image1.jpg', // 当前显示图片的链接
  urls: [
    'https://example.com/image1.jpg',
    'https://example.com/image2.jpg'
  ]
})

// 获取图片信息
wx.getImageInfo({
  src: 'https://example.com/image.jpg',
  success: (res) => {
    console.log('图片宽度', res.width)
    console.log('图片高度', res.height)
    console.log('图片路径', res.path)
  }
})

// 保存图片到相册
wx.saveImageToPhotosAlbum({
  filePath: 'tempFilePath',
  success: () => {
    console.log('保存成功')
  }
})
```

### 录音
```javascript
// 开始录音
const recorderManager = wx.getRecorderManager()

recorderManager.onStart(() => {
  console.log('开始录音')
})

recorderManager.onStop((res) => {
  console.log('录音结束', res.tempFilePath)
  console.log('录音时长', res.duration)
  console.log('文件大小', res.fileSize)
})

recorderManager.start({
  duration: 60000, // 最大录音时长，单位ms
  sampleRate: 44100,
  numberOfChannels: 1,
  encodeBitRate: 192000,
  format: 'aac'
})

// 停止录音
recorderManager.stop()
```

### 音频播放
```javascript
const innerAudioContext = wx.createInnerAudioContext()
innerAudioContext.src = 'https://example.com/music.mp3'
innerAudioContext.autoplay = true

innerAudioContext.onPlay(() => {
  console.log('开始播放')
})

innerAudioContext.onError((res) => {
  console.log('播放错误', res.errMsg)
})

// 控制播放
innerAudioContext.play()
innerAudioContext.pause()
innerAudioContext.stop()
innerAudioContext.seek(30) // 跳转到30秒处

// 获取播放状态
console.log('当前播放位置', innerAudioContext.currentTime)
console.log('音频时长', innerAudioContext.duration)
console.log('是否暂停', innerAudioContext.paused)
```

### 视频
```javascript
// 选择视频
wx.chooseVideo({
  sourceType: ['album', 'camera'],
  maxDuration: 60,
  camera: 'back',
  success: (res) => {
    console.log('视频临时路径', res.tempFilePath)
    console.log('视频时长', res.duration)
    console.log('视频大小', res.size)
    console.log('视频宽高', res.width, res.height)
  }
})

// 保存视频到相册
wx.saveVideoToPhotosAlbum({
  filePath: 'videoPath',
  success: () => {
    console.log('保存成功')
  }
})
```

## 位置相关

```javascript
// 获取当前位置
wx.getLocation({
  type: 'wgs84', // 坐标系类型
  success: (res) => {
    const latitude = res.latitude
    const longitude = res.longitude
    const speed = res.speed
    const accuracy = res.accuracy
    console.log('纬度', latitude)
    console.log('经度', longitude)
  }
})

// 打开地图选择位置
wx.chooseLocation({
  success: (res) => {
    console.log('位置名称', res.name)
    console.log('详细地址', res.address)
    console.log('纬度', res.latitude)
    console.log('经度', res.longitude)
  }
})

// 查看位置
wx.openLocation({
  latitude: 23.099994,
  longitude: 113.324520,
  name: 'T.I.T 创意园',
  address: '广州市海珠区新港中路397号'
})
```

## 设备信息

```javascript
// 获取系统信息
wx.getSystemInfo({
  success: (res) => {
    console.log('手机型号', res.model)
    console.log('设备像素比', res.pixelRatio)
    console.log('屏幕宽度', res.screenWidth)
    console.log('屏幕高度', res.screenHeight)
    console.log('窗口宽度', res.windowWidth)
    console.log('窗口高度', res.windowHeight)
    console.log('状态栏高度', res.statusBarHeight)
    console.log('微信版本号', res.version)
    console.log('操作系统版本', res.system)
    console.log('客户端平台', res.platform)
    console.log('基础库版本', res.SDKVersion)
  }
})

// 同步获取
const systemInfo = wx.getSystemInfoSync()

// 获取网络状态
wx.getNetworkType({
  success: (res) => {
    console.log('网络类型', res.networkType) // wifi/4g/3g/2g/offline
  }
})

// 监听网络状态变化
wx.onNetworkStatusChange((res) => {
  console.log('网络是否连接', res.isConnected)
  console.log('网络类型', res.networkType)
})

// 拨打电话
wx.makePhoneCall({
  phoneNumber: '10086'
})

// 扫码
wx.scanCode({
  onlyFromCamera: false, // 是否只允许相机扫码
  scanType: ['qrCode', 'barCode'], // 扫码类型
  success: (res) => {
    console.log('扫码结果', res.result)
    console.log('扫码类型', res.scanType)
  }
})

// 剪贴板
wx.setClipboardData({
  data: '复制的内容',
  success: () => {
    wx.getClipboardData({
      success: (res) => {
        console.log('剪贴板内容', res.data)
      }
    })
  }
})

// 屏幕亮度
wx.setScreenBrightness({
  value: 0.5 // 0-1
})

wx.getScreenBrightness({
  success: (res) => {
    console.log('屏幕亮度', res.value)
  }
})

// 振动
wx.vibrateShort() // 短振动
wx.vibrateLong()  // 长振动
```

## 开放能力

### 登录
```javascript
// 登录获取 code
wx.login({
  success: (res) => {
    if (res.code) {
      // 发送 res.code 到后台换取 openId, sessionKey, unionId
      console.log('登录凭证', res.code)
    }
  }
})

// 检查登录态
wx.checkSession({
  success: () => {
    // session 未过期
    console.log('登录态有效')
  },
  fail: () => {
    // session 已过期，需要重新登录
    console.log('登录态过期，需要重新登录')
  }
})
```

### 获取用户信息
```javascript
// 获取用户信息（需要用户授权）
wx.getUserProfile({
  desc: '用于完善会员资料',
  success: (res) => {
    console.log('用户信息', res.userInfo)
    console.log('昵称', res.userInfo.nickName)
    console.log('头像', res.userInfo.avatarUrl)
    console.log('性别', res.userInfo.gender) // 0未知 1男 2女
    console.log('国家', res.userInfo.country)
    console.log('省份', res.userInfo.province)
    console.log('城市', res.userInfo.city)
  }
})
```

### 分享
```javascript
// 页面内分享
Page({
  onShareAppMessage: function () {
    return {
      title: '分享标题',
      path: '/pages/index/index?id=123',
      imageUrl: '/images/share.png'
    }
  },
  
  onShareTimeline: function () {
    return {
      title: '分享到朋友圈标题',
      query: 'id=123'
    }
  }
})

// 主动触发分享
wx.showShareMenu({
  withShareTicket: true,
  menus: ['shareAppMessage', 'shareTimeline']
})

// 隐藏分享按钮
wx.hideShareMenu({
  menus: ['shareAppMessage', 'shareTimeline']
})
```

### 支付
```javascript
wx.requestPayment({
  timeStamp: '1234567890',
  nonceStr: '随机字符串',
  package: 'prepay_id=xxx',
  signType: 'RSA',
  paySign: '签名',
  success: (res) => {
    console.log('支付成功')
  },
  fail: (err) => {
    console.log('支付失败', err)
  }
})
```

### 订阅消息
```javascript
// 订阅消息
wx.requestSubscribeMessage({
  tmplIds: ['模板ID1', '模板ID2'],
  success: (res) => {
    console.log('订阅结果', res)
    // res[模板ID] 的值为 'accept'、'reject'、'ban'、'filter'
  }
})
```

### 客服消息
```xml
<button open-type="contact" bindcontact="handleContact">联系客服</button>
```

```javascript
Page({
  handleContact(e) {
    console.log('客服消息路径', e.detail.path)
    console.log('客服消息查询参数', e.detail.query)
  }
})
```

## 界面交互

```javascript
// 显示加载提示
wx.showLoading({
  title: '加载中',
  mask: true // 是否显示透明蒙层
})

// 隐藏加载提示
wx.hideLoading()

// 显示 Toast 提示
wx.showToast({
  title: '成功',
  icon: 'success', // success/error/loading/none
  duration: 2000
})

// 隐藏 Toast
wx.hideToast()

// 显示模态对话框
wx.showModal({
  title: '提示',
  content: '这是一个模态弹窗',
  showCancel: true,
  cancelText: '取消',
  confirmText: '确定',
  success: (res) => {
    if (res.confirm) {
      console.log('用户点击确定')
    } else if (res.cancel) {
      console.log('用户点击取消')
    }
  }
})

// 显示操作菜单
wx.showActionSheet({
  itemList: ['选项A', '选项B', '选项C'],
  success: (res) => {
    console.log('用户选择了第' + (res.tapIndex + 1) + '个选项')
  }
})

// 设置导航栏标题
wx.setNavigationBarTitle({
  title: '新标题'
})

// 设置导航栏颜色
wx.setNavigationBarColor({
  frontColor: '#ffffff',
  backgroundColor: '#ff0000'
})

// 显示/隐藏导航栏加载动画
wx.showNavigationBarLoading()
wx.hideNavigationBarLoading()

// 页面滚动到指定位置
wx.pageScrollTo({
  scrollTop: 0,
  duration: 300
})

// 下拉刷新
Page({
  onPullDownRefresh: function () {
    // 处理下拉刷新
    wx.stopPullDownRefresh() // 停止下拉刷新动画
  }
})

// 启用下拉刷新
wx.startPullDownRefresh()

// 创建动画
const animation = wx.createAnimation({
  duration: 1000,
  timingFunction: 'ease',
})

animation.scale(2, 2).rotate(45).step()

this.setData({
  animationData: animation.export()
})
```

## 文件系统

```javascript
const fs = wx.getFileSystemManager()

// 读取文件
fs.readFile({
  filePath: 'filePath',
  encoding: 'utf8',
  success: (res) => {
    console.log('文件内容', res.data)
  }
})

// 写入文件
fs.writeFile({
  filePath: 'filePath',
  data: '写入的内容',
  encoding: 'utf8',
  success: () => {
    console.log('写入成功')
  }
})

// 删除文件
fs.unlink({
  filePath: 'filePath',
  success: () => {
    console.log('删除成功')
  }
})

// 创建目录
fs.mkdir({
  dirPath: 'directoryPath',
  recursive: true,
  success: () => {
    console.log('创建目录成功')
  }
})

// 读取目录
fs.readdir({
  dirPath: 'directoryPath',
  success: (res) => {
    console.log('目录内容', res.files)
  }
})

// 获取文件信息
fs.getFileInfo({
  filePath: 'filePath',
  success: (res) => {
    console.log('文件大小', res.size)
  }
})
```
