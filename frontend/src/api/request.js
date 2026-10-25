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

function isAuthPage(path) {
  return path === '/login' || path === '/register' || path === '/admin/login'
}

function redirectToLogin() {
  const store = useUserStore()
  store.logout()
  const fullPath = router.currentRoute.value.fullPath || ''
  const path = router.currentRoute.value.path || ''
  // 登录页上的账号密码错误也会返回 401，不能再跳转，否则表单状态被冲掉
  if (isAuthPage(path)) return
  const loginPath = path.startsWith('/admin') ? '/admin/login' : '/login'
  router.push({ path: loginPath, query: { redirect: fullPath } })
}

request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res && typeof res.code === 'number' && res.code !== 0) {
      ElMessage.error(res.message || '请求失败')
      if (res.code === 401) {
        redirectToLogin()
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
      redirectToLogin()
    } else if (status === 403) {
      ElMessage.error(msg || '无权限访问')
    } else {
      ElMessage.error(msg)
    }
    return Promise.reject(error)
  }
)

export default request
