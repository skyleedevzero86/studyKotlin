import type { UserFriendRequestStatus } from './user-friend-request-status'
import type { UserRelationshipUser } from './user-relationship-user'

export type UserFriendRequestResponse = {
  id: number
  requester: UserRelationshipUser
  recipient: UserRelationshipUser
  status: UserFriendRequestStatus
  createdAt: string
  respondedAt: string | null
}
