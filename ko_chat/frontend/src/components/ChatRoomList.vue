<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { createChatRoom, findOrCreateDirectRoom, getChatRooms, searchChatRooms } from '../api/chatApi'
import { ApiError } from '../api/http'
import OpenChatSearchPanel from './OpenChatSearchPanel.vue'
import PaginationBar from './PaginationBar.vue'
import { usePagination } from '../composables/usePagination'
import type { ChatRoom, ChatUser, CreateChatRoomRequest, ChatMediaMode } from '../types/chat'

const props = defineProps<{
  token: string
  currentUserId: number
  selectedChatRoomId?: number | null
  refreshKey?: number
}>()

const emit = defineEmits<{
  select: [room: ChatRoom]
  error: [message: string]
  notice: [message: string]
  unreadCount: [count: number]
}>()

type RoomFilter = 'all' | 'unread'

const ROOM_PAGE_SIZE = 20
const pagination = usePagination(ROOM_PAGE_SIZE)
const searchPagination = usePagination(ROOM_PAGE_SIZE)

const chatRooms = ref<ChatRoom[]>([])
const loading = ref(true)
const roomFilter = ref<RoomFilter>('all')
const showCreateModal = ref(false)
const showOpenChatSearch = ref(false)
const showRoomSearch = ref(false)
const roomSearchQuery = ref('')
const roomSearchLoading = ref(false)
const roomSearchResults = ref<ChatRoom[]>([])
const createLoading = ref(false)
const newRoomData = ref<CreateChatRoomRequest>({
  name: '',
  description: '',
  type: 'GROUP',
  maxMembers: 100,
  isPrivate: false,
  password: '',
  mediaMode: 'TEXT',
})

const totalUnreadCount = computed(() =>
  chatRooms.value.reduce((sum, room) => sum + (room.unreadCount ?? 0), 0),
)

const displayedRooms = computed(() => {
  const source = showRoomSearch.value && roomSearchQuery.value.trim()
    ? roomSearchResults.value
    : chatRooms.value
  if (roomFilter.value === 'unread') {
    return source.filter((room) => room.unreadCount > 0)
  }
  return source
})

const resolveError = (error: unknown, fallback: string): string => {
  if (error instanceof ApiError) return error.message
  if (error instanceof Error) return error.message
  return fallback
}

const roomDisplayName = (room: ChatRoom): string => {
  if (room.type === 'DIRECT' && room.peerUser) {
    return room.peerUser.displayName ?? room.peerUser.username
  }
  return room.name
}

const avatarLabel = (room: ChatRoom) => roomDisplayName(room).slice(0, 1).toUpperCase()

const avatarColor = (room: ChatRoom) => {
  const palette = ['#5b8def', '#f59e0b', '#10b981', '#ef4444', '#8b5cf6', '#ec4899']
  return palette[room.id % palette.length]
}

const loadChatRooms = async () => {
  loading.value = true
  try {
    const response = await getChatRooms(
      props.token,
      pagination.page.value,
      pagination.size.value,
    )
    chatRooms.value = response.content
    pagination.applyPageResponse(response)
  } catch (error) {
    emit('error', resolveError(error, '채팅방 목록을 불러오지 못했습니다'))
  } finally {
    loading.value = false
  }
}

const loadRoomSearch = async () => {
  const query = roomSearchQuery.value.trim()
  if (!query) {
    roomSearchResults.value = []
    searchPagination.totalElements.value = 0
    searchPagination.totalPages.value = 0
    return
  }
  roomSearchLoading.value = true
  try {
    const response = await searchChatRooms(
      props.token,
      query,
      searchPagination.page.value,
      searchPagination.size.value,
    )
    roomSearchResults.value = response.content
    searchPagination.applyPageResponse(response)
  } catch (error) {
    emit('error', resolveError(error, '채팅방 검색에 실패했습니다'))
  } finally {
    roomSearchLoading.value = false
  }
}

