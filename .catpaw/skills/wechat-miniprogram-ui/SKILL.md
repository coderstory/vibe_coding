---
name: "wechat-miniprogram-ui"
description: "微信小程序 UI 组件库使用指南，包括 Vant Weapp、WeUI 等流行组件库的安装和用法。Invoke when user needs to use UI component libraries in WeChat Mini Program or asks about mini program UI design."
---

# 微信小程序 UI 组件库

## Vant Weapp

### 安装

```bash
# 通过 npm 安装
npm i @vant/weapp -S --production

# 在 project.config.json 中添加配置
{
  "setting": {
    "packNpmManually": true,
    "packNpmRelationList": [
      {
        "packageJsonPath": "./package.json",
        "miniprogramNpmDistDir": "./"
      }
    ]
  }
}
```

### 使用组件

```json
// app.json 或页面 json
{
  "usingComponents": {
    "van-button": "@vant/weapp/button/index",
    "van-cell": "@vant/weapp/cell/index",
    "van-cell-group": "@vant/weapp/cell-group/index",
    "van-icon": "@vant/weapp/icon/index",
    "van-image": "@vant/weapp/image/index",
    "van-loading": "@vant/weapp/loading/index",
    "van-nav-bar": "@vant/weapp/nav-bar/index",
    "van-notice-bar": "@vant/weapp/notice-bar/index",
    "van-panel": "@vant/weapp/panel/index",
    "van-popup": "@vant/weapp/popup/index",
    "van-toast": "@vant/weapp/toast/index",
    "van-dialog": "@vant/weapp/dialog/index",
    "van-field": "@vant/weapp/field/index",
    "van-search": "@vant/weapp/search/index",
    "van-stepper": "@vant/weapp/stepper/index",
    "van-radio": "@vant/weapp/radio/index",
    "van-radio-group": "@vant/weapp/radio-group/index",
    "van-checkbox": "@vant/weapp/checkbox/index",
    "van-checkbox-group": "@vant/weapp/checkbox-group/index",
    "van-switch": "@vant/weapp/switch/index",
    "van-slider": "@vant/weapp/slider/index",
    "van-datetime-picker": "@vant/weapp/datetime-picker/index",
    "van-rate": "@vant/weapp/rate/index",
    "van-picker": "@vant/weapp/picker/index",
    "van-uploader": "@vant/weapp/uploader/index",
    "van-tabs": "@vant/weapp/tabs/index",
    "van-tab": "@vant/weapp/tab/index",
    "van-tag": "@vant/weapp/tag/index",
    "van-grid": "@vant/weapp/grid/index",
    "van-grid-item": "@vant/weapp/grid-item/index",
    "van-card": "@vant/weapp/card/index",
    "van-submit-bar": "@vant/weapp/submit-bar/index",
    "van-goods-action": "@vant/weapp/goods-action/index",
    "van-goods-action-icon": "@vant/weapp/goods-action-icon/index",
    "van-goods-action-button": "@vant/weapp/goods-action-button/index",
    "van-swipe-cell": "@vant/weapp/swipe-cell/index",
    "van-collapse": "@vant/weapp/collapse/index",
    "van-collapse-item": "@vant/weapp/collapse-item/index",
    "van-count-down": "@vant/weapp/count-down/index",
    "van-divider": "@vant/weapp/divider/index",
    "van-empty": "@vant/weapp/empty/index",
    "van-notify": "@vant/weapp/notify/index",
    "van-skeleton": "@vant/weapp/skeleton/index",
    "van-steps": "@vant/weapp/steps/index",
    "van-sticky": "@vant/weapp/sticky/index",
    "van-swipe": "@vant/weapp/swipe/index",
    "van-swipe-item": "@vant/weapp/swipe-item/index",
    "van-tree-select": "@vant/weapp/tree-select/index",
    "van-area": "@vant/weapp/area/index",
    "van-share-sheet": "@vant/weapp/share-sheet/index",
    "van-dropdown-menu": "@vant/weapp/dropdown-menu/index",
    "van-dropdown-item": "@vant/weapp/dropdown-item/index",
    "van-index-bar": "@vant/weapp/index-bar/index",
    "van-index-anchor": "@vant/weapp/index-anchor/index",
    "van-calendar": "@vant/weapp/calendar/index",
    "van-cascader": "@vant/weapp/cascader/index",
    "van-config-provider": "@vant/weapp/config-provider/index"
  }
}
```

### 常用组件示例

#### Button 按钮
```xml
<van-button type="primary">主要按钮</van-button>
<van-button type="info">信息按钮</van-button>
<van-button type="default">默认按钮</van-button>
<van-button type="warning">警告按钮</van-button>
<van-button type="danger">危险按钮</van-button>

<van-button plain type="primary">朴素按钮</van-button>
<van-button disabled type="primary">禁用状态</van-button>
<van-button loading type="primary" loading-text="加载中..." />
<van-button icon="star-o" type="primary">图标按钮</van-button>
<van-button block type="primary">块级元素</van-button>
<van-button round type="primary">圆形按钮</van-button>
<van-button square type="primary">方形按钮</van-button>
<van-button size="large">大号按钮</van-button>
<van-button size="small">小号按钮</van-button>
<van-button size="mini">迷你按钮</van-button>
```

