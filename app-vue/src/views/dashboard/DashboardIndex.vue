<script lang="ts">
/**
 * 仪表盘首页。马卡龙配色导航卡片 + 动态渐变背景。
 */
</script>

<script lang="ts" setup>
import type { Component } from 'vue'
import { useRouter } from 'vue-router'
import {
  User, Setting, Monitor, Clock, DataBoard, Folder
} from '@element-plus/icons-vue'

const router = useRouter()

interface DashCard {
  title: string
  desc: string
  icon: Component
  route: string
  color: string
  gradient: string
}

const cards: DashCard[] = [
  {
    title: '用户管理',
    desc: '管理系统用户账号与权限',
    icon: User,
    route: '/system/user',
    color: '#FFB5C2',
    gradient: 'linear-gradient(135deg, #FFB5C2, #FF8FA3)'
  },
  {
    title: '角色管理',
    desc: '配置角色与权限分配',
    icon: Setting,
    route: '/system/role',
    color: '#C7CEEA',
    gradient: 'linear-gradient(135deg, #C7CEEA, #A8B5DF)'
  },
  {
    title: '硬件监控',
    desc: '实时 CPU、内存、磁盘监控',
    icon: Monitor,
    route: '/monitor/hardware',
    color: '#B5EAD7',
    gradient: 'linear-gradient(135deg, #B5EAD7, #7DD4B1)'
  },
  {
    title: '监控大盘',
    desc: '系统运行状态总览',
    icon: DataBoard,
    route: '/monitor',
    color: '#A8D8EA',
    gradient: 'linear-gradient(135deg, #A8D8EA, #7EC8E3)'
  },
  {
    title: '秒杀管理',
    desc: '秒杀活动与商品配置',
    icon: Clock,
    route: '/seckill',
    color: '#FFDAC1',
    gradient: 'linear-gradient(135deg, #FFDAC1, #FFB088)'
  },
  {
    title: '审计日志',
    desc: '查看系统操作记录',
    icon: Folder,
    route: '/system/audit',
    color: '#E2F0CB',
    gradient: 'linear-gradient(135deg, #E2F0CB, #C5DFA0)'
  }
]

function goRoute(route: string) {
  router.push(route)
}
</script>

