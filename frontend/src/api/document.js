import request from '@/utils/request'

export function uploadDocument(formData) {
  return request({
    url: '/document/parse/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export function getParseTask(id) {
  return request({
    url: `/document/parse/task/${id}`,
    method: 'get'
  })
}

export function getParseTaskList(params) {
  return request({
    url: '/document/parse/task/list',
    method: 'get',
    params
  })
}

export function retryParse(id) {
  return request({
    url: `/document/parse/task/${id}/retry`,
    method: 'post'
  })
}

export function getParseResult(id) {
  return request({
    url: `/document/parse/task/${id}/result`,
    method: 'get'
  })
}

export function deleteParseTask(id) {
  return request({
    url: `/document/parse/task/${id}`,
    method: 'delete'
  })
}
