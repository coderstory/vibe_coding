<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, h } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

const loginForm = reactive({
  username: '',
  password: ''
})

const loading = ref(false)
const loginFormRef = ref<FormInstance | null>(null)

const rules: FormRules = {
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
      router.push('/index')
    } catch (error: unknown) {
      const err = error as Error
      ElMessage.error(err.message || '用户名或密码错误')
    } finally {
      loading.value = false
    }
  })
}

function handleKeydown(e: KeyboardEvent) {
  if (e.key === 'Enter') {
    handleLogin()
  }
}

const canvasRef = ref<HTMLCanvasElement | null>(null)
let animationFrameId: number
let particles: Array<{
  x: number
  y: number
  vx: number
  vy: number
  radius: number
  color: string
  alpha: number
}>

function initParticles(canvas: HTMLCanvasElement) {
  particles = []
  const particleCount = Math.floor((canvas.width * canvas.height) / 8000)

  for (let i = 0; i < particleCount; i++) {
    particles.push({
      x: Math.random() * canvas.width,
      y: Math.random() * canvas.height,
      vx: (Math.random() - 0.5) * 0.5,
      vy: (Math.random() - 0.5) * 0.5,
      radius: Math.random() * 2 + 0.5,
      color: `hsl(${200 + Math.random() * 40}, 80%, ${60 + Math.random() * 20}%)`,
      alpha: Math.random() * 0.5 + 0.3
    })
  }
}

function drawParticles(ctx: CanvasRenderingContext2D, canvas: HTMLCanvasElement) {
  ctx.clearRect(0, 0, canvas.width, canvas.height)

  particles.forEach((p) => {
    p.x += p.vx
    p.y += p.vy

    if (p.x < 0 || p.x > canvas.width) p.vx *= -1
    if (p.y < 0 || p.y > canvas.height) p.vy *= -1

    ctx.beginPath()
    ctx.arc(p.x, p.y, p.radius, 0, Math.PI * 2)
    ctx.fillStyle = p.color
    ctx.globalAlpha = p.alpha
    ctx.fill()
    ctx.globalAlpha = 1
  })

  particles.forEach((p1, i) => {
    particles.slice(i + 1).forEach((p2) => {
      const dx = p1.x - p2.x
      const dy = p1.y - p2.y
      const dist = Math.sqrt(dx * dx + dy * dy)

      if (dist < 120) {
        ctx.beginPath()
        ctx.moveTo(p1.x, p1.y)
        ctx.lineTo(p2.x, p2.y)
        ctx.strokeStyle = `rgba(100, 180, 255, ${0.15 * (1 - dist / 120)})`
        ctx.lineWidth = 0.5
        ctx.stroke()
      }
    })
  })
}

function resizeCanvas(canvas: HTMLCanvasElement) {
  canvas.width = window.innerWidth
  canvas.height = window.innerHeight
  if (particles) {
    initParticles(canvas)
  }
}

onMounted(() => {
  const canvas = canvasRef.value
  if (!canvas) return

  resizeCanvas(canvas)
  initParticles(canvas)

  const ctx = canvas.getContext('2d')
  if (!ctx) return

  function animate() {
    drawParticles(ctx, canvas)
    animationFrameId = requestAnimationFrame(animate)
  }

  animate()

  window.addEventListener('resize', () => resizeCanvas(canvas))
})

onUnmounted(() => {
  if (animationFrameId) {
    cancelAnimationFrame(animationFrameId)
  }
  window.removeEventListener('resize', resizeCanvas)
})

const UserIcon = {
  render() {
    return h('svg', {
      xmlns: 'http://www.w3.org/2000/svg',
      viewBox: '0 0 24 24',
      fill: 'none',
      stroke: 'currentColor',
      'stroke-width': '2',
      width: '18',
      height: '18'
    }, [
      h('path', { d: 'M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2' }),
      h('circle', { cx: '12', cy: '7', r: '4' })
    ])
  }
}

const LockIcon = {
  render() {
    return h('svg', {
      xmlns: 'http://www.w3.org/2000/svg',
      viewBox: '0 0 24 24',
      fill: 'none',
      stroke: 'currentColor',
      'stroke-width': '2',
      width: '18',
      height: '18'
    }, [
      h('rect', { x: '3', y: '11', width: '18', height: '11', rx: '2', ry: '2' }),
      h('path', { d: 'M7 11V7a5 5 0 0 1 10 0v4' })
    ])
  }
}
</script>

