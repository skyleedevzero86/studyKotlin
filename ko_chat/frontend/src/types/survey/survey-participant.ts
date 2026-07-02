import type { ParticipantStatus } from './enums'

export interface SurveyParticipant {
  userId: number
  username: string
  displayName: string | null
  status: ParticipantStatus
  assignedAt: string | null
  completedAt: string | null
}
