import request from '@/utils/request'

export function getPendingAudits(params: any) {
  return request.get('/audit/pending', { params })
}

export function getMyBorrows(params: any) {
  return request.get('/audit/myBorrows', { params })
}

export function auditAction(data: any) {
  return request.post('/audit/action', data)
}

export function getAuditHistory(bizType: string, bizId: number) {
  return request.get('/audit/history', { params: { bizType, bizId } })
}
