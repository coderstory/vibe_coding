# Vue Flow 示例

## 基础示例

### 1. 最简单的流程图

```vue
<template>
  <div style="width: 100%; height: 400px;">
    <VueFlow
      :nodes="nodes"
      :edges="edges"
    />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { VueFlow } from '@vue-flow/core'
import '@vue-flow/core/dist/style.css'

const nodes = ref([
  { id: '1', type: 'input', position: { x: 250, y: 5 }, data: { label: '开始' } },
  { id: '2', position: { x: 100, y: 100 }, data: { label: '审批' } },
  { id: '3', type: 'output', position: { x: 250, y: 200 }, data: { label: '结束' } }
])

const edges = ref([
  { id: 'e1-2', source: '1', target: '2', animated: true },
  { id: 'e2-3', source: '2', target: '3', animated: true }
])
</script>
```

## 工作流场景示例

### 2. 带工具栏的工作流设计器

```vue
<template>
  <div class="workflow-designer">
    <div class="toolbar">
      <el-button size="small" @click="addNode('start')">开始</el-button>
      <el-button size="small" @click="addNode('task')">审批</el-button>
      <el-button size="small" @click="addNode('end')">结束</el-button>
      <el-divider direction="vertical" />
      <el-button size="small" type="primary" @click="handleSave">保存</el-button>
    </div>
    
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
  </div>
</template>

<script setup lang="ts">
import { ref, markRaw } from 'vue'
import { VueFlow, useVueFlow, MarkerType } from '@vue-flow/core'
import { Background } from '@vue-flow/background'
import { Controls } from '@vue-flow/controls'
import { MiniMap } from '@vue-flow/minimap'
import type { Node, Edge, Connection } from '@vue-flow/core'
import TaskNode from './nodes/TaskNode.vue'

const { fitView } = useVueFlow()

const nodeTypes = {
  task: markRaw(TaskNode)
}

const nodes = ref<Node[]>([
  { id: '1', type: 'input', position: { x: 250, y: 5 }, data: { label: '开始' } },
  { id: '2', type: 'task', position: { x: 100, y: 100 }, data: { label: '审批' } },
  { id: '3', type: 'output', position: { x: 250, y: 200 }, data: { label: '结束' } }
])

const edges = ref<Edge[]>([
  { id: 'e1-2', source: '1', target: '2', type: 'smoothstep', animated: true, markerEnd: { type: MarkerType.ArrowClosed } },
  { id: 'e2-3', source: '2', target: '3', type: 'smoothstep', animated: true, markerEnd: { type: MarkerType.ArrowClosed } }
])

const onConnect = (connection: Connection) => {
  if (connection.source && connection.target) {
    edges.value.push({
      id: `e${Date.now()}`,
      source: connection.source,
      target: connection.target,
      type: 'smoothstep',
      animated: true,
      markerEnd: { type: MarkerType.ArrowClosed }
    })
  }
}

const onNodeClick = (event: any) => {
  console.log('点击节点:', event.node)
}

const addNode = (type: string) => {
  const id = Date.now().toString()
  const label = type === 'start' ? '开始' : type === 'end' ? '结束' : '审批'
  const nodeType = type === 'start' ? 'input' : type === 'end' ? 'output' : 'task'
  
  nodes.value.push({
    id,
    type: nodeType,
    position: { x: 100 + Math.random() * 300, y: 100 + Math.random() * 200 },
    data: { label }
  })
}

const handleSave = () => {
  console.log('保存流程:', { nodes: nodes.value, edges: edges.value })
}
</script>

<style scoped>
.workflow-designer {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.toolbar {
  padding: 10px;
  background: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
}
</style>

<style>
@import '@vue-flow/core/dist/style.css';
@import '@vue-flow/core/dist/theme-default.css';
</style>
```

## 自定义节点示例

### 3. 审批节点

```vue
<!-- TaskNode.vue -->
<script setup lang="ts">
import { Handle, Position } from '@vue-flow/core'
import type { NodeProps } from '@vue-flow/core'

interface TaskNodeData {
  label: string
  assignee?: string
  status?: string
}

type Props = NodeProps<TaskNodeData>

const props = defineProps<Props>()
</script>

<template>
  <div class="task-node">
    <Handle type="target" :position="Position.Top" />
    
    <div class="node-header">
      <span class="node-icon">📋</span>
      <span class="node-title">{{ props.data?.label || '审批' }}</span>
    </div>
    
    <div class="node-body" v-if="props.data?.assignee">
      <span class="assignee">{{ props.data.assignee }}</span>
    </div>
    
    <Handle type="source" :position="Position.Bottom" />
  </div>
</template>

<style scoped lang="scss">
.task-node {
  min-width: 120px;
  padding: 0;
  background: #fff;
  border: 2px solid #409eff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  
  .node-header {
    display: flex;
    align-items: center;
    padding: 8px 12px;
    background: #ecf5ff;
    border-radius: 6px 6px 0 0;
    
    .node-icon {
      margin-right: 6px;
    }
    
    .node-title {
      font-size: 13px;
      font-weight: 500;
      color: #303133;
    }
  }
  
  .node-body {
    padding: 8px 12px;
    
    .assignee {
      font-size: 12px;
      color: #909399;
    }
  }
}
</style>
```

### 4. 开始节点

```vue
<!-- StartNode.vue -->
<script setup lang="ts">
import { Handle, Position } from '@vue-flow/core'
import type { NodeProps } from '@vue-flow/core'

type Props = NodeProps

const props = defineProps<Props>()
</script>

<template>
  <div class="start-node">
    <Handle type="target" :position="Position.Top" />
    <div class="node-content">
      <span class="icon">▶</span>
      <span class="label">{{ props.data?.label || '开始' }}</span>
    </div>
    <Handle type="source" :position="Position.Bottom" />
  </div>
</template>

<style scoped lang="scss">
.start-node {
  min-width: 80px;
  padding: 10px 16px;
  background: #67c23a;
  border-radius: 20px;
  color: #fff;
  
  .node-content {
    display: flex;
    align-items: center;
    gap: 6px;
    
    .icon {
      font-size: 10px;
    }
    
    .label {
      font-size: 13px;
      font-weight: 500;
    }
  }
}
</style>
```

### 5. 结束节点

```vue
<!-- EndNode.vue -->
<script setup lang="ts">
import { Handle, Position } from '@vue-flow/core'
import type { NodeProps } from '@vue-flow/core'

type Props = NodeProps

const props = defineProps<Props>()
</script>

<template>
  <div class="end-node">
    <Handle type="target" :position="Position.Top" />
    <div class="node-content">
      <span class="icon">■</span>
      <span class="label">{{ props.data?.label || '结束' }}</span>
    </div>
  </div>
</template>

<style scoped lang="scss">
.end-node {
  min-width: 80px;
  padding: 10px 16px;
  background: #f56c6c;
  border-radius: 20px;
  color: #fff;
  
  .node-content {
    display: flex;
    align-items: center;
    gap: 6px;
    
    .icon {
      font-size: 10px;
    }
    
    .label {
      font-size: 13px;
      font-weight: 500;
    }
  }
}
</style>
```
