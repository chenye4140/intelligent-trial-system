import request from '@/utils/request'

export function getMenuTree() {
  return request({
    url: '/system/menu/tree',
    method: 'get'
  })
}

export function getUserMenuTree() {
  return request({
    url: '/system/menu/user-tree',
    method: 'get'
  })
}

export function getRoleMenuTree(roleId) {
  return request({
    url: `/system/menu/role-tree/${roleId}`,
    method: 'get'
  })
}

export function addMenu(data) {
  return request({
    url: '/system/menu',
    method: 'post',
    data
  })
}

export function updateMenu(data) {
  return request({
    url: '/system/menu',
    method: 'put',
    data
  })
}

export function deleteMenu(id) {
  return request({
    url: `/system/menu/${id}`,
    method: 'delete'
  })
}
