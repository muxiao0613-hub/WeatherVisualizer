import axios from 'axios'
import { ElMessage } from 'element-plus'
import type { ApiResponse } from '@/types'

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || 'http://localhost:8080',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

http.interceptors.request.use(
  (config) => {
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

http.interceptors.response.use(
  (response) => {
    const res: ApiResponse<any> = response.data

    if (res.code === 0) {
      return res.data
    } else {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
  },
  (error) => {
    if (error.response) {
      ElMessage.error(error.response.data?.message || '服务器错误')
    } else if (error.request) {
      ElMessage.error('网络错误，请检查网络连接')
    } else {
      ElMessage.error('请求失败')
    }
    return Promise.reject(error)
  }
)

export default http
