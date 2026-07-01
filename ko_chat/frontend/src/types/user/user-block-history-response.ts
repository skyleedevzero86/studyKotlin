import type { UserRelationshipUser } from './user-relationship-user'

export type UserBlockHistoryResponse = {
  id: number
  user: UserRelationshipUser
  blockedAt: string
  unblockedAt: string | null
  isActive: boolean
}
