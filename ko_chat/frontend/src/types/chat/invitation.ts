import type { ChatRoom } from './chat-room'
import type { ChatUser } from './chat-user'

export interface ChatRoomInvitation {
  id: number
  chatRoom: ChatRoom
  inviter: ChatUser
  invitee: ChatUser
  status: 'PENDING' | 'ACCEPTED' | 'REJECTED'
  createdAt: string
  respondedAt?: string | null
}
