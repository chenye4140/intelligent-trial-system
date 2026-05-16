import request from '@/utils/request'

export function getUserList(params) {
  return request({
    url: '/system/user/page',
    method: 'get',
    params
  })
}

export function getUserDetail(id) {
  return request({
    url: `/system/user/${id}`,
    method: 'get'
  })
}

export function addUser(data) {
  return request({
    url: '/system/user',
    method: 'post',
    data
  })
}

export function updateUser(data) {
  return request({
    url: '/system/user',
    method: 'put',
    data
  })
}

export function deleteUser(id) {
  return request({
    url: `/system/user/${id}`,
    method: 'delete'
  })
}

export function resetPassword(data) {
  return request({
    url: '/system/user/reset-password',
    method: 'put',
    data
  })
}

export function toggleUserStatus(id, status) {
  return request({
    url: `/system/user/status/${id}`,
    method: 'put',
    params: { status }
  })
}

export function assignRoles(userId, roleIds) {
  return request({
    url: `/system/user/roles/${userId}`,
    method: 'put',
    data: roleIds
  })
}
