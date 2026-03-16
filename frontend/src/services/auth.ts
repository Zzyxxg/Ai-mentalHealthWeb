import type { AxiosResponse } from 'axios'

import { http } from './http'
import type { ApiResult } from '../types/api'

export interface LoginReq {
  username: string
  password: string
}

export interface LoginResp {
  accessToken: string
  tokenType: string
  expiresIn: number
}

export interface UserMeResp {
  userId: number
  username: string
}

export async function login(req: LoginReq) {
  const resp: AxiosResponse<ApiResult<LoginResp>> = await http.post('/api/v1/auth/login', req)
  return resp.data
}

export async function me() {
  const resp: AxiosResponse<ApiResult<UserMeResp>> = await http.get('/api/v1/auth/me')
  return resp.data
}