#### Cell 单元格
```xml
<van-cell-group>
  <van-cell title="单元格" value="内容" />
  <van-cell title="单元格" value="内容" label="描述信息" />
  <van-cell title="单元格" is-link />
  <van-cell title="单元格" is-link value="内容" arrow-direction="down" />
  <van-cell title="单元格" icon="location-o" />
  <van-cell title="单元格" center>
    <van-tag type="danger">标签</van-tag>
  </van-cell>
</van-cell-group>
```

#### Field 输入框
```xml
<van-cell-group>
  <van-field
    value="{{ username }}"
    label="用户名"
    placeholder="请输入用户名"
    bind:change="onUsernameChange"
  />
  <van-field
    value="{{ password }}"
    type="password"
    label="密码"
    placeholder="请输入密码"
    required
    border="{{ false }}"
  />
  <van-field
    value="{{ message }}"
    label="留言"
    type="textarea"
    placeholder="请输入留言"
    autosize
    maxlength="100"
    show-word-limit
  />
</van-cell-group>
```

#### Toast 轻提示
```javascript
import Toast from '@vant/weapp/toast/toast';

// 文字提示
Toast('提示内容');

// 加载提示
Toast.loading({
  message: '加载中...',
  forbidClick: true,
});

// 成功提示
Toast.success('成功文案');

// 失败提示
Toast.fail('失败文案');
```

#### Dialog 弹窗
```javascript
import Dialog from '@vant/weapp/dialog/dialog';

Dialog.alert({
  title: '标题',
  message: '弹窗内容',
}).then(() => {
  // on close
});

Dialog.confirm({
  title: '标题',
  message: '弹窗内容',
})
  .then(() => {
    // on confirm
  })
  .catch(() => {
    // on cancel
  });
```

#### Popup 弹出层
```xml
<van-popup
  show="{{ show }}"
  position="bottom"
  round
  bind:close="onClose"
>
  <view style="padding: 20px;">内容</view>
</van-popup>
```

#### Tabs 标签页
```xml
<van-tabs active="{{ active }}" bind:change="onChange">
  <van-tab title="标签 1">内容 1</van-tab>
  <van-tab title="标签 2">内容 2</van-tab>
  <van-tab title="标签 3">内容 3</van-tab>
</van-tabs>
```

#### Search 搜索
```xml
<van-search
  value="{{ value }}"
  placeholder="请输入搜索关键词"
  show-action
  bind:search="onSearch"
  bind:cancel="onCancel"
/>
```

#### Swipe 轮播
```xml
<van-swipe class="my-swipe" autoplay="{{ 3000 }}" indicator-color="white">
  <van-swipe-item>1</van-swipe-item>
  <van-swipe-item>2</van-swipe-item>
  <van-swipe-item>3</van-swipe-item>
  <van-swipe-item>4</van-swipe-item>
</van-swipe>
```

## WeUI

### 安装

```bash
npm install weui-miniprogram
```

### 使用

```json
// app.json
{
  "useExtendedLib": {
    "weui": true
  }
}
```

### 常用组件

```xml
<!-- 基础组件 -->
<mp-icon icon="add" color="black" size="{{25}}"></mp-icon>
<mp-badge content="9" style="margin-left: 5px;"></mp-badge>
<mp-loading type="circle"></mp-loading>
<mp-loading type="dot-gray"></mp-loading>

<!-- 表单组件 -->
<mp-form id="form" rules="{{rules}}" models="{{formData}}">
  <mp-cells title="表单">
    <mp-cell prop="name" title="姓名">
      <input bindinput="formInputChange" data-field="name" class="weui-input" placeholder="请输入姓名"/>
    </mp-cell>
  </mp-cells>
</mp-form>

<!-- 列表组件 -->
<mp-cells title="列表">
  <mp-cell value="内容" footer="说明文字"></mp-cell>
  <mp-cell>
    <view>标题文字</view>
    <view slot="footer">说明文字</view>
  </mp-cell>
</mp-cells>

<!-- 操作反馈 -->
<mp-half-screen-dialog
  show="{{showDialog}}"
  closabled="{{true}}"
  title="标题"
  desc="说明文字"
  bindbuttontap="buttontap"
  buttons="{{buttons}}"
>
</mp-half-screen-dialog>

<mp-action-sheet
  show="{{showActionsheet}}"
  actions="{{groups}}"
  title="这是一个标题，可以为一行或者两行。"
  bindclose="close"
  bindselect="btnClick"
>
</mp-action-sheet>

<!-- 导航组件 -->
<mp-navigation-bar
  title="WeUI"
  back="{{true}}"
  home="{{true}}"
  bindback="back"
  bindhome="home"
></mp-navigation-bar>

<mp-tabbar
  style="position:fixed;bottom:0;width:100%;left:0;right:0;"
  list="{{list}}"
  bindchange="tabChange"
></mp-tabbar>
```

## 自定义主题

### Vant Weapp 主题定制

```css
/* app.wxss */
page {
  --button-primary-background-color: #07c160;
  --button-primary-border-color: #07c160;
  --cell-text-color: #323233;
  --cell-label-color: #969799;
  --cell-value-color: #969799;
  --toast-max-width: 200px;
  --toast-background-color: rgba(0, 0, 0, 0.7);
  --toast-text-color: #fff;
}
```

## 响应式设计

```css
/* 使用 rpx 进行自适应布局 */
.container {
  width: 750rpx; /* 全屏宽度 */
  padding: 20rpx;
}

.card {
  width: 710rpx;
  margin: 0 auto;
  border-radius: 16rpx;
  background: #fff;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.1);
}

/* 安全区域适配 */
.safe-area-bottom {
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}
```
