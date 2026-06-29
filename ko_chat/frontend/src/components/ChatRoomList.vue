<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import {
  createChatRoom,
  discoverChatRooms,
  findOrCreateDirectRoom,
  getChatRoom,
  getChatRooms,
  getRecommendedChatRooms,
  joinChatRoom,
} from '../api/chatApi'
import { ApiError } from '../api/http'
import {
  addFriend,
  blockUser,
  fetchBlocks,
  fetchFriends,
  fetchRejectedFriendRequests,
  removeFriend,
  searchUsers,
  unblockUser,
} from '../api/userApi'
import type { ChatRoom, ChatRoomType, ChatUser, CreateChatRoomRequest, ChatMediaMode } from '../types/chat'
import type { UserFriendRequestResponse, UserRelationshipResponse } from '../types/user'

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
}>()

type SidebarTab = 'rooms' | 'friends' | 'blocks'
type JoinDiscoverTab = 'recommended' | 'search'

const ROOM_PAGE_SIZE = 20
const DISCOVER_PAGE_SIZE = 10

const activeTab = ref<SidebarTab>('rooms')
const chatRooms = ref<ChatRoom[]>([])
const friends = ref<UserRelationshipResponse[]>([])
const blocks = ref<UserRelationshipResponse[]>([])
const rejectedFriendRequests = ref<UserFriendRequestResponse[]>([])
const loading = ref(true)
const roomsLoadingMore = ref(false)
const roomsPage = ref(0)
const roomsHasMore = ref(false)
const relationLoading = ref(false)
const searchQuery = ref('')
const showCreateModal = ref(false)
const showJoinModal = ref(false)
const showDirectModal = ref(false)
const createLoading = ref(false)
const joinLoading = ref(false)
const directLoading = ref(false)
const actionUserId = ref<number | null>(null)
const userSearchQuery = ref('')
const userSearchLoading = ref(false)
const userSearchResults = ref<ChatUser[]>([])
const joinRoomId = ref('')
const joinPassword = ref('')
const joinDiscoverTab = ref<JoinDiscoverTab>('recommended')
const discoverQuery = ref('')
const discoverResults = ref<ChatRoom[]>([])
const discoverLoading = ref(false)
const discoverLoadingMore = ref(false)
const discoverPage = ref(0)
const discoverHasMore = ref(false)
const joiningRoomId = ref<number | null>(null)
const newRoomData = ref<CreateChatRoomRequest>({
  name: '',
  description: '',
  type: 'GROUP',
  maxMembers: 100,
  isPrivate: false,
  password: '',
  mediaMode: 'TEXT',
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

const userLabel = (user: ChatUser | UserRelationshipResponse['user']): string =>
  user.displayName ?? user.username

const loadChatRooms = async (reset = true) => {
  if (reset) {
    loading.value = true
    roomsPage.value = 0
    chatRooms.value = []
  } else {
    roomsLoadingMore.value = true
  }

  try {
    const response = await getChatRooms(props.token, roomsPage.value, ROOM_PAGE_SIZE)
    if (reset) {
      chatRooms.value = response.content
    } else {
      chatRooms.value = [...chatRooms.value, ...response.content]
    }
    roomsHasMore.value = !response.last
  } catch (error) {
    emit('error', resolveError(error, '채팅방 목록을 불러오지 못했습니다'))
  } finally {
    loading.value = false
    roomsLoadingMore.value = false
  }
}

const loadMoreChatRooms = async () => {
  if (!roomsHasMore.value || roomsLoadingMore.value || loading.value) {
    return
  }
  roomsPage.value += 1
  await loadChatRooms(false)
}

const loadFriends = async () => {
  relationLoading.value = true
  try {
    friends.value = await fetchFriends(props.token)
  } catch (error) {
    emit('error', resolveError(error, '친구 목록을 불러오지 못했습니다'))
  } finally {
    relationLoading.value = false
  }
}

const loadBlocks = async () => {
  relationLoading.value = true
  try {
    blocks.value = await fetchBlocks(props.token)
  } catch (error) {
    emit('error', resolveError(error, '차단 목록을 불러오지 못했습니다'))
  } finally {
    relationLoading.value = false
  }
}

const loadRejectedFriendRequests = async () => {
  relationLoading.value = true
  try {
    rejectedFriendRequests.value = await fetchRejectedFriendRequests(props.token)
  } catch (error) {
    emit('error', resolveError(error, '거부한 친구 요청을 불러오지 못했습니다'))
  } finally {
    relationLoading.value = false
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

const refreshRelationships = async () => {
  await Promise.all([loadFriends(), loadBlocks(), loadRejectedFriendRequests()])
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
    await loadChatRooms(true)
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

const handleStartDirectChat = async (user: ChatUser | UserRelationshipResponse['user']) => {
  directLoading.value = true
  try {
    const room = await findOrCreateDirectRoom(props.token, { targetUserId: user.id })
    upsertRoom(room)
    showDirectModal.value = false
    userSearchQuery.value = ''
    activeTab.value = 'rooms'
    emit('select', room)
  } catch (error) {
    emit('error', resolveError(error, '1:1 채팅을 시작하지 못했습니다'))
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

  await enterRoom(roomId, joinPassword.value.trim() || undefined)
}

const loadDiscoverRooms = async (reset = true) => {
  if (reset) {
    discoverLoading.value = true
    discoverPage.value = 0
    discoverResults.value = []
  } else {
    discoverLoadingMore.value = true
  }

  try {
    const response =
      joinDiscoverTab.value === 'recommended'
        ? await getRecommendedChatRooms(props.token, discoverPage.value, DISCOVER_PAGE_SIZE)
        : await discoverChatRooms(
            props.token,
            discoverQuery.value,
            discoverPage.value,
            DISCOVER_PAGE_SIZE,
          )

    if (reset) {
      discoverResults.value = response.content
    } else {
      discoverResults.value = [...discoverResults.value, ...response.content]
    }
    discoverHasMore.value = !response.last
  } catch (error) {
    emit('error', resolveError(error, '채팅방 목록을 불러오지 못했습니다'))
  } finally {
    discoverLoading.value = false
    discoverLoadingMore.value = false
  }
}

const loadMoreDiscoverRooms = async () => {
  if (!discoverHasMore.value || discoverLoadingMore.value || discoverLoading.value) {
    return
  }
  discoverPage.value += 1
  await loadDiscoverRooms(false)
}

const resetJoinModalState = () => {
  joinRoomId.value = ''
  joinPassword.value = ''
  discoverQuery.value = ''
  discoverResults.value = []
  discoverPage.value = 0
  discoverHasMore.value = false
}

const enterRoom = async (roomId: number, password?: string) => {
  joinLoading.value = true
  joiningRoomId.value = roomId
  try {
    const existing = chatRooms.value.find((room) => room.id === roomId)
    if (!existing) {
      const roomInfo = await getChatRoom(props.token, roomId)
      if (roomInfo.isPrivate && !password?.trim()) {
        emit('error', '비공개 방은 비밀번호를 입력해야 합니다')
        return
      }
      await joinChatRoom(props.token, roomId, password?.trim() || undefined)
    }
    await loadChatRooms(true)
    const joinedRoom = await getChatRoom(props.token, roomId)
    showJoinModal.value = false
    resetJoinModalState()
    activeTab.value = 'rooms'
    emit('select', joinedRoom)
  } catch (error) {
    emit('error', resolveError(error, '채팅방 참여에 실패했습니다'))
  } finally {
    joinLoading.value = false
    joiningRoomId.value = null
  }
}

const handleDiscoverJoin = async (room: ChatRoom) => {
  if (room.isJoined) {
    showJoinModal.value = false
    resetJoinModalState()
    activeTab.value = 'rooms'
    emit('select', room)
    return
  }

  if (room.memberCount >= room.maxMembers) {
    emit('error', '정원이 찼습니다')
    return
  }

  await enterRoom(room.id)
}

const openJoinModal = () => {
  showJoinModal.value = true
  joinDiscoverTab.value = 'recommended'
  resetJoinModalState()
  void loadDiscoverRooms(true)
}

const switchJoinDiscoverTab = (tab: JoinDiscoverTab) => {
  if (joinDiscoverTab.value === tab) {
    return
  }
  joinDiscoverTab.value = tab
  resetJoinModalState()
  void loadDiscoverRooms(true)
}

const handleAddFriend = async (user: ChatUser) => {
  actionUserId.value = user.id
  try {
    await addFriend(props.token, user.id)
    emit('notice', `${userLabel(user)} 님에게 친구 요청을 보냈습니다`)
    await loadRejectedFriendRequests()
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
    await refreshRelationships()
  } catch (error) {
    emit('error', resolveError(error, '차단에 실패했습니다'))
  } finally {
    actionUserId.value = null
  }
}

const handleUnblockUser = async (userId: number) => {
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

const roomVisibilityLabel = (room: ChatRoom): string => (room.isPrivate ? '비공개' : '공개')

const roomMediaLabel = (room: ChatRoom): string =>
  room.mediaMode === 'WEBRTC' ? 'WebRTC' : '채팅'

const onMediaModeChange = (mode: ChatMediaMode) => {
  newRoomData.value.mediaMode = mode
  if (mode === 'WEBRTC') {
    newRoomData.value.maxMembers = 4
    if (newRoomData.value.type === 'DIRECT') {
      newRoomData.value.type = 'GROUP'
    }
  } else {
    newRoomData.value.maxMembers = 100
  }
}

watch(showDirectModal, (open) => {
  if (open) {
    void loadUserSearch()
  }
})

watch(activeTab, (tab) => {
  if (tab === 'friends' && friends.value.length === 0) {
    void loadFriends()
    void loadRejectedFriendRequests()
    void loadUserSearch()
  }
  if (tab === 'blocks' && blocks.value.length === 0) {
    void loadBlocks()
  }
})

watch(
  () => props.refreshKey,
  () => {
    void loadChatRooms(true)
    if (activeTab.value === 'friends') {
      void loadFriends()
      void loadRejectedFriendRequests()
    }
    if (activeTab.value === 'blocks') {
      void loadBlocks()
    }
  },
)

let discoverDebounce: ReturnType<typeof setTimeout> | undefined
watch(discoverQuery, () => {
  if (!showJoinModal.value || joinDiscoverTab.value !== 'search') {
    return
  }
  clearTimeout(discoverDebounce)
  discoverDebounce = setTimeout(() => {
    void loadDiscoverRooms(true)
  }, 300)
})

let searchDebounce: ReturnType<typeof setTimeout> | undefined
watch(userSearchQuery, () => {
  if (!showDirectModal.value && activeTab.value !== 'friends') {
    return
  }
  clearTimeout(searchDebounce)
  searchDebounce = setTimeout(() => {
    void loadUserSearch()
  }, 300)
})

onMounted(() => {
  void loadChatRooms(true)
})

defineExpose({
  loadChatRooms,
  loadFriends,
  loadBlocks,
  loadRejectedFriendRequests,
})
</script>

<template>
  <aside class="chat-sidebar">
    <div class="chat-sidebar-header">
      <h2>내 메뉴</h2>
      <div class="sidebar-tabs" role="tablist">
        <button type="button" :class="{ active: activeTab === 'rooms' }" @click="activeTab = 'rooms'">
          대화
        </button>
        <button type="button" :class="{ active: activeTab === 'friends' }" @click="activeTab = 'friends'">
          친구
        </button>
        <button type="button" :class="{ active: activeTab === 'blocks' }" @click="activeTab = 'blocks'">
          차단
        </button>
      </div>
    </div>

    <template v-if="activeTab === 'rooms'">
      <div class="chat-sidebar-actions">
        <button type="button" @click="showDirectModal = true">1:1 채팅</button>
        <button type="button" class="secondary" @click="showCreateModal = true">그룹방</button>
        <button type="button" class="secondary" @click="openJoinModal">참여</button>
      </div>
      <div class="chat-sidebar-search">
        <input v-model="searchQuery" type="search" placeholder="대화 검색..." class="chat-search" />
      </div>

      <div class="chat-room-list">
        <p v-if="loading" class="chat-empty">채팅방을 불러오는 중...</p>
        <template v-else>
          <p v-if="filteredChatRooms.length === 0" class="chat-empty">
            {{ searchQuery ? '검색 결과가 없습니다' : '참여한 채팅방이 없습니다' }}
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
              <div class="chat-room-side">
                <span class="chat-room-time">{{ formatLastMessageTime(room) }}</span>
                <span v-if="room.unreadCount > 0" class="unread-badge">
                  {{ room.unreadCount > 99 ? '99+' : room.unreadCount }}
                </span>
              </div>
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
                  {{ roomTypeLabel(room.type) }} · {{ roomMediaLabel(room) }} ·
                  {{ roomVisibilityLabel(room) }} · 멤버 {{ room.memberCount }}
                </span>
                <span class="chat-room-preview">
                  {{ room.lastMessage?.content ?? '메시지가 없습니다' }}
                </span>
              </div>
              <div class="chat-room-side">
                <span class="chat-room-time">{{ formatLastMessageTime(room) }}</span>
                <span v-if="room.unreadCount > 0" class="unread-badge">
                  {{ room.unreadCount > 99 ? '99+' : room.unreadCount }}
                </span>
              </div>
            </button>
          </section>

          <button
            v-if="roomsHasMore"
            type="button"
            class="load-more-button"
            :disabled="roomsLoadingMore"
            @click="loadMoreChatRooms"
          >
            {{ roomsLoadingMore ? '불러오는 중...' : '대화 더 보기' }}
          </button>
        </template>
      </div>
    </template>

    <template v-else-if="activeTab === 'friends'">
      <div class="relationship-panel">
        <input v-model="userSearchQuery" type="search" placeholder="친구로 추가할 사용자 검색..." class="chat-search" />

        <section class="relationship-section">
          <h3 class="chat-room-section-title">검색 결과</h3>
          <p v-if="userSearchLoading" class="chat-empty slim">검색 중...</p>
          <p v-else-if="userSearchResults.length === 0" class="chat-empty slim">검색 결과가 없습니다</p>
          <div v-else class="relationship-list">
            <div v-for="user in userSearchResults" :key="user.id" class="relationship-item">
              <div>
                <strong>{{ userLabel(user) }}</strong>
                <span>@{{ user.username }}</span>
              </div>
              <div class="relationship-actions">
                <button
                  type="button"
                  class="compact"
                  :disabled="actionUserId === user.id"
                  @click="handleAddFriend(user)"
                >
                  추가
                </button>
                <button
                  type="button"
                  class="secondary compact"
                  :disabled="directLoading"
                  @click="handleStartDirectChat(user)"
                >
                  대화
                </button>
              </div>
            </div>
          </div>
        </section>

        <section class="relationship-section">
          <h3 class="chat-room-section-title">내 친구</h3>
          <p v-if="relationLoading" class="chat-empty slim">친구 목록을 불러오는 중...</p>
          <p v-else-if="friends.length === 0" class="chat-empty slim">등록한 친구가 없습니다</p>
          <div v-else class="relationship-list">
            <div v-for="friend in friends" :key="friend.id" class="relationship-item">
              <div>
                <strong>{{ userLabel(friend.user) }}</strong>
                <span>@{{ friend.user.username }}</span>
              </div>
              <div class="relationship-actions">
                <button type="button" class="compact" @click="handleStartDirectChat(friend.user)">
                  대화
                </button>
                <button
                  type="button"
                  class="secondary compact"
                  :disabled="actionUserId === friend.user.id"
                  @click="handleRemoveFriend(friend.user.id)"
                >
                  삭제
                </button>
                <button
                  type="button"
                  class="danger compact"
                  :disabled="actionUserId === friend.user.id"
                  @click="handleBlockUser(friend.user.id)"
                >
                  차단
                </button>
              </div>
            </div>
          </div>
        </section>

        <section class="relationship-section">
          <h3 class="chat-room-section-title">거부한 친구 요청</h3>
          <p v-if="relationLoading" class="chat-empty slim">거부 목록을 불러오는 중...</p>
          <p v-else-if="rejectedFriendRequests.length === 0" class="chat-empty slim">
            거부한 친구 요청이 없습니다
          </p>
          <div v-else class="relationship-list">
            <div v-for="request in rejectedFriendRequests" :key="request.id" class="relationship-item">
              <div>
                <strong>{{ userLabel(request.requester) }}</strong>
                <span>@{{ request.requester.username }}</span>
              </div>
              <button
                type="button"
                class="secondary compact"
                :disabled="actionUserId === request.requester.id"
                @click="handleAddFriend(request.requester)"
              >
                다시 요청
              </button>
            </div>
          </div>
        </section>
      </div>
    </template>

    <template v-else>
      <div class="relationship-panel">
        <section class="relationship-section">
          <h3 class="chat-room-section-title">차단한 사용자</h3>
          <p v-if="relationLoading" class="chat-empty slim">차단 목록을 불러오는 중...</p>
          <p v-else-if="blocks.length === 0" class="chat-empty slim">차단한 사용자가 없습니다</p>
          <div v-else class="relationship-list">
            <div v-for="block in blocks" :key="block.id" class="relationship-item">
              <div>
                <strong>{{ userLabel(block.user) }}</strong>
                <span>@{{ block.user.username }}</span>
              </div>
              <button
                type="button"
                class="secondary compact"
                :disabled="actionUserId === block.user.id"
                @click="handleUnblockUser(block.user.id)"
              >
                해제
              </button>
            </div>
          </div>
        </section>
      </div>
    </template>

    <div v-if="showDirectModal" class="modal-overlay" @click="showDirectModal = false">
      <div class="modal-card" @click.stop>
        <h2>1:1 채팅 시작</h2>
        <input v-model="userSearchQuery" type="search" placeholder="이름 또는 아이디 검색..." class="chat-search" />
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
            <strong>{{ userLabel(user) }}</strong>
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
              <option :value="false">공개 (검색·추천 목록에 표시)</option>
              <option :value="true">비공개 (비밀번호로만 참여)</option>
            </select>
          </label>
          <label v-if="newRoomData.isPrivate">
            비밀번호
            <input
              v-model="newRoomData.password"
              type="password"
              minlength="4"
              placeholder="4자 이상"
              required
            />
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
      <div class="modal-card join-room-modal" @click.stop>
        <h2>채팅방 찾기 · 참여</h2>

        <div class="join-discover-tabs" role="tablist">
          <button
            type="button"
            :class="{ active: joinDiscoverTab === 'recommended' }"
            @click="switchJoinDiscoverTab('recommended')"
          >
            추천
          </button>
          <button
            type="button"
            :class="{ active: joinDiscoverTab === 'search' }"
            @click="switchJoinDiscoverTab('search')"
          >
            검색
          </button>
        </div>

        <input
          v-if="joinDiscoverTab === 'search'"
          v-model="discoverQuery"
          type="search"
          placeholder="방 이름 또는 설명 검색..."
          class="chat-search"
        />

        <div class="discover-room-list">
          <p v-if="discoverLoading" class="chat-empty slim">불러오는 중...</p>
          <p v-else-if="discoverResults.length === 0" class="chat-empty slim">
            {{
              joinDiscoverTab === 'search' && discoverQuery
                ? '검색 결과가 없습니다'
                : '참여 가능한 공개 채팅방이 없습니다'
            }}
          </p>
          <div v-else class="relationship-list">
            <div v-for="room in discoverResults" :key="room.id" class="relationship-item discover-room-item">
              <div>
                <strong>{{ room.name }}</strong>
                <span>
                  {{ roomTypeLabel(room.type) }} ·
                  {{ room.mediaMode === 'WEBRTC' ? 'WebRTC' : '채팅' }} · 공개 · ID {{ room.id }} ·
                  {{ room.memberCount }}/{{ room.maxMembers }}명
                </span>
                <span v-if="room.description" class="discover-room-description">
                  {{ room.description }}
                </span>
              </div>
              <button
                type="button"
                class="compact"
                :disabled="joiningRoomId === room.id || (!room.isJoined && room.memberCount >= room.maxMembers)"
                @click="handleDiscoverJoin(room)"
              >
                {{
                  joiningRoomId === room.id
                    ? '처리 중...'
                    : room.isJoined
                      ? '열기'
                      : room.memberCount >= room.maxMembers
                        ? '정원 초과'
                        : '참여'
                }}
              </button>
            </div>
          </div>

          <button
            v-if="discoverHasMore"
            type="button"
            class="load-more-button"
            :disabled="discoverLoadingMore"
            @click="loadMoreDiscoverRooms"
          >
            {{ discoverLoadingMore ? '불러오는 중...' : '더 보기' }}
          </button>
        </div>

        <form class="profile-form join-by-id-form" @submit.prevent="handleJoinRoom">
          <label>
            방 ID로 참여
            <input v-model="joinRoomId" type="number" placeholder="예: 12" />
          </label>
          <label>
            비공개하기
            <input v-model="joinPassword" type="password" placeholder="비밀번호 입력" />
          </label>
          <div class="modal-actions">
            <button type="button" class="secondary" @click="showJoinModal = false">닫기</button>
            <button type="submit" :disabled="joinLoading || !joinRoomId.trim()">
              {{ joinLoading ? '참여 중...' : 'ID로 참여' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </aside>
</template>
