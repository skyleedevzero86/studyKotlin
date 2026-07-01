import type { WebMediaUser } from './webmedia-user'

export interface WebMediaJoinResponse {
  apiUrl: string
  streamUrl: string
  roomId: string
  user: WebMediaUser
  otherUsers: WebMediaUser[]
  maxMembers: number
}
