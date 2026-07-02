export interface ParticipantUploadRowResult {
  row: number
  identifier: string
  success: boolean
  message: string
}

export interface ParticipantUploadResult {
  totalRows: number
  successCount: number
  failureCount: number
  rows: ParticipantUploadRowResult[]
}
