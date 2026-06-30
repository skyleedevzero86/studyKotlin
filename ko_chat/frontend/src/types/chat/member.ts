import type { MemberRole } from './enums'
import type { ChatUser } from './chat-user'

export interface ChatRoomMember {
  id: number
  user: ChatUser
  role: MemberRole
  isActive: boolean
  lastReadMessageId?: number | null
  joinedAt: string
  leftAt?: string | null
}
