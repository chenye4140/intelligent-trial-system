import request from '@/utils/request'

export function getDirectoryTree(repoType) {
  return request({
    url: '/repository/directory/tree',
    method: 'get',
    params: { repoType }
  })
}

export function addDirectory(data) {
  return request({
    url: '/repository/directory',
    method: 'post',
    data
  })
}

export function updateDirectory(id, data) {
  return request({
    url: `/repository/directory/${id}`,
    method: 'put',
    data
  })
}

export function deleteDirectory(id) {
  return request({
    url: `/repository/directory/${id}`,
    method: 'delete'
  })
}

export function searchDocuments(data) {
  return request({
    url: '/repository/document/search',
    method: 'post',
    data
  })
}

export function getDocumentDetail(id) {
  return request({
    url: `/repository/document/${id}`,
    method: 'get'
  })
}

export function addDocument(data) {
  return request({
    url: '/repository/document',
    method: 'post',
    data
  })
}

export function updateDocument(id, data) {
  return request({
    url: `/repository/document/${id}`,
    method: 'put',
    data
  })
}

export function deleteDocument(id) {
  return request({
    url: `/repository/document/${id}`,
    method: 'delete'
  })
}

export function downloadDocument(id) {
  return request({
    url: `/repository/document/${id}/download`,
    method: 'get',
    responseType: 'blob'
  })
}

export function importDirectoryExcel(repoType, formData) {
  return request({
    url: `/repository/directory/import/${repoType}`,
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function exportDirectoryExcel(repoType) {
  return request({
    url: `/repository/directory/export/${repoType}`,
    method: 'get',
    responseType: 'blob'
  })
}
