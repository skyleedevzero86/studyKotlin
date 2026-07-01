<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ApiError } from '../api/http'
import { fetchBlocks, unblockUser } from '../api/userApi'
import type { UserRelationshipResponse } from '../types/user'

const props = defineProps<{
  token: string
  isAdmin?: boolean
}>()

const emit = defineEmits<{
  error: [message: string]
  goProfile: []
  goAdmin: []
  goAdminChatRooms: []
  goAdminStatistics: []
}>()

const blocks = ref<UserRelationshipResponse[]>([])
const loading = ref(false)
const actionUserId = ref<number | null>(null)

const resolveError = (error: unknown, fallback: string): string => {
  if (error instanceof ApiError) return error.message
  if (error instanceof Error) return error.message
  return fallback
}

const userLabel = (user: UserRelationshipResponse['user']) =>
  user.displayName ?? user.username

const loadBlocks = async () => {
  loading.value = true
  try {
    blocks.value = await fetchBlocks(props.token)
  } catch (error) {
    emit('error', resolveError(error, '차단 목록을 불러오지 못했습니다'))
  } finally {
    loading.value = false
  }
}

const handleUnblock = async (userId: number) => {
  actionUserId.value = userId
  try {
    await unblockUser(props.token, userId)
    await loadBlocks()
  } catch (error) {
    emit('error', resolveError(error, '차단 해제에 실패했습니다'))
  } finally {
    actionUserId.value = null
  }
}

onMounted(() => {
  void loadBlocks()
})

defineExpose({ loadBlocks })
</script>

<template>
  <div class="sleekydz86-more-panel">
    <header class="sleekydz86-panel-header">
      <h2>더보기</h2>
    </header>

    <div class="sleekydz86-more-menu">
      <button type="button" class="sleekydz86-more-menu-item" @click="emit('goProfile')">
        내 정보
      </button>
      <button
        v-if="isAdmin"
        type="button"
        class="sleekydz86-more-menu-item"
        @click="emit('goAdmin')"
      >
        관리자 · 사용자 목록
      </button>
      <button
        v-if="isAdmin"
        type="button"
        class="sleekydz86-more-menu-item"
        @click="emit('goAdminChatRooms')"
      >
        관리자 · 채팅방 목록
      </button>
      <button
        v-if="isAdmin"
        type="button"
        class="sleekydz86-more-menu-item"
        @click="emit('goAdminStatistics')"
      >
        관리자 · 통계
      </button>
    </div>

    <section class="sleekydz86-more-section">
      <h3>차단한 사용자</h3>
      <p v-if="loading" class="chat-empty slim">불러오는 중...</p>
      <p v-else-if="blocks.length === 0" class="chat-empty slim">차단한 사용자가 없습니다</p>
      <div v-else class="sleekydz86-friend-list">
        <div v-for="block in blocks" :key="block.id" class="sleekydz86-friend-item">
          <div class="sleekydz86-friend-info">
            <strong>{{ userLabel(block.user) }}</strong>
            <span>@{{ block.user.username }}</span>
          </div>
          <button
            type="button"
            class="sleekydz86-friend-action"
            :disabled="actionUserId === block.user.id"
            @click="handleUnblock(block.user.id)"
          >
            해제
          </button>
        </div>
      </div>
    </section>
  </div>
</template>
