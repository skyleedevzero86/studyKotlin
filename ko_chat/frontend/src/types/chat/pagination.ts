import type { Message } from './message'

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
