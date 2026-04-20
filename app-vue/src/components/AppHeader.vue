<script setup lang="ts">
/**
 * 应用顶部导航栏组件
 * 显示用户信息和动画切换控制
 */
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'
import { useAnimationToggle } from '@/composables/useAnimationToggle'

const router = useRouter()
const userStore = useUserStore()
const { animationsEnabled, toggleAnimations } = useAnimationToggle()

// 显示用户名，取 name 或 username
const username = computed(() => userStore.user?.name || userStore.user?.username || '未登录')
// 头像显示用户名的第一个字符
const avatar = computed(() => userStore.user?.name?.charAt(0) || 'U')

/**
 * 处理下拉菜单命令
 */
async function handleCommand(command: string) {
  if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })

      await userStore.logout()
      router.push('/login')
    } catch {
      // 用户取消操作
    }
  } else if (command === 'profile') {
    // TODO: 跳转到个人中心
  }
}
</script>

<template>
  <div class="app-header">
    <div class="header-right">
      <!-- 动画开关按钮 -->
      <el-tooltip :content="animationsEnabled ? '关闭动画' : '开启动画'" placement="bottom">
        <el-button :icon="animationsEnabled ? 'VideoPause' : 'VideoPlay'" circle @click="toggleAnimations" />
      </el-tooltip>

      <!-- 用户下拉菜单 -->
      <el-dropdown @command="handleCommand">
        <div class="user-info">
          <el-avatar :size="32" class="user-avatar">
            {{ avatar }}
          </el-avatar>
          <span class="username">{{ username }}</span>
          <el-icon><ArrowDown /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">
              <el-icon><User /></el-icon>
              个人中心
            </el-dropdown-item>
            <el-dropdown-item command="logout" divided>
              <el-icon><SwitchButton /></el-icon>
              退出登录
            </el-dropdown-item>
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
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 8px 16px;
  border-radius: var(--el-border-radius-round);
  transition: all 0.3s;
  background: transparent;
}

.user-info:hover {
  background: linear-gradient(135deg, #fef3c7, #fde68a);
}

.user-avatar {
  background: linear-gradient(135deg, #3b82f6, #1e3a8a);
  color: #fff;
  font-weight: 600;
  font-size: 14px;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.4);
}

.username {
  font-size: 14px;
  font-weight: 500;
  color: #1e293b;
}

.user-info:hover .username {
  color: #92400e;
}
</style>