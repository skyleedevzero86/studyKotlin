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
