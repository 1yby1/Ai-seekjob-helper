import axios, { type AxiosRequestConfig, type Method } from 'axios'

export interface RequestOptions {
  method?: Method
  params?: Record<string, string | number | boolean | undefined>
  data?: unknown
  headers?: Record<string, string>
}

export const http = axios.create({
  baseURL: '/',
  timeout: 30000
})

// 请求拦截器：自动添加Token
http.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('auth_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器：处理401未授权错误
http.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Token过期或无效，清除本地存储并跳转登录
      localStorage.removeItem('auth_token')
      localStorage.removeItem('current_user')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export async function request<T>(url: string, options: RequestOptions = {}): Promise<T> {
  const config: AxiosRequestConfig = {
    url,
    method: options.method ?? 'GET',
    params: options.params,
    data: options.data,
    headers: options.headers
  }

  const response = await http.request<T>(config)
  return response.data
}
