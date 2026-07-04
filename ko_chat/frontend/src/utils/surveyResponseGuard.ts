export type SurveyResponseBlockReason = 'responded' | 'waiting' | 'closed' | 'not-target'

export type SurveyResponseGuardInput = {
  status: string
  canRespond: boolean
  hasResponded: boolean
  waitingForStart?: boolean
}

export const getSurveyResponseBlockReason = (
  survey: SurveyResponseGuardInput,
): SurveyResponseBlockReason | null => {
  if (survey.hasResponded) return 'responded'
  if (survey.waitingForStart) return 'waiting'
  if (survey.status === 'CLOSED') return 'closed'
  if (survey.status !== 'ACTIVE' || !survey.canRespond) return 'not-target'
  return null
}

export const canSubmitSurveyResponse = (survey: SurveyResponseGuardInput): boolean =>
  getSurveyResponseBlockReason(survey) === null

export const surveyResponseBlockMessage = (
  reason: SurveyResponseBlockReason,
  options?: { startAt?: string | null; endAt?: string | null },
): string => {
  switch (reason) {
    case 'responded':
      return '이미 응답을 완료한 설문입니다.'
    case 'waiting':
      return options?.startAt
        ? `설문 시작 전입니다. (시작: ${options.startAt.replace('T', ' ').slice(0, 16)})`
        : '설문 시작 전입니다.'
    case 'closed':
      return options?.endAt
        ? `설문이 종료되어 참여할 수 없습니다. (종료: ${options.endAt.replace('T', ' ').slice(0, 16)})`
        : '설문이 종료되어 참여할 수 없습니다.'
    case 'not-target':
      return '설문에 참여할 수 없습니다.'
  }
}
