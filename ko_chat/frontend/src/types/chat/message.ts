import type { MessageType } from './enums'
import type { ChatUser } from './chat-user'

export interface MessageMetadata {
  objectKey?: string | null
  url?: string | null
  fileName?: string | null
  mimeType?: string | null
  size?: number | null
  expiresAt?: string | null
  linkUrl?: string | null
  title?: string | null
  description?: string | null
  imageUrl?: string | null
  siteName?: string | null
  domain?: string | null
}

export interface Message {
  id: number
  chatRoomId: number
  sender: ChatUser
  type: MessageType
  content: string | null
  metadata?: MessageMetadata | null
  isEdited: boolean
  isDeleted: boolean
  createdAt: string
  editedAt?: string | null
  sequenceNumber: number
}
