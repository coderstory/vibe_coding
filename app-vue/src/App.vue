<script setup>
import { onMounted } from 'vue'
import { useThemeStore } from '@/store/theme'

const themeStore = useThemeStore()

onMounted(() => {
  themeStore.initTheme()
})
</script>

<template>
  <router-view />
</template>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}
</style>
