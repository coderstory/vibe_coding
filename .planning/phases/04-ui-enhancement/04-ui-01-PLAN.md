---
phase: 04-ui-enhancement
plan: 01
type: execute
wave: 1
depends_on: []
files_modified:
  - app-vue/package.json
  - app-vue/src/main.js
autonomous: true
requirements:
  - UI-THEME-01
  - UI-THEME-02
  - UI-GLASS-01
  - UI-DYNAMIC-01
user_setup: []
must_haves:
  truths:
    - 用户可以切换亮色/暗色主题
    - 主题偏好存储在后端，用户下次登录保持
    - 登录页具有液态玻璃效果
    - 管理后台具有现代动态视觉效果
  artifacts:
    - path: "app-vue/src/stores/theme.js"
      provides: "主题状态管理"
    - path: "app-vue/src/views/Login.vue"
      provides: "新设计的登录页（液态玻璃+动态背景）"
    - path: "app-vue/src/views/Layout.vue"
      provides: "新设计的管理后台布局"
---

<objective>
使用 Vuestic UI 升级前端，实现用户级别主题切换和现代 UI 效果。
</objective>

<context>
@.planning/phases/04-ui-enhancement/04-ui-CONTEXT.md
@.planning/phases/04-ui-enhancement/04-ui-RESEARCH.md
@./AGENTS.md
</context>

<tasks>

<task type="auto">
  <name>Task 1: 安装 Vuestic UI</name>
  <files>app-vue/package.json</files>
  <read_first>app-vue/package.json</read_first>
  <action>
在 app-vue 目录安装 Vuestic UI:

```bash
cd app-vue
npm install vuestic-ui
```
  </action>
  <verify>
<code>grep -q "vuestic-ui" app-vue/package.json && echo "PASS" || echo "FAIL"</code>
  </verify>
  <done>Vuestic UI 已安装</done>
</task>

<task type="auto">
  <name>Task 2: 创建主题 Store</name>
  <files>app-vue/src/stores/theme.js</files>
  <read_first>app-vue/src/stores/user.js</read_first>
  <action>
创建主题状态管理 store (app-vue/src/stores/theme.js):

```javascript
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useThemeStore = defineStore('theme', () => {
  const theme = ref('light')
  
  const isDark = computed(() => theme.value === 'dark')
  
  function setTheme(newTheme) {
    theme.value = newTheme
    applyTheme(newTheme)
  }
  
  function toggleTheme() {
    const newTheme = theme.value === 'light' ? 'dark' : 'light'
    setTheme(newTheme)
  }
  
  function applyTheme(themeName) {
    document.documentElement.setAttribute('data-theme', themeName)
  }
  
  return { theme, isDark, setTheme, toggleTheme, applyTheme }
})
```

包名: app-vue/src/stores/
  </action>
  <verify>
<code>ls app-vue/src/stores/theme.js && echo "PASS" || echo "FAIL"</code>
  </verify>
  <done>主题 Store 已创建</done>
</task>

<task type="auto">
  <name>Task 3: 更新 main.js 引入 Vuestic</name>
  <files>app-vue/src/main.js</files>
  <read_first>app-vue/src/main.js</read_first>
  <action>
更新 main.js 引入 Vuestic UI:

```javascript
import './assets/main.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'

import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

import Vuestic from 'vuestic-ui'
import 'vuestic-ui/styles'

const app = createApp(App)
const pinia = createPinia()

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(pinia)
app.use(router)
app.use(ElementPlus)
app.use(Vuestic)

app.mount('#app')
```

这保留了 Element Plus 用于兼容现有组件，新增 Vuestic UI 用于新组件和主题系统。
  </action>
  <verify>
<code>grep -q "vuestic-ui" app-vue/src/main.js && echo "PASS" || echo "FAIL"</code>
  </verify>
  <done>main.js 已更新引入 Vuestic UI</done>
</task>

<task type="auto">
  <name>Task 4: 添加液态玻璃和动态背景 CSS</name>
  <files>app-vue/src/assets/glass.css</files>
  <read_first>app-vue/src/assets/main.css</read_first>
  <action>
创建液态玻璃和动态背景样式文件 (app-vue/src/assets/glass.css):

