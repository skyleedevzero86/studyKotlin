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

export interface CreateChatRoomRequest {
  name: string
  description?: string | null
  type: ChatRoomType
  imageUrl?: string | null
  maxMembers?: number
  isPrivate?: boolean
  password?: string | null
  mediaMode?: ChatMediaMode
}

export interface UpdateChatRoomSettingsRequest {
  name?: string | null
  description?: string | null
  isPrivate?: boolean
  password?: string | null
}

export interface CreateDirectChatRequest {
  targetUserId: number
}

export const getChatRoomDisplayName = (room: ChatRoom): string => {
  if (room.type === 'DIRECT' && room.peerUser) {
    return room.peerUser.displayName ?? room.peerUser.username
  }
  return room.name
}