const handleCreateRoom = async () => {
  if (!newRoomData.value.name.trim()) {
    emit('error', '채팅방 이름을 입력해주세요')
    return
  }

  if (newRoomData.value.mediaMode === 'WEBRTC') {
    const members = newRoomData.value.maxMembers ?? 4
    if (members < 2 || members > 6) {
      emit('error', 'WebRTC 방은 2~6명까지 설정할 수 있습니다')
      return
    }
  }

  if (newRoomData.value.isPrivate) {
    const password = newRoomData.value.password?.trim() ?? ''
    if (password.length < 4) {
      emit('error', '비공개 방 비밀번호는 4자 이상이어야 합니다')
      return
    }
  }

  createLoading.value = true
  try {
    const payload: CreateChatRoomRequest = {
      name: newRoomData.value.name.trim(),
      description: newRoomData.value.description?.trim() || null,
      type: newRoomData.value.type,
      maxMembers: newRoomData.value.maxMembers,
      isPrivate: newRoomData.value.isPrivate,
      password: newRoomData.value.isPrivate ? newRoomData.value.password?.trim() : null,
      mediaMode: newRoomData.value.mediaMode ?? 'TEXT',
    }
    const newRoom = await createChatRoom(props.token, payload)
    pagination.resetPage()
    await loadChatRooms()
    showCreateModal.value = false
    newRoomData.value = {
      name: '',
      description: '',
      type: 'GROUP',
      maxMembers: 100,
      isPrivate: false,
      password: '',
      mediaMode: 'TEXT',
    }
    emit('select', newRoom)
  } catch (error) {
    emit('error', resolveError(error, '채팅방 생성에 실패했습니다'))
  } finally {
    createLoading.value = false
  }
}

const upsertRoom = (room: ChatRoom) => {
  const existingIndex = chatRooms.value.findIndex((item) => item.id === room.id)
  if (existingIndex >= 0) {
    chatRooms.value[existingIndex] = room
    chatRooms.value = [...chatRooms.value]
  } else {
    chatRooms.value = [room, ...chatRooms.value]
  }
}

const handleOpenChatSelect = (room: ChatRoom) => {
  upsertRoom(room)
  emit('select', room)
}

const handleStartDirect = async (user: ChatUser) => {
  try {
    const room = await findOrCreateDirectRoom(props.token, { targetUserId: user.id })
    upsertRoom(room)
    emit('select', room)
  } catch (error) {
    emit('error', resolveError(error, '1:1 채팅을 시작하지 못했습니다'))
  }
}

const formatLastMessageTime = (room: ChatRoom): string => {
  if (!room.lastMessage?.createdAt) return ''
  const date = new Date(room.lastMessage.createdAt)
  const now = new Date()
  if (date.toDateString() === now.toDateString()) {
    return date.toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit', hour12: true })
  }
  const diff = now.getTime() - date.getTime()
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  if (days < 7) {
    return date.toLocaleDateString('ko-KR', { weekday: 'short' })
  }
  return date.toLocaleDateString('ko-KR', { month: 'numeric', day: 'numeric' })
}

const formatUnread = (count: number): string => {
  if (count > 300) return '300+'
  if (count > 99) return '99+'
  return String(count)
}

const lastMessagePreview = (room: ChatRoom): string => {
  const content = room.lastMessage?.content
  if (!content) return '메시지가 없습니다'
  return content.length > 40 ? `${content.slice(0, 40)}...` : content
}

const onMediaModeChange = (mode: ChatMediaMode) => {
  newRoomData.value.mediaMode = mode
  if (mode === 'WEBRTC') {
    newRoomData.value.maxMembers = 4
  } else {
    newRoomData.value.maxMembers = 100
  }
}

const openRoomSearch = () => {
  showRoomSearch.value = true
  roomSearchQuery.value = ''
  roomSearchResults.value = []
  searchPagination.resetPage()
}