```css
/* 动态渐变背景 */
.dynamic-gradient {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 50%, #f093fb 100%);
  background-size: 400% 400%;
  animation: gradientShift 15s ease infinite;
}

@keyframes gradientShift {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

/* 暗色主题动态背景 */
[data-theme="dark"] .dynamic-gradient {
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  background-size: 400% 400%;
  animation: gradientShift 15s ease infinite;
}

/* 液态玻璃效果 */
.glass {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

[data-theme="dark"] .glass {
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

/* 玻璃按钮 */
.glass-button {
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 12px;
  padding: 12px 24px;
  color: white;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.glass-button:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: translateY(-2px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.2);
}

[data-theme="dark"] .glass-button {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

/* 输入框玻璃效果 */
.glass-input {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  padding: 12px 16px;
  color: white;
  transition: all 0.3s ease;
}

.glass-input:focus {
  outline: none;
  border-color: rgba(255, 255, 255, 0.5);
  box-shadow: 0 0 20px rgba(255, 255, 255, 0.2);
}

.glass-input::placeholder {
  color: rgba(255, 255, 255, 0.6);
}

/* 浮动动画 */
@keyframes float {
  0%, 100% { transform: translateY(0px); }
  50% { transform: translateY(-10px); }
}

.floating {
  animation: float 3s ease-in-out infinite;
}

/* 脉冲动画 */
@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.7; }
}

.pulse {
  animation: pulse 2s ease-in-out infinite;
}

/* 发光效果 */
.glow {
  box-shadow: 0 0 20px rgba(102, 126, 234, 0.5);
}

.glow:hover {
  box-shadow: 0 0 40px rgba(102, 126, 234, 0.8);
}
```

在 main.js 中引入此文件:
```javascript
import './assets/glass.css'
```
  </action>
  <verify>
<code>ls app-vue/src/assets/glass.css && echo "PASS" || echo "FAIL"</code>
  </verify>
  <done>液态玻璃和动态背景 CSS 已创建</done>
</task>

<task type="auto">
  <name>Task 5: 重构登录页 Login.vue</name>
  <files>app-vue/src/views/Login.vue</files>
  <read_first>app-vue/src/views/Login.vue</read_first>
  <action>
重构登录页，使用液态玻璃和动态背景效果:

```vue
<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { useThemeStore } from '@/store/theme'

const router = useRouter()
const userStore = useUserStore()
const themeStore = useThemeStore()

const loginForm = reactive({
  username: '',
  password: ''
})

const loading = ref(false)
const loginFormRef = ref(null)

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

async function handleLogin() {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    loading.value = true
    try {
      await userStore.login(loginForm.username, loginForm.password)
      router.push('/dashboard')
    } catch (error) {
      ElMessage.error(error.message || '用户名或密码错误')
    } finally {
      loading.value = false
    }
  })
}

function handleKeydown(e) {
  if (e.key === 'Enter') {
    handleLogin()
  }
}
</script>

<template>
  <div class="login-container dynamic-gradient" @keydown="handleKeydown">
    <div class="login-particles">
      <div class="particle floating" style="top: 10%; left: 20%;"></div>
      <div class="particle floating" style="top: 60%; left: 80%; animation-delay: 0.5s;"></div>
      <div class="particle floating" style="top: 80%; left: 10%; animation-delay: 1s;"></div>
      <div class="particle floating" style="top: 20%; left: 70%; animation-delay: 1.5s;"></div>
    </div>
    
    <div class="login-card glass glow">
      <div class="login-header">
        <h1 class="login-title">管理系统</h1>
        <p class="login-subtitle">欢迎回来</p>
      </div>
      
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="rules"
        class="login-form"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            size="large"
            class="glass-input"
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            show-password
            class="glass-input"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="login-button glass-button"
            @click="handleLogin"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="login-footer">
        <span class="theme-toggle" @click="themeStore.toggleTheme">
          {{ themeStore.isDark ? '☀️ 亮色模式' : '🌙 暗色模式' }}
        </span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.login-particles {
  position: absolute;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.particle {
  position: absolute;
  width: 20px;
  height: 20px;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  box-shadow: 0 0 20px rgba(255, 255, 255, 0.5);
}

.login-card {
  width: 420px;
  padding: 48px 40px;
  position: relative;
  z-index: 10;
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.login-title {
  font-size: 32px;
  font-weight: 700;
  color: white;
  margin: 0 0 12px 0;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
}

.login-subtitle {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.8);
  margin: 0;
}

.login-form {
  margin-top: 24px;
}

.login-form :deep(.el-input__wrapper) {
  background: transparent;
  box-shadow: none;
  padding: 0;
}

.login-form :deep(.el-input__inner) {
  color: white;
}

.login-form :deep(.el-input__inner::placeholder) {
  color: rgba(255, 255, 255, 0.6);
}

.login-button {
  width: 100%;
  height: 48px;
  font-size: 18px;
  border: none;
  margin-top: 8px;
}

.login-footer {
  text-align: center;
  margin-top: 24px;
}

.theme-toggle {
  color: rgba(255, 255, 255, 0.7);
  cursor: pointer;
  font-size: 14px;
  transition: color 0.3s;
}

.theme-toggle:hover {
  color: white;
}
</style>
```
  </action>
  <verify>
