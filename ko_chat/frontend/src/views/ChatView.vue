<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  acceptChatInvitation,
  checkHealth,
  getPendingChatInvitations,
  rejectChatInvitation,
} from '../api/chatApi'
import { getJson } from '../api/http'
import {
  acceptFriendRequest,
  fetchIncomingFriendRequests,
  rejectFriendRequest,
} from '../api/userApi'
import ChatRoomList from '../components/ChatRoomList.vue'
import ChatWindow from '../components/ChatWindow.vue'
import { useAuth } from '../composables/useAuth'
import type { UserFriendRequestResponse, UserProfileResponse } from '../types/user'
import type { ChatNotification, ChatRoom, ChatRoomInvitation } from '../types/chat'

const router = useRouter()
const { accessToken, logout } = useAuth()

const profile = ref<UserProfileResponse | null>(null)
const selectedChatRoom = ref<ChatRoom | null>(null)
const notifications = ref<ChatNotification[]>([])
const serverStatus = ref<'checking' | 'online' | 'offline'>('checking')
const roomListRefreshKey = ref(0)
const friendRequests = ref<UserFriendRequestResponse[]>([])
const chatInvitations = ref<ChatRoomInvitation[]>([])
const pendingActionId = ref<string | null>(null)
let pendingPollTimer: ReturnType<typeof setInterval> | undefined

const pendingActionCount = computed(() => friendRequests.value.length + chatInvitations.value.length)

const addNotification = (type: ChatNotification['type'], title: string, message: string) => {
  const notification: ChatNotification = {
    id: `${Date.now()}`,
    type,
    title,
    message,
    timestamp: Date.now(),
  }
  notifications.value = [notification, ...notifications.value.slice(0, 2)]
  setTimeout(() => {
    notifications.value = notifications.value.filter((item) => item.id !== notification.id)
  }, type === 'error' ? 10000 : 3000)
}

const handleError = (message: string) => {
  addNotification('error', '오류', message)
}

const handleNotice = (message: string) => {
  addNotification('system', '알림', message)
}

const handleChatRoomSelect = (room: ChatRoom) => {
  selectedChatRoom.value = room
}

const handleRoomRead = (room: ChatRoom) => {
  if (selectedChatRoom.value?.id === room.id) {
    selectedChatRoom.value = room
  }
  roomListRefreshKey.value += 1
}

const handleRelationshipChanged = () => {
  roomListRefreshKey.value += 1
  void loadPendingActions()
}

const handleLogout = async () => {
  if (pendingPollTimer) {
    clearInterval(pendingPollTimer)
  }
  logout()
  await router.push({ name: 'login' })
}

const goProfile = async () => {
  await router.push({ name: 'profile' })
}

const checkServerHealth = async () => {
  try {
    await checkHealth()
    serverStatus.value = 'online'
  } catch {
    serverStatus.value = 'offline'
  }
}

const userLabel = (user: UserFriendRequestResponse['requester']): string =>
  user.displayName ?? user.username

const roomLabel = (room: ChatRoom): string => {
  if (room.type === 'DIRECT' && room.peerUser) {
    return room.peerUser.displayName ?? room.peerUser.username
  }
  return room.name
}

const loadPendingActions = async () => {
  if (!accessToken.value || serverStatus.value !== 'online') {
    return
  }

  try {
    const [incomingFriendRequests, pendingChatInvitations] = await Promise.all([
      fetchIncomingFriendRequests(accessToken.value),
      getPendingChatInvitations(accessToken.value),
    ])
    friendRequests.value = incomingFriendRequests
    chatInvitations.value = pendingChatInvitations
  } catch (error) {
    console.error(error)
  }
}

const handleAcceptFriendRequest = async (request: UserFriendRequestResponse) => {
  if (!accessToken.value) return
  pendingActionId.value = `friend-${request.id}`
  try {
    await acceptFriendRequest(accessToken.value, request.id)
    friendRequests.value = friendRequests.value.filter((item) => item.id !== request.id)
    roomListRefreshKey.value += 1
    handleNotice(`${userLabel(request.requester)} 님을 친구로 등록했습니다`)
  } catch (error) {
    handleError(error instanceof Error ? error.message : '친구 요청 수락에 실패했습니다')
  } finally {
    pendingActionId.value = null
  }
}

const handleRejectFriendRequest = async (request: UserFriendRequestResponse) => {
  if (!accessToken.value) return
  pendingActionId.value = `friend-${request.id}`
  try {
    await rejectFriendRequest(accessToken.value, request.id)
    friendRequests.value = friendRequests.value.filter((item) => item.id !== request.id)
    roomListRefreshKey.value += 1
    handleNotice(`${userLabel(request.requester)} 님의 친구 요청을 거부했습니다`)
  } catch (error) {
    handleError(error instanceof Error ? error.message : '친구 요청 거부에 실패했습니다')
  } finally {
    pendingActionId.value = null
  }
}

const handleAcceptChatInvitation = async (invitation: ChatRoomInvitation) => {
  if (!accessToken.value) return
  pendingActionId.value = `chat-${invitation.id}`
  try {
    const room = await acceptChatInvitation(accessToken.value, invitation.id)
    chatInvitations.value = chatInvitations.value.filter((item) => item.id !== invitation.id)
    selectedChatRoom.value = room
    roomListRefreshKey.value += 1
    handleNotice(`${roomLabel(room)} 초대를 수락했습니다`)
  } catch (error) {
    handleError(error instanceof Error ? error.message : '채팅 초대 수락에 실패했습니다')
  } finally {
    pendingActionId.value = null
  }
}

