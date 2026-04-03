<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'
import { useThemeStore } from '@/store/theme'

const router = useRouter()
const userStore = useUserStore()
const themeStore = useThemeStore()

const username = computed(() => userStore.user?.name || userStore.user?.username || '未登录')
const avatar = computed(() => userStore.user?.name?.charAt(0) || 'U')

async function handleCommand(command) {
  if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      
      await userStore.logout()
      router.push('/login')
    } catch (e) {
      // 用户取消
    }
  } else if (command === 'profile') {
    // TODO: 跳转到个人中心
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

.theme-btn {
  font-size: 20px;
  padding: 8px;
  border-radius: 8px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.user-avatar {
  background: #409eff;
  color: #fff;
}

.username {
  font-size: 14px;
  color: #303133;
}

:deep(.dark) .theme-btn {
  color: #e0e0e0;
}

:deep(.dark) .username {
  color: #e0e0e0;
}
</style>
