import request from '@/utils/request'

export function applyAsset(data: any) {
  return request.post('/asset/application', data)
}

export function getMyApplications(params: any) {
  return request.get('/asset/application/my', { params })
}

export function getPendingApplications(params: any) {
  return request.get('/asset/application/pending', { params })
}

export function getApplicationDetail(id: number) {
  return request.get(`/asset/application/${id}`)
}

export function approveApplication(id: number, data: any) {
  return request.post(`/asset/application/${id}/approve`, data)
}

export function createPurchaseOrder(data: any) {
  return request.post('/asset/application/purchase-order', data)
}

export function updatePurchaseOrder(data: any) {
  return request.put('/asset/application/purchase-order', data)
}

export function getPurchaseOrders(params: any) {
  return request.get('/asset/application/purchase-order/list', { params })
}

export function getPurchaseOrderDetail(id: number) {
  return request.get(`/asset/application/purchase-order/${id}`)
}

export function submitWarehouseEntry(data: any) {
  return request.post('/asset/application/warehouse-entry', data)
}

export function getWarehouseEntries(params: any) {
  return request.get('/asset/application/warehouse-entry/list', { params })
}

export function getWarehouseEntryDetail(id: number) {
  return request.get(`/asset/application/warehouse-entry/${id}`)
}

export function approveWarehouseEntry(id: number, data: any) {
  return request.post(`/asset/application/warehouse-entry/${id}/approve`, data)
}
