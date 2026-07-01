import type { ChatMediaMode, ChatRoomType } from './enums'

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
