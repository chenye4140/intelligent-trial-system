import request from '@/utils/request'

/**
 * 阅卷笔记模块 API
 */
export const readingNoteApi = {
  // 分页查询
  getPage(params) {
    return request.get('/reading-note/page', { params })
  },

  // 获取详情
  getDetail(id) {
    return request.get(`/reading-note/${id}`)
  },

  // 创建
  create(data) {
    return request.post('/reading-note', data)
  },

  // 更新
  update(data) {
    return request.put('/reading-note', data)
  },

  // 删除
  delete(id) {
    return request.delete(`/reading-note/${id}`)
  },

  // 按案件ID查询
  getByCaseId(caseId) {
    return request.get(`/reading-note/case/${caseId}`)
  },

  // 查询共享笔记
  getSharedNotes(caseId) {
    return request.get(`/reading-note/shared/${caseId}`)
  },

  // 切换共享状态
  toggleShared(id, isShared) {
    return request.put(`/reading-note/${id}/share`, null, { params: { isShared } })
  },
}
