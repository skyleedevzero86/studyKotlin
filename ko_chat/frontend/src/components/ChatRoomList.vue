<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import {
  createChatRoom,
  findOrCreateDirectRoom,
  getChatRooms,
  getChatRoom,
  joinChatRoom,
} from '../api/chatApi'
import { searchUsers } from '../api/userApi'
import { ApiError } from '../api/http'
import type { ChatRoom, ChatRoomType, ChatUser, CreateChatRoomRequest } from '../types/chat'

const props = defineProps<{
  token: string
  currentUserId: number
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
const showDirectModal = ref(false)
const createLoading = ref(false)
const joinLoading = ref(false)
const directLoading = ref(false)
const userSearchQuery = ref('')
const userSearchLoading = ref(false)
const userSearchResults = ref<ChatUser[]>([])
const joinRoomId = ref('')
const newRoomData = ref<CreateChatRoomRequest>({
  name: '',
  description: '',
  type: 'GROUP',
  maxMembers: 100,
})

const filteredChatRooms = computed(() => {
  const query = searchQuery.value.toLowerCase()
  return chatRooms.value.filter((room) => {
    const displayName = roomDisplayName(room).toLowerCase()
    return (
      displayName.includes(query) ||
      room.description?.toLowerCase().includes(query) ||
      room.lastMessage?.content?.toLowerCase().includes(query)
    )
  })
})

const directRooms = computed(() =>
  filteredChatRooms.value.filter((room) => room.type === 'DIRECT'),
)

const groupRooms = computed(() =>
  filteredChatRooms.value.filter((room) => room.type !== 'DIRECT'),
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

const roomDisplayName = (room: ChatRoom): string => {
  if (room.type === 'DIRECT' && room.peerUser) {
    return room.peerUser.displayName ?? room.peerUser.username
  }
  return room.name
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

const loadUserSearch = async () => {
  userSearchLoading.value = true
  try {
    userSearchResults.value = await searchUsers(props.token, userSearchQuery.value)
  } catch (error) {
    emit('error', resolveError(error, '사용자 검색에 실패했습니다'))
  } finally {
    userSearchLoading.value = false
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

const handleStartDirectChat = async (user: ChatUser) => {
  directLoading.value = true
  try {
    const room = await findOrCreateDirectRoom(props.token, { targetUserId: user.id })
    const existingIndex = chatRooms.value.findIndex((item) => item.id === room.id)
    if (existingIndex >= 0) {
      chatRooms.value[existingIndex] = room
      chatRooms.value = [...chatRooms.value]
    } else {
      chatRooms.value = [room, ...chatRooms.value]
    }
    showDirectModal.value = false
    userSearchQuery.value = ''
    emit('select', room)
  } catch (error) {
    emit('error', resolveError(error, '1:1 채팅을 시작할 수 없습니다'))
  } finally {
    directLoading.value = false
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

watch(showDirectModal, (open) => {
  if (open) {
    void loadUserSearch()
  }
})

let searchDebounce: ReturnType<typeof setTimeout> | undefined
watch(userSearchQuery, () => {
  if (!showDirectModal.value) {
    return
  }
  clearTimeout(searchDebounce)
  searchDebounce = setTimeout(() => {
    void loadUserSearch()
  }, 300)
})

onMounted(() => {
  void loadChatRooms()
})
</script>

<template>
  <aside class="chat-sidebar">
    <div class="chat-sidebar-header">
      <h2>채팅</h2>
      <div class="chat-sidebar-actions">
        <button type="button" @click="showDirectModal = true">1:1 채팅</button>
        <button type="button" class="secondary" @click="showCreateModal = true">그룹방</button>
        <button type="button" class="secondary" @click="showJoinModal = true">참여</button>
      </div>
      <input
        v-model="searchQuery"
        type="search"
        placeholder="대화 검색..."
        class="chat-search"
      />
    </div>

    <div class="chat-room-list">
      <p v-if="loading" class="chat-empty">채팅방을 불러오는 중...</p>
      <template v-else>
        <p v-if="filteredChatRooms.length === 0" class="chat-empty">
          {{ searchQuery ? '검색 결과가 없습니다' : '채팅방이 없습니다' }}
        </p>

        <section v-if="directRooms.length > 0" class="chat-room-section">
          <h3 class="chat-room-section-title">1:1 대화</h3>
          <button
            v-for="room in directRooms"
            :key="room.id"
            type="button"
            class="chat-room-item"
            :class="{ selected: selectedChatRoomId === room.id }"
            @click="emit('select', room)"
          >
            <div class="chat-room-item-main">
              <strong>{{ roomDisplayName(room) }}</strong>
              <span class="chat-room-meta">{{ roomTypeLabel(room.type) }}</span>
              <span class="chat-room-preview">
                {{ room.lastMessage?.content ?? '메시지가 없습니다' }}
              </span>
            </div>
            <span class="chat-room-time">{{ formatLastMessageTime(room) }}</span>
          </button>
        </section>

        <section v-if="groupRooms.length > 0" class="chat-room-section">
          <h3 class="chat-room-section-title">그룹 · 채널</h3>
          <button
            v-for="room in groupRooms"
            :key="room.id"
            type="button"
            class="chat-room-item"
            :class="{ selected: selectedChatRoomId === room.id }"
            @click="emit('select', room)"
          >
            <div class="chat-room-item-main">
              <strong>{{ roomDisplayName(room) }}</strong>
              <span class="chat-room-meta">
                {{ roomTypeLabel(room.type) }} · 멤버 {{ room.memberCount }}
              </span>
              <span class="chat-room-preview">
                {{ room.lastMessage?.content ?? '메시지가 없습니다' }}
              </span>
            </div>
            <span class="chat-room-time">{{ formatLastMessageTime(room) }}</span>
          </button>
        </section>
      </template>
    </div>

    <div v-if="showDirectModal" class="modal-overlay" @click="showDirectModal = false">
      <div class="modal-card" @click.stop>
        <h2>1:1 채팅 시작</h2>
        <p class="hint">대화할 사용자를 검색하고 선택하세요.</p>
        <input
          v-model="userSearchQuery"
          type="search"
          placeholder="이름 또는 아이디 검색..."
          class="chat-search"
        />
        <div class="user-search-list">
          <p v-if="userSearchLoading" class="chat-empty">검색 중...</p>
          <p v-else-if="userSearchResults.length === 0" class="chat-empty">
            {{ userSearchQuery ? '검색 결과가 없습니다' : '사용자가 없습니다' }}
          </p>
          <button
            v-for="user in userSearchResults"
            :key="user.id"
            type="button"
            class="user-search-item"
            :disabled="directLoading"
            @click="handleStartDirectChat(user)"
          >
            <strong>{{ user.displayName ?? user.username }}</strong>
            <span>@{{ user.username }}</span>
          </button>
        </div>
        <div class="modal-actions">
          <button type="button" class="secondary" @click="showDirectModal = false">닫기</button>
        </div>
      </div>
    </div>

    <div v-if="showCreateModal" class="modal-overlay" @click="showCreateModal = false">
      <div class="modal-card" @click.stop>
        <h2>그룹 채팅방 만들기</h2>
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
