import request from './request'

/** 管理端登录（必须是管理员角色） */
export const login = (data) => request.post('/auth/login', data)

/** 退出登录 */
export const logout = () => request.post('/auth/logout')

/** 当前登录用户 */
export const fetchUserInfo = () => request.get('/auth/info')

export const getToken = () => localStorage.getItem('token') || ''

export const getUser = () => {
  try {
    return JSON.parse(localStorage.getItem('user'))
  } catch (e) {
    return null
  }
}

export const isLogin = () => Boolean(getToken())

export const saveAuth = (data) => {
  localStorage.setItem('token', data.token)
  localStorage.setItem('user', JSON.stringify(data.user))
}

export const clearAuth = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
}
