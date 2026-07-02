import type { SurveyStatus, TargetMode } from './enums'

export interface AdminSurveyFilter {
  status?: SurveyStatus
  chatRoomId?: number
  targetMode?: TargetMode
  title?: string
  from?: string
  to?: string
}
