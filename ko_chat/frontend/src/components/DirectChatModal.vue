<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { findOrCreateDirectRoom } from '../api/chatApi'
import { searchUsers } from '../api/userApi'
import { ApiError } from '../api/http'
import PaginationBar from './PaginationBar.vue'
import { usePagination } from '../composables/usePagination'
import type { ChatRoom, ChatUser } from '../types/chat'

const props = defineProps<{
  token: string
  open: boolean
}>()

const emit = defineEmits<{
  close: []
  select: [room: ChatRoom]
  error: [message: string]
}>()

const users = ref<ChatUser[]>([])
const searchQuery = ref('')
const loading = ref(false)
const startingUserId = ref<number | null>(null)
const pagination = usePagination(15)

const resolveError = (error: unknown, fallback: string): string => {
  if (error instanceof ApiError) {
    return error.message
  }
  if (error instanceof Error) {
    return error.message
  }
  return fallback
}

const userLabel = (user: ChatUser): string => user.displayName ?? user.username

const loadUsers = async () => {
  loading.value = true
  try {
    const response = await searchUsers(
      props.token,
      searchQuery.value,
      pagination.page.value,
      pagination.size.value,
    )
    users.value = response.content
    pagination.applyPageResponse(response)
  } catch (error) {
    emit('error', resolveError(error, '사용자 목록을 불러올 수 없습니다'))
  } finally {
    loading.value = false
  }
}

const goUsersPrev = () => {
  pagination.goPrev()
  void loadUsers()
}

const goUsersNext = () => {
  pagination.goNext()
  void loadUsers()
}

const handleStartChat = async (user: ChatUser) => {
  startingUserId.value = user.id
  try {
    const room = await findOrCreateDirectRoom(props.token, { targetUserId: user.id })
    emit('select', room)
    emit('close')
  } catch (error) {
    emit('error', resolveError(error, '1:1 채팅을 시작할 수 없습니다'))
  } finally {
    startingUserId.value = null
  }
}

watch(
  () => props.open,
  (isOpen) => {
    if (isOpen) {
      searchQuery.value = ''
      pagination.resetPage()
      void loadUsers()
    }
  },
)

watch(searchQuery, () => {
  if (props.open) {
    pagination.resetPage()
    void loadUsers()
  }
})

onMounted(() => {
  if (props.open) {
    void loadUsers()
  }
})
</script>

<template>
  <div v-if="open" class="modal-overlay" @click="emit('close')">
    <div class="modal-card direct-chat-modal" @click.stop>
      <h2>1:1 대화 시작</h2>
      <p class="hint">대화할 사용자를 선택하세요. 기존 1:1 채팅방이 있으면 바로 열립니다.</p>

      <input
        v-model="searchQuery"
        type="search"
        placeholder="이름 또는 아이디 검색..."
        class="chat-search"
      />

      <div class="direct-user-list">
        <p v-if="loading" class="chat-empty">사용자를 불러오는 중...</p>
        <p v-else-if="users.length === 0" class="chat-empty">검색 결과가 없습니다</p>
        <button
          v-for="user in users"
          :key="user.id"
          type="button"
          class="direct-user-item"
          :disabled="startingUserId === user.id"
          @click="handleStartChat(user)"
        >
          <span class="direct-user-avatar">{{ userLabel(user).charAt(0).toUpperCase() }}</span>
          <span class="direct-user-info">
            <strong>{{ userLabel(user) }}</strong>
            <span>@{{ user.username }}</span>
          </span>
          <span class="direct-user-action">
            {{ startingUserId === user.id ? '연결 중...' : '대화' }}
          </span>
        </button>
      </div>

      <PaginationBar
        v-if="users.length || pagination.totalElements.value > 0"
        :page="pagination.page.value"
        :total-pages="pagination.totalPages.value"
        :total-elements="pagination.totalElements.value"
        :has-prev="pagination.hasPrev.value"
        :has-next="pagination.hasNext.value"
        :page-label="pagination.pageLabel.value"
        @prev="goUsersPrev()"
        @next="goUsersNext()"
      />

      <div class="modal-actions">
        <button type="button" class="secondary" @click="emit('close')">닫기</button>
      </div>
    </div>
  </div>
</template>