<code>ls app-vue/src/views/Login.vue && echo "PASS" || echo "FAIL"</code>
  </verify>
  <done>登录页已重构，使用液态玻璃和动态背景</done>
</task>

<task type="auto">
  <name>Task 6: 重构管理后台 Layout.vue</name>
  <files>app-vue/src/views/Layout.vue</files>
  <read_first>app-vue/src/views/Layout.vue</read_first>
  <action>
重构管理后台布局，添加现代 UI 效果:

```vue
<script setup>
import { ref } from 'vue'
import AppMenu from '@/components/AppMenu.vue'
import AppHeader from '@/components/AppHeader.vue'
import AppTabs from '@/components/AppTabs.vue'
import { useThemeStore } from '@/store/theme'

const themeStore = useThemeStore()
const collapsed = ref(false)

function toggleCollapse() {
  collapsed.value = !collapsed.value
}
</script>

<template>
  <el-container class="layout-container" :class="{ 'dark-theme': themeStore.isDark }">
    <!-- 左侧菜单 -->
    <el-aside :width="collapsed ? '64px' : '220px'" class="layout-aside glass">
      <div class="logo">
        <span v-if="!collapsed" class="logo-text">管理系统</span>
        <span v-else class="logo-icon">M</span>
      </div>
      <AppMenu :collapsed="collapsed" />
    </el-aside>
    
    <el-container class="main-container">
      <!-- 顶部头部 -->
      <el-header class="layout-header glass">
        <div class="header-left">
          <el-button text @click="toggleCollapse" class="toggle-btn">
            <el-icon size="20"><Fold v-if="!collapsed" /><Expand v-else /></el-icon>
          </el-button>
        </div>
        <AppHeader />
      </el-header>
      
      <!-- 页签栏 -->
      <AppTabs />
      
      <!-- 内容区域 -->
      <el-main class="layout-main">
        <router-view v-slot="{ Component }">
          <keep-alive>
            <component :is="Component" />
          </keep-alive>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.layout-container {
  min-height: 100vh;
  background: #f5f7fa;
  transition: background 0.3s;
}

.layout-container.dark-theme {
  background: #1a1a2e;
}

.layout-aside {
  background: rgba(48, 65, 86, 0.9);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-right: 1px solid rgba(255, 255, 255, 0.1);
  transition: all 0.3s ease;
}

.dark-theme .layout-aside {
  background: rgba(22, 33, 62, 0.9);
}

.logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(38, 52, 69, 0.8);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo-text {
  color: white;
  font-size: 18px;
  font-weight: 600;
  letter-spacing: 2px;
}

.logo-icon {
  color: #667eea;
  font-size: 24px;
  font-weight: 700;
}

.layout-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.1);
  padding: 0 20px;
  transition: all 0.3s;
}

.dark-theme .layout-header {
  background: rgba(26, 26, 46, 0.8);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.toggle-btn {
  padding: 8px;
  border-radius: 8px;
  transition: background 0.3s;
}

.toggle-btn:hover {
  background: rgba(102, 126, 234, 0.1);
}

.layout-main {
  background: transparent;
  padding: 20px;
  min-height: calc(100vh - 120px);
}

.dark-theme .layout-main {
  background: transparent;
}
</style>
```
  </action>
  <verify>
<code>ls app-vue/src/views/Layout.vue && echo "PASS" || echo "FAIL"</code>
  </verify>
  <done>管理后台 Layout 已重构，添加现代 UI 效果</done>
</task>

<task type="auto">
  <name>Task 7: 创建后端主题偏好 API</name>
  <files>springboot/src/main/java/cn/coderstory/springboot/entity/UserSettings.java, springboot/src/main/java/cn/coderstory/springboot/mapper/UserSettingsMapper.java, springboot/src/main/java/cn/coderstory/springboot/service/UserSettingsService.java, springboot/src/main/java/cn/coderstory/springboot/controller/SettingsController.java</files>
  <read_first>springboot/src/main/java/cn/coderstory/springboot/entity/User.java</read_first>
  <action>
