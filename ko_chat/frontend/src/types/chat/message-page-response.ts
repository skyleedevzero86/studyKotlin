import type { Message } from './message'

export interface MessagePageResponse {
  messages: Message[]
  nextCursor: number | null
  prevCursor: number | null
  hasNext: boolean
  hasPrev: boolean
}
