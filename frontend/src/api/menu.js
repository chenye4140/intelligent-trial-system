import request from '@/utils/request'

export function getMenuTree() {
  return request({
    url: '/menu/tree',
    method: 'get'
  })
}

export function addMenu(data) {
  return request({
    url: '/menu',
    method: 'post',
    data
  })
}

export function updateMenu(id, data) {
  return request({
    url: `/menu/${id}`,
    method: 'put',
    data
  })
}

export function deleteMenu(id) {
  return request({
    url: `/menu/${id}`,
    method: 'delete'
  })
}

export function getMenuList() {
  return request({
    url: '/menu/list',
    method: 'get'
  })
}
