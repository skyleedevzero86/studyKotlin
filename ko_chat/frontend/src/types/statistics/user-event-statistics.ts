import type { TypeCountRatio } from './message-type-statistics'

export interface UserEventDailyRow {
  date: string
  types: Record<string, TypeCountRatio>
  total: number
}

export interface UserEventDailyStatisticsResponse {
  title: string
  from: string
  to: string
  eventType: string | null
  typeLabels: string[]
  rows: UserEventDailyRow[]
  totals: Record<string, TypeCountRatio>
  grandTotal: number
}

export type UserActivityEventType = 'JOIN' | 'PASSWORD_CHANGE' | 'SUSPEND' | 'WITHDRAW'
