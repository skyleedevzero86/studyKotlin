export interface StatisticsCountRow {
  label: string
  count: number
  ratio: number
}

export interface StatisticsPeriodResponse {
  title: string
  from: string
  to: string
  roomType: string | null
  messageType: string | null
  rows: StatisticsCountRow[]
  total: number
}

export interface TypeCountRatio {
  count: number
  ratio: number
}

export interface MessageTypeYearRow {
  year: number
  types: Record<string, TypeCountRatio>
  total: number
}

export interface MessageTypeYearStatisticsResponse {
  title: string
  from: string
  to: string
  roomType: string | null
  typeLabels: string[]
  rows: MessageTypeYearRow[]
  totals: Record<string, TypeCountRatio>
  grandTotal: number
}

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

export type StatisticsTab = 'hourly' | 'message-types' | 'room-types'

export interface StatisticsFilterState {
  from: string
  to: string
  roomType: string
  messageType: string
}
