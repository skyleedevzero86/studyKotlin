<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  acceptChatInvitation,
  checkHealth,
  getPendingChatInvitations,
  rejectChatInvitation,
} from '../api/chatApi'
import { ApiError, getJson } from '../api/http'
import {
  acceptFriendRequest,
  fetchIncomingFriendRequests,
  rejectFriendRequest,
} from '../api/userApi'
import ChatRoomList from '../components/ChatRoomList.vue'
import ChatWindow from '../components/ChatWindow.vue'
import FriendListPanel from '../components/FriendListPanel.vue'
import MorePanel from '../components/MorePanel.vue'
import PaginationBar from '../components/PaginationBar.vue'
import SurveyNotificationPopup from '../components/SurveyNotificationPopup.vue'
import type { SurveyNotification } from '../components/SurveyNotificationPopup.vue'
import { useAuth } from '../composables/useAuth'
import { usePagination } from '../composables/usePagination'
import type { ChatNotification, ChatRoom, ChatRoomInvitation } from '../types/chat'
import type { UserFriendRequestResponse, UserProfileResponse } from '../types/user'

type MainNav = 'friends' | 'chats' | 'more'

const router = useRouter()
const { accessToken, logout, isAdmin } = useAuth()

const profile = ref<UserProfileResponse | null>(null)
const selectedChatRoom = ref<ChatRoom | null>(null)
const notifications = ref<ChatNotification[]>([])
const serverStatus = ref<'checking' | 'online' | 'offline'>('checking')
const roomListRefreshKey = ref(0)
const friendRequests = ref<UserFriendRequestResponse[]>([])
const chatInvitations = ref<ChatRoomInvitation[]>([])
const invitationPagination = usePagination(5)
const pendingActionId = ref<string | null>(null)
const mainNav = ref<MainNav>('chats')
const chatUnreadCount = ref(0)
const friendListRef = ref<InstanceType<typeof FriendListPanel> | null>(null)
const surveyNotification = ref<SurveyNotification | null>(null)
let pendingPollTimer: ReturnType<typeof setInterval> | undefined
let notificationWs: WebSocket | null = null

const connectNotificationWs = () => {
  if (!accessToken.value) return
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const wsUrl = `${protocol}//${window.location.host}/api/v1/ws/chat?token=${encodeURIComponent(accessToken.value)}`
  notificationWs = new WebSocket(wsUrl)
  notificationWs.onmessage = (event) => {
    try {
      const data = JSON.parse(event.data)
      if (data.type === 'SURVEY_NOTIFICATION') {
        surveyNotification.value = {
          surveyId: data.surveyId,
          title: data.title,
          description: data.description ?? null,
        }
      }
    } catch { /* ignore parse errors */ }
  }
  notificationWs.onclose = () => {
    notificationWs = null
  }
}

const disconnectNotificationWs = () => {
  if (notificationWs) {
    notificationWs.close(1000)
    notificationWs = null
  }
}

const pendingActionCount = computed(
  () => friendRequests.value.length + invitationPagination.totalElements.value,
)

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
  mainNav.value = 'chats'
}

const handleRoomRead = (room: ChatRoom) => {
  if (selectedChatRoom.value?.id === room.id) {
    selectedChatRoom.value = room
  }
  roomListRefreshKey.value += 1
}

const handleRoomUpdated = (room: ChatRoom) => {
  if (selectedChatRoom.value?.id === room.id) {
    selectedChatRoom.value = room
  }
  roomListRefreshKey.value += 1
}

const handleLeftRoom = () => {
  selectedChatRoom.value = null
  roomListRefreshKey.value += 1
}

