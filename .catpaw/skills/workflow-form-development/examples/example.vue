<template>
  <WorkflowPanel
      ref="workflowRef"
      :mode="pageMode"
      :workflow-definition-id="workflowDefinitionId"
      :workflow-name="workflowName"
      :instance-id="instanceId"
      :task-id="taskId"
      :task-name="taskName"
      :form-data="formData"
      :node-operations="nodeOperations"
      :return-config="returnConfig"
      :return-target-nodes="returnTargetNodes"
      :current-node-name="currentNodeName"
      :form-loading="loading"
      @submit="handleSubmit"
      @approve="handleApprove"
      @reject="handleReject"
      @success="onSuccess"
      @cancel="onCancel"
      @error="onError"
  >
    <!-- 【顶部】DynamicForm 渲染内嵌 JSON 配置（如果定义了） -->
    <DynamicForm
        v-if="nodeFormConfig"
        ref="dynamicFormRef"
        :config="nodeFormConfig"
        v-model="formData"
        :readonly="!isFormEditable"
    />

    <!-- 【下方】自定义内容（可选） -->
    <!-- 可以在这里添加额外的自定义表单项或特殊控件 -->
  </WorkflowPanel>
</template>

<script setup lang="ts">
/**
 * 周报流程表单 - Vue 组件模式 + 内嵌 JSON 配置
 *
 * 这是推荐的流程表单开发方式：
 * 1. 使用 useWorkflowForm 获取页面模式和节点信息
 * 2. 定义内嵌的 formConfig（支持节点级配置）
 * 3. 使用 DynamicForm 渲染节点表单
 * 4. WorkflowPanel 内置审批意见功能
 */
import { ref, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import WorkflowPanel from '../components/WorkflowPanel.vue'
import DynamicForm from '@/components/DynamicForm/index.vue'
import type { FormConfig, NodeFormConfig } from '@/components/DynamicForm/types'
import { useWorkflowForm } from '@/composables/useWorkflowForm'

const { t } = useI18n()
const router = useRouter()

// ============ 使用 useWorkflowForm 组合式函数 ============
const {
  pageMode,
  workflowDefinitionId,
  instanceId,
  taskId,
  taskName,
  currentNodeName,
  formData,
  nodeOperations,
  returnConfig,
  returnTargetNodes,
  isFormEditable,
  loading,
  currentUser,
  mergeNodeFormConfig  // 【重要】合并节点配置的方法
} = useWorkflowForm({
  autoLoadUser: true,
  autoLoadInstance: true,
  startNodeName: '开始'
})

// 流程名称
const workflowName = computed(() => t('weeklyReport.workflowName'))

// 流程标题
const title = computed(() => {
  const name = currentUser.value?.nickname || currentUser.value?.username || '用户'
  const date = formData.value.startDate || new Date().toISOString().split('T')[0]
  return `${name}的周报申请 (${date})`
})

// 引用
const workflowRef = ref()
const dynamicFormRef = ref()

// ============ 内嵌 JSON 配置 ============
const formConfig: FormConfig = {
  formKey: 'WEEKLY_REPORT',
  version: '2.0.0',
  labelWidth: '120px',
  nodes: {
    "开始": {
      sections: [
        {
          key: 'initiatorInfo',
          title: '发起人信息',
          collapsible: false,
          fields: [
            { name: 'serialNo', label: '流水号', type: 'text', disabled: true, colSpan: 12 },
            { name: 'initiatorName', label: '姓名', type: 'text', required: true, colSpan: 12 },
            { name: 'initiatorDept', label: '部门', type: 'text', required: true, colSpan: 12 },
            { name: 'initiatorPost', label: '岗位', type: 'text', required: true, colSpan: 12 },
            { name: 'createTime', label: '创建时间', type: 'text', disabled: true, colSpan: 12 }
          ]
        },
        {
          key: 'applicationInfo',
          title: '申请信息',
          collapsible: false,
          fields: [
            {
              name: 'startDate',
              label: '开始日期',
              type: 'date',
              required: true,
              colSpan: 12,
              props: {
                disabledDate: 'future',
                format: 'YYYY-MM-DD',
                valueFormat: 'YYYY-MM-DD',
                placeholder: '请选择开始日期'
              }
            },
            {
              name: 'endDate',
              label: '截止日期',
              type: 'date',
              colSpan: 12,
              props: {
                minDate: 'startDate',
                format: 'YYYY-MM-DD',
                valueFormat: 'YYYY-MM-DD',
                placeholder: '请选择截止日期'
              }
            },
            {
              name: 'days',
              label: '天数',
              type: 'number',
              required: true,
              colSpan: 12,
              props: { min: 1, max: 365, step: 1, placeholder: '请输入天数' }
            }
          ]
        }
      ],
      initScript: {
        onLoad: `
          if (!form.startDate) {
            form.startDate = getMonday();
          }
          if (!form.endDate) {
            form.endDate = getSunday(form.startDate);
          }
          if (!form.days) {
            form.days = 7;
          }
        `
      }
    },
    // 审批节点：无需重复定义字段，mergeNodeFormConfig 会自动合并发起节点的配置
    // 如果审批节点需要额外字段，可以在这里添加 sections
    "部门审批": {
      sections: []  // 空数组，发起节点的字段会自动以只读方式显示
    }
  }
}

// 获取当前节点的表单配置（合并发起节点和当前节点）
// 审批/查看模式下会自动合并发起节点的表单配置，使审批人能看到发起人填写的数据
const nodeFormConfig = computed<NodeFormConfig | null>(() => {
  return mergeNodeFormConfig(formConfig)
})

// 流水号
const serialNo = computed(() => {
  const now = new Date()
  return now.toISOString().replace(/[-:TZ]/g, '').slice(0, 14)
})

// ============ 初始化表单数据 ============
watch(currentUser, (user) => {
  if (pageMode.value === 'start' && user && !formData.value?.initiatorName) {
    // 发起模式：预填充发起人信息
    if (!formData.value) formData.value = {}
    formData.value.serialNo = serialNo.value
    formData.value.initiatorName = user.name || ''
    formData.value.initiatorDept = user.deptName || ''
    formData.value.initiatorPost = user.postName || ''
    formData.value.createTime = new Date().toLocaleString('zh-CN')
  }
}, { immediate: true })

// ============ 事件处理 ============

// 提交处理（发起模式）
const handleSubmit = async () => {
  // 校验动态表单
  if (dynamicFormRef.value) {
    const valid = await dynamicFormRef.value.validate()
    if (!valid) {
      workflowRef.value?.resetSubmitting()
      return
    }
  }

  try {
    await workflowRef.value?.doStartWorkflow(formData.value, { title })
  } catch (error) {
    console.error('发起流程失败:', error)
  }
}

// 审批通过
const handleApprove = (event: { opinion: string; markHandled?: () => void }) => {
  console.log('审批意见:', event.opinion)
  // WorkflowPanel 会自动处理审批
}

// 审批拒绝
const handleReject = async () => {
  try {
    await workflowRef.value?.doReject()
  } catch (error) {
    console.error('拒绝失败:', error)
  }
}

// 成功回调
const onSuccess = () => {
  ElMessage.success(pageMode.value === 'start' ? t('weeklyReport.reportSubmitted') : t('weeklyReport.approvalCompleted'))
  router.push('/dashboard/TodoWorkflow')
}

// 取消回调
const onCancel = () => {
  router.back()
}

// 错误回调
const onError = (error: any) => {
  console.error('操作错误:', error)
}
</script>

<style scoped lang="scss">
// 可以在这里添加额外的自定义样式
</style>