<template>
  <div class="login-container" @keydown="handleKeydown">
    <canvas ref="canvasRef" class="particle-canvas" />

    <div class="wave-container">
      <div class="wave wave1" />
      <div class="wave wave2" />
      <div class="wave wave3" />
    </div>

    <div class="login-content">
      <el-card class="login-card" shadow="hover">
        <template #header>
          <div class="login-header">
            <div class="logo-container">
              <div class="logo-icon">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M12 2L2 7L12 12L22 7L12 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  <path d="M2 17L12 22L22 17" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  <path d="M2 12L12 17L22 12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </div>
            </div>
            <h1>AI 管理系统</h1>
            <p>智能 · 高效 · 安全</p>
          </div>
        </template>

        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="rules"
          label-position="top"
        >
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名"
              size="large"
              :prefix-icon="UserIcon"
              class="cyber-input"
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              size="large"
              show-password
              :prefix-icon="LockIcon"
              class="cyber-input"
            />
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              size="large"
              :loading="loading"
              class="cyber-button"
              @click="handleLogin"
            >
              <span v-if="!loading">登 录</span>
              <span v-else>验证中...</span>
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <div class="cyber-lines">
      <div class="line line1" />
      <div class="line line2" />
      <div class="line line3" />
    </div>
  </div>
</template>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0a0e27 0%, #1a1f4e 50%, #0d1229 100%);
  position: relative;
  overflow: hidden;
}

.particle-canvas {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
}

.wave-container {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 200px;
  overflow: hidden;
  z-index: 2;
}

.wave {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 200%;
  height: 100%;
  background-repeat: repeat-x;
  transform-origin: center bottom;
}

.wave1 {
  background: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 1440 320'%3E%3Cpath fill='%230088ff' fill-opacity='0.3' d='M0,192L48,176C96,160,192,128,288,138.7C384,149,480,203,576,208C672,213,768,171,864,149.3C960,128,1056,128,1152,149.3C1248,171,1344,213,1392,234.7L1440,256L1440,320L1392,320C1344,320,1248,320,1152,320C1056,320,960,320,864,320C768,320,672,320,576,320C480,320,384,320,288,320C192,320,96,320,48,320L0,320Z'%3E%3C/path%3E%3C/svg%3E") repeat-x;
  background-size: 50% 100%;
  animation: wave 12s linear infinite;
  opacity: 0.8;
}

.wave2 {
  background: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 1440 320'%3E%3Cpath fill='%2300d4ff' fill-opacity='0.2' d='M0,224L48,213.3C96,203,192,181,288,181.3C384,181,480,203,576,218.7C672,235,768,245,864,229.3C960,213,1056,171,1152,165.3C1248,160,1344,192,1392,208L1440,224L1440,320L1392,320C1344,320,1248,320,1152,320C1056,320,960,320,864,320C768,320,672,320,576,320C480,320,384,320,288,320C192,320,96,320,48,320L0,320Z'%3E%3C/path%3E%3C/svg%3E") repeat-x;
  background-size: 50% 100%;
  animation: wave 15s linear infinite reverse;
  opacity: 0.6;
  animation-delay: -2s;
}

.wave3 {
  background: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 1440 320'%3E%3Cpath fill='%230066cc' fill-opacity='0.15' d='M0,256L48,240C96,224,192,192,288,181.3C384,171,480,181,576,197.3C672,213,768,235,864,229.3C960,224,1056,192,1152,170.7C1248,149,1344,139,1392,133.3L1440,128L1440,320L1392,320C1344,320,1248,320,1152,320C1056,320,960,320,864,320C768,320,672,320,576,320C480,320,384,320,288,320C192,320,96,320,48,320L0,320Z'%3E%3C/path%3E%3C/svg%3E") repeat-x;
  background-size: 50% 100%;
  animation: wave 20s linear infinite;
  opacity: 0.4;
  animation-delay: -5s;
}

@keyframes wave {
  0% {
    transform: translateX(0);
  }
  100% {
    transform: translateX(-50%);
  }
}

.login-content {
  position: relative;
  z-index: 10;
}

.login-card {
  width: 420px;
  background: rgba(15, 20, 50, 0.85);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(0, 136, 255, 0.3);
  border-radius: 16px;
  box-shadow:
    0 0 40px rgba(0, 136, 255, 0.15),
    0 25px 50px rgba(0, 0, 0, 0.4),
    inset 0 1px 0 rgba(255, 255, 255, 0.1);
  animation: cardFloat 3s ease-in-out infinite;
}

