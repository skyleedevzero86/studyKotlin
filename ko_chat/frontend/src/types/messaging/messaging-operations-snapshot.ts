import type { OutboxStatusSnapshot, ProcessedEventSnapshot, DlqStatusSnapshot } from './messaging-snapshots'
import type { ConsumerLagSnapshot } from './consumer-lag-snapshot'

export interface MessagingOperationsSnapshot {
  kafkaEnabled: boolean
  outbox: OutboxStatusSnapshot
  processedEvents: ProcessedEventSnapshot
  dlq: DlqStatusSnapshot
  consumerLag: ConsumerLagSnapshot[]
  lagHealthy: boolean
  checkedAt: string
}
