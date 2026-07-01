import type { IncomingChatMessage } from './incoming-chat-message'
import type { IncomingErrorMessage } from './incoming-error-message'

export type IncomingWebSocketMessage =
  | IncomingChatMessage
  | IncomingErrorMessage
  | Record<string, unknown>
