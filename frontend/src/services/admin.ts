import type { AxiosResponse } from 'axios'
import { http } from './http'
import type { ApiResult } from '../types/api'

export interface PageResp<T> {
  total: number
  pageNum: number
  pageSize: number
  list: T[]
}

export interface AdminUser {
  id: number
  username: string
  role: string
  status: 'ENABLED' | 'DISABLED'
  nickname?: string
  avatarUrl?: string
  createTime?: number
}

export async function adminListUsers(pageNum = 1, pageSize = 10, role?: string, keyword?: string) {
  const params = { pageNum, pageSize, role, keyword }
  const resp: AxiosResponse<ApiResult<PageResp<AdminUser>>> = await http.get('/api/v1/admin/users', { params })
  return resp.data
}

export async function adminPatchUserStatus(id: number, status: 'ENABLED' | 'DISABLED') {
  const resp: AxiosResponse<ApiResult<void>> = await http.patch(`/api/v1/admin/users/${id}/status`, { status })
  return resp.data
}

export async function adminResetPassword(id: number, newPassword: string) {
  const resp: AxiosResponse<ApiResult<void>> = await http.post(`/api/v1/admin/users/${id}/reset-password`, { newPassword })
  return resp.data
}

export async function adminStats(days = 30) {
  const resp: AxiosResponse<ApiResult<any>> = await http.get('/api/v1/admin/stats', { params: { days } })
  return resp.data
}

export async function adminPageAppointments(pageNum = 1, pageSize = 10, status?: string) {
  const resp: AxiosResponse<ApiResult<PageResp<any>>> = await http.get('/api/v1/admin/appointments', { params: { pageNum, pageSize, status } })
  return resp.data
}

export async function adminPageThreads(pageNum = 1, pageSize = 10) {
  const resp: AxiosResponse<ApiResult<PageResp<any>>> = await http.get('/api/v1/admin/consult-threads', { params: { pageNum, pageSize } })
  return resp.data
}

export async function adminPageAssessments(pageNum = 1, pageSize = 10, scaleType?: string) {
  const resp: AxiosResponse<ApiResult<PageResp<any>>> = await http.get('/api/v1/admin/assessments', { params: { pageNum, pageSize, scaleType } })
  return resp.data
}

export async function adminHideThread(id: number, reason: string) {
  const resp: AxiosResponse<ApiResult<void>> = await http.patch(`/api/v1/admin/consult-threads/${id}/hide`, { reason })
  return resp.data
}

export async function adminHideMessage(id: number, reason: string) {
  const resp: AxiosResponse<ApiResult<void>> = await http.patch(`/api/v1/admin/consult-messages/${id}/hide`, { reason })
  return resp.data
}

export interface AdminAuditLog {
  id: number
  userId: number
  username: string
  action: string
  targetId?: number
  detail?: string
  ip?: string
  createTime: number
}

export async function adminListAuditLogs(pageNum = 1, pageSize = 10, userId?: number, action?: string) {
  const params = { pageNum, pageSize, userId, action }
  const resp: AxiosResponse<ApiResult<PageResp<AdminAuditLog>>> = await http.get('/api/v1/admin/audit-logs', { params })
  return resp.data
}

