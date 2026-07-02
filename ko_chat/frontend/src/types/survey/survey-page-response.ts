import type { SurveySummary } from './survey-summary'

export interface SurveyPageResponse {
  content: SurveySummary[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}
