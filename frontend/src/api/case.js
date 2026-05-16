import api from './index'

export const caseApi = {
  // POST /api/case/page - pagination search
  pageCase(data) { return api.post('/case/page', data) },
  // GET /api/case/{id} - detail
  getCase(id) { return api.get(`/case/${id}`) },
  // POST /api/case - create
  addCase(data) { return api.post('/case', data) },
  // PUT /api/case - update
  updateCase(data) { return api.put('/case', data) },
  // DELETE /api/case/{id} - delete
  deleteCase(id) { return api.delete(`/case/${id}`) },
  // PUT /api/case/status/{id}?status=x - change status
  changeStatus(id, status) { return api.put(`/case/status/${id}`, null, { params: { status } }) },
  // GET /api/case/{caseId}/parties - get parties
  getParties(caseId) { return api.get(`/case/${caseId}/parties`) },
  // POST /api/case/party - add party
  addParty(data) { return api.post('/case/party', data) },
  // DELETE /api/case/party/{id} - delete party
  deleteParty(id) { return api.delete(`/case/party/${id}`) },
  // GET /api/case/{caseId}/violations - get violation facts
  getViolationFacts(caseId) { return api.get(`/case/${caseId}/violations`) },
  // POST /api/case/violation - add violation fact
  addViolationFact(data) { return api.post('/case/violation', data) },
  // PUT /api/case/violation - update violation fact
  updateViolationFact(data) { return api.put('/case/violation', data) },
  // DELETE /api/case/violation/{id} - delete violation fact
  deleteViolationFact(id) { return api.delete(`/case/violation/${id}`) },
}
