import type { QuestionType, TargetMode } from './enums'

export interface SurveyOptionRequest {
  optionText: string
}

export interface SurveyQuestionRequest {
  questionText: string
  questionType: QuestionType
  options: SurveyOptionRequest[]
}

export interface CreateSurveyRequest {
  title: string
  description?: string | null
  targetMode: TargetMode
  randomTargetCount?: number | null
  startAt?: string | null
  endAt?: string | null
  questions: SurveyQuestionRequest[]
  targetUserIds?: number[]
}

export interface SurveyAnswerItemRequest {
  questionId: number
  optionIds?: number[]
  textAnswer?: string | null
}

export interface SubmitSurveyResponseRequest {
  answers: SurveyAnswerItemRequest[]
}

export interface AssignRandomParticipantsRequest {
  count: number
}
