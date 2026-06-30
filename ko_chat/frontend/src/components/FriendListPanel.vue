<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { findOrCreateDirectRoom } from '../api/chatApi'
import { ApiError } from '../api/http'
import {
  addFriend,
  blockUser,
  fetchFriends,
  fetchRejectedFriendRequests,
  removeFriend,
  searchUsers,
} from '../api/userApi'
import type { ChatRoom, ChatUser } from '../types/chat'
import type { UserFriendRequestResponse, UserRelationshipResponse } from '../types/user'

const props = defineProps<{
  token: string
  currentUserId: number
  profileName?: string
}>()

const emit = defineEmits<{
  select: [room: ChatRoom]
  error: [message: string]
  notice: [message: string]
  relationshipChanged: []
}>()

const friends = ref<UserRelationshipResponse[]>([])
const rejectedFriendRequests = ref<UserFriendRequestResponse[]>([])
const loading = ref(false)
const showSearch = ref(false)
const showAddFriend = ref(false)
const searchQuery = ref('')
const searchLoading = ref(false)
const searchResults = ref<ChatUser[]>([])
const actionUserId = ref<number | null>(null)
const directLoading = ref(false)
const friendsExpanded = ref(true)

const friendCount = computed(() => friends.value.length)

const resolveError = (error: unknown, fallback: string): string => {
  if (error instanceof ApiError) return error.message
  if (error instanceof Error) return error.message
  return fallback
}

const userLabel = (user: ChatUser | UserRelationshipResponse['user']) =>
  user.displayName ?? user.username

const avatarLabel = (user: ChatUser | UserRelationshipResponse['user']) =>
  userLabel(user).slice(0, 1).toUpperCase()

const avatarColor = (userId: number) => {
  const palette = ['#5b8def', '#f59e0b', '#10b981', '#ef4444', '#8b5cf6', '#ec4899']
  return palette[userId % palette.length]
}

const loadFriends = async () => {
  loading.value = true
  try {
    friends.value = await fetchFriends(props.token)
    rejectedFriendRequests.value = await fetchRejectedFriendRequests(props.token)
  } catch (error) {
    emit('error', resolveError(error, '친구 목록을 불러오지 못했습니다'))
  } finally {
    loading.value = false
  }
}

const loadSearch = async () => {
  searchLoading.value = true
  try {
    searchResults.value = await searchUsers(props.token, searchQuery.value)
  } catch (error) {
    emit('error', resolveError(error, '사용자 검색에 실패했습니다'))
  } finally {
    searchLoading.value = false
  }
}

const handleStartDirectChat = async (user: ChatUser | UserRelationshipResponse['user']) => {
  directLoading.value = true
  try {
    const room = await findOrCreateDirectRoom(props.token, { targetUserId: user.id })
    showSearch.value = false
    showAddFriend.value = false
    searchQuery.value = ''
    emit('select', room)
  } catch (error) {
    emit('error', resolveError(error, '1:1 채팅을 시작하지 못했습니다'))
  } finally {
    directLoading.value = false
  }
}

const handleAddFriend = async (user: ChatUser) => {
  actionUserId.value = user.id
  try {
    await addFriend(props.token, user.id)
    emit('notice', `${userLabel(user)} 님에게 친구 요청을 보냈습니다`)
    emit('relationshipChanged')
    await loadFriends()
  } catch (error) {
    emit('error', resolveError(error, '친구 추가에 실패했습니다'))
  } finally {
    actionUserId.value = null
  }
}

const handleRemoveFriend = async (userId: number) => {
  actionUserId.value = userId
  try {
    await removeFriend(props.token, userId)
    await loadFriends()
    emit('relationshipChanged')
  } catch (error) {
    emit('error', resolveError(error, '친구 삭제에 실패했습니다'))
  } finally {
    actionUserId.value = null
  }
}

const handleBlockUser = async (userId: number) => {
  actionUserId.value = userId
  try {
    await blockUser(props.token, userId)
    await loadFriends()
    emit('relationshipChanged')
  } catch (error) {
    emit('error', resolveError(error, '차단에 실패했습니다'))
  } finally {
    actionUserId.value = null
  }
}

let debounce: ReturnType<typeof setTimeout> | undefined
watch(searchQuery, () => {
  if (!showSearch.value && !showAddFriend.value) return
  clearTimeout(debounce)
  debounce = setTimeout(() => {
    void loadSearch()
  }, 300)
})

watch(showAddFriend, (open) => {
  if (open) void loadSearch()
})

watch(showSearch, (open) => {
  if (open) void loadSearch()
})

