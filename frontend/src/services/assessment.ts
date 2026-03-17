import type { AxiosResponse } from 'axios'
import { http } from './http'
import type { ApiResult } from '../types/api'

export interface ScaleOption {
  score: number
  label: string
}

export interface ScaleQuestion {
  index: number
  title: string
  options: ScaleOption[]
}

export interface AssessmentScale {
  type: 'PHQ9' | 'GAD7'
  name: string
  questions: ScaleQuestion[]
}

export interface AssessmentRecord {
  id: number
  userId: number
  scaleType: string
  totalScore: number
  level: string
  suggestion: string
  createTime: number
}

export interface PageResp<T> {
  total: number
  pageNum: number
  pageSize: number
  list: T[]
}

export async function listScales() {
  const resp: AxiosResponse<ApiResult<AssessmentScale[]>> = await http.get('/api/v1/assessments/scales')
  return resp.data
}

export async function getScale(type: string) {
  const resp: AxiosResponse<ApiResult<AssessmentScale>> = await http.get(`/api/v1/assessments/scales/${type}`)
  return resp.data
}

export async function submitAssessment(data: { scaleType: string; answers: number[] }) {
  const resp: AxiosResponse<ApiResult<AssessmentRecord>> = await http.post('/api/v1/assessments', data)
  return resp.data
}

export async function listMyAssessments(pageNum = 1, pageSize = 10, scaleType?: string) {
  const params = { pageNum, pageSize, scaleType }
  const resp: AxiosResponse<ApiResult<PageResp<AssessmentRecord>>> = await http.get('/api/v1/assessments/my', { params })
  return resp.data
}

