import request from '@/utils/request'

export function getAssetList(params: any) {
  return request.get('/asset/list', { params })
}

export function getAssetDetail(id: number) {
  return request.get(`/asset/${id}`)
}

export function getAssetByCode(assetCode: string) {
  return request.get(`/asset/code/${assetCode}`)
}

export function addAsset(data: any) {
  return request.post('/asset', data)
}

export function updateAsset(data: any) {
  return request.put('/asset', data)
}

export function deleteAsset(id: number) {
  return request.delete(`/asset/${id}`)
}

export function borrowAsset(data: any) {
  return request.post('/asset/borrow', data)
}

export function returnAsset(borrowId: number) {
  return request.put(`/asset/return/${borrowId}`)
}

export function getStatusStats() {
  return request.get('/asset/statistics/status')
}

export function getCategoryStats() {
  return request.get('/asset/statistics/category')
}
