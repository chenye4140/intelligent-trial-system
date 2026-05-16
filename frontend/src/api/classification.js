import api from './index'

export const classificationApi = {
  getSuggestion(caseId) { return api.get(`/system/classification/suggestion/${caseId}`) },
  generate(data) { return api.post('/system/classification/suggestion/generate', data) },
  adopt(suggestionId) { return api.put(`/system/classification/suggestion/adopt/${suggestionId}`) }
}
