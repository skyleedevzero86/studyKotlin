import type { ChatMediaMode, ChatRoomType } from './enums'
import type { ChatUser } from './chat-user'
import type { Message } from './message'

export interface ChatRoom {
  id: number
  name: string
  description?: string | null
  type: ChatRoomType
  imageUrl?: string | null
  isActive: boolean
  maxMembers: number
  isPrivate?: boolean
  memberCount: number
  createdBy: ChatUser
  createdAt: string
  lastMessage?: Message | null
  peerUser?: ChatUser | null
  unreadCount: number
  isJoined?: boolean
  mediaMode?: ChatMediaMode
}
