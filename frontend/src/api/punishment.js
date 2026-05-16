import request from '@/utils/request'

/**
 * 处分执行模块 API
 */
export const punishmentApi = {
  // 分页查询
  getPage(params) {
    return request.get('/api/punishment/page', { params })
  },

  // 获取详情
  getDetail(id) {
    return request.get(`/api/punishment/${id}`)
  },

  // 创建
  create(data) {
    return request.post('/api/punishment', data)
  },

  // 更新
  update(data) {
    return request.put('/api/punishment', data)
  },

  // 删除
  delete(id) {
    return request.delete(`/api/punishment/${id}`)
  },

  // 变更状态
  changeStatus(id, status) {
    return request.put(`/api/punishment/${id}/status`, null, { params: { status } })
  },

  // 按案件ID查询
  getByCaseId(caseId) {
    return request.get(`/api/punishment/case/${caseId}`)
  },

  // 查询逾期记录
  getOverdue() {
    return request.get('/api/punishment/overdue')
  },

  // 统计
  statistics() {
    return request.get('/api/punishment/statistics')
  },

  // 上传材料
  uploadMaterial(params) {
    return request.post('/api/punishment/material', null, { params })
  },

  // 获取材料列表
  getMaterials(executionId) {
    return request.get(`/api/punishment/material/${executionId}`)
  },

  // 删除材料
  deleteMaterial(materialId) {
    return request.delete(`/api/punishment/material/${materialId}`)
  },
}
