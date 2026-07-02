export interface ConsumerLagSnapshot {
  consumerGroup: string
  topic: string
  partition: number
  currentOffset: number
  endOffset: number
  lag: number
}
