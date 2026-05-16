import request from '@/utils/request'

export function getAuditLogList(params) {
  return request({
    url: '/system/audit-log/page',
    method: 'get',
    params
  })
}
