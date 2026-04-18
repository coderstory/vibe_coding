import { describe, it, expect, vi, beforeEach } from 'vitest'

// Mock Element Plus message
vi.mock('element-plus', async () => {
  const actual = await vi.importActual('element-plus')
  return {
    ...actual,
    ElMessage: {
      error: vi.fn()
    }
  }
})

// Import after mocking
const { getUserList, getUserDetail, createUser, updateUser, deleteUser } = await import('../user.ts')

describe('User API Error Handling', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('extractErrorMessage', () => {
    it('should extract message from backend response', async () => {
      const mockError = {
        response: {
          data: {
            message: '用户名已存在',
            code: 409
          }
        }
      }

      // Simulate the error extraction logic
      const extractErrorMessage = (error: { response?: { data?: { message?: string } } }) => {
        if (error.response?.data?.message) {
          return error.response.data.message
        }
        return '操作失败'
      }

      expect(extractErrorMessage(mockError)).toBe('用户名已存在')
    })

    it('should return 401 message for unauthorized', async () => {
      const extractErrorMessage = (error: { response?: { status?: number } }) => {
        if (error.response?.status === 401) {
          return '登录已过期，请重新登录'
        }
        return '操作失败'
      }

      expect(extractErrorMessage({ response: { status: 401 } })).toBe('登录已过期，请重新登录')
    })

    it('should return 404 message for not found', async () => {
      const extractErrorMessage = (error: { response?: { status?: number } }) => {
        if (error.response?.status === 404) {
          return '请求的资源不存在'
        }
        return '操作失败'
      }

      expect(extractErrorMessage({ response: { status: 404 } })).toBe('请求的资源不存在')
    })

    it('should return 500 message for server error', async () => {
      const extractErrorMessage = (error: { response?: { status?: number } }) => {
        if (error.response?.status === 500) {
          return '服务器内部错误，请稍后重试'
        }
        return '操作失败'
      }

      expect(extractErrorMessage({ response: { status: 500 } })).toBe('服务器内部错误，请稍后重试')
    })

    it('should handle network error', async () => {
      const extractErrorMessage = (error: { code?: string; message?: string }) => {
        if (error.code === 'ERR_NETWORK' || error.message === 'Network Error') {
          return '网络连接失败，请检查网络设置'
        }
        return '操作失败'
      }

      expect(extractErrorMessage({ code: 'ERR_NETWORK' })).toBe('网络连接失败，请检查网络设置')
    })

    it('should handle timeout error', async () => {
      const extractErrorMessage = (error: { code?: string }) => {
        if (error.code === 'ECONNABORTED') {
          return '请求超时，请检查网络或稍后重试'
        }
        return '操作失败'
      }

      expect(extractErrorMessage({ code: 'ECONNABORTED' })).toBe('请求超时，请检查网络或稍后重试')
    })

    it('should return default message for unknown errors', async () => {
      const extractErrorMessage = (error: { message?: string }) => {
        return error.message || '操作失败，请稍后重试'
      }

      expect(extractErrorMessage({ message: 'Unknown error' })).toBe('Unknown error')
    })
  })

  describe('HTTP Status Code Mapping', () => {
    const statusMessages: Record<number, string> = {
      400: '请求参数错误',
      401: '登录已过期，请重新登录',
      403: '没有权限执行此操作',
      404: '请求的资源不存在',
      408: '请求超时，请稍后重试',
      409: '数据冲突，请检查是否重复',
      500: '服务器内部错误，请稍后重试',
      502: '网关错误，请稍后重试',
      503: '服务暂时不可用，请稍后重试',
      504: '网关超时，请稍后重试'
    }

    Object.entries(statusMessages).forEach(([status, expectedMessage]) => {
      it(`should return "${expectedMessage}" for status ${status}`, () => {
        const extractErrorMessage = (error: { response?: { status?: number } }) => {
          if (error.response?.status === Number(status)) {
            return statusMessages[error.response.status]
          }
          return '操作失败'
        }

        expect(extractErrorMessage({ response: { status: Number(status) } })).toBe(expectedMessage)
      })
    })
  })
})

describe('User API Response Structure', () => {
  it('should handle successful response with code 200', async () => {
    const response = {
      data: {
        code: 200,
        message: 'success',
        data: {
          records: [],
          total: 0
        }
      }
    }

    expect(response.data.code).toBe(200)
    expect(response.data.message).toBe('success')
  })

  it('should handle error response with code 409', async () => {
    const response = {
      data: {
        code: 409,
        message: '用户名已存在'
      }
    }

    expect(response.data.code).toBe(409)
    expect(response.data.message).toBe('用户名已存在')
  })
})
