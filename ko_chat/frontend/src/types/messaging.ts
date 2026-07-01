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

export interface ConsumerLagSnapshot {
  consumerGroup: string
  topic: string
  partition: number
  currentOffset: number
  endOffset: number
  lag: number
}

export interface MessagingOperationsSnapshot {
  outbox: OutboxStatusSnapshot
  processedEvents: ProcessedEventSnapshot
  dlq: DlqStatusSnapshot
  consumerLag: ConsumerLagSnapshot[]
  lagHealthy: boolean
  checkedAt: string
}
