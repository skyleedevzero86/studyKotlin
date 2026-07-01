import { checkBackendAvailability, deleteJson, getJson, postFormData, postJson, putJson } from './http'
import type {
  ChatRoom,
  ChatRoomInvitation,
  ChatRoomMember,
  CreateChatRoomRequest,
  CreateDirectChatRequest,
  Message,
  MessageDirection,
  MessageMetadata,
  MessagePageResponse,
  PageResponse,
  UpdateChatRoomSettingsRequest,
  AttachmentUploadResponse,
} from '../types/chat'

const chatPath = '/api/v1/chat-rooms'
const adminChatPath = '/api/v1/admin/chat-rooms'

export const createChatRoom = (
  token: string,
  data: CreateChatRoomRequest,
): Promise<ChatRoom> => postJson(chatPath, data, token)

export const findOrCreateDirectRoom = (
  token: string,
  data: CreateDirectChatRequest,
): Promise<ChatRoom> => postJson(`${chatPath}/direct`, data, token)

export const getPendingChatInvitations = (
  token: string,
): Promise<ChatRoomInvitation[]> => getJson(`${chatPath}/invitations/pending`, token)

export const inviteToChatRoom = (
  token: string,
  chatRoomId: number,
  targetUserId: number,
): Promise<ChatRoomInvitation> =>
  postJson(`${chatPath}/${chatRoomId}/invitations/${targetUserId}`, {}, token)

export const acceptChatInvitation = (
  token: string,
  invitationId: number,
): Promise<ChatRoom> => postJson(`${chatPath}/invitations/${invitationId}/accept`, {}, token)

export const rejectChatInvitation = (
  token: string,
  invitationId: number,
): Promise<ChatRoomInvitation> => postJson(`${chatPath}/invitations/${invitationId}/reject`, {}, token)

export const kickChatRoomMember = (
  token: string,
  chatRoomId: number,
  targetUserId: number,
): Promise<void> => postJson(`${chatPath}/${chatRoomId}/members/${targetUserId}/kick`, {}, token)

export const updateChatRoomSettings = (
  token: string,
  chatRoomId: number,
  data: UpdateChatRoomSettingsRequest,
): Promise<ChatRoom> => putJson(`${chatPath}/${chatRoomId}/settings`, data, token)

export const updateChatRoomCapacity = (
  token: string,
  chatRoomId: number,
  maxMembers: number,
): Promise<ChatRoom> => putJson(`${chatPath}/${chatRoomId}/capacity`, { maxMembers }, token)

export const getChatRooms = (
  token: string,
  page = 0,
  size = 20,
): Promise<PageResponse<ChatRoom>> =>
  getJson(`${chatPath}?page=${page}&size=${size}&sort=createdAt,desc`, token)

export const getChatRoom = (token: string, chatRoomId: number): Promise<ChatRoom> =>
  getJson(`${chatPath}/${chatRoomId}`, token)

export const getChatRoomMembers = (
  token: string,
  chatRoomId: number,
): Promise<ChatRoomMember[]> => getJson(`${chatPath}/${chatRoomId}/members`, token)

export const joinChatRoom = (
  token: string,
  chatRoomId: number,
  password?: string,
): Promise<void> => postJson(`${chatPath}/${chatRoomId}/members`, { password: password ?? null }, token)

export const leaveChatRoom = (
  token: string,
  chatRoomId: number,
): Promise<void> => deleteJson(`${chatPath}/${chatRoomId}/members/me`, token)

export const markChatRoomRead = (
  token: string,
  chatRoomId: number,
): Promise<ChatRoom> => postJson(`${chatPath}/${chatRoomId}/read`, {}, token)

export const discoverChatRooms = (
  token: string,
  query = '',
  page = 0,
  size = 10,
  roomType: 'GROUP' | 'DIRECT' = 'GROUP',
  includePrivate = false,
): Promise<PageResponse<ChatRoom>> => {
  const params = new URLSearchParams({
    q: query,
    page: page.toString(),
    size: size.toString(),
    sort: 'updatedAt,desc',
    roomType,
    includePrivate: includePrivate.toString(),
  })
  return getJson(`${chatPath}/discover?${params}`, token)
}

export const getRecommendedChatRooms = (
  token: string,
  page = 0,
  size = 10,
): Promise<PageResponse<ChatRoom>> =>
  getJson(
    `${chatPath}/discover/recommended?page=${page}&size=${size}&sort=updatedAt,desc`,
    token,
  )

export const searchChatRooms = (
  token: string,
  query: string,
): Promise<ChatRoom[]> =>
  getJson(`${chatPath}/search?q=${encodeURIComponent(query)}`, token)

export const getMessages = (
  token: string,
  chatRoomId: number,
  page = 0,
  size = 50,
): Promise<PageResponse<Message>> =>
  getJson(
    `${chatPath}/${chatRoomId}/messages?page=${page}&size=${size}&sort=sequenceNumber,asc`,
    token,
  )

export const getMessagesByCursor = (
  token: string,
  chatRoomId: number,
  cursor?: number,
  limit = 50,
  direction: MessageDirection = 'BEFORE',
): Promise<MessagePageResponse> => {
  const params = new URLSearchParams({
    limit: limit.toString(),
    direction,
  })
  if (cursor !== undefined) {
    params.append('cursor', cursor.toString())
  }
  return getJson(`${chatPath}/${chatRoomId}/messages/cursor?${params}`, token)
}

export const checkHealth = (): Promise<void> => checkBackendAvailability()

export const uploadChatAttachment = (
  token: string,
  roomId: number,
  file: File,
): Promise<AttachmentUploadResponse> => {
  const formData = new FormData()
  formData.append('file', file)
  return postFormData(`${chatPath}/${roomId}/attachments`, formData, token)
}

export const fetchLinkPreview = (token: string, url: string): Promise<MessageMetadata> =>
  postJson(`${chatPath}/link-preview`, { url }, token)

export const getAdminChatRooms = (
  token: string,
  page = 0,
  size = 30,
): Promise<PageResponse<ChatRoom>> =>
  getJson(`${adminChatPath}?page=${page}&size=${size}&sort=updatedAt,desc`, token)

export const getAdminChatRoomMembers = (
  token: string,
  chatRoomId: number,
): Promise<ChatRoomMember[]> => getJson(`${adminChatPath}/${chatRoomId}/members`, token)

export const adminKickChatRoomMember = (
  token: string,
  chatRoomId: number,
  targetUserId: number,
): Promise<void> =>
  postJson(`${adminChatPath}/${chatRoomId}/members/${targetUserId}/kick`, {}, token)
