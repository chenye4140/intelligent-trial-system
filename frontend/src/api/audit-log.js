import request from '@/utils/request'

export function getAuditLogList(params) {
  return request({
    url: '/audit-log/list',
    method: 'get',
    params
  })
}

export function getAuditLogDetail(id) {
  return request({
    url: `/audit-log/${id}`,
    method: 'get'
  })
}
