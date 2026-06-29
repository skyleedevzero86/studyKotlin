export type UserSensitivePayload = {
  displayName: string | null
  role: string
  status: string
  createdAt: string
  passwordChangedAt: string
  passwordChangeFailCount: number
  loginFailCount: number
  lastLoginAt: string | null
  passwordExpired: boolean
  daysUntilPasswordChange: number
}

export type UserSummaryResponse = {
  username: string
  encryptedPayload: string
}

export type UserProfileResponse = {
  id: number
  username: string
  displayName: string | null
  role: string
  status: string
  createdAt: string
  passwordChangedAt: string
  passwordChangeFailCount: number
  loginFailCount: number
  lastLoginAt: string | null
  passwordExpired: boolean
  daysUntilPasswordChange: number
}

export type RevealedUserRow = {
  username: string
  encryptedPayload: string
  revealed: boolean
  sensitive?: UserSensitivePayload
  decryptError?: string
}

export type ApiMessageResponse = {
  message: string
  status?: string
  role?: string
}

export type UserRelationshipResponse = {
  id: number
  user: {
    id: number
    username: string
    displayName: string | null
    isActive: boolean
    createdAt?: string | null
  }
  createdAt: string
}

export type UserFriendRequestStatus = 'PENDING' | 'ACCEPTED' | 'REJECTED'

export type UserFriendRequestResponse = {
  id: number
  requester: UserRelationshipResponse['user']
  recipient: UserRelationshipResponse['user']
  status: UserFriendRequestStatus
  createdAt: string
  respondedAt: string | null
}

export type UserBlockHistoryResponse = {
  id: number
  user: UserRelationshipResponse['user']
  blockedAt: string
  unblockedAt: string | null
  isActive: boolean
}
