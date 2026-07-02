import type { TypeCountRatio } from './message-type-statistics'

export interface RoomTypeDailyRow {
  date: string
  types: Record<string, TypeCountRatio>
  total: number
}

export interface RoomTypeDailyStatisticsResponse {
  title: string
  from: string
  to: string
  messageType: string | null
  typeLabels: string[]
  rows: RoomTypeDailyRow[]
  totals: Record<string, TypeCountRatio>
  grandTotal: number
}
