export type StatisticsTab = 'hourly' | 'message-types' | 'room-types' | 'user-events'

export interface StatisticsFilterState {
  from: string
  to: string
  roomType: string
  messageType: string
  userEventType: string
}
