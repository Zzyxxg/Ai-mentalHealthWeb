export interface ApiResult<T> {
  code: number
  msg: string
  data: T
  timestamp: number
  traceId: string
}
