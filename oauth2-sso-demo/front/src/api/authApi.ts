import type { LoginRequest, LoginResponse } from '../types/auth'
import { postJson } from './http'

export const login = (request: LoginRequest): Promise<LoginResponse> =>
  postJson<LoginResponse>('/api/v1/login', request)
