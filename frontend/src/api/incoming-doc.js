import request from '@/utils/request'

/**
 * 来文登记模块 API
 */
export const incomingDocApi = {
  // 分页查询
  getPage(params) {
    return request.get('/incoming-doc/page', { params })
  },

  // 获取详情
  getDetail(id) {
    return request.get(`/incoming-doc/${id}`)
  },

  // 新增来文登记
  create(data) {
    return request.post('/incoming-doc', data)
  },

  // 更新来文登记
  update(data) {
    return request.put('/incoming-doc', data)
  },

  // 删除来文登记
  delete(id) {
    return request.delete(`/incoming-doc/${id}`)
  },

  // 变更来文状态
  changeStatus(id, status) {
    return request.put(`/incoming-doc/status/${id}`, null, { params: { status } })
  },
}
