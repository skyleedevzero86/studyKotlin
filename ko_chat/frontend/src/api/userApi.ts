import { deleteJson, getJson, postJson, putJson } from './http'
import type { ApiMessageResponse } from '../types/user'
import type {
  UserBlockHistoryResponse,
  UserFriendRequestResponse,
  UserProfileResponse,
  UserRelationshipResponse,
  UserSummaryResponse,
} from '../types/user'
import type { ChatUser } from '../types/chat'
import type { PageResponse } from '../types/chat'

export const fetchMyProfile = (token: string): Promise<UserProfileResponse> =>
  getJson<UserProfileResponse>('/api/v1/user/me', token)

export const searchUsers = (
  token: string,
  query = '',
  page = 0,
  size = 20,
): Promise<PageResponse<ChatUser>> =>
  getJson(
    `/api/v1/users/search?q=${encodeURIComponent(query)}&page=${page}&size=${size}`,
    token,
  )

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

export const fetchFriends = (token: string): Promise<UserRelationshipResponse[]> =>
  getJson<UserRelationshipResponse[]>('/api/v1/users/friends', token)

export const addFriend = (
  token: string,
  targetUserId: number,
): Promise<UserFriendRequestResponse> =>
  postJson<UserFriendRequestResponse>(`/api/v1/users/friends/${targetUserId}`, {}, token)

export const fetchIncomingFriendRequests = (token: string): Promise<UserFriendRequestResponse[]> =>
  getJson<UserFriendRequestResponse[]>('/api/v1/users/friend-requests/incoming', token)

export const fetchRejectedFriendRequests = (token: string): Promise<UserFriendRequestResponse[]> =>
  getJson<UserFriendRequestResponse[]>('/api/v1/users/friend-requests/rejected', token)

export const acceptFriendRequest = (
  token: string,
  requestId: number,
): Promise<UserFriendRequestResponse> =>
  postJson<UserFriendRequestResponse>(`/api/v1/users/friend-requests/${requestId}/accept`, {}, token)

export const rejectFriendRequest = (
  token: string,
  requestId: number,
): Promise<UserFriendRequestResponse> =>
  postJson<UserFriendRequestResponse>(`/api/v1/users/friend-requests/${requestId}/reject`, {}, token)

export const removeFriend = (token: string, targetUserId: number): Promise<void> =>
  deleteJson<void>(`/api/v1/users/friends/${targetUserId}`, token)

export const fetchBlocks = (
  token: string,
  page = 0,
  size = 10,
): Promise<PageResponse<UserRelationshipResponse>> =>
  getJson(`/api/v1/users/blocks?page=${page}&size=${size}`, token)

export const fetchBlockHistory = (
  token: string,
  page = 0,
  size = 10,
): Promise<PageResponse<UserBlockHistoryResponse>> =>
  getJson(`/api/v1/users/blocks/history?page=${page}&size=${size}`, token)

export const blockUser = (
  token: string,
  targetUserId: number,
): Promise<UserRelationshipResponse> =>
  postJson<UserRelationshipResponse>(`/api/v1/users/blocks/${targetUserId}`, {}, token)

export const unblockUser = (token: string, targetUserId: number): Promise<void> =>
  deleteJson<void>(`/api/v1/users/blocks/${targetUserId}`, token)

export const fetchAdminUsers = (
  token: string,
  page = 0,
  size = 20,
): Promise<PageResponse<UserSummaryResponse>> =>
  getJson(`/api/v1/admin/users?page=${page}&size=${size}&sort=username,asc`, token)

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

export const restoreUser = (token: string, username: string): Promise<ApiMessageResponse> =>
  postJson<ApiMessageResponse>(`/api/v1/admin/users/${username}/restore`, {}, token)

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

export const updateUserProfileByAdmin = (
  token: string,
  username: string,
  body: { displayName: string | null },
): Promise<ApiMessageResponse> =>
  putJson<ApiMessageResponse>(`/api/v1/admin/users/${username}/profile`, body, token)
