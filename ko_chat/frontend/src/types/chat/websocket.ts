import type { MessageType } from './enums'
import type { MessageMetadata } from './message'

export type WebSocketMessageType = 'SEND_MESSAGE' | 'CHAT_MESSAGE' | 'ERROR'

export interface OutgoingWebSocketMessage {
  type: 'SEND_MESSAGE'
  chatRoomId: number
  messageType: MessageType
  content?: string | null
  metadata?: string | null
}

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
}

export interface IncomingErrorMessage {
  type: 'ERROR'
  message: string
  code?: string
  chatRoomId?: number | null
  timestamp?: string
}

export type IncomingWebSocketMessage = IncomingChatMessage | IncomingErrorMessage | Record<string, unknown>
