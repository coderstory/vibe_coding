# Technology Stack: Beach Theme CSS Animations

**Project:** Summer Beach Theme for Vue + Element Plus Admin Dashboard
**Researched:** 2026-04-03
**Confidence:** HIGH (sources: MDN official documentation)

---

## Executive Summary

CSS-only animations for beach theme require focusing on `transform` and `opacity` for GPU acceleration. Wave and bubble effects use `@keyframes` with `translateY` and `scale` transforms. `backdrop-filter` enables glassmorphism and is now Baseline 2024 (widely supported). `will-change` should be used sparingly as a last-resort optimization.

---

## 1. Wave Animations (Performance-Friendly)

### Recommended Approach
Use CSS `@keyframes` with `transform: translateY()` on SVG wave shapes.

### Core Properties
```css
/* Wave container - use overflow:hidden for clip */
.wave-container {
  position: relative;
  overflow: hidden;
  height: 100px;
}

/* Wave element - animate translateY for floating effect */
.wave {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 200%;
  height: 100%;
  background: linear-gradient(180deg, rgba(255,255,255,0.3) 0%, rgba(255,255,255,0) 100%);
  border-radius: 40% 40% 0 0;
  
  animation: wave-float 8s ease-in-out infinite;
  will-change: transform;
}

/* Staggered second wave */
.wave:nth-child(2) {
  left: -50%;
  animation-delay: -2s;
  opacity: 0.6;
}

@keyframes wave-float {
  0%, 100% {
    transform: translateY(0) translateX(0);
  }
  50% {
    transform: translateY(-15px) translateX(10px);
  }
}
```

### Performance Guidelines
| Property | Performance | Notes |
|----------|-------------|-------|
| `transform: translateY()` | ✅ Best | GPU-accelerated, no layout recalc |
| `transform: scale()` | ✅ Best | GPU-accelerated |
| `opacity` | ✅ Best | GPU-accelerated |
| `background-position` | ⚠️ Moderate | Can cause repaints |
| `box-shadow` | ❌ Avoid | Triggers layout/paint |

### will-change Usage
```css
/* Good: Apply only during animation */
.wave {
  will-change: transform;
  animation: wave-float 8s ease-in-out infinite;
}

/* Better: Use JS to toggle before/after animation */
/*
element.addEventListener('mouseenter', () => {
  element.style.willChange = 'transform';
});
element.addEventListener('animationend', () => {
  element.style.willChange = 'auto';
});
*/
```

---

## 2. Bubble Floating Effects

### CSS-Only Approach
Multiple pseudo-elements or divs with varied `animation-delay` and `animation-duration`.

### Core Implementation
```css
.bubble-container {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
}

.bubble {
  position: absolute;
  bottom: -20px;
  width: 20px;
  height: 20px;
  background: radial-gradient(circle at 30% 30%, 
    rgba(255,255,255,0.8) 0%, 
    rgba(255,255,255,0.2) 50%,
    rgba(255,255,255,0) 100%);
  border-radius: 50%;
  
  animation: bubble-rise 6s ease-in infinite;
  will-change: transform, opacity;
}

/* Vary bubble sizes, positions, and timings */
.bubble:nth-child(1) { left: 10%; width: 15px; height: 15px; animation-duration: 7s; animation-delay: 0s; }
.bubble:nth-child(2) { left: 25%; width: 25px; height: 25px; animation-duration: 5s; animation-delay: 1s; }
.bubble:nth-child(3) { left: 50%; width: 12px; height: 12px; animation-duration: 8s; animation-delay: 2s; }
.bubble:nth-child(4) { left: 75%; width: 20px; height: 20px; animation-duration: 6s; animation-delay: 0.5s; }
.bubble:nth-child(5) { left: 90%; width: 18px; height: 18px; animation-duration: 7.5s; animation-delay: 3s; }

@keyframes bubble-rise {
  0% {
    transform: translateY(0) scale(1);
    opacity: 0;
  }
  10% {
    opacity: 0.8;
  }
  90% {
    opacity: 0.6;
  }
  100% {
    transform: translateY(-100vh) scale(0.5);
    opacity: 0;
  }
}
```

### Alternative: Single Element with Multiple Shadows
```css
.single-bubble {
  position: absolute;
  bottom: 0;
  width: 30px;
  height: 30px;
  background: radial-gradient(circle at 30% 30%, 
    rgba(255,255,255,0.9) 0%, 
    rgba(255,255,255,0.1) 70%);
  border-radius: 50%;
  
  animation: bubble-rise 8s ease-in-out infinite;
  box-shadow: 
    20px 10px 0 -3px rgba(255,255,255,0.3),
    50px 20px 0 -6px rgba(255,255,255,0.2),
    80px 5px 0 -4px rgba(255,255,255,0.4);
}

@keyframes bubble-rise {
  0% { transform: translateY(0); }
  100% { transform: translateY(-100vh); }
}
```

---

## 3. Glassmorphism / Blur Effects

### backdrop-filter (Baseline 2024)
```css
.glass-effect {
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px); /* Safari */
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}
```

### Browser Compatibility
| Browser | Support | Prefix Needed |
|---------|---------|---------------|
| Chrome 76+ | ✅ | No |
| Firefox 103+ | ✅ | No |
| Safari 9+ | ✅ | `-webkit-` always |
| Edge 79+ | ✅ | No |
| **Note** | Baseline 2024 = Widely available across all modern browsers | |

