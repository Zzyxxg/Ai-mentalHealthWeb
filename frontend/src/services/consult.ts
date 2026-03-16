import type { AxiosResponse } from 'axios'

import { http } from './http'
import type { ApiResult } from '../types/api'

export interface Counselor {
  id: number
  userId: number
  realName: string
  title: string
  expertise: string
  intro: string
}

export interface Appointment {
  id: number
  userId: number
  counselorUserId: number
  startTime: number
  durationMinutes: number
  status: string
  note: string
}

export interface CreateAppointmentReq {
  counselorUserId: number
  startTime: number
  durationMinutes: number
  note?: string
}

export interface PageResp<T> {
  total: number
  pageNum: number
  pageSize: number
  list: T[]
}

export async function listConsultants(keyword?: string) {
  const params = keyword ? { keyword } : {}
  const resp: AxiosResponse<ApiResult<Counselor[]>> = await http.get('/api/v1/consultants', { params })
  return resp.data
}

export async function getConsultant(id: number) {
  const resp: AxiosResponse<ApiResult<Counselor>> = await http.get(`/api/v1/consultants/${id}`)
  return resp.data
}

export async function createAppointment(data: CreateAppointmentReq, idempotencyKey?: string) {
  const headers = idempotencyKey ? { 'Idempotency-Key': idempotencyKey } : {}
  const resp: AxiosResponse<ApiResult<Appointment>> = await http.post('/api/v1/consult-appointments', data, { headers })
  return resp.data
}

export async function listAppointments(pageNum = 1, pageSize = 10, status?: string) {
  const params = { pageNum, pageSize, status }
  const resp: AxiosResponse<ApiResult<PageResp<Appointment>>> = await http.get('/api/v1/consult-appointments', { params })
  return resp.data
}

export async function cancelAppointment(id: number) {
  const resp: AxiosResponse<ApiResult<Appointment>> = await http.patch(`/api/v1/consult-appointments/${id}`, { action: 'CANCEL' })
  return resp.data
}
