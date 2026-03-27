import request from '@/utils/request'

export function getUserList(params: any) {
  return request.get('/sys/user/list', { params })
}

export function addUser(data: any) {
  return request.post('/sys/user', data)
}

export function updateUser(data: any) {
  return request.put('/sys/user', data)
}

export function deleteUser(userId: number) {
  return request.delete(`/sys/user/${userId}`)
}

export function resetPassword(userId: number, password: string) {
  return request.put('/sys/user/resetPwd', { userId, password })
}

export function getDeptTree() {
  return request.get('/sys/dept/tree')
}

export function addDept(data: any) {
  return request.post('/sys/dept', data)
}

export function updateDept(data: any) {
  return request.put('/sys/dept', data)
}

export function deleteDept(deptId: number) {
  return request.delete(`/sys/dept/${deptId}`)
}

export function getRoleList() {
  return request.get('/sys/role/list')
}

export function addRole(data: any) {
  return request.post('/sys/role', data)
}

export function updateRole(data: any) {
  return request.put('/sys/role', data)
}

export function deleteRole(roleId: number) {
  return request.delete(`/sys/role/${roleId}`)
}

export function getRoleMenuIds(roleId: number) {
  return request.get(`/sys/role/${roleId}/menuIds`)
}

export function getMenuTree() {
  return request.get('/sys/menu/tree')
}

export function getLogList(params: any) {
  return request.get('/sys/log/list', { params })
}
