import axios from 'axios'
import { ElMessage } from 'element-plus'

// 统一后端响应体：{ code, message, data }，code === 200 时直接返回 data
const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 20000
})

// 请求头带上登录令牌
request.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 令牌失效或未授权时清理本地状态并跳回登录页
const redirectToLogin = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  if (!location.pathname.startsWith('/login')) {
    location.href = `/login?redirect=${encodeURIComponent(location.pathname + location.search)}`
  }
}

request.interceptors.response.use(
  (response) => {
    const body = response.data
    if (body && typeof body === 'object' && 'code' in body) {
      if (body.code === 200) {
        return body.data
      }
      if (body.code === 401 || body.code === 403) {
        redirectToLogin()
        return Promise.reject(new Error(body.message || '登录已失效'))
      }
      const message = body.message || '请求失败'
      ElMessage.error(message)
      return Promise.reject(new Error(message))
    }
    return body
  },
  (error) => {
    const status = error?.response?.status
    if (status === 401 || status === 403) {
      redirectToLogin()
      return Promise.reject(error)
    }
    const message = error?.response?.data?.message || '网络异常，请稍后重试'
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default request
