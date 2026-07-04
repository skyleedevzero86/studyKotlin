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

type ChatRoomLike = ChatRoom & { active?: boolean }

export const normalizeChatRoom = (room: ChatRoomLike): ChatRoom => ({
  ...room,
  isActive: room.isActive ?? room.active ?? true,
})

const normalizeChatRoomPage = (
  page: PageResponse<ChatRoomLike>,
): PageResponse<ChatRoom> => ({
  ...page,
  content: page.content.map(normalizeChatRoom),
})

const normalizeInvitation = (invitation: ChatRoomInvitation & { chatRoom?: ChatRoomLike }): ChatRoomInvitation => ({
  ...invitation,
  chatRoom: invitation.chatRoom ? normalizeChatRoom(invitation.chatRoom) : invitation.chatRoom,
})

export const createChatRoom = async (
  token: string,
  data: CreateChatRoomRequest,
): Promise<ChatRoom> => normalizeChatRoom(await postJson<ChatRoomLike>(chatPath, data, token))

export const findOrCreateDirectRoom = async (
  token: string,
  data: CreateDirectChatRequest,
): Promise<ChatRoom> => normalizeChatRoom(await postJson<ChatRoomLike>(`${chatPath}/direct`, data, token))

export const getPendingChatInvitations = async (
  token: string,
  page = 0,
  size = 10,
): Promise<PageResponse<ChatRoomInvitation>> => {
  const pageResult = await getJson<PageResponse<ChatRoomInvitation & { chatRoom?: ChatRoomLike }>>(
    `${chatPath}/invitations/pending?page=${page}&size=${size}`,
    token,
  )
  return {
    ...pageResult,
    content: pageResult.content.map(normalizeInvitation),
  }
}

export const inviteToChatRoom = (
  token: string,
  chatRoomId: number,
  targetUserId: number,
): Promise<ChatRoomInvitation> =>
  postJson(`${chatPath}/${chatRoomId}/invitations/${targetUserId}`, {}, token)

export const acceptChatInvitation = async (
  token: string,
  invitationId: number,
): Promise<ChatRoom> =>
  normalizeChatRoom(await postJson<ChatRoomLike>(`${chatPath}/invitations/${invitationId}/accept`, {}, token))

export const rejectChatInvitation = async (
  token: string,
  invitationId: number,
): Promise<ChatRoomInvitation> =>
  normalizeInvitation(await postJson(`${chatPath}/invitations/${invitationId}/reject`, {}, token))

export const kickChatRoomMember = (
  token: string,
  chatRoomId: number,
  targetUserId: number,
): Promise<void> => postJson(`${chatPath}/${chatRoomId}/members/${targetUserId}/kick`, {}, token)

export const updateChatRoomSettings = async (
  token: string,
  chatRoomId: number,
  data: UpdateChatRoomSettingsRequest,
): Promise<ChatRoom> =>
  normalizeChatRoom(await putJson<ChatRoomLike>(`${chatPath}/${chatRoomId}/settings`, data, token))

export const updateChatRoomCapacity = async (
  token: string,
  chatRoomId: number,
  maxMembers: number,
): Promise<ChatRoom> =>
  normalizeChatRoom(await putJson<ChatRoomLike>(`${chatPath}/${chatRoomId}/capacity`, { maxMembers }, token))

export const getChatRooms = async (
  token: string,
  page = 0,
  size = 20,
): Promise<PageResponse<ChatRoom>> =>
  normalizeChatRoomPage(
    await getJson<PageResponse<ChatRoomLike>>(
      `${chatPath}?page=${page}&size=${size}&sort=createdAt,desc`,
      token,
    ),
  )

export const getChatRoom = async (token: string, chatRoomId: number): Promise<ChatRoom> =>
  normalizeChatRoom(await getJson<ChatRoomLike>(`${chatPath}/${chatRoomId}`, token))

export const getChatRoomMembers = (
  token: string,
  chatRoomId: number,
  page = 0,
  size = 20,
): Promise<PageResponse<ChatRoomMember>> =>
  getJson(`${chatPath}/${chatRoomId}/members?page=${page}&size=${size}`, token)

export const joinChatRoom = (
  token: string,
  chatRoomId: number,
  password?: string,
): Promise<void> => postJson(`${chatPath}/${chatRoomId}/members`, { password: password ?? null }, token)

export const leaveChatRoom = (
  token: string,
  chatRoomId: number,
): Promise<void> => deleteJson(`${chatPath}/${chatRoomId}/members/me`, token)

export const markChatRoomRead = async (
  token: string,
  chatRoomId: number,
): Promise<ChatRoom> =>
  normalizeChatRoom(await postJson<ChatRoomLike>(`${chatPath}/${chatRoomId}/read`, {}, token))

export const discoverChatRooms = async (
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
  return normalizeChatRoomPage(
    await getJson<PageResponse<ChatRoomLike>>(`${chatPath}/discover?${params}`, token),
  )
}

export const getRecommendedChatRooms = async (
  token: string,
  page = 0,
  size = 10,
): Promise<PageResponse<ChatRoom>> =>
  normalizeChatRoomPage(
    await getJson<PageResponse<ChatRoomLike>>(
      `${chatPath}/discover/recommended?page=${page}&size=${size}&sort=updatedAt,desc`,
      token,
    ),
  )

export const searchChatRooms = async (
  token: string,
  query: string,
  page = 0,
  size = 20,
): Promise<PageResponse<ChatRoom>> =>
  normalizeChatRoomPage(
    await getJson<PageResponse<ChatRoomLike>>(
      `${chatPath}/search?q=${encodeURIComponent(query)}&page=${page}&size=${size}`,
      token,
    ),
  )

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

export const getAdminChatRooms = async (
  token: string,
  page = 0,
  size = 30,
): Promise<PageResponse<ChatRoom>> =>
  normalizeChatRoomPage(
    await getJson<PageResponse<ChatRoomLike>>(
      `${adminChatPath}?page=${page}&size=${size}&sort=updatedAt,desc`,
      token,
    ),
  )

export const getAdminChatRoomMembers = (
  token: string,
  chatRoomId: number,
  page = 0,
  size = 20,
): Promise<PageResponse<ChatRoomMember>> =>
  getJson(`${adminChatPath}/${chatRoomId}/members?page=${page}&size=${size}`, token)

export const adminKickChatRoomMember = (
  token: string,
  chatRoomId: number,
  targetUserId: number,
): Promise<void> =>
  postJson(`${adminChatPath}/${chatRoomId}/members/${targetUserId}/kick`, {}, token)
