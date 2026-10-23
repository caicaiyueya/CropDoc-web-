import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import router from '@/router'

/**
 * Axios 封装：携带 token，统一处理 401/403。
 * 对应 UML：与后端 API 边界的通信组件。
 */
const request = axios.create({
  baseURL: '/api',
  timeout: 60000
})

request.interceptors.request.use((config) => {
  const store = useUserStore()
  if (store.token) {
    config.headers.Authorization = `Bearer ${store.token}`
  }
  return config
})

request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res && typeof res.code === 'number' && res.code !== 0) {
      ElMessage.error(res.message || '请求失败')
      if (res.code === 401) {
        const store = useUserStore()
        store.logout()
        router.push({ path: '/login', query: { redirect: router.currentRoute.value.fullPath } })
      }
      return Promise.reject(res)
    }
    return res
  },
  (error) => {
    const status = error.response?.status
    const msg = error.response?.data?.message || error.message || '网络异常'
    if (status === 401) {
      ElMessage.error(msg || '未登录或登录已失效')
      const store = useUserStore()
      store.logout()
      router.push({ path: '/login', query: { redirect: router.currentRoute.value.fullPath } })
    } else if (status === 403) {
      ElMessage.error(msg || '无权限访问')
    } else {
      ElMessage.error(msg)
    }
    return Promise.reject(error)
  }
)

export default request
