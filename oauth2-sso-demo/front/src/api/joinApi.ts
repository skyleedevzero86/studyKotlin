import { postJson } from './http'
import type { JoinRequest, JoinResponse } from '../types/auth'

export const join = (body: JoinRequest): Promise<JoinResponse> =>
  postJson<JoinResponse>('/api/v1/join', body)
