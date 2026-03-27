import request from '@/utils/request'

export function getNotificationList(params: any) {
  return request({ url: '/notification/list', method: 'get', params })
}

export function getUnreadCount() {
  return request({ url: '/notification/unread-count', method: 'get' })
}

export function markAsRead(id: number) {
  return request({ url: `/notification/${id}/read`, method: 'put' })
}

export function markAllAsRead() {
  return request({ url: '/notification/read-all', method: 'put' })
}
