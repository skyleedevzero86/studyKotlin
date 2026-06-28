import { deleteJson, getJson, postJson } from './http'
import type {
  ChatRoom,
  ChatRoomMember,
  CreateChatRoomRequest,
  CreateDirectChatRequest,
  Message,
  MessageDirection,
  MessagePageResponse,
  PageResponse,
} from '../types/chat'

const chatPath = '/api/v1/chat-rooms'

export const createChatRoom = (
  token: string,
  data: CreateChatRoomRequest,
): Promise<ChatRoom> => postJson(chatPath, data, token)

export const findOrCreateDirectRoom = (
  token: string,
  data: CreateDirectChatRequest,
): Promise<ChatRoom> => postJson(`${chatPath}/direct`, data, token)

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
): Promise<void> => postJson(`${chatPath}/${chatRoomId}/members`, {}, token)

export const leaveChatRoom = (
  token: string,
  chatRoomId: number,
): Promise<void> => deleteJson(`${chatPath}/${chatRoomId}/members/me`, token)

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

export const checkHealth = async (): Promise<void> => {
  const response = await fetch('/actuator/health')
  if (!response.ok) {
    throw new Error('서버에 연결할 수 없습니다')
  }
}
