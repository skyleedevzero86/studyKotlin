import type { IncomingChatMessage } from './incoming-chat-message'
import type { IncomingErrorMessage } from './incoming-error-message'
import type { IncomingMemberReadReceipt } from './incoming-member-read-receipt'

export type IncomingWebSocketMessage =
  | IncomingChatMessage
  | IncomingMemberReadReceipt
  | IncomingErrorMessage
  | Record<string, unknown>
