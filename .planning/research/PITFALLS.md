# Domain Pitfalls: Beach Theme Completion

**Domain:** Vue 3 + Element Plus beach-themed admin dashboard
**Researched:** 2026-04-03
**Confidence:** MEDIUM

## Critical Pitfalls

### 1. Vue CSS Animation Pitfalls

#### Pitfall: Scoped Styles Breaking Transitions
**What goes wrong:** Vue's scoped styles add data-v attributes that can interfere with CSS transition selectors like `v-enter-active`.

**Prevention:** 
- Use `:deep()` selector for transitioning child components
- Apply transitions at the parent level, not inside scoped child components
- For Element Plus components, wrap in transition inside your component, not modify component internals

```vue
<!-- Wrong: Transition inside scoped component -->
<template>
  <div class="wave-container" v-if="show">
    <!-- CSS .wave-container v-enter-active won't work properly -->
  </div>
</template>

<!-- Correct: Transition at parent level or use non-scoped wrapper -->
<template>
  <Transition name="wave">
    <div v-if="show" class="wave-container">
      <!-- Works because transition CSS is at parent level -->
    </div>
  </Transition>
</template>
```

#### Pitfall: RequestAnimationFrame Memory Leaks
**What goes wrong:** Wave/bubble animations using RAF are not cleaned up when component unmounts.

**Prevention:**
- Always cancel RAF in `onUnmounted`
- Store RAF ID in a ref

```javascript
const animationId = ref(null)

function animate() {
  // ... animation logic
  animationId.value = requestAnimationFrame(animate)
}

onMounted(() => { animationId.value = requestAnimationFrame(animate) })

onUnmounted(() => {
  if (animationId.value) cancelAnimationFrame(animationId.value)
})
```

#### Pitfall: CSS Animation Performance in Vue Reactive Context
**What goes wrong:** Animating reactive data on every frame triggers Vue's reactivity system, causing performance issues.

**Prevention:**
- Never animate Vue refs directly in RAF loops
- Use plain variables for animation state, sync to Vue state only on completion

---

### 2. Element Plus Theming Pitfalls

#### Pitfall: CSS Variable Specificity Wars
**What goes wrong:** Element Plus uses BEM CSS classes with high specificity. Your custom CSS variables get overridden.

**Prevention:**
- Always use `:root` or a wrapping class with higher specificity
- Prefer using Element Plus's SCSS variable override system over CSS variables for theme-wide changes
- Check computed styles in DevTools to verify your variables are applied

```css
/* Wrong: May not override Element Plus */
.el-button { --el-button-bg-color: beach-amber; }

/* Correct: Wrapped with higher specificity */
.beach-theme .el-button { --el-button-bg-color: beach-amber; }

/* Best: Use Element Plus's recommended SCSS override */
@forward 'element-plus/theme-chalk/src/common/var.scss' with (
  $colors: ('primary': ('base': beach-amber))
)
```

#### Pitfall: Incomplete Component Theming
**What goes wrong:** Theming one component state (hover) but not others (active, disabled).

**Prevention:**
- Use CSS variable browser DevTools to inspect ALL states
- Create a theme checklist: default, hover, active, disabled, focus, loading
- Test with `prefers-reduced-motion` media query

**Common Element Plus CSS variable groups to check for beach theme:**
```css
:root {
  /* Primary colors */
  --el-color-primary: #f0c674;           /* Beach amber */
  --el-color-primary-light-3: #f4d89a;
  --el-color-primary-light-5: #f7e4b8;
  --el-color-primary-light-7: #faefd4;
  --el-color-primary-light-8: #fcf3e3;
  --el-color-primary-light-9: #fdf7ed;
  
  /* Background */
  --el-bg-color: #faf8f5;                /* Sand white */
  --el-bg-color-overlay: #ffffff;
  
  /* Text */
  --el-text-color-primary: #2c3e50;      /* Deep ocean */
  
  /* Border */
  --el-border-color: #e8dfd5;            /* Sand border */
}
```

#### Pitfall: Overwriting Not Extending
**What goes wrong:** Using Element Plus SCSS overrides replaces entire color maps instead of merging.

**Prevention:** Use `map.deep-merge()` when overriding color maps in SCSS.

---

### 3. Glassmorphism Effect Pitfalls

