export type StatisticsTab = 'hourly' | 'message-types' | 'room-types'

export interface StatisticsFilterState {
  from: string
  to: string
  roomType: string
  messageType: string
}
