import type { UserRelationshipUser } from './user-relationship-user'

export type UserRelationshipResponse = {
  id: number
  user: UserRelationshipUser
  createdAt: string
}
