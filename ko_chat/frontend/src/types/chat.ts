export type ChatRoomType = 'GROUP' | 'DIRECT' | 'CHANNEL'
export type MessageType = 'TEXT' | 'SYSTEM'
export type MemberRole = 'OWNER' | 'ADMIN' | 'MEMBER'
export type MessageDirection = 'BEFORE' | 'AFTER'

export interface ChatUser {
  id: number
  username: string
  displayName: string | null
  profileImageUrl?: string | null
  status?: string | null
  isActive: boolean
  lastSeenAt?: string | null
  createdAt?: string | null
}

export interface ChatRoom {
  id: number
  name: string
  description?: string | null
  type: ChatRoomType
  imageUrl?: string | null
  isActive: boolean
  maxMembers: number
  memberCount: number
  createdBy: ChatUser
  createdAt: string
  lastMessage?: Message | null
}

export interface CreateChatRoomRequest {
  name: string
  description?: string | null
  type: ChatRoomType
  imageUrl?: string | null
  maxMembers?: number
}

export interface Message {
  id: number
  chatRoomId: number
  sender: ChatUser
  type: MessageType
  content: string | null
  isEdited: boolean
  isDeleted: boolean
  createdAt: string
  editedAt?: string | null
  sequenceNumber: number
}

export interface ChatRoomMember {
  id: number
  user: ChatUser
  role: MemberRole
  isActive: boolean
  lastReadMessageId?: number | null
  joinedAt: string
  leftAt?: string | null
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  first: boolean
  last: boolean
  empty: boolean
}

export interface MessagePageResponse {
  messages: Message[]
  nextCursor: number | null
  prevCursor: number | null
  hasNext: boolean
  hasPrev: boolean
}

export type WebSocketMessageType = 'SEND_MESSAGE' | 'CHAT_MESSAGE' | 'ERROR'

export interface OutgoingWebSocketMessage {
  type: 'SEND_MESSAGE'
  chatRoomId: number
  messageType: MessageType
  content: string
}

export interface IncomingChatMessage {
  type: 'CHAT_MESSAGE'
  id: number
  content: string
  messageType?: MessageType
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

export interface ChatNotification {
  id: string
  type: 'error' | 'system'
  title: string
  message: string
  timestamp: number
}
