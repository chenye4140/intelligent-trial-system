import request from '@/utils/request'

export function getUserList(params) {
  return request({
    url: '/user/list',
    method: 'get',
    params
  })
}

export function addUser(data) {
  return request({
    url: '/user',
    method: 'post',
    data
  })
}

export function updateUser(id, data) {
  return request({
    url: `/user/${id}`,
    method: 'put',
    data
  })
}

export function deleteUser(id) {
  return request({
    url: `/user/${id}`,
    method: 'delete'
  })
}

export function resetPassword(id) {
  return request({
    url: `/user/${id}/reset-password`,
    method: 'put'
  })
}

export function toggleUserStatus(id, status) {
  return request({
    url: `/user/${id}/status`,
    method: 'put',
    data: { status }
  })
}

export function assignRoles(id, roleIds) {
  return request({
    url: `/user/${id}/roles`,
    method: 'put',
    data: { roleIds }
  })
}
