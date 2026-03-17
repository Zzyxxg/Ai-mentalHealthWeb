import type { AxiosResponse } from 'axios'

import { http } from './http'
import type { ApiResult } from '../types/api'

export interface LoginReq {
  username: string
  password: string
}

export interface LoginResp {
  token: string
  type: string
  expiresIn: number
}

export interface UserMeResp {
  id: number
  username: string
  role: string
  nickname?: string
  avatarUrl?: string
}

export interface RegisterStudentReq {
  username: string
  password: string
  nickname: string
  realName?: string
  studentNo?: string
}

export interface RegisterCounselorReq {
  username: string
  password: string
  realName: string
  title: string
  expertise: string
  intro?: string
}

export async function login(req: LoginReq) {
  const resp: AxiosResponse<ApiResult<LoginResp>> = await http.post('/api/v1/auth/login', req)
  return resp.data
}

export async function registerStudent(req: RegisterStudentReq) {
  const resp: AxiosResponse<ApiResult<any>> = await http.post('/api/v1/auth/register/student', req)
  return resp.data
}

export async function registerCounselor(req: RegisterCounselorReq) {
  const resp: AxiosResponse<ApiResult<any>> = await http.post('/api/v1/auth/register/counselor', req)
  return resp.data
}

export async function me() {
  const resp: AxiosResponse<ApiResult<UserMeResp>> = await http.get('/api/v1/auth/me')
  return resp.data
}
