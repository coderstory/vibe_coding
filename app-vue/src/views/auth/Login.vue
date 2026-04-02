<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
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
      ElMessage.success('登录成功')
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
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            show-password
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
  background: rgba(255, 255, 255, 0.1) !important;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  box-shadow: none !important;
  border: none !important;
  border-radius: 8px;
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