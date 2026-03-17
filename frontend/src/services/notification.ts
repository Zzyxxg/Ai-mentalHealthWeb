import type { AxiosResponse } from 'axios'
import { http } from './http'
import type { ApiResult } from '../types/api'
import type { PageResp } from './consult'

export interface NotificationItem {
  id: number
  receiverUserId: number
  studentName?: string | null
  counselorName?: string | null
  type: string
  title: string
  content: string
  readFlag: boolean
  createTime: number
}

export async function listNotifications(pageNum = 1, pageSize = 10, readFlag?: 0 | 1) {
  const params: Record<string, any> = { pageNum, pageSize }
  if (readFlag !== undefined) params.readFlag = readFlag
  const resp: AxiosResponse<ApiResult<PageResp<NotificationItem>>> = await http.get('/api/v1/notifications', { params })
  return resp.data
}

export async function readNotification(id: number) {
  const resp: AxiosResponse<ApiResult<void>> = await http.patch(`/api/v1/notifications/${id}/read`)
  return resp.data
}

export async function readAllNotifications() {
  const resp: AxiosResponse<ApiResult<void>> = await http.post('/api/v1/notifications/read-all')
  return resp.data
}
