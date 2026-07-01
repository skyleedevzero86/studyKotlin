import type { MessageType } from './message-type'
import type { MessageMetadata } from './message-metadata'

export interface AttachmentUploadResponse {
  messageType: MessageType
  metadata: MessageMetadata
  content: string | null
}
