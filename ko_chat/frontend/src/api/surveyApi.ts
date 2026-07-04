import { deleteJson, downloadFile, getJson, postFormData, postJson, putJson } from './http'
import type {
  AdminSurveyFilter,
  AssignRandomParticipantsRequest,
  CreateSurveyRequest,
  ParticipantUploadResult,
  SubmitSurveyResponseRequest,
  SurveyDetail,
  SurveyRoomStatisticsListResponse,
  SurveyStatistics,
  SurveySummary,
} from '../types/survey'
import type { PageResponse } from '../types/chat'

const roomSurveyPath = (roomId: number) => `/api/v1/chat-rooms/${roomId}/surveys`
const adminSurveyPath = '/api/v1/admin/surveys'

export const listRoomSurveys = (
  token: string,
  roomId: number,
  includeAll = false,
  page = 0,
  size = 10,
): Promise<PageResponse<SurveySummary>> =>
  getJson(
    `${roomSurveyPath(roomId)}?includeAll=${includeAll}&page=${page}&size=${size}`,
    token,
  )

export const getSurvey = (
  token: string,
  roomId: number,
  surveyId: number,
): Promise<SurveyDetail> => getJson(`${roomSurveyPath(roomId)}/${surveyId}`, token)

export const createSurvey = (
  token: string,
  roomId: number,
  data: CreateSurveyRequest,
): Promise<SurveyDetail> => postJson(roomSurveyPath(roomId), data, token)

export const updateSurvey = (
  token: string,
  roomId: number,
  surveyId: number,
  data: CreateSurveyRequest,
): Promise<SurveyDetail> => putJson(`${roomSurveyPath(roomId)}/${surveyId}`, data, token)

export const publishSurvey = (
  token: string,
  roomId: number,
  surveyId: number,
): Promise<SurveyDetail> => postJson(`${roomSurveyPath(roomId)}/${surveyId}/publish`, {}, token)

export const closeSurvey = (
  token: string,
  roomId: number,
  surveyId: number,
): Promise<SurveyDetail> => postJson(`${roomSurveyPath(roomId)}/${surveyId}/close`, {}, token)

export const deleteSurvey = (
  token: string,
  roomId: number,
  surveyId: number,
): Promise<void> => deleteJson(`${roomSurveyPath(roomId)}/${surveyId}`, token)

export const submitSurveyResponse = (
  token: string,
  roomId: number,
  surveyId: number,
  data: SubmitSurveyResponseRequest,
): Promise<SurveyDetail> =>
  postJson(`${roomSurveyPath(roomId)}/${surveyId}/responses`, data, token)

export const getSurveyStatistics = (
  token: string,
  roomId: number,
  surveyId: number,
): Promise<SurveyStatistics> =>
  getJson(`${roomSurveyPath(roomId)}/${surveyId}/statistics`, token)

export const exportSurveyStatisticsExcel = (
  token: string,
  roomId: number,
  surveyId: number,
): Promise<void> =>
  downloadFile(`${roomSurveyPath(roomId)}/${surveyId}/statistics/export/excel`, token, 'survey-statistics.xlsx')

export const exportSurveyStatisticsPdf = (
  token: string,
  roomId: number,
  surveyId: number,
): Promise<void> =>
  downloadFile(`${roomSurveyPath(roomId)}/${surveyId}/statistics/export/pdf`, token, 'survey-statistics.pdf')

export const uploadSurveyParticipants = (
  token: string,
  roomId: number,
  surveyId: number,
  file: File,
): Promise<ParticipantUploadResult> => {
  const formData = new FormData()
  formData.append('file', file)
  return postFormData(`${roomSurveyPath(roomId)}/${surveyId}/participants/upload`, formData, token)
}

export const listAdminSurveys = (
  token: string,
  page = 0,
  size = 20,
  filter: AdminSurveyFilter = {},
): Promise<PageResponse<SurveySummary>> => {
  const params = new URLSearchParams({ page: String(page), size: String(size) })
  if (filter.status) params.set('status', filter.status)
  if (filter.chatRoomId) params.set('chatRoomId', String(filter.chatRoomId))
  if (filter.targetMode) params.set('targetMode', filter.targetMode)
  if (filter.title) params.set('title', filter.title)
  if (filter.from) params.set('from', filter.from)
  if (filter.to) params.set('to', filter.to)
  return getJson(`${adminSurveyPath}?${params}`, token)
}

export const adminGetSurveyDetail = (
  token: string,
  surveyId: number,
): Promise<SurveyDetail> => getJson(`${adminSurveyPath}/${surveyId}`, token)

export const adminCreateSurvey = (
  token: string,
  data: CreateSurveyRequest,
): Promise<SurveyDetail> => postJson(adminSurveyPath, data, token)

export type SurveyUserItem = { id: number; username: string; displayName: string | null }

export const adminListSelectableUsers = (
  token: string,
): Promise<SurveyUserItem[]> => getJson(`${adminSurveyPath}/users`, token)

export const adminPublishSurvey = (
  token: string,
  surveyId: number,
): Promise<SurveyDetail> => postJson(`${adminSurveyPath}/${surveyId}/publish`, {}, token)

export const adminCloseSurvey = (
  token: string,
  surveyId: number,
): Promise<SurveyDetail> => postJson(`${adminSurveyPath}/${surveyId}/close`, {}, token)

export const adminAssignRandomParticipants = (
  token: string,
  surveyId: number,
  data: AssignRandomParticipantsRequest,
): Promise<SurveyDetail> =>
  postJson(`${adminSurveyPath}/${surveyId}/assign-random`, data, token)

export const adminUploadParticipants = (
  token: string,
  surveyId: number,
  file: File,
): Promise<ParticipantUploadResult> => {
  const formData = new FormData()
  formData.append('file', file)
  return postFormData(`${adminSurveyPath}/${surveyId}/participants/upload`, formData, token)
}

export const adminGetSurveyStatistics = (
  token: string,
  surveyId: number,
): Promise<SurveyStatistics> => getJson(`${adminSurveyPath}/${surveyId}/statistics`, token)

export const adminExportSurveyStatisticsExcel = (
  token: string,
  surveyId: number,
): Promise<void> =>
  downloadFile(`${adminSurveyPath}/${surveyId}/statistics/export/excel`, token, 'survey-statistics.xlsx')

export const adminExportSurveyStatisticsPdf = (
  token: string,
  surveyId: number,
): Promise<void> =>
  downloadFile(`${adminSurveyPath}/${surveyId}/statistics/export/pdf`, token, 'survey-statistics.pdf')

export const adminRoomStatistics = (
  token: string,
  surveyId?: number,
): Promise<SurveyRoomStatisticsListResponse> => {
  const query = surveyId ? `?surveyId=${surveyId}` : ''
  return getJson(`${adminSurveyPath}/statistics/by-room${query}`, token)
}

export type MySurveyItem = {
  surveyId: number
  title: string
  description: string | null
  status: string
  chatRoomId: number | null
  chatRoomName: string | null
  hasResponded: boolean
}

export const fetchMySurveys = (token: string): Promise<MySurveyItem[]> =>
  getJson('/api/v1/surveys/my', token)

export const getMySurveyDetail = (
  token: string,
  surveyId: number,
): Promise<SurveyDetail> => getJson(`/api/v1/surveys/my/${surveyId}`, token)

export const submitMySurveyResponse = (
  token: string,
  surveyId: number,
  data: SubmitSurveyResponseRequest,
): Promise<SurveyDetail> => postJson(`/api/v1/surveys/my/${surveyId}/responses`, data, token)
