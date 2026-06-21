export type UserSensitivePayload = {
  role: string
  status: string
  createdAt: string
  passwordChangedAt: string
  passwordChangeFailCount: number
  lastLoginAt: string | null
  passwordExpired: boolean
  daysUntilPasswordChange: number
}

export type UserSummaryResponse = {
  username: string
  encryptedPayload: string
}

export type RevealedUserRow = {
  username: string
  encryptedPayload: string
  revealed: boolean
  sensitive?: UserSensitivePayload
  decryptError?: string
}
