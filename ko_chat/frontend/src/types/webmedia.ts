export type WebMediaMessageType =
  | 'JoinRequest'
  | 'JoinResponse'
  | 'ErrorResponse'
  | 'UserJoinedEvent'
  | 'UserLeftEvent'
  | 'UserStateChangedEvent'
  | 'UserPublishedChangeReport'
  | 'UserKickedEvent'

export interface WebMediaUser {
  userId: string
  published: boolean
}

export interface WebMediaJoinResponse {
  apiUrl: string
  streamUrl: string
  roomId: string
  user: WebMediaUser
  otherUsers: WebMediaUser[]
  maxMembers: number
}

export interface WebMediaObjectMessageContainer {
  roomId: string
  from: string
  to: string
  type: WebMediaMessageType
  messageId: string
  message: Record<string, unknown>
}
