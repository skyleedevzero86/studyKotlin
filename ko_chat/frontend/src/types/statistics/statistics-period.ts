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