const handleRelationshipChanged = () => {
  roomListRefreshKey.value += 1
  void loadPendingActions()
  friendListRef.value?.loadFriends()
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

const goAdminUsers = async () => {
  await router.push({ name: 'admin-users' })
}

const goAdminChatRooms = async () => {
  await router.push({ name: 'admin-chat-rooms' })
}

const goAdminStatistics = async () => {
  await router.push({ name: 'admin-statistics' })
}

const goAdminMessaging = async () => {
  await router.push({ name: 'admin-messaging' })
}

const goAdminSurveys = async () => {
  await router.push({ name: 'admin-surveys' })
}

const goMySurveys = async () => {
  await router.push({ name: 'my-surveys' })
}

const dismissSurveyNotification = () => {
  surveyNotification.value = null
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

const stopPendingPoll = () => {
  if (pendingPollTimer) {
    clearInterval(pendingPollTimer)
    pendingPollTimer = undefined
  }
}

const loadPendingActions = async () => {
  if (!accessToken.value || serverStatus.value !== 'online') {
    return
  }

  try {
    const [incomingFriendRequests, pendingChatInvitations] = await Promise.all([
      fetchIncomingFriendRequests(accessToken.value),
      getPendingChatInvitations(
        accessToken.value,
        invitationPagination.page.value,
        invitationPagination.size.value,
      ),
    ])
    friendRequests.value = incomingFriendRequests
    chatInvitations.value = pendingChatInvitations.content
    invitationPagination.applyPageResponse(pendingChatInvitations)
  } catch (error) {
    if (error instanceof ApiError && error.status === 401) {
      stopPendingPoll()
      return
    }
  }
}

const handleAcceptFriendRequest = async (request: UserFriendRequestResponse) => {
  if (!accessToken.value) return
  pendingActionId.value = `friend-${request.id}`
  try {
    await acceptFriendRequest(accessToken.value, request.id)
    friendRequests.value = friendRequests.value.filter((item) => item.id !== request.id)
    roomListRefreshKey.value += 1
    friendListRef.value?.loadFriends()
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
    chatInvitations.value = chatInvitations.value.filter((i) => i.id !== invitation.id)
    selectedChatRoom.value = room
    mainNav.value = 'chats'
    roomListRefreshKey.value += 1
    handleNotice(`${roomLabel(room)} 초대를 수락했습니다`)
    void loadPendingActions()
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
    chatInvitations.value = chatInvitations.value.filter((i) => i.id !== invitation.id)
    handleNotice(`${roomLabel(invitation.chatRoom)} 초대를 거부했습니다`)
    void loadPendingActions()
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
    connectNotificationWs()
    await loadPendingActions()
    pendingPollTimer = setInterval(() => {
      void loadPendingActions()
    }, 15000)
  } catch {
    handleError('프로필 정보를 불러올 수 없습니다')
  }
})

onBeforeUnmount(() => {
  stopPendingPoll()
  disconnectNotificationWs()
})
</script>

<template>
  <div class="chat-page sleekydz86-app">
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

        <PaginationBar
          v-if="chatInvitations.length || invitationPagination.totalElements.value > 0"
          :page="invitationPagination.page.value"
          :total-pages="invitationPagination.totalPages.value"
          :total-elements="invitationPagination.totalElements.value"
          :has-prev="invitationPagination.hasPrev.value"
          :has-next="invitationPagination.hasNext.value"
          :page-label="invitationPagination.pageLabel.value"
          @prev="() => { invitationPagination.goPrev(); void loadPendingActions() }"
          @next="() => { invitationPagination.goNext(); void loadPendingActions() }"
        />
      </div>
    </div>

    <div v-if="serverStatus === 'offline'" class="chat-offline">
      <p>백엔드 서버에 연결할 수 없습니다. Spring Boot가 localhost:8080에서 실행 중인지 확인해주세요.</p>
      <button type="button" @click="checkServerHealth">다시 확인</button>
    </div>

    <div v-else-if="accessToken && profile" class="sleekydz86-app-body">
      <nav class="sleekydz86-nav-rail">
        <div class="sleekydz86-nav-top">
          <button
            type="button"
            class="sleekydz86-nav-item"
            :class="{ active: mainNav === 'friends' }"
            title="친구"
            @click="mainNav = 'friends'"
          >
            <span class="sleekydz86-nav-icon">👤</span>
            <span v-if="pendingActionCount > 0" class="sleekydz86-nav-badge">{{ pendingActionCount > 99 ? '99+' : pendingActionCount }}</span>
          </button>
          <button
            type="button"
            class="sleekydz86-nav-item"
            :class="{ active: mainNav === 'chats' }"
            title="채팅"
            @click="mainNav = 'chats'"
          >
            <span class="sleekydz86-nav-icon">💬</span>
            <span v-if="chatUnreadCount > 0" class="sleekydz86-nav-badge">{{ chatUnreadCount > 99 ? '99+' : chatUnreadCount }}</span>
          </button>
          <button
            type="button"
            class="sleekydz86-nav-item"
            :class="{ active: mainNav === 'more' }"
            title="더보기"
            @click="mainNav = 'more'"
          >
            <span class="sleekydz86-nav-icon">⋯</span>
          </button>
        </div>
        <div class="sleekydz86-nav-bottom">
          <button type="button" class="sleekydz86-nav-item" title="내 정보" @click="goProfile">
            <span class="sleekydz86-nav-icon">⚙</span>
          </button>
          <button type="button" class="sleekydz86-nav-item" title="로그아웃" @click="handleLogout">
            <span class="sleekydz86-nav-icon">⎋</span>
          </button>
        </div>
      </nav>

      <aside class="sleekydz86-list-panel">
        <FriendListPanel
          v-if="mainNav === 'friends'"
          ref="friendListRef"
          :token="accessToken"
          :current-user-id="profile.id"
          :profile-name="profile.displayName ?? profile.username"
          @select="handleChatRoomSelect"
          @error="handleError"
          @notice="handleNotice"
          @relationship-changed="handleRelationshipChanged"
        />
        <ChatRoomList
          v-else-if="mainNav === 'chats'"
          :token="accessToken"
          :current-user-id="profile.id"
          :selected-chat-room-id="selectedChatRoom?.id ?? null"
          :refresh-key="roomListRefreshKey"
          @select="handleChatRoomSelect"
          @error="handleError"
          @notice="handleNotice"
          @unread-count="chatUnreadCount = $event"
        />
        <MorePanel
          v-else
          :token="accessToken"
          :is-admin="isAdmin"
          @error="handleError"
          @go-profile="goProfile"
          @go-my-surveys="goMySurveys"
          @go-admin="goAdminUsers"
          @go-admin-chat-rooms="goAdminChatRooms"
          @go-admin-statistics="goAdminStatistics"
          @go-admin-messaging="goAdminMessaging"
          @go-admin-surveys="goAdminSurveys"
        />
      </aside>

      <main class="sleekydz86-main-panel">
        <ChatWindow
          v-if="selectedChatRoom"
          :token="accessToken"
          :chat-room="selectedChatRoom"
          :current-user-id="profile.id"
          :is-admin="isAdmin"
          @error="handleError"
          @notice="handleNotice"
          @read="handleRoomRead"
          @room-updated="handleRoomUpdated"
          @left="handleLeftRoom"
          @relationship-changed="handleRelationshipChanged"
        />

        <section v-else class="chat-welcome sleekydz86-welcome">
          <div class="sleekydz86-welcome-icon">💬</div>
          <h2>채팅을 시작해보세요</h2>
          <p>왼쪽에서 대화를 선택하거나 새 채팅방을 만들어보세요.</p>
        </section>
      </main>
    </div>
    <SurveyNotificationPopup
      :notification="surveyNotification"
      @dismiss="dismissSurveyNotification"
    />
  </div>
</template>