在后端添加用户设置实体和相关类:

1. UserSettings.java (实体):
```java
package cn.coderstory.springboot.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_user_settings")
public class UserSettings {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private String theme;  // "light" or "dark"
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
```

2. UserSettingsMapper.java (Mapper):
```java
package cn.coderstory.springboot.mapper;

import cn.coderstory.springboot.entity.UserSettings;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserSettingsMapper extends BaseMapper<UserSettings> {
}
```

3. UserSettingsService.java (Service):
```java
package cn.coderstory.springboot.service;

import cn.coderstory.springboot.entity.UserSettings;
import cn.coderstory.springboot.mapper.UserSettingsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserSettingsService {
    
    private final UserSettingsMapper userSettingsMapper;
    
    public String getTheme(Long userId) {
        UserSettings settings = userSettingsMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<UserSettings>()
                .eq("user_id", userId)
        );
        return settings != null ? settings.getTheme() : "light";
    }
    
    public void saveTheme(Long userId, String theme) {
        UserSettings settings = userSettingsMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<UserSettings>()
                .eq("user_id", userId)
        );
        
        if (settings == null) {
            settings = new UserSettings();
            settings.setUserId(userId);
            settings.setTheme(theme);
            userSettingsMapper.insert(settings);
        } else {
            settings.setTheme(theme);
            userSettingsMapper.updateById(settings);
        }
    }
}
```

4. SettingsController.java (Controller):
```java
package cn.coderstory.springboot.controller;

import cn.coderstory.springboot.service.UserSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class SettingsController {
    
    private final UserSettingsService userSettingsService;
    
    @GetMapping("/theme")
    public ResponseEntity<Map<String, Object>> getTheme(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Map<String, Object> response = new HashMap<>();
        
        if (userId == null) {
            response.put("code", 401);
            response.put("message", "未登录");
            return ResponseEntity.status(401).body(response);
        }
        
        String theme = userSettingsService.getTheme(userId);
        response.put("code", 200);
        response.put("data", Map.of("theme", theme));
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/theme")
    public ResponseEntity<Map<String, Object>> saveTheme(
            HttpServletRequest request,
            @RequestBody Map<String, String> body) {
        Long userId = (Long) request.getAttribute("userId");
        Map<String, Object> response = new HashMap<>();
        
        if (userId == null) {
            response.put("code", 401);
            response.put("message", "未登录");
            return ResponseEntity.status(401).body(response);
        }
        
        String theme = body.getOrDefault("theme", "light");
        userSettingsService.saveTheme(userId, theme);
        
        response.put("code", 200);
        response.put("message", "success");
        return ResponseEntity.ok(response);
    }
}
```
  </action>
  <verify>
<code>ls springboot/src/main/java/cn/coderstory/springboot/entity/UserSettings.java && echo "PASS" || echo "FAIL"</code>
  </verify>
  <done>后端主题偏好 API 已创建</done>
</task>

<task type="auto">
  <name>Task 8: 创建数据库用户设置表</name>
  <files>springboot/src/main/resources/data.sql</files>
  <read_first>springboot/src/main/resources/schema.sql</read_first>
  <action>
创建用户设置表 SQL (在 schema.sql 中添加):

```sql
-- 用户设置表
CREATE TABLE IF NOT EXISTS sys_user_settings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    theme VARCHAR(20) DEFAULT 'light' COMMENT '主题(light/dark)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户设置表';
```

将此 SQL 添加到 schema.sql 文件末尾。
  </action>
  <verify>
<code>grep -q "sys_user_settings" springboot/src/main/resources/schema.sql && echo "PASS" || echo "FAIL"</code>
  </verify>
  <done>用户设置表已添加到 schema.sql</done>
</task>

<task type="auto">
  <name>Task 9: 更新前端 API 和 Store 集成主题同步</name>
  <files>app-vue/src/api/settings.js, app-vue/src/stores/theme.js</files>
  <read_first>app-vue/src/api/user.js</read_first>
  <action>
1. 创建 settings API (app-vue/src/api/settings.js):

```javascript
import axios from 'axios'

export function getTheme() {
  return axios.get('/api/settings/theme')
}

export function saveTheme(theme) {
  return axios.put('/api/settings/theme', { theme })
}
```

