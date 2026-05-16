import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

let instance = null

export function getAxios() {
  if (!instance) {
    instance = axios.create({
      baseURL: '/api',
      timeout: 30000,
      headers: {
        'Content-Type': 'application/json'
      }
    })
  }
  return instance
}

export function setupAxios() {
  const service = getAxios()

  // Request interceptor
  service.interceptors.request.use(
    (config) => {
      const token = localStorage.getItem('accessToken')
      if (token) {
        config.headers['Authorization'] = `Bearer ${token}`
      }
      return config
    },
    (error) => {
      return Promise.reject(error)
    }
  )

  // Response interceptor
  service.interceptors.response.use(
    (response) => {
      const res = response.data
      if (res.code && res.code !== 200 && res.code !== 0) {
        ElMessage.error(res.message || '请求失败')
        if (res.code === 401) {
          localStorage.removeItem('accessToken')
          localStorage.removeItem('refreshToken')
          localStorage.removeItem('userInfo')
          router.push('/login')
        }
        return Promise.reject(new Error(res.message || '请求失败'))
      }
      return res
    },
    (error) => {
      if (error.response) {
        const { status, data } = error.response
        if (status === 401) {
          ElMessage.error('登录已过期，请重新登录')
          localStorage.removeItem('accessToken')
          localStorage.removeItem('refreshToken')
          localStorage.removeItem('userInfo')
          router.push('/login')
        } else if (status === 403) {
          ElMessage.error('没有权限访问')
        } else {
          ElMessage.error(data?.message || '服务器错误')
        }
      } else {
        ElMessage.error('网络错误，请检查网络连接')
      }
      return Promise.reject(error)
    }
  )
}

export default getAxios()
