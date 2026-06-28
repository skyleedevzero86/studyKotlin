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
