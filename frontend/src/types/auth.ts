export type UserRole = 'STUDENT' | 'CONSULTANT' | 'ADMIN'

export interface UserMe {
  id: number
  username: string
  role: UserRole
  nickname?: string
  avatarUrl?: string
}
