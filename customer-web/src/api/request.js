import axios from 'axios'
import { showToast } from 'vant'

// 统一后端响应体：{ code, message, data }，code === 200 时直接返回 data
const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000
})

// 请求头带上登录令牌
request.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 令牌失效时清理本地状态并跳转登录页
const redirectToLogin = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  // 通知页面同步清空全局登录态（这里不能反向 import auth，会形成循环依赖）
  window.dispatchEvent(new Event('auth:logout'))
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
      if (body.code === 401) {
        redirectToLogin()
        return Promise.reject(new Error(body.message || '请先登录'))
      }
      const message = body.message || '请求失败'
      showToast(message)
      return Promise.reject(new Error(message))
    }
    return body
  },
  (error) => {
    if (error?.response?.status === 401) {
      redirectToLogin()
      return Promise.reject(error)
    }
    const message = error?.response?.data?.message || '网络异常，请稍后重试'
    showToast(message)
    return Promise.reject(error)
  }
)

export default request