const closeRoomSearch = () => {
  showRoomSearch.value = false
  roomSearchQuery.value = ''
  roomSearchResults.value = []
  searchPagination.resetPage()
}

watch(totalUnreadCount, (count) => {
  emit('unreadCount', count)
}, { immediate: true })

watch(
  () => props.refreshKey,
  () => {
    void loadChatRooms()
  },
)

watch(() => pagination.page.value, () => {
  if (!showRoomSearch.value) {
    void loadChatRooms()
  }
})

const goRoomSearchPrev = () => {
  searchPagination.goPrev()
  void loadRoomSearch()
}

const goRoomSearchNext = () => {
  searchPagination.goNext()
  void loadRoomSearch()
}

let roomSearchDebounce: ReturnType<typeof setTimeout> | undefined
watch(roomSearchQuery, () => {
  if (!showRoomSearch.value) return
  searchPagination.resetPage()
  clearTimeout(roomSearchDebounce)
  roomSearchDebounce = setTimeout(() => {
    void loadRoomSearch()
  }, 300)
})

onMounted(() => {
  void loadChatRooms()
})

defineExpose({ loadChatRooms, totalUnreadCount })
</script>

<template>
  <div class="sleekydz86-chat-list-panel">
    <header v-if="!showRoomSearch" class="sleekydz86-panel-header">
      <h2>채팅</h2>
      <div class="sleekydz86-panel-header-actions">
        <button type="button" class="sleekydz86-panel-icon-btn" title="대화 검색" @click="openRoomSearch">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="7" />
            <line x1="16.5" y1="16.5" x2="21" y2="21" />
          </svg>
        </button>
        <button type="button" class="sleekydz86-panel-icon-btn" title="오픈채팅 검색" @click="showOpenChatSearch = true">
          <span class="sleekydz86-header-icon">◉◉</span>
        </button>
        <button type="button" class="sleekydz86-panel-icon-btn sleekydz86-create-btn" title="채팅방 만들기" @click="showCreateModal = true">
          <span class="sleekydz86-header-icon">💬</span>
          <span class="sleekydz86-header-plus">+</span>
        </button>
      </div>
    </header>

    <header v-else class="sleekydz86-room-search-header">
      <button type="button" class="open-chat-back" @click="closeRoomSearch">←</button>
      <input
        v-model="roomSearchQuery"
        type="search"
        class="sleekydz86-room-search-input"
        placeholder="채팅방 이름, 메시지 검색"
        autofocus
      />
      <button v-if="roomSearchQuery" type="button" class="open-chat-clear" @click="roomSearchQuery = ''">✕</button>
    </header>

    <div v-if="!showRoomSearch" class="sleekydz86-chat-filters">
      <button type="button" class="sleekydz86-chat-filter" :class="{ active: roomFilter === 'all' }" @click="roomFilter = 'all'">
        전체
      </button>
      <button type="button" class="sleekydz86-chat-filter" :class="{ active: roomFilter === 'unread' }" @click="roomFilter = 'unread'">
        안읽음
        <span v-if="totalUnreadCount > 0" class="sleekydz86-filter-badge">
          {{ totalUnreadCount > 99 ? '99+' : totalUnreadCount }}
        </span>
      </button>
    </div>

    <div class="sleekydz86-chat-room-list">
      <p v-if="loading || roomSearchLoading" class="chat-empty slim">불러오는 중...</p>
      <p v-else-if="displayedRooms.length === 0" class="chat-empty slim">
        {{ showRoomSearch && roomSearchQuery ? '검색 결과가 없습니다' : roomFilter === 'unread' ? '안읽은 대화가 없습니다' : '참여한 채팅방이 없습니다' }}
      </p>

      <button
        v-for="room in displayedRooms"
        :key="room.id"
        type="button"
        class="sleekydz86-chat-room-item"
        :class="{ selected: selectedChatRoomId === room.id }"
        @click="emit('select', room)"
      >
        <span class="sleekydz86-chat-room-avatar" :style="{ backgroundColor: avatarColor(room) }">
          {{ avatarLabel(room) }}
        </span>
        <div class="sleekydz86-chat-room-body">
          <div class="sleekydz86-chat-room-top">
            <span v-if="!room.isPrivate && room.type !== 'DIRECT'" class="sleekydz86-room-globe">🌐</span>
            <strong class="sleekydz86-chat-room-name">{{ roomDisplayName(room) }}</strong>
            <span v-if="room.type !== 'DIRECT'" class="sleekydz86-chat-room-count">{{ room.memberCount }}</span>
          </div>
          <p class="sleekydz86-chat-room-preview">{{ lastMessagePreview(room) }}</p>
        </div>
        <div class="sleekydz86-chat-room-side">
          <time>{{ formatLastMessageTime(room) }}</time>
          <span v-if="room.unreadCount > 0" class="sleekydz86-unread-badge">
            {{ formatUnread(room.unreadCount) }}
          </span>
        </div>
      </button>

      <PaginationBar
        v-if="showRoomSearch && roomSearchQuery.trim() && (roomSearchResults.length || searchPagination.totalElements.value > 0)"
        :page="searchPagination.page.value"
        :total-pages="searchPagination.totalPages.value"
        :total-elements="searchPagination.totalElements.value"
        :has-prev="searchPagination.hasPrev.value"
        :has-next="searchPagination.hasNext.value"
        :page-label="searchPagination.pageLabel.value"
        @prev="goRoomSearchPrev()"
        @next="goRoomSearchNext()"
      />

      <PaginationBar
        v-if="!showRoomSearch && (chatRooms.length || pagination.totalElements.value > 0)"
        :page="pagination.page.value"
        :total-pages="pagination.totalPages.value"
        :total-elements="pagination.totalElements.value"
        :has-prev="pagination.hasPrev.value"
        :has-next="pagination.hasNext.value"
        :page-label="pagination.pageLabel.value"
        @prev="pagination.goPrev()"
        @next="pagination.goNext()"
      />
    </div>

    <div v-if="showOpenChatSearch" class="sleekydz86-fullscreen-overlay">
      <OpenChatSearchPanel
        :token="token"
        :current-user-id="currentUserId"
        @close="showOpenChatSearch = false"
        @select="handleOpenChatSelect"
        @start-direct="handleStartDirect"
        @error="emit('error', $event)"
        @notice="emit('notice', $event)"
      />
    </div>

    <div v-if="showCreateModal" class="modal-overlay" @click.self="showCreateModal = false">
      <div class="modal-card" @click.stop>
        <h2>채팅방 만들기</h2>
        <form class="profile-form" @submit.prevent="handleCreateRoom">
          <label>
            방 종류
            <select
              :value="newRoomData.mediaMode ?? 'TEXT'"
              @change="onMediaModeChange(($event.target as HTMLSelectElement).value as ChatMediaMode)"
            >
              <option value="TEXT">일반 채팅</option>
              <option value="WEBRTC">WebRTC 화상</option>
            </select>
          </label>
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
          <label>
            최대 인원
            <input
              v-model.number="newRoomData.maxMembers"
              type="number"
              :min="newRoomData.mediaMode === 'WEBRTC' ? 2 : 1"
              :max="newRoomData.mediaMode === 'WEBRTC' ? 6 : 100"
            />
          </label>
          <label>
            공개 설정
            <select v-model="newRoomData.isPrivate">
              <option :value="false">공개 (오픈채팅 검색에 표시)</option>
              <option :value="true">비공개 (참여 코드 필요)</option>
            </select>
          </label>
          <label v-if="newRoomData.isPrivate">
            비밀번호
            <input v-model="newRoomData.password" type="password" minlength="4" placeholder="4자 이상" required />
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
  </div>
</template>