2. 更新 theme store (app-vue/src/stores/theme.js):

```javascript
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getTheme, saveTheme } from '@/api/settings'

export const useThemeStore = defineStore('theme', () => {
  const theme = ref('light')
  const isLoading = ref(false)
  
  const isDark = computed(() => theme.value === 'dark')
  
  function setTheme(newTheme) {
    theme.value = newTheme
    applyTheme(newTheme)
  }
  
  function toggleTheme() {
    const newTheme = theme.value === 'light' ? 'dark' : 'light'
    setTheme(newTheme)
  }
  
  function applyTheme(themeName) {
    document.documentElement.setAttribute('data-theme', themeName)
  }
  
  async function loadTheme() {
    try {
      isLoading.value = true
      const response = await getTheme()
      if (response.data.code === 200) {
        setTheme(response.data.data.theme)
      }
    } catch (error) {
      console.error('加载主题失败:', error)
    } finally {
      isLoading.value = false
    }
  }
  
  async function syncTheme() {
    try {
      await saveTheme(theme.value)
    } catch (error) {
      console.error('同步主题失败:', error)
    }
  }
  
  return { theme, isDark, isLoading, setTheme, toggleTheme, applyTheme, loadTheme, syncTheme }
})
```

3. 更新 user store 登录后加载主题:

在 user.js 的 login success 回调中添加:
```javascript
themeStore.loadTheme()
```
  </action>
  <verify>
<code>ls app-vue/src/api/settings.js && echo "PASS" || echo "FAIL"</code>
  </verify>
  <done>主题同步功能已集成</done>
</task>

<task type="auto">
  <name>Task 10: 添加 AppHeader 主题切换按钮</name>
  <files>app-vue/src/components/AppHeader.vue</files>
  <read_first>app-vue/src/components/AppHeader.vue</read_first>
  <action>
更新 AppHeader.vue 添加主题切换按钮:

```vue
<script setup>
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'
import { useThemeStore } from '@/store/theme'

const router = useRouter()
const userStore = useUserStore()
const themeStore = useThemeStore()

async function handleLogout() {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await userStore.logout()
    router.push('/login')
  } catch (error) {
    // 用户取消
  }
}

function toggleTheme() {
  themeStore.toggleTheme()
  themeStore.syncTheme()
}
</script>

<template>
  <div class="app-header">
    <div class="header-right">
      <el-button text @click="toggleTheme" class="theme-btn">
        {{ themeStore.isDark ? '☀️' : '🌙' }}
      </el-button>
      <el-dropdown @command="handleCommand">
        <span class="user-info">
          <el-avatar :size="32" icon="User" />
          <span class="username">{{ userStore.userInfo?.name || userStore.userInfo?.username }}</span>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="logout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<style scoped>
.app-header {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  height: 100%;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.theme-btn {
  font-size: 20px;
  padding: 8px;
  border-radius: 8px;
  transition: background 0.3s;
}

.theme-btn:hover {
  background: rgba(102, 126, 234, 0.1);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 8px;
  transition: background 0.3s;
}

.user-info:hover {
  background: rgba(0, 0, 0, 0.05);
}

.username {
  font-size: 14px;
  color: #303133;
}
</style>
```
  </action>
  <verify>
<code>grep -q "toggleTheme" app-vue/src/components/AppHeader.vue && echo "PASS" || echo "FAIL"</code>
  </verify>
  <done>AppHeader 已添加主题切换按钮</done>
</task>

</tasks>

<verification>
手动验证步骤：
1. 启动 MySQL，更新 schema.sql: `mysql -u root -p < springboot/src/main/resources/schema.sql`
2. 启动后端: `cd springboot && ./mvnw spring-boot:run`
3. 启动前端: `cd app-vue && npm run dev`
4. 登录后点击右上角主题切换按钮
5. 刷新页面验证主题是否保持
6. 检查登录页液态玻璃效果是否正常显示
</verification>

<success_criteria>
- [ ] 用户可以切换亮色/暗色主题
- [ ] 主题偏好存储在后端
- [ ] 刷新页面后主题保持
- [ ] 登录页显示液态玻璃效果
- [ ] 管理后台有现代动态视觉效果
</success_criteria>

<output>
After completion, create `.planning/phases/04-ui-enhancement/04-ui-01-SUMMARY.md`
</output>
