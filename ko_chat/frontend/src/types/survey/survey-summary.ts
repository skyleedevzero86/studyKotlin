import type { SurveyStatus, TargetMode } from './enums'

export interface SurveySummary {
  id: number
  chatRoomId: number | null
  chatRoomName: string
  title: string
  description: string | null
  status: SurveyStatus
  targetMode: TargetMode
  randomTargetCount: number | null
  startAt: string | null
  endAt: string | null
  questionCount: number
  participantCount: number
  completedCount: number
  createdByUserId: number
  createdByUsername: string
  createdAt: string | null
  canRespond?: boolean
  hasResponded?: boolean
  waitingForStart?: boolean
}
