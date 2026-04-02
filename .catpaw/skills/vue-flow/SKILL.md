---
name: vue-flow
description: 当使用Vue Flow 时，请阅读本指南。
---

# Vue Flow 使用 Skill

## 概述

Vue Flow 是一个用于构建交互式流程图和图表的 Vue 3 库。本 Skill 提供 Vue Flow 的完整使用指南。

## 安装

```bash
npm install @vue-flow/core @vue-flow/background @vue-flow/controls @vue-flow/minimap
```

## 基础用法

### 1. 引入样式

```vue
<style>
@import '@vue-flow/core/dist/style.css';
@import '@vue-flow/core/dist/theme-default.css';
</style>
```

### 2. 基础组件

```vue
<template>
  <VueFlow
    v-model:nodes="nodes"
    v-model:edges="edges"
    :default-viewport="{ x: 0, y: 0, zoom: 1 }"
    :min-zoom="0.5"
    :max-zoom="2"
    fit-view-on-init
    @connect="onConnect"
    @node-click="onNodeClick"
  >
    <Background pattern-color="#eee" :gap="20" />
    <Controls />
    <MiniMap />
  </VueFlow>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { VueFlow, useVueFlow } from '@vue-flow/core'
import { Background } from '@vue-flow/background'
import { Controls } from '@vue-flow/controls'
import { MiniMap } from '@vue-flow/minimap'
import type { Node, Edge, Connection } from '@vue-flow/core'

const { fitView } = useVueFlow()

const nodes = ref<Node[]>([
  { 
    id: '1', 
    type: 'input',
    position: { x: 250, y: 5 }, 
    data: { label: '开始' } 
  },
  { 
    id: '2', 
    position: { x: 100, y: 100 }, 
    data: { label: '审批' } 
  },
  { 
    id: '3', 
    type: 'output',
    position: { x: 250, y: 200 }, 
    data: { label: '结束' } 
  }
])

const edges = ref<Edge[]>([
  { id: 'e1-2', source: '1', target: '2', animated: true },
  { id: 'e2-3', source: '2', target: '3', animated: true }
])

const onConnect = (connection: Connection) => {
  edges.value.push({
    id: `e${Date.now()}`,
    source: connection.source!,
    target: connection.target!,
    animated: true
  })
}
</script>
```

### 3. 容器样式要求

**重要**：Vue Flow 的父容器必须有明确的宽高：

```vue
<div style="width: 100%; height: 500px;">
  <VueFlow :nodes="nodes" :edges="edges" />
</div>
```

或使用 CSS：

```scss
.canvas {
  width: 100%;
  height: 500px;
}
```

## 自定义节点

### 1. 创建自定义节点组件

```vue
<!-- CustomNode.vue -->
<script setup lang="ts">
import { Handle, Position } from '@vue-flow/core'
import type { NodeProps } from '@vue-flow/core'

interface CustomNodeData {
  label: string
  [key: string]: any
}

type Props = NodeProps<CustomNodeData>

const props = defineProps<Props>()
</script>

<template>
  <div class="custom-node">
    <!-- 输入连接点 -->
    <Handle type="target" :position="Position.Top" />
    
    <div class="node-content">
      {{ props.data.label }}
    </div>
    
    <!-- 输出连接点 -->
    <Handle type="source" :position="Position.Bottom" />
  </div>
</template>

<style scoped>
.custom-node {
  padding: 10px;
  border: 2px solid #409eff;
  border-radius: 4px;
  background: #ecf5ff;
}
</style>
```

### 2. 注册自定义节点

```vue
<script setup lang="ts">
import { ref, markRaw } from 'vue'
import { VueFlow } from '@vue-flow/core'
import CustomNode from './CustomNode.vue'

// 使用 markRaw 避免 Vue 将组件转换为响应式对象
const nodeTypes = {
  custom: markRaw(CustomNode)
}
</script>

<template>
  <VueFlow
    :nodes="nodes"
    :edges="edges"
    :node-types="nodeTypes"
  />
</template>
```

### 3. 节点数据结构

```typescript
interface Node<NodeData = any> {
  id: string
  type?: string
  position: { x: number; y: number }
  data?: NodeData
  style?: CSSProperties
  class?: string
  label?: string
  animated?: boolean
  selected?: boolean
  dragging?: boolean
  hidden?: boolean
  selectable?: boolean
  connectable?: boolean
  focusable?: boolean
  draggable?: boolean
  origin?: 'center' | [number, number]
}
```

## 自定义边

### 1. 使用内置边类型

```typescript
const edges = ref([
  { 
    id: 'e1', 
    source: '1', 
    target: '2',
    type: 'smoothstep',  // bezier, smoothstep, straight
    animated: true,
    markerEnd: { type: 'arrowclosed' },
    label: '审批通过'
  }
])
```

### 2. 创建自定义边组件

```vue
<!-- CustomEdge.vue -->
<script setup lang="ts">
import { BaseEdge, getBezierPath, EdgeProps } from '@vue-flow/core'

const props = defineProps<EdgeProps>()

// 计算路径
const pathParams = getBezierPath({
  sourceX: props.sourceX,
  sourceY: props.sourceY,
  sourcePosition: props.sourcePosition,
  targetX: props.targetX,
  targetY: props.targetY,
  targetPosition: props.targetPosition,
  curvature: 0.25
})
</script>

<template>
  <BaseEdge :path="pathParams[0]" />
</template>
```