<template>
  <div class="dash-container">
    <div class="dash-bg">
      <div class="bg-orb bg-orb-1" />
      <div class="bg-orb bg-orb-2" />
      <div class="bg-orb bg-orb-3" />
      <div class="bg-orb bg-orb-4" />
      <div class="bg-float circle-1" />
      <div class="bg-float circle-2" />
      <div class="bg-float circle-3" />
      <div class="bg-float circle-4" />
      <div class="bg-float circle-5" />
    </div>

    <div class="dash-content">
      <div class="dash-header">
        <h1 class="dash-title">欢迎回来</h1>
        <p class="dash-subtitle">AI 驱动的管理系统 · 选择功能模块开始工作</p>
      </div>

      <div class="dash-grid">
        <div
          v-for="card in cards"
          :key="card.title"
          class="dash-card"
          :style="{ '--card-grad': card.gradient, '--card-color': card.color }"
          @click="goRoute(card.route)"
        >
          <div class="card-glow" />
          <div class="card-icon-wrap">
            <el-icon :size="28"><component :is="card.icon" /></el-icon>
          </div>
          <h3 class="card-title">{{ card.title }}</h3>
          <p class="card-desc">{{ card.desc }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.dash-container {
  position: relative;
  width: 100%;
  min-height: 100%;
  overflow: hidden;
}

/* ===== 动态渐变背景 ===== */
.dash-bg {
  position: absolute;
  inset: -50%;
  z-index: 0;
  background: linear-gradient(300deg, #fce4ec, #e8eaf6, #e0f7fa, #f3e5f5, #fff3e0, #f1f8e9);
  background-size: 400% 400%;
  animation: bg-shift 20s ease-in-out infinite;
}

@keyframes bg-shift {
  0%   { transform: scale(1) rotate(0deg); }
  25%  { transform: scale(1.05) rotate(1deg); }
  50%  { transform: scale(1) rotate(0deg); }
  75%  { transform: scale(1.05) rotate(-1deg); }
  100% { transform: scale(1) rotate(0deg); }
}

/* ===== 装饰光晕 ===== */
.bg-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.35;
  animation: orb-float 12s ease-in-out infinite;
}

.bg-orb-1 { width: 500px; height: 500px; top: -10%; left: -5%; background: #FFB5C2; }
.bg-orb-2 { width: 400px; height: 400px; bottom: -5%; right: -5%; background: #B5EAD7; animation-delay: -3s; }
.bg-orb-3 { width: 350px; height: 350px; top: 30%; right: 15%; background: #C7CEEA; animation-delay: -6s; }
.bg-orb-4 { width: 300px; height: 300px; bottom: 20%; left: 10%; background: #FFDAC1; animation-delay: -9s; }

@keyframes orb-float {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(30px, -30px) scale(1.1); }
  66% { transform: translate(-20px, 20px) scale(0.9); }
}

/* ===== 浮动小圆 ===== */
.bg-float {
  position: absolute;
  border-radius: 50%;
  opacity: 0.2;
  animation: float-rise 15s ease-in-out infinite;
}

.circle-1 { width: 60px; height: 60px; top: 15%; left: 8%; background: #FFB5C2; animation-delay: 0s; }
.circle-2 { width: 40px; height: 40px; top: 60%; left: 20%; background: #C7CEEA; animation-delay: -4s; }
.circle-3 { width: 80px; height: 80px; top: 40%; right: 10%; background: #B5EAD7; animation-delay: -7s; }
.circle-4 { width: 35px; height: 35px; bottom: 30%; right: 25%; background: #FFDAC1; animation-delay: -10s; }
.circle-5 { width: 50px; height: 50px; bottom: 10%; left: 40%; background: #E2F0CB; animation-delay: -13s; }

@keyframes float-rise {
  0%, 100% { transform: translateY(0) scale(1); opacity: 0.2; }
  50% { transform: translateY(-40px) scale(1.2); opacity: 0.35; }
}

/* ===== 内容层 ===== */
.dash-content {
  position: relative;
  z-index: 1;
  padding: 40px 48px;
}

.dash-header {
  text-align: center;
  margin-bottom: 48px;
}

.dash-title {
  font-size: 2.4rem;
  font-weight: 300;
  color: #4a4a6a;
  margin: 0 0 8px;
  letter-spacing: 2px;
}

.dash-subtitle {
  font-size: 1rem;
  color: #8888aa;
  margin: 0;
  letter-spacing: 1px;
}

/* ===== 卡片网格 ===== */
.dash-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
  max-width: 960px;
  margin: 0 auto;
}

/* ===== 卡片 ===== */
.dash-card {
  position: relative;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(16px);
  border-radius: 20px;
  padding: 32px 24px;
  cursor: pointer;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.5);
  transition: transform 0.4s cubic-bezier(0.34, 1.56, 0.64, 1), box-shadow 0.4s ease;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
}

.dash-card:hover {
  transform: translateY(-8px) scale(1.02);
  box-shadow: 0 16px 48px rgba(0, 0, 0, 0.12);
}

/* 卡片顶部彩色条 */
.dash-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: var(--card-grad);
  transition: height 0.3s ease;
}

.dash-card:hover::before {
  height: 6px;
}

/* 图标 */
.card-icon-wrap {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  border-radius: 16px;
  background: var(--card-grad);
  color: #fff;
  margin-bottom: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: transform 0.4s cubic-bezier(0.34, 1.56, 0.64, 1), box-shadow 0.4s ease;
}

.dash-card:hover .card-icon-wrap {
  transform: scale(1.1) rotate(-5deg);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

/* 标题 */
.card-title {
  font-size: 1.1rem;
  font-weight: 600;
  color: #4a4a6a;
  margin: 0 0 6px;
  transition: color 0.3s;
}

.dash-card:hover .card-title {
  color: #2d2d4a;
}

/* 描述 */
.card-desc {
  font-size: 0.85rem;
  color: #9999bb;
  margin: 0;
  line-height: 1.5;
}
</style>
