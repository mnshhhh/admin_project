import request from '@/utils/request'

export function applyRepair(data: any) {
  return request.post('/repair/apply', data)
}

export function getRepairList(params: any) {
  return request.get('/repair/list', { params })
}

export function acceptRepair(id: number) {
  return request.put(`/repair/${id}/accept`)
}

export function completeRepair(id: number, data: any) {
  return request.put(`/repair/${id}/complete`, data)
}
