import request from '@/utils/request'

export function getRoleList(params) {
  return request({
    url: '/system/role/page',
    method: 'get',
    params
  })
}

export function getRoleDetail(id) {
  return request({
    url: `/system/role/${id}`,
    method: 'get'
  })
}

export function addRole(data) {
  return request({
    url: '/system/role',
    method: 'post',
    data
  })
}

export function updateRole(data) {
  return request({
    url: '/system/role',
    method: 'put',
    data
  })
}

export function deleteRole(id) {
  return request({
    url: `/system/role/${id}`,
    method: 'delete'
  })
}

export function assignMenus(roleId, menuIds) {
  return request({
    url: `/system/role/menus/${roleId}`,
    method: 'put',
    data: menuIds
  })
}

export function getRoleUsers(roleId) {
  return request({
    url: `/system/role/${roleId}/users`,
    method: 'get'
  })
}
