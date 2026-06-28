<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import {
  createChatRoom,
  getChatRooms,
  getChatRoom,
  joinChatRoom,
} from '../api/chatApi'
import { ApiError } from '../api/http'
import type { ChatRoom, ChatRoomType, CreateChatRoomRequest } from '../types/chat'

const props = defineProps<{
  token: string
  selectedChatRoomId?: number | null
}>()

const emit = defineEmits<{
  select: [room: ChatRoom]
  error: [message: string]
}>()

const chatRooms = ref<ChatRoom[]>([])
const loading = ref(true)
const searchQuery = ref('')
const showCreateModal = ref(false)
const showJoinModal = ref(false)
const createLoading = ref(false)
const joinLoading = ref(false)
const joinRoomId = ref('')
const newRoomData = ref<CreateChatRoomRequest>({
  name: '',
  description: '',
  type: 'GROUP',
  maxMembers: 100,
})

const filteredChatRooms = computed(() =>
  chatRooms.value.filter(
    (room) =>
      room.name.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      room.description?.toLowerCase().includes(searchQuery.value.toLowerCase()),
  ),
)

const resolveError = (error: unknown, fallback: string): string => {
  if (error instanceof ApiError) {
    return error.message
  }
  if (error instanceof Error) {
    return error.message
  }
  return fallback
}

const loadChatRooms = async () => {
  loading.value = true
  try {
    const response = await getChatRooms(props.token)
    chatRooms.value = response.content
  } catch (error) {
    emit('error', resolveError(error, '채팅방 목록을 불러오는데 실패했습니다'))
  } finally {
    loading.value = false
  }
}

const handleCreateRoom = async () => {
  if (!newRoomData.value.name.trim()) {
    emit('error', '채팅방 이름을 입력해주세요')
    return
  }

  createLoading.value = true
  try {
    const newRoom = await createChatRoom(props.token, newRoomData.value)
    chatRooms.value = [newRoom, ...chatRooms.value]
    showCreateModal.value = false
    newRoomData.value = { name: '', description: '', type: 'GROUP', maxMembers: 100 }
    emit('select', newRoom)
  } catch (error) {
    emit('error', resolveError(error, '채팅방 생성에 실패했습니다'))
  } finally {
    createLoading.value = false
  }
}

const handleJoinRoom = async () => {
  if (!joinRoomId.value.trim()) {
    emit('error', '채팅방 ID를 입력해주세요')
    return
  }

  const roomId = Number.parseInt(joinRoomId.value, 10)
  if (Number.isNaN(roomId)) {
    emit('error', '올바른 채팅방 ID를 입력해주세요')
    return
  }

  joinLoading.value = true
  try {
    await joinChatRoom(props.token, roomId)
    await loadChatRooms()
    const joinedRoom = await getChatRoom(props.token, roomId)
    showJoinModal.value = false
    joinRoomId.value = ''
    emit('select', joinedRoom)
  } catch (error) {
    if (error instanceof ApiError && error.status === 409) {
      emit('error', '이미 참여한 채팅방입니다')
    } else {
      emit('error', resolveError(error, '채팅방 참여에 실패했습니다'))
    }
  } finally {
    joinLoading.value = false
  }
}

const formatLastMessageTime = (room: ChatRoom): string => {
  if (!room.lastMessage?.createdAt) {
    return ''
  }

  const date = new Date(room.lastMessage.createdAt)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const minutes = Math.floor(diff / (1000 * 60))
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))

  if (minutes < 1) return '방금'
  if (minutes < 60) return `${minutes}분 전`
  if (hours < 24) return `${hours}시간 전`
  if (days < 7) return `${days}일 전`
  return date.toLocaleDateString('ko-KR', { month: 'short', day: 'numeric' })
}

const roomTypeLabel = (type: ChatRoomType): string => {
  switch (type) {
    case 'DIRECT':
      return '1:1'
    case 'CHANNEL':
      return '채널'
    default:
      return '그룹'
  }
}

onMounted(() => {
  void loadChatRooms()
})
</script>

<template>
  <aside class="chat-sidebar">
    <div class="chat-sidebar-header">
      <h2>채팅방</h2>
      <div class="chat-sidebar-actions">
        <button type="button" @click="showCreateModal = true">새 채팅방</button>
        <button type="button" class="secondary" @click="showJoinModal = true">참여하기</button>
      </div>
      <input
        v-model="searchQuery"
        type="search"
        placeholder="채팅방 검색..."
        class="chat-search"
      />
    </div>

    <div class="chat-room-list">
      <p v-if="loading" class="chat-empty">채팅방을 불러오는 중...</p>
      <p v-else-if="filteredChatRooms.length === 0" class="chat-empty">
        {{ searchQuery ? '검색 결과가 없습니다' : '채팅방이 없습니다' }}
      </p>
      <button
        v-for="room in filteredChatRooms"
        :key="room.id"
        type="button"
        class="chat-room-item"
        :class="{ selected: selectedChatRoomId === room.id }"
        @click="emit('select', room)"
      >
        <div class="chat-room-item-main">
          <strong>{{ room.name }}</strong>
          <span class="chat-room-meta">
            {{ roomTypeLabel(room.type) }} · 멤버 {{ room.memberCount }}
          </span>
          <span class="chat-room-preview">
            {{ room.lastMessage?.content ?? '메시지가 없습니다' }}
          </span>
        </div>
        <span class="chat-room-time">{{ formatLastMessageTime(room) }}</span>
      </button>
    </div>

    <div v-if="showCreateModal" class="modal-overlay" @click="showCreateModal = false">
      <div class="modal-card" @click.stop>
        <h2>새 채팅방 만들기</h2>
        <form class="profile-form" @submit.prevent="handleCreateRoom">
          <label>
            채팅방 이름
            <input v-model="newRoomData.name" type="text" maxlength="100" required />
          </label>
          <label>
            설명
            <input v-model="newRoomData.description" type="text" />
          </label>
          <label>
            타입
            <select v-model="newRoomData.type">
              <option value="GROUP">그룹</option>
              <option value="DIRECT">1:1</option>
              <option value="CHANNEL">채널</option>
            </select>
          </label>
          <div class="modal-actions">
            <button type="button" class="secondary" @click="showCreateModal = false">취소</button>
            <button type="submit" :disabled="createLoading">
              {{ createLoading ? '생성 중...' : '생성' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <div v-if="showJoinModal" class="modal-overlay" @click="showJoinModal = false">
      <div class="modal-card" @click.stop>
        <h2>채팅방 참여하기</h2>
        <form class="profile-form" @submit.prevent="handleJoinRoom">
          <label>
            채팅방 ID
            <input v-model="joinRoomId" type="number" required />
          </label>
          <p class="hint">채팅방 ID는 채팅방 헤더에서 확인할 수 있습니다.</p>
          <div class="modal-actions">
            <button type="button" class="secondary" @click="showJoinModal = false">취소</button>
            <button type="submit" :disabled="joinLoading">
              {{ joinLoading ? '참여 중...' : '참여' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </aside>
</template>
