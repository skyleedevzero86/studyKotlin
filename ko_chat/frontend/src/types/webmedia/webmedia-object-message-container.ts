import type { WebMediaMessageType } from './webmedia-message-type'

export interface WebMediaObjectMessageContainer {
  roomId: string
  from: string
  to: string
  type: WebMediaMessageType
  messageId: string
  message: Record<string, unknown>
}
