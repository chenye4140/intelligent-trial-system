import api from './index'

export const reportApi = {
  generate(data) { return api.post('/report/generate', data) },
  getRecord(id) { return api.get(`/report/record/${id}`) },
  list(params) { return api.get('/report/list', { params }) },
  getTemplates() { return api.get('/report/templates') },
  getStatus(id) { return api.get(`/report/status/${id}`) }
}
