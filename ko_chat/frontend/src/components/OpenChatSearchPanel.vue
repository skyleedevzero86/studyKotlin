<script setup lang="ts">
import { ref, watch } from 'vue'
import { discoverChatRooms, getChatRoom, joinChatRoom } from '../api/chatApi'
import { ApiError } from '../api/http'
import { searchUsers } from '../api/userApi'
import type { ChatRoom, ChatUser } from '../types/chat'

const props = defineProps<{
  token: string
  currentUserId: number
}>()

const emit = defineEmits<{
  close: []
  select: [room: ChatRoom]
  error: [message: string]
  notice: [message: string]
  startDirect: [user: ChatUser]
}>()

type OpenChatTab = 'GROUP' | 'DIRECT'

const searchQuery = ref('')
const activeTab = ref<OpenChatTab>('GROUP')
const excludePrivate = ref(true)
const loading = ref(false)
const loadingMore = ref(false)
const page = ref(0)
const hasMore = ref(false)
const groupResults = ref<ChatRoom[]>([])
const directResults = ref<ChatUser[]>([])
const joiningRoomId = ref<number | null>(null)

const resolveError = (error: unknown, fallback: string): string => {
  if (error instanceof ApiError) return error.message
  if (error instanceof Error) return error.message
  return fallback
}

const avatarLabel = (name: string) => name.slice(0, 1).toUpperCase()

const avatarColor = (id: number) => {
  const palette = ['#5b8def', '#f59e0b', '#10b981', '#ef4444', '#8b5cf6', '#ec4899']
  return palette[id % palette.length]
}

const roomThumbStyle = (room: ChatRoom) => ({
  backgroundColor: avatarColor(room.id),
})

const userThumbStyle = (user: ChatUser) => ({
  backgroundColor: avatarColor(user.id),
})

const loadResults = async (reset = true) => {
  if (reset) {
    loading.value = true
    page.value = 0
    groupResults.value = []
    directResults.value = []
  } else {
    loadingMore.value = true
  }

  try {
    if (activeTab.value === 'GROUP') {
      const response = await discoverChatRooms(
        props.token,
        searchQuery.value,
        page.value,
        15,
        'GROUP',
        !excludePrivate.value,
      )
      if (reset) {
        groupResults.value = response.content
      } else {
        groupResults.value = [...groupResults.value, ...response.content]
      }
      hasMore.value = !response.last
    } else {
      const users = await searchUsers(props.token, searchQuery.value, 30)
      directResults.value = users.filter((user) => user.id !== props.currentUserId)
      hasMore.value = false
    }
  } catch (error) {
    emit('error', resolveError(error, '검색에 실패했습니다'))
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

const loadMore = async () => {
  if (!hasMore.value || loadingMore.value || activeTab.value !== 'GROUP') return
  page.value += 1
  await loadResults(false)
}

const switchTab = (tab: OpenChatTab) => {
  if (activeTab.value === tab) return
  activeTab.value = tab
  void loadResults(true)
}

const enterRoom = async (room: ChatRoom) => {
  if (room.isJoined) {
    emit('select', room)
    emit('close')
    return
  }
  if (room.memberCount >= room.maxMembers) {
    emit('error', '정원이 찼습니다')
    return
  }

  joiningRoomId.value = room.id
  try {
    await joinChatRoom(props.token, room.id)
    const joined = await getChatRoom(props.token, room.id)
    emit('notice', `${room.name} 채팅방에 참여했습니다`)
    emit('select', joined)
    emit('close')
  } catch (error) {
    emit('error', resolveError(error, '채팅방 참여에 실패했습니다'))
  } finally {
    joiningRoomId.value = null
  }
}

const handleDirectSelect = (user: ChatUser) => {
  emit('startDirect', user)
  emit('close')
}

let debounce: ReturnType<typeof setTimeout> | undefined
watch(searchQuery, () => {
  clearTimeout(debounce)
  debounce = setTimeout(() => {
    void loadResults(true)
  }, 300)
})

watch(excludePrivate, () => {
  if (activeTab.value === 'GROUP') {
    void loadResults(true)
  }
})

void loadResults(true)
</script>

<template>
  <div class="open-chat-search">
    <header class="open-chat-search-header">
      <button type="button" class="open-chat-back" @click="emit('close')">←</button>
      <h2>오픈채팅 검색</h2>
      <button type="button" class="open-chat-close" @click="emit('close')">✕</button>
    </header>

    <div class="open-chat-search-bar">
      <span class="open-chat-search-icon">🔍</span>
      <input
        v-model="searchQuery"
        type="search"
        class="open-chat-search-input"
        placeholder="검색어를 입력하세요"
      />
      <button v-if="searchQuery" type="button" class="open-chat-clear" @click="searchQuery = ''">✕</button>
    </div>

    <div class="open-chat-tabs">
      <button type="button" :class="{ active: activeTab === 'GROUP' }" @click="switchTab('GROUP')">
        그룹 채팅
      </button>
      <button type="button" :class="{ active: activeTab === 'DIRECT' }" @click="switchTab('DIRECT')">
        1:1 채팅
      </button>
    </div>

    <div class="open-chat-filters">
      <span class="open-chat-sort">최신</span>
      <label v-if="activeTab === 'GROUP'" class="open-chat-exclude">
        <input v-model="excludePrivate" type="checkbox" />
        참여 코드 채팅방 제외
      </label>
    </div>

    <div class="open-chat-results">
      <p v-if="loading" class="chat-empty slim">검색 중...</p>

      <template v-else-if="activeTab === 'GROUP'">
        <p v-if="groupResults.length === 0" class="chat-empty slim">검색 결과가 없습니다</p>
        <button
          v-for="room in groupResults"
          :key="room.id"
          type="button"
          class="open-chat-result-item"
          :disabled="joiningRoomId === room.id"
          @click="enterRoom(room)"
        >
          <div class="open-chat-result-text">
            <strong>{{ room.name }}</strong>
            <span>
              <span class="open-chat-result-host">👤 {{ room.createdBy.displayName ?? room.createdBy.username }}</span>
            </span>
          </div>
          <span class="open-chat-result-thumb" :style="roomThumbStyle(room)">
            {{ avatarLabel(room.name) }}
          </span>
        </button>
        <button
          v-if="hasMore"
          type="button"
          class="load-more-button"
          :disabled="loadingMore"
          @click="loadMore"
        >
          {{ loadingMore ? '불러오는 중...' : '더 보기' }}
        </button>
      </template>

      <template v-else>
        <p v-if="directResults.length === 0" class="chat-empty slim">검색 결과가 없습니다</p>
        <button
          v-for="user in directResults"
          :key="user.id"
          type="button"
          class="open-chat-result-item"
          @click="handleDirectSelect(user)"
        >
          <div class="open-chat-result-text">
            <strong>{{ user.displayName ?? user.username }}</strong>
            <span>
              <span class="open-chat-result-host">👤 @{{ user.username }}</span>
            </span>
          </div>
          <span class="open-chat-result-thumb" :style="userThumbStyle(user)">
            {{ avatarLabel(user.displayName ?? user.username) }}
          </span>
        </button>
      </template>
    </div>
  </div>
</template>
