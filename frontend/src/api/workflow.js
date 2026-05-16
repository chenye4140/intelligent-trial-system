import api from './index'

export const workflowApi = {
  getDefinitions() { return api.get('/workflow/process/definitions') },
  deploy(key) { return api.post(`/workflow/process/definitions/deploy/${key}`) },
  startProcess(data) { return api.post('/workflow/process/start', data) },
  getInstances(caseId) { return api.get(`/workflow/process/instances/${caseId}`) },
  getInstanceStatus(id) { return api.get(`/workflow/process/instances/${id}/status`) },
  cancelInstance(id) { return api.delete(`/workflow/process/instances/${id}`) },
  getMyTasks(params) { return api.get('/workflow/task/my-tasks', { params }) },
  getPendingTasks(params) { return api.get('/workflow/task/pending-tasks', { params }) },
  completeTask(taskId, data) { return api.put(`/workflow/task/complete/${taskId}`, data) },
  claimTask(taskId) { return api.put(`/workflow/task/claim/${taskId}`) },
  delegateTask(taskId, data) { return api.put(`/workflow/task/delegate/${taskId}`, data) },
  getHistory(processInstanceId) { return api.get(`/workflow/task/history/${processInstanceId}`) }
}
