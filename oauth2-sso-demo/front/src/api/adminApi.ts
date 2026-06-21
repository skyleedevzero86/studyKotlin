import { getJson } from './http'
import type { UserSummaryResponse } from '../types/user'

export const fetchAdminUsers = (token: string): Promise<UserSummaryResponse[]> =>
  getJson<UserSummaryResponse[]>('/api/v1/admin/users', token)
