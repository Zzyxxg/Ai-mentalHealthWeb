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

export async function login(req: LoginReq) {
  const resp: AxiosResponse<ApiResult<LoginResp>> = await http.post('/api/v1/auth/login', req)
  return resp.data
}

export async function me() {
  const resp: AxiosResponse<ApiResult<UserMeResp>> = await http.get('/api/v1/auth/me')
  return resp.data
}