@keyframes cardFloat {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-8px);
  }
}

.login-card :deep(.el-card__header) {
  background: transparent;
  border-bottom: 1px solid rgba(0, 136, 255, 0.2);
  padding: 28px 24px 20px;
}

.login-card :deep(.el-card__body) {
  padding: 24px;
}

.login-header {
  text-align: center;
}

.logo-container {
  margin-bottom: 16px;
}

.logo-icon {
  width: 64px;
  height: 64px;
  margin: 0 auto;
  background: linear-gradient(135deg, #0088ff 0%, #00d4ff 100%);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 32px rgba(0, 136, 255, 0.4);
  animation: logoPulse 2s ease-in-out infinite;
}

.logo-icon svg {
  width: 36px;
  height: 36px;
  color: white;
}

@keyframes logoPulse {
  0%, 100% {
    box-shadow: 0 8px 32px rgba(0, 136, 255, 0.4);
  }
  50% {
    box-shadow: 0 8px 48px rgba(0, 136, 255, 0.6);
  }
}

.login-header h1 {
  margin: 0 0 8px 0;
  font-size: 26px;
  font-weight: 700;
  background: linear-gradient(90deg, #ffffff 0%, #00d4ff 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-shadow: 0 0 30px rgba(0, 212, 255, 0.3);
}

.login-header p {
  margin: 0;
  color: rgba(255, 255, 255, 0.6);
  font-size: 14px;
  letter-spacing: 4px;
}

.cyber-input :deep(.el-input__wrapper) {
  background: rgba(0, 20, 60, 0.6);
  border: 1px solid rgba(0, 136, 255, 0.3);
  border-radius: 10px;
  box-shadow: none;
  padding: 4px 12px;
  transition: all 0.3s ease;
}

.cyber-input :deep(.el-input__wrapper:hover),
.cyber-input :deep(.el-input__wrapper.is-focus) {
  border-color: rgba(0, 212, 255, 0.6);
  box-shadow: 0 0 20px rgba(0, 136, 255, 0.2);
  background: rgba(0, 30, 80, 0.6);
}

.cyber-input :deep(.el-input__inner) {
  color: rgba(255, 255, 255, 0.9);
  height: 40px;
}

.cyber-input :deep(.el-input__inner::placeholder) {
  color: rgba(255, 255, 255, 0.4);
}

.cyber-input :deep(.el-input__prefix) {
  color: rgba(0, 212, 255, 0.7);
}

.cyber-button {
  width: 100%;
  height: 48px;
  background: linear-gradient(135deg, #0088ff 0%, #00d4ff 100%);
  border: none;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 6px;
  color: white;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
  box-shadow: 0 4px 20px rgba(0, 136, 255, 0.4);
}

.cyber-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: left 0.5s ease;
}

.cyber-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 30px rgba(0, 136, 255, 0.6);
}

.cyber-button:hover::before {
  left: 100%;
}

.cyber-button:active {
  transform: translateY(0);
}

.cyber-button :deep(.el-button__content) {
  letter-spacing: 6px;
}

.cyber-lines .line {
  position: absolute;
  background: linear-gradient(90deg, transparent, rgba(0, 136, 255, 0.5), transparent);
  animation: lineMove 8s linear infinite;
}

.line1 {
  top: 15%;
  left: 0;
  width: 100%;
  height: 1px;
  animation-delay: 0s;
}

.line2 {
  top: 50%;
  left: 0;
  width: 100%;
  height: 1px;
  animation-delay: -3s;
}

.line3 {
  top: 80%;
  left: 0;
  width: 100%;
  height: 1px;
  animation-delay: -6s;
}

@keyframes lineMove {
  0% {
    opacity: 0;
    transform: translateX(-100%);
  }
  50% {
    opacity: 1;
  }
  100% {
    opacity: 0;
    transform: translateX(100%);
  }
}

.login-card :deep(.el-form-item__label) {
  color: rgba(255, 255, 255, 0.8);
  font-weight: 500;
}

.login-card :deep(.el-form-item) {
  margin-bottom: 20px;
}

.login-card :deep(.el-form-item:last-child) {
  margin-bottom: 0;
  margin-top: 28px;
}
</style>