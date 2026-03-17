import dayjs from 'dayjs'

/**
 * 状态映射工具函数 (英文标识符 -> 中文)
 */

export const USER_ROLE_MAP: Record<string, string> = {
  'STUDENT': '学生',
  'CONSULTANT': '咨询师',
  'ADMIN': '管理员'
}

export const USER_STATUS_MAP: Record<string, string> = {
  'ENABLED': '启用',
  'DISABLED': '禁用'
}

export const APPOINTMENT_STATUS_MAP: Record<string, string> = {
  'CREATED': '已提交',
  'CONFIRMED': '已预约',
  'CANCELED': '已取消',
  'CANCELLED': '已取消',
  'COMPLETED': '已完成'
}

export const CONSULT_THREAD_STATUS_MAP: Record<string, string> = {
  'UNPROCESSED': '待处理',
  'PROCESSING': '进行中',
  'CLOSED': '已结束'
}

export const SCHEDULE_SLOT_STATUS_MAP: Record<string, string> = {
  'AVAILABLE': '空闲',
  'OCCUPIED': '已占用',
  'UNAVAILABLE': '不可用'
}

export const NOTIFICATION_STATUS_MAP: Record<number, string> = {
  0: '未读',
  1: '已读'
}

export const NOTIFICATION_TYPE_MAP: Record<string, string> = {
  'APPOINTMENT_CREATED': '预约成功',
  'APPOINTMENT_CANCELED': '预约取消',
  'CONSULTANT_REPLIED': '咨询回复',
  'STUDENT_MESSAGE': '咨询新消息'
}

export const ASSESSMENT_LEVEL_MAP: Record<string, string> = {
  'MINIMAL': '轻微',
  'MILD': '轻度',
  'MODERATE': '中度',
  'MODERATELY_SEVERE': '中重度',
  'SEVERE': '重度',
  'NORMAL': '正常'
}

/**
 * 格式化函数
 */

export function formatNotificationType(type: string) {
  return NOTIFICATION_TYPE_MAP[type] || type
}

export function formatAssessmentLevel(level: string) {
  return ASSESSMENT_LEVEL_MAP[level] || level
}

export function formatRole(role: string) {
  return USER_ROLE_MAP[role] || role
}

export function formatUserStatus(status: string) {
  return USER_STATUS_MAP[status] || status
}

export function formatAppointmentStatus(status: string) {
  return APPOINTMENT_STATUS_MAP[status] || status
}

export function formatConsultThreadStatus(status: string) {
  return CONSULT_THREAD_STATUS_MAP[status] || status
}

export function formatScheduleSlotStatus(status: string) {
  return SCHEDULE_SLOT_STATUS_MAP[status] || status
}

export function formatDateTime(time?: number | string) {
  if (!time) return '-'
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

export function formatFullTime(time?: number | string) {
  if (!time) return '-'
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}
