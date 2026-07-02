import type { QuestionType, ParticipantStatus } from './enums'
import type { SurveyOption } from './survey-question'

export interface SurveyQuestionStatistics {
  questionId: number
  questionNo: number
  questionText: string
  questionType: QuestionType
  respondentCount: number
  options: SurveyOption[]
  textAnswers: string[]
}

export interface ParticipantAnswer {
  questionId: number
  questionText: string
  optionTexts: string[]
  textAnswer: string | null
}

export interface SurveyParticipantStatistics {
  userId: number
  username: string
  displayName: string | null
  status: ParticipantStatus
  answers: ParticipantAnswer[]
}

export interface SurveyStatistics {
  surveyId: number
  title: string
  totalParticipants: number
  completedParticipants: number
  byQuestion: SurveyQuestionStatistics[]
  byParticipant: SurveyParticipantStatistics[]
}

export interface SurveyRoomStatistics {
  chatRoomId: number
  chatRoomName: string
  surveyCount: number
  respondentCount: number
  completedCount: number
}

export interface SurveyRoomStatisticsListResponse {
  rows: SurveyRoomStatistics[]
}
