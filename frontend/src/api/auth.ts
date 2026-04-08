import { request } from './request'

export interface LoginPayload {
  username: string
  password: string
}

export interface RegisterPayload {
  username: string
  email: string
  password: string
}

export interface AuthResponse {
  success: boolean
  message: string
  token?: string
  userId?: number
  username?: string
}

export async function login(data: LoginPayload): Promise<AuthResponse> {
  const response = await request<AuthResponse>('/api/auth/login', {
    method: 'POST',
    data
  })
  
  // 登录成功后保存Token和用户信息
  if (response.success && response.token) {
    localStorage.setItem('auth_token', response.token)
    localStorage.setItem('current_user', JSON.stringify({
      id: response.userId,
      username: response.username
    }))
  }
  
  return response
}

export async function register(data: RegisterPayload): Promise<AuthResponse> {
  const response = await request<AuthResponse>('/api/auth/register', {
    method: 'POST',
    data
  })
  
  // 注册成功后保存Token和用户信息
  if (response.success && response.token) {
    localStorage.setItem('auth_token', response.token)
    localStorage.setItem('current_user', JSON.stringify({
      id: response.userId,
      username: response.username
    }))
  }
  
  return response
}

export function logout() {
  // 先获取当前用户ID，用于清除对应的聊天数据
  const userStr = localStorage.getItem('current_user')
  if (userStr) {
    const user = JSON.parse(userStr)
    if (user?.id) {
      localStorage.removeItem(`chat_topics_user_${user.id}`)
    }
  }
  
  localStorage.removeItem('auth_token')
  localStorage.removeItem('current_user')
}

export function getCurrentUser(): { id: number; username: string } | null {
  const userStr = localStorage.getItem('current_user')
  return userStr ? JSON.parse(userStr) : null
}

export function isAuthenticated(): boolean {
  return !!localStorage.getItem('auth_token')
}
