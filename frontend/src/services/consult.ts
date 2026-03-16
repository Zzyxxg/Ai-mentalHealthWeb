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

export interface ConsultThread {
  id: number
  studentUserId: number
  counselorUserId: number
  counselorName?: string
  studentName?: string
  topic: string
  content: string
  status: string
  createTime: number
  updateTime: number
  messages?: ConsultMessage[]
}

export interface CreateThreadReq {
  counselorUserId: number
  topic: string
  content: string
}

export interface ConsultMessage {
  id: number
  threadId: number
  senderRole: string
  senderId?: number
  senderName?: string
  content: string
  createTime: number
}

export interface SendMessageReq {
  threadId: number
  content: string
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

export async function createConsultThread(data: CreateThreadReq) {
  const resp: AxiosResponse<ApiResult<ConsultThread>> = await http.post('/api/v1/consult-threads', data)
  return resp.data
}

export async function listConsultThreads(pageNum = 1, pageSize = 10, status?: string) {
  const params = { pageNum, pageSize, status }
  const resp: AxiosResponse<ApiResult<PageResp<ConsultThread>>> = await http.get('/api/v1/consult-threads', { params })
  return resp.data
}

export async function getConsultThread(id: number) {
  const resp: AxiosResponse<ApiResult<ConsultThread>> = await http.get(`/api/v1/consult-threads/${id}`)
  return resp.data
}

export async function listConsultMessages(threadId: number) {
  // 后端目前在 getConsultThread 中直接返回 messages，此方法仅作为预留
  const resp = await getConsultThread(threadId)
  return {
    code: resp.code,
    msg: resp.msg,
    data: {
      list: resp.data.messages || [],
      total: resp.data.messages?.length || 0,
      pageNum: 1,
      pageSize: 100
    }
  }
}

export async function sendConsultMessage(data: SendMessageReq) {
  const resp: AxiosResponse<ApiResult<ConsultMessage>> = await http.post('/api/v1/consult-messages', data)
  return resp.data
}
