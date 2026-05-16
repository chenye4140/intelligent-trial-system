import axios from 'axios'
import { ElMessage } from 'element-plus'

const api = axios.create({
  baseURL: '/api',
  timeout: 30000,
})

// 请求拦截器
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = token
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

// 文档相关API
export const documentApi = {
  // 上传文档
  upload(file, docType, categoryPath, securityLevel) {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('docType', docType)
    if (categoryPath) formData.append('categoryPath', categoryPath)
    if (securityLevel) formData.append('securityLevel', securityLevel)
    return api.post('/api/document/parse/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
  },

  // 查询文档列表（解析任务）
  list(params) {
    return api.get('/api/document/parse/tasks', { params })
  },

  // 获取文档详情
  getById(id) {
    return api.get(`/api/document/parse/task/${id}`)
  },

  // 删除文档
  delete(id) {
    return api.delete(`/api/document/parse/task/${id}`)
  },
}

export default api
