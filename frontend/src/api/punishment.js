import request from '@/utils/request'

/**
 * 处分执行模块 API
 */
export const punishmentApi = {
  // 分页查询
  getPage(params) {
    return request.get('/punishment/page', { params })
  },

  // 获取详情
  getDetail(id) {
    return request.get(`/punishment/${id}`)
  },

  // 创建
  create(data) {
    return request.post('/punishment', data)
  },

  // 更新
  update(data) {
    return request.put('/punishment', data)
  },

  // 删除
  delete(id) {
    return request.delete(`/punishment/${id}`)
  },

  // 变更状态
  changeStatus(id, status) {
    return request.put(`/punishment/${id}/status`, null, { params: { status } })
  },

  // 按案件ID查询
  getByCaseId(caseId) {
    return request.get(`/punishment/case/${caseId}`)
  },

  // 查询逾期记录
  getOverdue() {
    return request.get('/punishment/overdue')
  },

  // 统计
  statistics() {
    return request.get('/punishment/statistics')
  },

  // 上传材料
  uploadMaterial(params) {
    return request.post('/punishment/material', null, { params })
  },

  // 获取材料列表
  getMaterials(executionId) {
    return request.get(`/punishment/material/${executionId}`)
  },

  // 删除材料
  deleteMaterial(materialId) {
    return request.delete(`/punishment/material/${materialId}`)
  },
}
