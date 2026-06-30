export interface ChatNotification {
  id: string
  type: 'error' | 'system'
  title: string
  message: string
  timestamp: number
}
