import request from '@/utils/request'

export function getCheckTasks() {
  return request.get('/asset/check/tasks')
}

export function createCheckTask(data: any) {
  return request.post('/asset/check/task', data)
}

export function scanCheck(taskId: number, assetCode: string) {
  return request.post(`/asset/check/task/${taskId}/scan`, { assetCode })
}

export function completeCheckTask(taskId: number) {
  return request.put(`/asset/check/task/${taskId}/complete`)
}

export function getCheckDetails(taskId: number) {
  return request.get(`/asset/check/task/${taskId}/details`)
}
