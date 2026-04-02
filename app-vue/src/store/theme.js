import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { getTheme, saveTheme } from '@/api/settings'

export const useThemeStore = defineStore('theme', () => {
  const theme = ref(localStorage.getItem('theme') || 'light')
  const isLoading = ref(false)
  
  const isDark = computed(() => theme.value === 'dark')
  
  function setTheme(newTheme) {
    theme.value = newTheme
    localStorage.setItem('theme', newTheme)
    applyTheme(newTheme)
  }
  
  function toggleTheme() {
    const newTheme = theme.value === 'light' ? 'dark' : 'light'
    setTheme(newTheme)
  }
  
  function applyTheme(themeName) {
    document.documentElement.setAttribute('data-theme', themeName)
  }
  
  function initTheme() {
    applyTheme(theme.value)
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
  
  return { 
    theme, 
    isDark, 
    isLoading, 
    setTheme, 
    toggleTheme, 
    applyTheme,
    initTheme,
    loadTheme,
    syncTheme 
  }
})
