import { deleteJson, getJson, postJson, putJson } from './http'
import type { ApiMessageResponse } from '../types/user'
import type { UserProfileResponse, UserSummaryResponse } from '../types/user'

export const fetchMyProfile = (token: string): Promise<UserProfileResponse> =>
  getJson<UserProfileResponse>('/api/v1/user/me', token)

export const updateProfile = (
  token: string,
  body: { displayName: string | null },
): Promise<ApiMessageResponse> =>
  putJson<ApiMessageResponse>('/api/v1/user/profile', body, token)

export const changePassword = (
  body: { username: string; currentPassword: string; newPassword: string },
): Promise<ApiMessageResponse> =>
  postJson<ApiMessageResponse>('/api/v1/user/password/change', body)

export const withdraw = (token: string): Promise<ApiMessageResponse> =>
  postJson<ApiMessageResponse>('/api/v1/user/withdraw', {}, token)

export const fetchAdminUsers = (token: string): Promise<UserSummaryResponse[]> =>
  getJson<UserSummaryResponse[]>('/api/v1/admin/users', token)

export const createAdminUser = (
  token: string,
  body: {
    username: string
    password: string
    displayName?: string | null
    role: 'USER' | 'ADMIN'
    activateImmediately: boolean
  },
): Promise<ApiMessageResponse> =>
  postJson<ApiMessageResponse>('/api/v1/admin/users', body, token)

export const approveUser = (
  token: string,
  username: string,
  role: 'USER' | 'ADMIN' = 'USER',
): Promise<ApiMessageResponse> =>
  postJson<ApiMessageResponse>(`/api/v1/admin/users/${username}/approve`, { role }, token)

export const suspendUser = (token: string, username: string): Promise<ApiMessageResponse> =>
  postJson<ApiMessageResponse>(`/api/v1/admin/users/${username}/suspend`, {}, token)

export const activateUser = (token: string, username: string): Promise<ApiMessageResponse> =>
  postJson<ApiMessageResponse>(`/api/v1/admin/users/${username}/activate`, {}, token)

export const unlockUser = (token: string, username: string): Promise<ApiMessageResponse> =>
  postJson<ApiMessageResponse>(`/api/v1/admin/users/${username}/unlock`, {}, token)

export const withdrawUserByAdmin = (token: string, username: string): Promise<ApiMessageResponse> =>
  postJson<ApiMessageResponse>(`/api/v1/admin/users/${username}/withdraw`, {}, token)

export const changeUserRole = (
  token: string,
  username: string,
  role: 'USER' | 'ADMIN',
): Promise<ApiMessageResponse> =>
  putJson<ApiMessageResponse>(`/api/v1/admin/users/${username}/role`, { role }, token)

export const deleteUser = (token: string, username: string): Promise<ApiMessageResponse> =>
  deleteJson<ApiMessageResponse>(`/api/v1/admin/users/${username}`, token)

export const resetPasswordFailCount = (token: string, username: string): Promise<ApiMessageResponse> =>
  postJson<ApiMessageResponse>(`/api/v1/admin/users/${username}/reset-password-fail-count`, {}, token)

export const resetLoginFailCount = (token: string, username: string): Promise<ApiMessageResponse> =>
  postJson<ApiMessageResponse>(`/api/v1/admin/users/${username}/reset-login-fail-count`, {}, token)
