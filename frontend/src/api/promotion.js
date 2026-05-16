import api from './index'

export const promotionApi = {
  generate(data) { return api.post('/promotion/generate', data) },
  getStatus(taskId) { return api.get(`/promotion/status/${taskId}`) },
  getById(id) { return api.get(`/promotion/${id}`) },
  getByCase(caseId) { return api.get(`/promotion/case/${caseId}`) },
  list(params) { return api.get('/promotion/list', { params }) },
  create(data) { return api.post('/promotion', data) },
  update(data) { return api.put('/promotion', data) },
  updateStatus(id, status) { return api.put(`/promotion/status/${id}`, null, { params: { status } }) },
  remove(id) { return api.delete(`/promotion/${id}`) }
}