### 3. 注册自定义边

```vue
<script setup lang="ts">
import { ref, markRaw } from 'vue'
import { VueFlow } from '@vue-flow/core'
import CustomEdge from './CustomEdge.vue'

const edgeTypes = {
  custom: markRaw(CustomEdge)
}
</script>

<template>
  <VueFlow
    :edges="edges"
    :edge-types="edgeTypes"
  />
</template>
```

## 事件处理

```vue
<script setup>
// 所有可用事件
const events = {
  onNodeClick: (event) => console.log('node click', event.node),
  onNodeDoubleClick: (event) => console.log('node double click', event.node),
  onNodeDragStart: (event) => console.log('node drag start', event.node),
  onNodeDrag: (event) => console.log('node drag', event.node),
  onNodeDragStop: (event) => console.log('node drag stop', event.node),
  onEdgeClick: (event) => console.log('edge click', event.edge),
  onEdgeDoubleClick: (event) => console.log('edge double click', event.edge),
  onConnect: (connection) => console.log('connect', connection),
  onPaneClick: (event) => console.log('pane click', event),
  onPaneScroll: (event) => console.log('pane scroll', event),
  onPaneMove: (event) => console.log('pane move', event),
  onPaneMoveEnd: (event) => console.log('pane move end', event),
  onSelectionChange: (event) => console.log('selection change', event),
}
</script>
```

## 常用 API

### useVueFlow()

```typescript
const { 
  nodes, 
  edges, 
  onNodeClick, 
  addNodes, 
  removeNodes, 
  addEdges,
  findNode,
  findEdge,
  fitView,
  zoomIn,
  zoomOut,
  setViewport,
  getViewport
} = useVueFlow()
```

### 节点操作

```typescript
// 添加节点
addNodes([
  {
    id: '4',
    type: 'custom',
    position: { x: 100, y: 100 },
    data: { label: '新节点' }
  }
])

// 删除节点
removeNodes(['4'])

// 查找节点
const node = findNode('1')
```

### 边操作

```typescript
// 添加边
addEdges([
  {
    id: 'e3-4',
    source: '3',
    target: '4',
    type: 'smoothstep'
  }
])
```

### 视图控制

```typescript
// 自适应视图
fitView()

// 缩放
zoomIn()
zoomOut()

// 设置视图
setViewport({ x: 0, y: 0, zoom: 1 })
```

## 样式定制

```scss
<style scoped>
// 节点样式
:deep(.vue-flow__node) {
  cursor: pointer;
}

// 边样式
:deep(.vue-flow__edge-path) {
  stroke: #606266;
  stroke-width: 2;
}

// 连接点样式
:deep(.vue-flow__handle) {
  width: 8px;
  height: 8px;
  background: #409eff;
}

// Controls 组件样式
:deep(.vue-flow__controls) {
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

// Minimap 样式
:deep(.vue-flow__minimap) {
  background: #f5f7fa;
}
</style>
```

## 常见问题

### 1. 容器宽高警告

如果看到 "The Vue Flow parent container needs a width and a height" 警告，确保父容器有明确样式：

```vue
<div style="width: 100%; height: 500px;">
  <VueFlow ... />
</div>
```

### 2. 自定义组件不渲染

确保使用 `markRaw()` 包装组件：

```typescript
const nodeTypes = {
  custom: markRaw(CustomNodeComponent)
}
```

### 3. 样式不生效

Vue Flow 样式需要单独引入：

```vue
<style>
@import '@vue-flow/core/dist/style.css';
@import '@vue-flow/core/dist/theme-default.css';
</style>
```

### 4. 事件不触发

检查是否正确绑定了事件：

```vue
<VueFlow
  @node-click="handleNodeClick"
  @connect="handleConnect"
/>
```

## 工作流场景示例

```vue
<template>
  <div class="workflow-designer">
    <!-- 工具栏 -->
    <div class="toolbar">
      <el-button @click="addNode('start')">开始</el-button>
      <el-button @click="addNode('task')">审批</el-button>
      <el-button @click="addNode('end')">结束</el-button>
      <el-button type="primary" @click="handleSave">保存</el-button>
    </div>
    
    <!-- 画布 -->
    <div class="canvas" style="width: 100%; height: 500px;">
      <VueFlow
        v-model:nodes="nodes"
        v-model:edges="edges"
        :node-types="nodeTypes"
        fit-view-on-init
        @connect="onConnect"
        @node-click="onNodeClick"
      >
        <Background pattern-color="#eee" :gap="20" />
        <Controls />
        <MiniMap />
      </VueFlow>
    </div>
    
    <!-- 属性面板 -->
    <div v-if="selectedNode" class="property-panel">
      <h4>节点属性</h4>
      <el-form>
        <el-form-item label="节点名称">
          <el-input v-model="selectedNode.data.label" />
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>
```

## 最佳实践

1. **始终为父容器设置明确宽高**
2. **自定义组件使用 markRaw() 包装**
3. **引入必要的 CSS 样式文件**
4. **使用 TypeScript 定义数据类型**
5. **使用 v-model 双向绑定 nodes 和 edges**
6. **复杂逻辑使用 useVueFlow() composable**

## 详细示例

更多完整示例请参考 [examples/basic-examples.md](examples/basic-examples.md)，包括：
- 最简单的流程图
- 带工具栏的工作流设计器
- 自定义审批节点、开始节点、结束节点
