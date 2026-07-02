export interface OutboxStatusSnapshot {
  pending: number
  published: number
  failed: number
}

export interface ProcessedEventSnapshot {
  total: number
  byConsumer: Record<string, number>
}

export interface DlqStatusSnapshot {
  open: number
  replayed: number
}
