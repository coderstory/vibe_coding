import { ref } from 'vue'
import { defineStore } from 'pinia'

export const useThemeStore = defineStore('theme', () => {
  const theme = ref('light')
  
  function setTheme(newTheme) {
    theme.value = newTheme
  }
  
  return { 
    theme, 
    setTheme
  }
})
