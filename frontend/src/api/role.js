import request from '@/utils/request'

export function getRoleList(params) {
  return request({
    url: '/role/list',
    method: 'get',
    params
  })
}

export function getRoleMenuTree(roleId) {
  return request({
    url: `/role/${roleId}/menus`,
    method: 'get'
  })
}

export function addRole(data) {
  return request({
    url: '/role',
    method: 'post',
    data
  })
}

export function updateRole(id, data) {
  return request({
    url: `/role/${id}`,
    method: 'put',
    data
  })
}

export function deleteRole(id) {
  return request({
    url: `/role/${id}`,
    method: 'delete'
  })
}

export function assignMenus(id, menuIds) {
  return request({
    url: `/role/${id}/menus`,
    method: 'put',
    data: { menuIds }
  })
}
