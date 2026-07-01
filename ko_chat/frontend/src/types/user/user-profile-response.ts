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
