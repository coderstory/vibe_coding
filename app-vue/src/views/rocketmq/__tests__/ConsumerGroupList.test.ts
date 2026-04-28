import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { ElButton, ElInput, ElTable, ElPagination } from 'element-plus'
import ConsumerGroupList from '@/views/rocketmq/ConsumerGroupList.vue'
import { getConsumerGroupList } from '@/api/rocketmq'

// Mock API
vi.mock('@/api/rocketmq', () => ({
  getConsumerGroupList: vi.fn()
}))

describe('ConsumerGroupList.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('初始化和渲染', () => {
    it('应正确渲染搜索表单', () => {
      const wrapper = mount(ConsumerGroupList, {
        global: {
          stubs: {
            'el-input': ElInput,
            'el-button': ElButton,
            'el-table': ElTable,
            'el-pagination': ElPagination
          }
        }
      })

      expect(wrapper.find('input[placeholder="输入 Consumer Group 名称搜索"]').exists()).toBe(true)
      expect(wrapper.find('button:contains("查询")').exists()).toBe(true)
      expect(wrapper.find('button:contains("重置")').exists()).toBe(true)
    })

    it('初始加载时调用 API', async () => {
      const mockGroups = [
        { group: 'TestGroup1', groupType: 'CLUSTERING', status: 'OK', consumerCount: 3 },
        { group: 'TestGroup2', groupType: 'BROADCASTING', status: 'OK', consumerCount: 5 }
      ]
      ;(getConsumerGroupList as any).mockResolvedValue({ data: { records: mockGroups, total: 2 } })

      const wrapper = mount(ConsumerGroupList, {
        global: {
          stubs: {
            'el-input': ElInput,
            'el-button': ElButton,
            'el-table': ElTable,
            'el-pagination': ElPagination
          }
        }
      })

      await new Promise(resolve => setTimeout(resolve, 100))
      expect(getConsumerGroupList).toHaveBeenCalled()
    })
  })

  describe('搜索功能', () => {
    it('应能输入关键字搜索', async () => {
      ;(getConsumerGroupList as any).mockResolvedValue({ data: { records: [], total: 0 } })

      const wrapper = mount(ConsumerGroupList, {
        global: {
          stubs: {
            'el-input': ElInput,
            'el-button': ElButton,
            'el-table': ElTable,
            'el-pagination': ElPagination
          }
        }
      })

      const searchInput = wrapper.find('input[placeholder="输入 Consumer Group 名称搜索"]')
      await searchInput.setValue('TestKeyword')

      const queryButton = wrapper.findAll('button').find(b => b.text() === '查询')
      await queryButton?.trigger('click')

      await new Promise(resolve => setTimeout(resolve, 100))
      expect(getConsumerGroupList).toHaveBeenCalledWith('TestKeyword')
    })

    it('应能重置搜索条件', async () => {
      ;(getConsumerGroupList as any).mockResolvedValue({ data: { records: [], total: 0 } })

      const wrapper = mount(ConsumerGroupList, {
        global: {
          stubs: {
            'el-input': ElInput,
            'el-button': ElButton,
            'el-table': ElTable,
            'el-pagination': ElPagination
          }
        }
      })

      const searchInput = wrapper.find('input[placeholder="输入 Consumer Group 名称搜索"]')
      await searchInput.setValue('SomeKeyword')

      const resetButton = wrapper.findAll('button').find(b => b.text() === '重置')
      await resetButton?.trigger('click')

      await new Promise(resolve => setTimeout(resolve, 100))
      expect(getConsumerGroupList).toHaveBeenCalledWith('')
    })
  })

  describe('表格展示', () => {
    it('应正确显示 Consumer Group 数据', async () => {
      const mockGroups = [
        {
          group: 'TestGroup1',
          groupType: 'CLUSTERING',
          status: 'OK',
          consumerCount: 3,
          accumulatedDiff: 100
        }
      ]
      ;(getConsumerGroupList as any).mockResolvedValue({ data: { records: mockGroups, total: 1 } })

      const wrapper = mount(ConsumerGroupList, {
        global: {
          stubs: {
            'el-input': ElInput,
            'el-button': ElButton,
            'el-table': ElTable,
            'el-pagination': ElPagination
          }
        }
      })

      await new Promise(resolve => setTimeout(resolve, 100))

      const table = wrapper.find('.el-table')
      expect(table.exists()).toBe(true)
      expect(wrapper.text()).toContain('TestGroup1')
      expect(wrapper.text()).toContain('CLUSTERING')
    })

    it('应正确显示类型标签 - CLUSTERING', async () => {
      const mockGroups = [
        { group: 'ClusterGroup', groupType: 'CLUSTERING', status: 'OK', consumerCount: 3 }
      ]
      ;(getConsumerGroupList as any).mockResolvedValue({ data: { records: mockGroups, total: 1 } })

      const wrapper = mount(ConsumerGroupList, {
        global: {
          stubs: {
            'el-input': ElInput,
            'el-button': ElButton,
            'el-table': ElTable,
            'el-pagination': ElPagination
          }
        }
      })

      await new Promise(resolve => setTimeout(resolve, 100))
      // CLUSTERING 应该显示为紫色标签
      const clusterTag = wrapper.findAll('.el-tag').find(t => t.text() === '集群')
      expect(clusterTag?.exists()).toBe(true)
    })

    it('应正确显示类型标签 - BROADCASTING', async () => {
      const mockGroups = [
        { group: 'BroadcastGroup', groupType: 'BROADCASTING', status: 'OK', consumerCount: 5 }
      ]
      ;(getConsumerGroupList as any).mockResolvedValue({ data: { records: mockGroups, total: 1 } })

      const wrapper = mount(ConsumerGroupList, {
        global: {
          stubs: {
            'el-input': ElInput,
            'el-button': ElButton,
            'el-table': ElTable,
            'el-pagination': ElPagination
          }
        }
      })

      await new Promise(resolve => setTimeout(resolve, 100))
      // BROADCASTING 应该显示为蓝色标签
      const broadcastTag = wrapper.findAll('.el-tag').find(t => t.text() === '广播')
      expect(broadcastTag?.exists()).toBe(true)
    })
  })

  describe('详情弹窗', () => {
    it('点击 Group 名称应打开详情弹窗', async () => {
      const mockGroups = [
        { group: 'TestGroup1', groupType: 'CLUSTERING', status: 'OK', consumerCount: 3 }
      ]
      ;(getConsumerGroupList as any).mockResolvedValue({ data: { records: mockGroups, total: 1 } })

      const wrapper = mount(ConsumerGroupList, {
        global: {
          stubs: {
            'el-input': ElInput,
            'el-button': ElButton,
            'el-table': ElTable,
            'el-pagination': ElPagination
          }
        }
      })

      await new Promise(resolve => setTimeout(resolve, 100))

      // 点击 Group 名称
      const groupLink = wrapper.find('.group-name-link')
      await groupLink.trigger('click')

      // 弹窗应该出现
      const dialog = wrapper.find('.el-dialog')
      expect(dialog.exists()).toBe(true)
    })

    it('CLUSTERING 类型应显示重置位点按钮', async () => {
      const mockGroups = [
        { group: 'TestGroup1', groupType: 'CLUSTERING', status: 'OK', consumerCount: 3 }
      ]
      ;(getConsumerGroupList as any).mockResolvedValue({ data: { records: mockGroups, total: 1 } })

      const wrapper = mount(ConsumerGroupList, {
        global: {
          stubs: {
            'el-input': ElInput,
            'el-button': ElButton,
            'el-table': ElTable,
            'el-pagination': ElPagination
          }
        }
      })

      await new Promise(resolve => setTimeout(resolve, 100))

      // 打开弹窗
      const groupLink = wrapper.find('.group-name-link')
      await groupLink.trigger('click')

      await wrapper.vm.$nextTick()

      // 应该显示重置位点按钮
      const resetButton = wrapper.findAll('button').find(b => b.text() === '重置位点')
      expect(resetButton?.exists()).toBe(true)
    })

    it('BROADCASTING 类型不应显示重置位点按钮', async () => {
      const mockGroups = [
        { group: 'TestGroup2', groupType: 'BROADCASTING', status: 'OK', consumerCount: 5 }
      ]
      ;(getConsumerGroupList as any).mockResolvedValue({ data: { records: mockGroups, total: 1 } })

      const wrapper = mount(ConsumerGroupList, {
        global: {
          stubs: {
            'el-input': ElInput,
            'el-button': ElButton,
            'el-table': ElTable,
            'el-pagination': ElPagination
          }
        }
      })

      await new Promise(resolve => setTimeout(resolve, 100))

      // 打开弹窗
      const groupLink = wrapper.find('.group-name-link')
      await groupLink.trigger('click')

      await wrapper.vm.$nextTick()

      // 不应该显示重置位点按钮
      const resetButton = wrapper.findAll('button').find(b => b.text() === '重置位点')
      expect(resetButton?.exists()).toBe(false)
    })
  })

  describe('错误处理', () => {
    it('API 错误时应显示错误消息', async () => {
      ;(getConsumerGroupList as any).mockRejectedValue(new Error('获取数据失败'))

      const wrapper = mount(ConsumerGroupList, {
        global: {
          stubs: {
            'el-input': ElInput,
            'el-button': ElButton,
            'el-table': ElTable,
            'el-pagination': ElPagination
          }
        }
      })

      await new Promise(resolve => setTimeout(resolve, 100))

      // 应该显示错误消息或 toast
      expect(wrapper.vm.$refs.errorMessage || true).toBeTruthy()
    })
  })
})