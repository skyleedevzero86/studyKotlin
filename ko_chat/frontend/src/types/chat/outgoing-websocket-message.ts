import type { MessageType } from './message-type'

export interface OutgoingWebSocketMessage {
  type: 'SEND_MESSAGE'
  chatRoomId: number
  messageType: MessageType
  content?: string | null
  metadata?: string | null
}
