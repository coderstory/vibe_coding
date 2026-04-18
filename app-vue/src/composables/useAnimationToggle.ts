import { ref, onMounted, onUnmounted } from 'vue'

export function useAnimationToggle() {
  const animationsEnabled = ref(true)

  function toggleAnimations() {
    animationsEnabled.value = !animationsEnabled.value
    document.body.classList.toggle('animations-disabled', !animationsEnabled.value)
  }

  function setAnimations(value: boolean) {
    animationsEnabled.value = value
    document.body.classList.toggle('animations-disabled', !value)
  }

  const mediaQuery = window.matchMedia('(prefers-reduced-motion: reduce)')

  function handleReducedMotion(e: MediaQueryListEvent) {
    if (e.matches) {
      setAnimations(false)
    }
  }

  onMounted(() => {
    mediaQuery.addEventListener('change', handleReducedMotion)
    if (mediaQuery.matches) {
      setAnimations(false)
    }
  })

  onUnmounted(() => {
    mediaQuery.removeEventListener('change', handleReducedMotion)
  })

  return {
    animationsEnabled,
    toggleAnimations,
    setAnimations
  }
}