### Filter Functions Available
```css
/* All Baseline 2024 supported */
backdrop-filter: blur(10px);
backdrop-filter: brightness(60%);
backdrop-filter: contrast(40%);
backdrop-filter: drop-shadow(4px 4px 10px blue);
backdrop-filter: grayscale(30%);
backdrop-filter: hue-rotate(120deg);
backdrop-filter: invert(70%);
backdrop-filter: opacity(20%);
backdrop-filter: sepia(90%);
backdrop-filter: saturate(80%);

/* Multiple filters combined */
backdrop-filter: blur(4px) saturate(150%);
```

### Fallback for Unsupported Browsers
```css
.glass-effect {
  background: rgba(255, 255, 255, 0.3);
}

/* Feature query for enhanced experience */
@supports (backdrop-filter: blur(20px)) {
  .glass-effect {
    background: rgba(255, 255, 255, 0.15);
    backdrop-filter: blur(20px);
    -webkit-backdrop-filter: blur(20px);
  }
}
```

---

## 4. CSS Animation Performance Best Practices

### Animatable Properties Priority
| Priority | Properties | Notes |
|----------|------------|-------|
| **GPU-Accelerated** | `transform`, `opacity`, `filter` | Use these first |
| **Layout Triggers** | `width`, `height`, `margin`, `padding` | Avoid during animation |
| **Paint Triggers** | `background`, `border`, `box-shadow` | Use sparingly |

### Animation Checklist
```css
/* 1. Use transform/opacity */
.element {
  transform: translate3d(0, 0, 0); /* Forces GPU layer */
  opacity: 1;
  transition: transform 0.3s ease, opacity 0.3s ease;
}

/* 2. Use ease timing, not linear */
.ease {
  animation-timing-function: ease-out; /* Natural feel */
  /* or use cubic-bezier for custom curves */
}

/* 3. Limit animation scope */
.animated-element {
  contain: layout paint; /* Reduces layout scope */
}

/* 4. Pause when not visible */
@media (prefers-reduced-motion: reduce) {
  .wave, .bubble {
    animation: none;
  }
}
```

### Reduced Motion Support
```css
@media (prefers-reduced-motion: reduce) {
  * {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
  }
}
```

---

## 5. will-change Guidelines (from MDN)

### When to Use
- **Last resort** for existing performance problems
- **Page-flip transitions** (slideshows, albums)
- **Elements that animate frequently**

### When NOT to Use
- **Never** for elements that don't animate
- **Never** "just in case" for performance
- **Never** on too many elements (memory bloat)

### Correct Usage Pattern
```javascript
// Preferred: Toggle will-change via JS
const el = document.querySelector('.animated-element');

el.addEventListener('mouseenter', () => {
  el.style.willChange = 'transform, opacity';
});

el.addEventListener('animationend', () => {
  el.style.willChange = 'auto';
});
```

### CSS-Only Alternative
```css
/* Only if animation is continuous/permanent */
.wave {
  will-change: transform;
  animation: wave-float 8s ease-in-out infinite;
}
```

---

## 6. Element Plus Component Overrides

### Dialog
```css
/* Glass effect for el-dialog */
.el-dialog {
  background: rgba(255, 255, 255, 0.15) !important;
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 16px;
}

/* Dialog body */
.el-dialog__body {
  background: transparent;
  color: white;
}
```

### Table
```css
/* Glass effect for el-table */
.el-table {
  background: rgba(255, 255, 255, 0.1) !important;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border-radius: 12px;
  overflow: hidden;
}

/* Table header */
.el-table__header th {
  background: rgba(255, 255, 255, 0.2) !important;
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  color: white;
  border: none !important;
}

/* Table rows */
.el-table__body tr {
  background: transparent !important;
}

.el-table__body td {
  background: rgba(255, 255, 255, 0.05) !important;
  border: none !important;
  color: white;
}

.el-table__body tr:hover > td {
  background: rgba(255, 255, 255, 0.15) !important;
}
```

### Pagination
```css
.el-pagination {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border-radius: 8px;
  padding: 8px 16px;
}

.el-pagination button,
.el-pager li {
  background: rgba(255, 255, 255, 0.2) !important;
  color: white !important;
}

.el-pager li.is-active {
  background: rgba(255, 255, 255, 0.4) !important;
}
```

### Form Elements
```css
/* Glass inputs */
.el-input__wrapper {
  background: rgba(255, 255, 255, 0.15) !important;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2) !important;
  box-shadow: none !important;
  border-radius: 8px;
}

.el-input__inner {
  color: white !important;
}

.el-input__inner::placeholder {
  color: rgba(255, 255, 255, 0.6) !important;
}
```

---

## Sources

| Source | Confidence | Relevance |
|--------|------------|-----------|
| [MDN: backdrop-filter](https://developer.mozilla.org/en-US/docs/Web/CSS/Reference/Properties/backdrop-filter) | HIGH | Official documentation, Baseline 2024 |
| [MDN: CSS Animations](https://developer.mozilla.org/en-US/docs/Web/CSS/Guides/Animations/Using) | HIGH | Official guide |
| [MDN: will-change](https://developer.mozilla.org/en-US/docs/Web/CSS/Reference/Properties/will-change) | HIGH | Official documentation |

---

## Quick Reference Cheatsheet

```css
/* Wave animation */
@keyframes wave-float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-15px); }
}

/* Bubble animation */
@keyframes bubble-rise {
  0% { transform: translateY(0) scale(1); opacity: 0; }
  10% { opacity: 0.8; }
  100% { transform: translateY(-100vh) scale(0.5); opacity: 0; }
}

/* Glassmorphism */
.glass {
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 16px;
}

/* Performance optimization */
.performance-ready {
  will-change: transform;
  contain: layout paint;
}

/* Reduced motion */
@media (prefers-reduced-motion: reduce) {
  .wave, .bubble { animation: none; }
}
```