#### Pitfall: Backdrop-Filter Performance on Mobile
**What goes wrong:** `backdrop-filter: blur()` causes severe performance issues on mobile devices, especially older iPhones and low-end Android.

**Prevention:**
- Use `@supports (backdrop-filter: blur(1px))` to detect support
- Provide a solid semi-transparent fallback
- Reduce blur radius on mobile (2-4px instead of 10-20px)

```css
.glass-card {
  background: rgba(255, 255, 255, 0.2);
  
  @supports (backdrop-filter: blur(1px)) {
    backdrop-filter: blur(10px);
  }
  
  /* Fallback for no support */
  @supports not (backdrop-filter: blur(1px)) {
    background: rgba(255, 255, 255, 0.4);
  }
}
```

#### Pitfall: Text Becomes Unreadable
**What goes wrong:** Glassmorphism over varying backgrounds (images, wave animations) makes text unreadable.

**Prevention:**
- Always ensure sufficient contrast ratio (minimum 4.5:1 for body text)
- Add subtle text-shadow or separate text from glass panel
- Test over actual wave/animated backgrounds

#### Pitfall: Background Bleeding Through Interactive Elements
**What goes wrong:** Buttons and inputs inside glass containers show background bleeding through.

**Prevention:**
- Apply `backdrop-filter` to containers, not interactive elements
- Give interactive elements solid backgrounds with glass-like borders
- Use `isolation: isolate` on glass containers

---

### 4. Wave/Bubble Animation Pitfalls

#### Pitfall: Infinite Animation Jank
**What goes wrong:** Wave animations stutter or jump when the animation duration doesn't divide evenly into the container.

**Prevention:**
- Ensure wave width is exactly 50% or 100% of container (allows seamless loop)
- Use `translateX(-50%)` to `translateX(0)` for horizontal waves

```css
@keyframes wave {
  0% { transform: translateX(-50%); }
  100% { transform: translateX(0); }
}

.wave {
  animation: wave 8s linear infinite; /* 8s allows clean loop with 200% width */
}
```

#### Pitfall: Multiple Animations Desyncing
**What goes wrong:** Multiple bubbles with different durations drift apart over time, looking chaotic.

**Prevention:**
- Use CSS custom properties for animation duration so they can be synchronized
- Consider staggered start times using `animation-delay` (scaled by container size)

#### Pitfall: GPU Memory Exhaustion
**What goes wrong:** Many animated elements with complex SVG paths cause GPU memory issues.

**Prevention:**
- Limit simultaneous animated bubbles to 15-20
- Use CSS `will-change: transform` sparingly (only on actively animating elements)
- Remove off-screen animated elements from DOM

---

## Moderate Pitfalls

### Transition Performance
**What goes wrong:** Vue `<Transition>` with large lists causes layout thrashing.

**Prevention:**
- Use `<TransitionGroup>` with `v-move` class for list animations
- Avoid animating layout properties (width, height, margin)

### Element Plus Dark Mode Conflicts
**What goes wrong:** Custom beach theme colors get overridden when Element Plus dark mode activates.

**Prevention:**
- Scope beach theme under `.light-mode` or similar
- Check for `prefers-color-scheme` media query overrides

### Z-Index Layering
**What goes wrong:** Glassmorphism elements appear behind other UI despite high z-index.

**Prevention:**
- Create explicit z-index layers: waves (1-10), glass (20-30), content (40+), modals (100+)

---

## Phase-Specific Warnings

| Phase | Likely Pitfall | Mitigation |
|-------|---------------|------------|
| Phase 1: Base Theme | CSS variable specificity | Test Element Plus components early, establish override pattern |
| Phase 2: Glassmorphism | Mobile performance | Use @supports, reduce blur on mobile |
| Phase 3: Animations | RAF cleanup, jank | Implement proper lifecycle cleanup, test on actual devices |
| Phase 4: Polish | Animation desync, z-index | Create animation constants, document z-index scale |

---

## Sources

- [Element Plus Theming Guide](https://element-plus.org/en-US/guide/theming.html) — HIGH confidence
- [Vue Transition API](https://vuejs.org/api/built-in-components.html#transition) — HIGH confidence
- [CSS Animation Performance](https://developer.mozilla.org/en-US/docs/Web/Performance/Animation_performance) — MEDIUM confidence
- [Glassmorphism Best Practices](https://web.dev/glassmorphism/) — MEDIUM confidence
