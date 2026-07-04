import type { MessageType } from './message-type'
import type { ChatUser } from './chat-user'
import type { MessageMetadata } from './message-metadata'

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
  unreadMemberCount?: number | null
}
