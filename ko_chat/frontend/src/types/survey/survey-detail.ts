import type { SurveySummary } from './survey-summary'
import type { SurveyQuestion } from './survey-question'
import type { SurveyParticipant } from './survey-participant'

export interface SurveyDetail extends SurveySummary {
  questions: SurveyQuestion[]
  participants: SurveyParticipant[]
  canRespond: boolean
  hasResponded: boolean
}
