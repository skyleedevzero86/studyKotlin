export interface ChatUser {
  id: number
  username: string
  displayName: string | null
  profileImageUrl?: string | null
  status?: string | null
  isActive: boolean
  lastSeenAt?: string | null
  createdAt?: string | null
}
