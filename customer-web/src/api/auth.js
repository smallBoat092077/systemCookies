import { reactive } from 'vue'
import request from './request'

/** 登录 */
export const login = (data) => request.post('/auth/login', data)

/** 注册 */
export const register = (data) => request.post('/auth/register', data)

/** 退出登录 */
export const logout = () => request.post('/auth/logout')

/** 当前登录用户 */
export const fetchUserInfo = () => request.get('/auth/info')

const readUser = () => {
  try {
    return JSON.parse(localStorage.getItem('user'))
  } catch (e) {
    return null
  }
}

/**
 * 全局登录态：登录/退出时同步更新，页面无需等组件重新挂载就能刷新。
 * localStorage 只做持久化，页面一律读这里，避免各组件读到过期快照。
 */
export const authState = reactive({
  token: localStorage.getItem('token') || '',
  user: readUser()
})

export const getToken = () => authState.token

export const getUser = () => authState.user

export const isLogin = () => Boolean(authState.token)

export const saveAuth = (data) => {
  authState.token = data?.token || ''
  authState.user = data?.user || null
  localStorage.setItem('token', authState.token)
  localStorage.setItem('user', JSON.stringify(authState.user))
}

export const clearAuth = () => {
  authState.token = ''
  authState.user = null
  localStorage.removeItem('token')
  localStorage.removeItem('user')
}

/** 登录后同步拉取一次用户信息，保证展示的是最新资料 */
export const refreshUser = async () => {
  if (!isLogin()) {
    authState.user = null
    return null
  }
  try {
    const info = await fetchUserInfo()
    authState.user = info
    localStorage.setItem('user', JSON.stringify(info))
    return info
  } catch (e) {
    authState.user = readUser()
    return authState.user
  }
}
