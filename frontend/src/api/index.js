import axios from 'axios'
import { ElMessage } from 'element-plus'

const api = axios.create({
  baseURL: '/api',
  timeout: 30000,
})

// 请求拦截器
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器
api.interceptors.response.use(
  (response) => {
    const data = response.data
    if (data.code === 200) {
      return data
    }
    ElMessage.error(data.msg || data.message || '请求失败')
    return Promise.reject(new Error(data.msg || data.message))
  },
  (error) => {
    ElMessage.error(error.response?.data?.msg || error.response?.data?.message || '网络异常')
    return Promise.reject(error)
  }
)

// 文档相关API（解析任务管理）
export const documentApi = {
  // 上传文档并创建解析任务
  upload(file) {
    const formData = new FormData()
    formData.append('file', file)
    return api.post('/document/parse/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
  },

  // 查询解析任务列表
  list(params) {
    return api.get('/document/parse/tasks', { params })
  },

  // 获取解析任务详情
  getById(id) {
    return api.get(`/document/parse/task/${id}`)
  },

  // 获取解析结果
  getResult(id) {
    return api.get(`/document/parse/task/${id}/result`)
  },

  // 删除解析任务
  delete(id) {
    return api.delete(`/document/parse/task/${id}`)
  },

  // 重试失败任务
  retry(id) {
    return api.post(`/document/parse/task/${id}/retry`)
  },
}

export default api
