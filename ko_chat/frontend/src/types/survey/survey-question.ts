import type { QuestionType } from './enums'

export interface SurveyOption {
  id: number
  optionNo: number
  optionText: string
  selectCount?: number
}

export interface SurveyQuestion {
  id: number
  questionNo: number
  questionType: QuestionType
  questionText: string
  options: SurveyOption[]
}
