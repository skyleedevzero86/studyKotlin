export interface IncomingErrorMessage {
  type: 'ERROR'
  message: string
  code?: string
  chatRoomId?: number | null
  timestamp?: string
}
