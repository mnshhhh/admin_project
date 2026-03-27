import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code === 200) return res
    if (res.code === 401) {
      localStorage.removeItem('token')
      router.push('/login')
      return Promise.reject(new Error(res.msg || '登录已过期'))
    }
    // 403 静默处理（权限不足由调用方自行决定是否提示）
    if (res.code === 403) {
      return Promise.reject(Object.assign(new Error(res.msg), { silent: true }))
    }
    if (!(response.config as any).silent) {
      ElMessage.error(res.msg || '操作失败')
    }
    return Promise.reject(new Error(res.msg))
  },
  (error) => {
    const status = error?.response?.status
    if (status === 401) {
      localStorage.removeItem('token')
      router.push('/login')
      return Promise.reject(new Error('登录已过期'))
    }
    if (status === 403) {
      return Promise.reject(Object.assign(new Error(error?.response?.data?.msg || '您没有操作权限'), { silent: true }))
    }
    if (!error?.config?.silent) {
      ElMessage.error(error.message || '网络错误')
    }
    return Promise.reject(error)
  }
)

export default request
