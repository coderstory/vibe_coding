<template>
  <div class="page-content">
    <canvas ref="canvasRef" class="wave-canvas"></canvas>
    <div class="wave-overlay">
      <h1 class="wave-title">Welcome</h1>
      <p class="wave-subtitle">AI驱动的管理系统</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'

const canvasRef = ref<HTMLCanvasElement | null>(null)
let animationId: number | null = null
let time = 0

interface Wave {
  amplitude: number
  frequency: number
  speed: number
  phase: number
  color: string
  yOffset: number
}

interface Bubble {
  x: number
  y: number
  radius: number
  speed: number
  wobble: number
  wobbleSpeed: number
  alpha: number
}

interface Whale {
  x: number
  y: number
  size: number
  speed: number
  tailPhase: number
  bodyWave: number
  blowholeTimer: number
  bubbles: Bubble[]
  direction: number // 1 = right, -1 = left
}

function initCanvas() {
  const canvas = canvasRef.value
  if (!canvas) return

  const ctx = canvas.getContext('2d')
  if (!ctx) return

  canvas.width = canvas.offsetWidth
  canvas.height = canvas.offsetHeight

  const waves: Wave[] = [
    { amplitude: 60, frequency: 0.008, speed: 0.02, phase: 0, color: 'rgba(26, 82, 118, 0.8)', yOffset: 0.7 },
    { amplitude: 50, frequency: 0.012, speed: 0.025, phase: 1, color: 'rgba(36, 113, 163, 0.7)', yOffset: 0.72 },
    { amplitude: 45, frequency: 0.015, speed: 0.03, phase: 2, color: 'rgba(52, 152, 219, 0.6)', yOffset: 0.74 },
    { amplitude: 40, frequency: 0.018, speed: 0.035, phase: 3, color: 'rgba(93, 173, 226, 0.5)', yOffset: 0.76 },
    { amplitude: 35, frequency: 0.02, speed: 0.04, phase: 4, color: 'rgba(133, 193, 233, 0.4)', yOffset: 0.78 },
    { amplitude: 30, frequency: 0.025, speed: 0.05, phase: 5, color: 'rgba(180, 220, 245, 0.3)', yOffset: 0.8 },
  ]

  const ambientBubbles: Bubble[] = []
  for (let i = 0; i < 40; i++) {
    ambientBubbles.push(createBubble(canvas))
  }

  // 鲸鱼
  const whale: Whale = {
    x: canvas.width * 0.3,
    y: canvas.height * 0.55,
    size: 80,
    speed: 0.8,
    tailPhase: 0,
    bodyWave: 0,
    blowholeTimer: 0,
    bubbles: [],
    direction: 1
  }

  function createBubble(canvas: HTMLCanvasElement): Bubble {
    return {
      x: Math.random() * canvas.width,
      y: canvas.height + Math.random() * 100,
      radius: Math.random() * 4 + 2,
      speed: Math.random() * 1.5 + 0.5,
      wobble: Math.random() * Math.PI * 2,
      wobbleSpeed: Math.random() * 0.05 + 0.02,
      alpha: Math.random() * 0.4 + 0.2
    }
  }

  function updateWhale(whale: Whale, canvas: HTMLCanvasElement) {
    whale.x += whale.speed * whale.direction
    whale.tailPhase += 0.15
    whale.bodyWave += 0.05
    whale.blowholeTimer += 0.02

    // 吹泡泡
    if (whale.blowholeTimer > 3 + Math.random() * 2) {
      whale.blowholeTimer = 0
      for (let i = 0; i < 5; i++) {
        whale.bubbles.push({
          x: whale.x - whale.direction * whale.size * 0.8,
          y: whale.y - whale.size * 0.15,
          radius: Math.random() * 4 + 2,
          speed: Math.random() * 0.8 + 0.3,
          wobble: Math.random() * Math.PI * 2,
          wobbleSpeed: Math.random() * 0.1 + 0.05,
          alpha: 0.6
        })
      }
    }

    // 更新鲸鱼泡泡
    whale.bubbles = whale.bubbles.filter(b => {
      b.y -= b.speed
      b.x -= whale.speed * whale.direction * 0.3
      b.wobble += b.wobbleSpeed
      b.x += Math.sin(b.wobble) * 0.8
      b.alpha -= 0.008
      return b.alpha > 0 && b.y > 0
    })

    // 到达边界转向
    if (whale.x > canvas.width + whale.size * 2) {
      whale.direction = -1
    } else if (whale.x < -whale.size * 2) {
      whale.direction = 1
    }
  }

  function drawWhale(ctx: CanvasRenderingContext2D, whale: Whale) {
    ctx.save()
    ctx.translate(whale.x, whale.y)
    if (whale.direction < 0) ctx.scale(-1, 1)

    const s = whale.size
    const tailAngle = Math.sin(whale.tailPhase) * 0.4
    const bodyFlex = Math.sin(whale.bodyWave) * 0.05

    // 尾鳍
    ctx.save()
    ctx.translate(-s * 0.85, -s * 0.05)
    ctx.rotate(tailAngle)
    ctx.beginPath()
    ctx.moveTo(0, 0)
    ctx.quadraticCurveTo(-s * 0.4, -s * 0.5, -s * 0.55, -s * 0.3)
    ctx.quadraticCurveTo(-s * 0.45, 0, -s * 0.55, s * 0.3)
    ctx.quadraticCurveTo(-s * 0.4, s * 0.5, 0, 0)
    ctx.fillStyle = '#2c3e50'
    ctx.fill()
    ctx.restore()

    // 身体
    ctx.beginPath()
    // 尾部连接处
    ctx.moveTo(-s * 0.7, 0)
    // 下半部腹部
    ctx.quadraticCurveTo(-s * 0.5, s * 0.4, -s * 0.1, s * 0.38)
    ctx.quadraticCurveTo(s * 0.3, s * 0.42, s * 0.5, s * 0.3)
    ctx.quadraticCurveTo(s * 0.8, s * 0.1, s * 0.9, -s * 0.05)
    // 背部曲线
    ctx.quadraticCurveTo(s * 0.85, -s * 0.25, s * 0.6, -s * 0.35)
    ctx.quadraticCurveTo(s * 0.3, -s * 0.42, s * 0.1, -s * 0.4)
    ctx.quadraticCurveTo(-s * 0.2, -s * 0.45, -s * 0.5, -s * 0.38)
    ctx.quadraticCurveTo(-s * 0.75, -s * 0.2, -s * 0.7, 0)

    const bodyGradient = ctx.createLinearGradient(0, -s * 0.5, 0, s * 0.5)
    bodyGradient.addColorStop(0, '#5d6d7e')
    bodyGradient.addColorStop(0.3, '#85929e')
    bodyGradient.addColorStop(0.7, '#aeb6bf')
    bodyGradient.addColorStop(1, '#d5d8dc')
    ctx.fillStyle = bodyGradient
    ctx.fill()

    // 腹部条纹
    ctx.strokeStyle = 'rgba(255, 255, 255, 0.3)'
    ctx.lineWidth = 2
    for (let i = 0; i < 4; i++) {
      ctx.beginPath()
      ctx.moveTo(-s * 0.4 + i * s * 0.12, s * 0.15)
      ctx.quadraticCurveTo(s * 0.0 + i * s * 0.08, s * 0.32 + i * 2, s * 0.35 + i * s * 0.08, s * 0.2)
      ctx.stroke()
    }

    // 胸鳍
    ctx.beginPath()
    ctx.moveTo(s * 0.1, s * 0.2)
    ctx.quadraticCurveTo(s * 0.0, s * 0.55, s * 0.2, s * 0.6)
    ctx.quadraticCurveTo(s * 0.4, s * 0.5, s * 0.35, s * 0.25)
    ctx.quadraticCurveTo(s * 0.25, s * 0.22, s * 0.1, s * 0.2)
    ctx.fillStyle = '#5d6d7e'
    ctx.fill()

    // 眼睛
    ctx.beginPath()
    ctx.arc(s * 0.7, -s * 0.1, s * 0.055, 0, Math.PI * 2)
    ctx.fillStyle = '#1a1a2e'
    ctx.fill()
    // 眼睛高光
    ctx.beginPath()
    ctx.arc(s * 0.72, -s * 0.12, s * 0.018, 0, Math.PI * 2)
    ctx.fillStyle = '#fff'
    ctx.fill()

    // 鲸须板
    ctx.strokeStyle = 'rgba(44, 62, 80, 0.3)'
    ctx.lineWidth = 1
    for (let i = 0; i < 6; i++) {
      ctx.beginPath()
      ctx.moveTo(s * 0.8 + i * 3, -s * 0.02)
      ctx.lineTo(s * 0.8 + i * 3, s * 0.12)
      ctx.stroke()
    }

    ctx.restore()

    // 绘制鲸鱼泡泡
    whale.bubbles.forEach(b => {
      ctx.beginPath()
      ctx.arc(b.x, b.y, b.radius, 0, Math.PI * 2)
      ctx.fillStyle = `rgba(255, 255, 255, ${b.alpha * 0.4})`
      ctx.fill()
      ctx.beginPath()
      ctx.arc(b.x - b.radius * 0.3, b.y - b.radius * 0.3, b.radius * 0.4, 0, Math.PI * 2)
      ctx.fillStyle = `rgba(255, 255, 255, ${b.alpha * 0.6})`
      ctx.fill()
    })
  }

  function drawBackground(ctx: CanvasRenderingContext2D, canvas: HTMLCanvasElement) {
    const gradient = ctx.createLinearGradient(0, 0, 0, canvas.height)
    gradient.addColorStop(0, '#0a1628')
    gradient.addColorStop(0.4, '#0d2847')
    gradient.addColorStop(0.7, '#1a5276')
    gradient.addColorStop(1, '#2471a3')
    ctx.fillStyle = gradient
    ctx.fillRect(0, 0, canvas.width, canvas.height)

    // 水下光柱
    ctx.globalAlpha = 0.03
    for (let i = 0; i < 8; i++) {
      const x = (i + 0.5) * (canvas.width / 8)
      const shimmer = Math.sin(time * 2 + i) * 20
      const grad = ctx.createLinearGradient(x - 30 + shimmer, 0, x + 30 + shimmer, canvas.height)
      grad.addColorStop(0, '#87ceeb')
      grad.addColorStop(1, 'transparent')
      ctx.fillStyle = grad
      ctx.fillRect(x - 50 + shimmer, 0, 100, canvas.height)
    }
    ctx.globalAlpha = 1

    // 远景水流
    ctx.globalAlpha = 0.08
    for (let i = 0; i < 3; i++) {
      ctx.beginPath()
      for (let x = 0; x <= canvas.width; x += 10) {
        const y = canvas.height * (0.4 + i * 0.15)
          + Math.sin((x + time * 25) * 0.004 + i) * 40
          + Math.sin((x + time * 15) * 0.008 + i * 2) * 20
        if (x === 0) ctx.moveTo(x, y)
        else ctx.lineTo(x, y)
      }
      ctx.strokeStyle = '#5dade2'
      ctx.lineWidth = 3
      ctx.stroke()
    }
    ctx.globalAlpha = 1
  }

  function drawWave(ctx: CanvasRenderingContext2D, canvas: HTMLCanvasElement, wave: Wave) {
    ctx.beginPath()
    ctx.moveTo(0, canvas.height)

    const baseY = canvas.height * wave.yOffset

    for (let x = 0; x <= canvas.width; x += 3) {
      const y = baseY
        + Math.sin(x * wave.frequency + time * wave.speed + wave.phase) * wave.amplitude
        + Math.sin(x * wave.frequency * 2.3 + time * wave.speed * 1.5 + wave.phase * 1.7) * (wave.amplitude * 0.4)
        + Math.sin(x * wave.frequency * 0.7 + time * wave.speed * 0.8 + wave.phase * 0.5) * (wave.amplitude * 0.3)

      if (x === 0) ctx.moveTo(x, y)
      else ctx.lineTo(x, y)
    }

    ctx.lineTo(canvas.width, canvas.height)
    ctx.lineTo(0, canvas.height)
    ctx.closePath()
    ctx.fillStyle = wave.color
    ctx.fill()
  }

  function drawBubbles(ctx: CanvasRenderingContext2D, canvas: HTMLCanvasElement, bubbles: Bubble[]) {
    bubbles.forEach((b, i) => {
      b.y -= b.speed
      b.wobble += b.wobbleSpeed
      b.x += Math.sin(b.wobble) * 0.5

      if (b.y < -20) {
        ambientBubbles[i] = createBubble(canvas)
        ambientBubbles[i].y = canvas.height + 20
      }

      ctx.beginPath()
      ctx.arc(b.x, b.y, b.radius, 0, Math.PI * 2)
      ctx.fillStyle = `rgba(255, 255, 255, ${b.alpha * 0.15})`
      ctx.fill()

      ctx.beginPath()
      ctx.arc(b.x - b.radius * 0.3, b.y - b.radius * 0.3, b.radius * 0.3, 0, Math.PI * 2)
      ctx.fillStyle = `rgba(255, 255, 255, ${b.alpha * 0.3})`
      ctx.fill()
    })
  }

  function drawSpray(ctx: CanvasRenderingContext2D, canvas: HTMLCanvasElement) {
    ctx.globalAlpha = 0.25
    for (let i = 0; i < 25; i++) {
      const x = (Math.sin(time * 0.4 + i * 0.6) * 0.5 + 0.5) * canvas.width
      const baseY = canvas.height * 0.68
      const y = baseY + Math.sin(time * 1.8 + i) * 15 - Math.random() * 30

      if (Math.random() > 0.6) {
        ctx.beginPath()
        ctx.arc(x + (Math.random() - 0.5) * 40, y, Math.random() * 2.5 + 0.5, 0, Math.PI * 2)
        ctx.fillStyle = 'rgba(255, 255, 255, 0.5)'
        ctx.fill()
      }
    }
    ctx.globalAlpha = 1
  }

  function draw() {
    if (!ctx || !canvas) return

    drawBackground(ctx, canvas)

    updateWhale(whale, canvas)
    drawWhale(ctx, whale)

    for (let i = waves.length - 1; i >= 0; i--) {
      drawWave(ctx, canvas, waves[i])
    }

    drawBubbles(ctx, canvas, ambientBubbles)

    time += 0.016
    animationId = requestAnimationFrame(draw)
  }

  draw()
}

function handleResize() {
  const canvas = canvasRef.value
  if (!canvas) return
  canvas.width = canvas.offsetWidth
  canvas.height = canvas.offsetHeight
}

onMounted(() => {
  initCanvas()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  if (animationId !== null) {
    cancelAnimationFrame(animationId)
  }
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.page-content {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.wave-canvas {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}

.wave-overlay {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
  z-index: 10;
  pointer-events: none;
}

.wave-title {
  font-size: 4rem;
  font-weight: 300;
  color: #fff;
  letter-spacing: 1rem;
  margin: 0;
  text-shadow: 0 0 40px rgba(0, 150, 255, 0.5);
  animation: glow 3s ease-in-out infinite;
}

.wave-subtitle {
  font-size: 1.5rem;
  color: rgba(255, 255, 255, 0.7);
  margin-top: 1rem;
  letter-spacing: 0.3rem;
}

@keyframes glow {
  0%, 100% { text-shadow: 0 0 40px rgba(0, 150, 255, 0.5); }
  50% { text-shadow: 0 0 60px rgba(0, 200, 255, 0.8), 0 0 80px rgba(0, 150, 255, 0.6); }
}
</style>