onMounted(() => {
  void loadFriends()
})

defineExpose({ loadFriends })
</script>

<template>
  <div class="sleekydz86-friends-panel">
    <header class="sleekydz86-panel-header">
      <h2>친구</h2>
      <div class="sleekydz86-panel-header-actions">
        <button type="button" class="sleekydz86-panel-icon-btn" title="검색" @click="showSearch = !showSearch; showAddFriend = false">
          🔍
        </button>
        <button type="button" class="sleekydz86-panel-icon-btn" title="친구 추가" @click="showAddFriend = !showAddFriend; showSearch = false">
          👤+
        </button>
      </div>
    </header>

    <div v-if="showSearch || showAddFriend" class="sleekydz86-friends-search-bar">
      <input
        v-model="searchQuery"
        type="search"
        class="sleekydz86-friends-search-input"
        :placeholder="showAddFriend ? '친구 추가할 사용자 검색' : '친구 검색'"
      />
    </div>

    <div v-if="showSearch || showAddFriend" class="sleekydz86-friends-search-results">
      <p v-if="searchLoading" class="chat-empty slim">검색 중...</p>
      <p v-else-if="searchResults.length === 0" class="chat-empty slim">검색 결과가 없습니다</p>
      <div v-else class="sleekydz86-friend-list">
        <div v-for="user in searchResults" :key="user.id" class="sleekydz86-friend-item">
          <span class="sleekydz86-friend-avatar" :style="{ backgroundColor: avatarColor(user.id) }">
            {{ avatarLabel(user) }}
          </span>
          <div class="sleekydz86-friend-info">
            <strong>{{ userLabel(user) }}</strong>
          </div>
          <div class="sleekydz86-friend-item-actions">
            <button
              v-if="showAddFriend"
              type="button"
              class="sleekydz86-friend-action"
              :disabled="actionUserId === user.id"
              @click="handleAddFriend(user)"
            >
              추가
            </button>
            <button
              type="button"
              class="sleekydz86-friend-action"
              :disabled="directLoading"
              @click="handleStartDirectChat(user)"
            >
              대화
            </button>
          </div>
        </div>
      </div>
    </div>

    <div class="sleekydz86-friends-body">
      <button type="button" class="sleekydz86-friends-section-toggle" @click="friendsExpanded = !friendsExpanded">
        <span>친구 {{ friendCount }}</span>
        <span>{{ friendsExpanded ? '▲' : '▼' }}</span>
      </button>

      <div v-if="friendsExpanded" class="sleekydz86-friend-list">
        <p v-if="loading" class="chat-empty slim">친구 목록을 불러오는 중...</p>
        <p v-else-if="friends.length === 0" class="chat-empty slim">등록한 친구가 없습니다</p>
        <div
          v-for="friend in friends"
          :key="friend.id"
          class="sleekydz86-friend-item"
        >
          <span class="sleekydz86-friend-avatar" :style="{ backgroundColor: avatarColor(friend.user.id) }">
            {{ avatarLabel(friend.user) }}
          </span>
          <button type="button" class="sleekydz86-friend-info sleekydz86-friend-info-btn" @click="handleStartDirectChat(friend.user)">
            <strong>{{ userLabel(friend.user) }}</strong>
            <span>@{{ friend.user.username }}</span>
          </button>
          <div class="sleekydz86-friend-item-actions">
            <button
              type="button"
              class="sleekydz86-friend-action danger"
              :disabled="actionUserId === friend.user.id"
              @click="handleRemoveFriend(friend.user.id)"
            >
              삭제
            </button>
            <button
              type="button"
              class="sleekydz86-friend-action"
              :disabled="actionUserId === friend.user.id"
              @click="handleBlockUser(friend.user.id)"
            >
              차단
            </button>
          </div>
        </div>
      </div>

      <section v-if="rejectedFriendRequests.length > 0" class="sleekydz86-friends-rejected">
        <h3>거부한 친구 요청</h3>
        <div v-for="request in rejectedFriendRequests" :key="request.id" class="sleekydz86-friend-item">
          <span class="sleekydz86-friend-avatar" :style="{ backgroundColor: avatarColor(request.requester.id) }">
            {{ avatarLabel(request.requester) }}
          </span>
          <div class="sleekydz86-friend-info">
            <strong>{{ userLabel(request.requester) }}</strong>
          </div>
          <button
            type="button"
            class="sleekydz86-friend-action"
            :disabled="actionUserId === request.requester.id"
            @click="handleAddFriend(request.requester)"
          >
            다시 요청
          </button>
        </div>
      </section>
    </div>
  </div>
</template>
