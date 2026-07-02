import { downloadFile, getJson } from './http'
import type {
  MessageTypeYearStatisticsResponse,
  RoomTypeDailyStatisticsResponse,
  StatisticsFilterState,
  StatisticsPeriodResponse,
  StatisticsTab,
  UserEventDailyStatisticsResponse,
} from '../types/statistics'

const statsPath = '/api/v1/admin/statistics'

const buildQuery = (filter: StatisticsFilterState, tab: StatisticsTab): string => {
  const params = new URLSearchParams({
    from: filter.from,
    to: filter.to,
  })
  if (tab === 'hourly') {
    if (filter.roomType) params.set('roomType', filter.roomType)
    if (filter.messageType) params.set('messageType', filter.messageType)
  } else if (tab === 'message-types') {
    if (filter.roomType) params.set('roomType', filter.roomType)
  } else if (tab === 'room-types') {
    if (filter.messageType) params.set('messageType', filter.messageType)
  } else if (tab === 'user-events') {
    if (filter.userEventType) params.set('eventType', filter.userEventType)
  }
  return params.toString()
}

export const getHourlyStatistics = (
  token: string,
  filter: StatisticsFilterState,
): Promise<StatisticsPeriodResponse> =>
  getJson(`${statsPath}/hourly?${buildQuery(filter, 'hourly')}`, token)

export const getMessageTypeStatistics = (
  token: string,
  filter: StatisticsFilterState,
): Promise<MessageTypeYearStatisticsResponse> =>
  getJson(`${statsPath}/message-types?${buildQuery(filter, 'message-types')}`, token)

export const getRoomTypeStatistics = (
  token: string,
  filter: StatisticsFilterState,
): Promise<RoomTypeDailyStatisticsResponse> =>
  getJson(`${statsPath}/room-types?${buildQuery(filter, 'room-types')}`, token)

export const getUserEventStatistics = (
  token: string,
  filter: StatisticsFilterState,
): Promise<UserEventDailyStatisticsResponse> =>
  getJson(`${statsPath}/users/daily?${buildQuery(filter, 'user-events')}`, token)

export const exportStatisticsExcel = (
  token: string,
  filter: StatisticsFilterState,
  tab: StatisticsTab,
): Promise<void> => {
  const query = buildQuery(filter, tab)
  const filename =
    tab === 'hourly'
      ? 'hourly-statistics.xlsx'
      : tab === 'message-types'
        ? 'message-type-statistics.xlsx'
        : tab === 'room-types'
          ? 'room-type-statistics.xlsx'
          : 'user-statistics.xlsx'
  const exportPath =
    tab === 'user-events' ? `${statsPath}/users/daily/export/excel` : `${statsPath}/${tab}/export/excel`
  return downloadFile(`${exportPath}?${query}`, token, filename)
}

export const exportStatisticsPdf = (
  token: string,
  filter: StatisticsFilterState,
  tab: StatisticsTab,
): Promise<void> => {
  const query = buildQuery(filter, tab)
  const filename =
    tab === 'hourly'
      ? 'hourly-statistics.pdf'
      : tab === 'message-types'
        ? 'message-type-statistics.pdf'
        : tab === 'room-types'
          ? 'room-type-statistics.pdf'
          : 'user-statistics.pdf'
  const exportPath =
    tab === 'user-events' ? `${statsPath}/users/daily/export/pdf` : `${statsPath}/${tab}/export/pdf`
  return downloadFile(`${exportPath}?${query}`, token, filename)
}
