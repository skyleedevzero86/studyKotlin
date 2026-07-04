export interface IncomingMemberReadReceipt {
  type: 'MEMBER_READ'
  userId: number
  lastReadMessageId?: number | null
  chatRoomId: number
  timestamp: string
}
