import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi } from '@/api/auth'
import request from '@/utils/request'

const CACHE_KEY = 'user_info'

function saveCache(data: any) {
  sessionStorage.setItem(CACHE_KEY, JSON.stringify(data))
}

function loadCache() {
  try {
    const raw = sessionStorage.getItem(CACHE_KEY)
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem('token') || '')
  const userId = ref<number>(0)
  const username = ref<string>('')
  const nickname = ref<string>('')
  const roles = ref<string[]>([])
  const permissions = ref<string[]>([])
  const routers = ref<any[]>([])

  function applyData(data: any) {
    userId.value = data.userId || 0
    username.value = data.username || ''
    nickname.value = data.nickname || ''
    roles.value = data.roles || []
    permissions.value = Array.from(data.permissions || [])
    routers.value = data.routers || []
  }

  function hasPermission(perm: string) {
    if (!permissions.value.length) return false
    return permissions.value.includes('*:*:*') || permissions.value.includes(perm)
  }

  async function loginAction(form: { username: string; password: string }) {
    const res: any = await loginApi(form)
    const data = res.data
    token.value = data.token
    localStorage.setItem('token', data.token)
    applyData(data)
    saveCache(data)
    return data
  }

  async function restoreFromServer() {
    try {
      const res: any = await request.post('/auth/login', null)
      applyData(res.data)
      saveCache(res.data)
    } catch {
      // 降级到 sessionStorage 缓存
    }
  }

  function restoreFromCache(): boolean {
    const cached = loadCache()
    if (cached && token.value) {
      applyData(cached)
      return true
    }
    return false
  }

  function logout() {
    token.value = ''
    userId.value = 0
    username.value = ''
    nickname.value = ''
    roles.value = []
    permissions.value = []
    routers.value = []
    localStorage.removeItem('token')
    sessionStorage.removeItem(CACHE_KEY)
  }

  function isAdmin() {
    return roles.value.includes('admin')
  }

  return {
    token, userId, username, nickname, roles, permissions, routers,
    hasPermission, loginAction, restoreFromCache, logout, isAdmin
  }
})