const handleRejectChatInvitation = async (invitation: ChatRoomInvitation) => {
  if (!accessToken.value) return
  pendingActionId.value = `chat-${invitation.id}`
  try {
    await rejectChatInvitation(accessToken.value, invitation.id)
    chatInvitations.value = chatInvitations.value.filter((item) => item.id !== invitation.id)
    roomListRefreshKey.value += 1
    handleNotice(`${roomLabel(invitation.chatRoom)} 초대를 거부했습니다`)
  } catch (error) {
    handleError(error instanceof Error ? error.message : '채팅 초대 거부에 실패했습니다')
  } finally {
    pendingActionId.value = null
  }
}

onMounted(async () => {
  await checkServerHealth()

  if (!accessToken.value) {
    await router.push({ name: 'login' })
    return
  }

  try {
    profile.value = await getJson<UserProfileResponse>('/api/v1/user/me', accessToken.value)
    await loadPendingActions()
    pendingPollTimer = setInterval(() => {
      void loadPendingActions()
    }, 15000)
  } catch {
    handleError('프로필 정보를 불러올 수 없습니다')
  }
})

onBeforeUnmount(() => {
  if (pendingPollTimer) {
    clearInterval(pendingPollTimer)
  }
})
</script>

<template>
  <div class="chat-page">
    <div class="chat-topbar">
      <div class="chat-topbar-left">
        <button type="button" class="secondary" @click="goProfile">내 정보</button>
        <span class="server-status" :class="serverStatus">
          {{ serverStatus === 'online' ? '서버 연결됨' : serverStatus === 'offline' ? '서버 오프라인' : '확인 중' }}
        </span>
      </div>
      <div class="chat-topbar-right">
        <span v-if="profile">{{ profile.displayName ?? profile.username }}님</span>
        <button type="button" class="secondary" @click="handleLogout">로그아웃</button>
      </div>
    </div>

    <div v-if="notifications.length > 0" class="chat-notifications">
      <div
        v-for="notification in notifications"
        :key="notification.id"
        class="chat-notification"
        :class="notification.type"
      >
        <strong>{{ notification.title }}</strong>
        <p>{{ notification.message }}</p>
      </div>
    </div>

    <div v-if="pendingActionCount > 0" class="pending-actions-popup">
      <strong>새 요청 {{ pendingActionCount }}개</strong>
      <div class="pending-action-list">
        <div v-for="request in friendRequests" :key="`friend-${request.id}`" class="pending-action-item">
          <p>{{ userLabel(request.requester) }} 님이 친구 요청을 보냈습니다</p>
          <div class="pending-action-buttons">
            <button
              type="button"
              class="compact"
              :disabled="pendingActionId === `friend-${request.id}`"
              @click="handleAcceptFriendRequest(request)"
            >
              수락
            </button>
            <button
              type="button"
              class="secondary compact"
              :disabled="pendingActionId === `friend-${request.id}`"
              @click="handleRejectFriendRequest(request)"
            >
              거부
            </button>
          </div>
        </div>

        <div v-for="invitation in chatInvitations" :key="`chat-${invitation.id}`" class="pending-action-item">
          <p>
            {{ userLabel(invitation.inviter) }} 님이
            {{ roomLabel(invitation.chatRoom) }} 채팅에 초대했습니다
          </p>
          <div class="pending-action-buttons">
            <button
              type="button"
              class="compact"
              :disabled="pendingActionId === `chat-${invitation.id}`"
              @click="handleAcceptChatInvitation(invitation)"
            >
              수락
            </button>
            <button
              type="button"
              class="secondary compact"
              :disabled="pendingActionId === `chat-${invitation.id}`"
              @click="handleRejectChatInvitation(invitation)"
            >
              거부
            </button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="serverStatus === 'offline'" class="chat-offline">
      <p>서버에 연결할 수 없습니다. 백엔드와 Redis가 실행 중인지 확인해주세요.</p>
      <button type="button" @click="checkServerHealth">다시 확인</button>
    </div>

    <div v-else-if="accessToken && profile" class="chat-layout">
      <ChatRoomList
        :token="accessToken"
        :current-user-id="profile.id"
        :selected-chat-room-id="selectedChatRoom?.id ?? null"
        :refresh-key="roomListRefreshKey"
        @select="handleChatRoomSelect"
        @error="handleError"
        @notice="handleNotice"
      />

      <ChatWindow
        v-if="selectedChatRoom"
        :token="accessToken"
        :chat-room="selectedChatRoom"
        :current-user-id="profile.id"
        @error="handleError"
        @notice="handleNotice"
        @read="handleRoomRead"
        @room-updated="handleRoomRead"
        @relationship-changed="handleRelationshipChanged"
      />

      <section v-else class="chat-welcome">
        <h2>채팅을 시작해보세요</h2>
        <p>왼쪽에서 1:1 대화를 시작하거나 그룹 채팅방을 만들어 실시간 대화를 시작할 수 있습니다.</p>
        <ul>
          <li>1:1 개인 채팅 (상대 검색 후 바로 시작)</li>
          <li>실시간 메시지 전송 (WebSocket)</li>
          <li>그룹 채팅방 생성 및 참여</li>
          <li>메시지 기록 조회</li>
        </ul>
      </section>
    </div>
  </div>
</template>
