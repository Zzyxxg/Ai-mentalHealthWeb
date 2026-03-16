export type UserRole = 'STUDENT' | 'CONSULTANT' | 'ADMIN'

export interface UserMe {
  id: string
  username: string
  role: UserRole
  nickname?: string
  avatarUrl?: string
}
