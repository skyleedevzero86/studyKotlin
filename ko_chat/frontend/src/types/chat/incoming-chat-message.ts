import type { MessageType } from './message-type'
import type { MessageMetadata } from './message-metadata'

export interface IncomingChatMessage {
  type: 'CHAT_MESSAGE'
  id: number
  content: string
  messageType?: MessageType
  metadata?: string | MessageMetadata | null
  senderId: number
  senderName: string
  sequenceNumber: number
  chatRoomId: number
  timestamp: string
  unreadMemberCount?: number | null
}
