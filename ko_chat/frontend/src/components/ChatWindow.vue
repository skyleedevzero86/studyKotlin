<script setup lang="ts">
import { computed, nextTick, ref, watch } from 'vue'
import {
  getChatRoom,
  getChatRoomMembers,
  getMessages,
  inviteToChatRoom,
  kickChatRoomMember,
  leaveChatRoom,
  markChatRoomRead,
  updateChatRoomCapacity,
  updateChatRoomSettings,
} from '../api/chatApi'
import { ApiError } from '../api/http'
import { addFriend, blockUser, searchUsers } from '../api/userApi'
import { useWebSocket } from '../composables/useWebSocket'
import WebRtcPanel from './WebRtcPanel.vue'
import type {
  ChatRoom,
  ChatRoomMember,
  ChatUser,
  IncomingWebSocketMessage,
  Message,
  OutgoingWebSocketMessage,
} from '../types/chat'

const props = defineProps<{
  token: string
  chatRoom: ChatRoom
  currentUserId: number
}>()

const emit = defineEmits<{
  error: [message: string]
  notice: [message: string]
  read: [room: ChatRoom]
  roomUpdated: [room: ChatRoom]
  left: []
  relationshipChanged: []
}>()

const messages = ref<Message[]>([])
const members = ref<ChatRoomMember[]>([])
const messageInput = ref('')
const isLoadingMessages = ref(false)
const membersLoading = ref(false)
const showMembers = ref(false)
const actionUserId = ref<number | null>(null)
const inviteQuery = ref('')
const inviteResults = ref<ChatUser[]>([])
const inviteLoading = ref(false)
const capacityInput = ref(props.chatRoom.maxMembers)
const capacityLoading = ref(false)
const showSettings = ref(false)
const settingsLoading = ref(false)
const leaveLoading = ref(false)
const settingsName = ref(props.chatRoom.name)
const settingsDescription = ref(props.chatRoom.description ?? '')
const settingsPrivate = ref(props.chatRoom.isPrivate ?? false)
const settingsPassword = ref('')
const messagesEndRef = ref<HTMLElement | null>(null)

const resolveError = (error: unknown, fallback: string): string => {
  if (error instanceof ApiError) {
    return error.message
  }
  if (error instanceof Error) {
    return error.message
  }
  return fallback
}

const scrollToBottom = async () => {
  await nextTick()
  messagesEndRef.value?.scrollIntoView({ behavior: 'smooth' })
}

const markRead = async () => {
  try {
    const room = await markChatRoomRead(props.token, props.chatRoom.id)
    emit('read', room)
  } catch (error) {
    emit('error', resolveError(error, '읽음 처리에 실패했습니다'))
  }
}

const loadMessages = async () => {
  isLoadingMessages.value = true
  try {
    const response = await getMessages(props.token, props.chatRoom.id, 0, 50)
    const sorted = [...response.content].sort(
      (a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime(),
    )
    messages.value = sorted
    await scrollToBottom()
    await markRead()
  } catch (error) {
    emit('error', resolveError(error, '메시지를 불러오지 못했습니다'))
  } finally {
    isLoadingMessages.value = false
  }
}

const loadMembers = async () => {
  membersLoading.value = true
  try {
    members.value = await getChatRoomMembers(props.token, props.chatRoom.id)
  } catch (error) {
    emit('error', resolveError(error, '참여자 목록을 불러오지 못했습니다'))
  } finally {
    membersLoading.value = false
  }
}

const loadInviteCandidates = async () => {
  if (!canManageRoom.value) {
    inviteResults.value = []
    return
  }

  inviteLoading.value = true
  try {
    const results = await searchUsers(props.token, inviteQuery.value)
    const activeMemberIds = new Set(members.value.map((member) => member.user.id))
    inviteResults.value = results.filter(
      (user) => user.id !== props.currentUserId && !activeMemberIds.has(user.id),
    )
  } catch (error) {
    emit('error', resolveError(error, '초대할 사용자를 검색하지 못했습니다'))
  } finally {
    inviteLoading.value = false
  }
}

const isChatMessage = (
  payload: IncomingWebSocketMessage,
): payload is IncomingWebSocketMessage & {
  id: number
  content: string
  senderId: number
  senderName: string
  chatRoomId: number
  sequenceNumber: number
  timestamp: string
} => {
  return (
    typeof payload === 'object' &&
    payload !== null &&
    'content' in payload &&
    typeof payload.content === 'string' &&
    'senderId' in payload &&
    typeof payload.senderId === 'number'
  )
}

const handleIncomingMessage = (payload: IncomingWebSocketMessage) => {
  if ('type' in payload && payload.type === 'ERROR' && 'message' in payload) {
    emit('error', String(payload.message))
    return
  }

  if (!isChatMessage(payload) || payload.chatRoomId !== props.chatRoom.id) {
    return
  }

  if (messages.value.some((message) => message.id === payload.id)) {
    return
  }

  const newMessage: Message = {
    id: payload.id,
    chatRoomId: payload.chatRoomId,
    sender: {
      id: payload.senderId,
      username: payload.senderName,
      displayName: payload.senderName,
      isActive: true,
    },
    type: 'TEXT',
    content: payload.content,
    sequenceNumber: payload.sequenceNumber,
    isEdited: false,
    isDeleted: false,
    createdAt: new Date(payload.timestamp).toISOString(),
  }

  messages.value = [...messages.value, newMessage]
  void scrollToBottom()
  void markRead()
}

const { isConnected, sendMessage, error: wsError } = useWebSocket({
  token: props.token,
  onMessage: handleIncomingMessage,
  onError: (message) => emit('error', message),
})

const handleSendMessage = () => {
  const content = messageInput.value.trim()
  if (!content || !isConnected.value) {
    return
  }

  const wsMessage: OutgoingWebSocketMessage = {
    type: 'SEND_MESSAGE',
    chatRoomId: props.chatRoom.id,
    messageType: 'TEXT',
    content,
  }

  if (sendMessage(wsMessage)) {
    messageInput.value = ''
  }
}

const handleAddFriend = async (userId: number) => {
  actionUserId.value = userId
  try {
    await addFriend(props.token, userId)
    emit('notice', '친구 요청을 보냈습니다')
    emit('relationshipChanged')
  } catch (error) {
    emit('error', resolveError(error, '친구 추가에 실패했습니다'))
  } finally {
    actionUserId.value = null
  }
}

const handleInviteUser = async (user: ChatUser) => {
  actionUserId.value = user.id
  try {
    await inviteToChatRoom(props.token, props.chatRoom.id, user.id)
    emit('notice', `${user.displayName ?? user.username} 님에게 채팅 초대를 보냈습니다`)
    inviteQuery.value = ''
    inviteResults.value = []
  } catch (error) {
    emit('error', resolveError(error, '채팅 초대에 실패했습니다'))
  } finally {
    actionUserId.value = null
  }
}

const handleKickMember = async (user: ChatUser) => {
  actionUserId.value = user.id
  try {
    await kickChatRoomMember(props.token, props.chatRoom.id, user.id)
    await loadMembers()
    const room = await getChatRoom(props.token, props.chatRoom.id)
    emit('roomUpdated', room)
    emit('notice', `${user.displayName ?? user.username} 님을 채팅방에서 내보냈습니다`)
  } catch (error) {
    emit('error', resolveError(error, '참여자를 내보내지 못했습니다'))
  } finally {
    actionUserId.value = null
  }
}

const handleUpdateCapacity = async () => {
  capacityLoading.value = true
  try {
    const room = await updateChatRoomCapacity(
      props.token,
      props.chatRoom.id,
      Math.trunc(capacityInput.value),
    )
    capacityInput.value = room.maxMembers
    emit('roomUpdated', room)
    emit('notice', `채팅방 정원을 ${room.maxMembers}명으로 변경했습니다`)
  } catch (error) {
    emit('error', resolveError(error, '정원 변경에 실패했습니다'))
  } finally {
    capacityLoading.value = false
  }
}

const handleUpdateSettings = async () => {
  if (!settingsName.value.trim()) {
    emit('error', '채팅방 이름을 입력해주세요')
    return
  }
  if (settingsPrivate.value && !props.chatRoom.isPrivate && settingsPassword.value.trim().length < 4) {
    emit('error', '비공개로 변경하려면 비밀번호 4자 이상이 필요합니다')
    return
  }

  settingsLoading.value = true
  try {
    const room = await updateChatRoomSettings(props.token, props.chatRoom.id, {
      name: settingsName.value.trim(),
      description: settingsDescription.value.trim() || null,
      isPrivate: settingsPrivate.value,
      password: settingsPassword.value.trim() || null,
    })
    settingsPassword.value = ''
    emit('roomUpdated', room)
    emit('notice', '채팅방 설정을 저장했습니다')
    showSettings.value = false
  } catch (error) {
    emit('error', resolveError(error, '채팅방 설정 저장에 실패했습니다'))
  } finally {
    settingsLoading.value = false
  }
}

const handleLeaveRoom = async () => {
  if (!window.confirm('채팅방에서 나가시겠습니까?')) {
    return
  }

  leaveLoading.value = true
  try {
    await leaveChatRoom(props.token, props.chatRoom.id)
    emit('left')
    emit('notice', '채팅방에서 나갔습니다')
  } catch (error) {
    emit('error', resolveError(error, '채팅방 나가기에 실패했습니다'))
  } finally {
    leaveLoading.value = false
  }
}

const handleKicked = (message: string) => {
  emit('error', message)
  emit('left')
}

const handleBlockUser = async (userId: number) => {
  actionUserId.value = userId
  try {
    await blockUser(props.token, userId)
    emit('relationshipChanged')
  } catch (error) {
    emit('error', resolveError(error, '차단에 실패했습니다'))
  } finally {
    actionUserId.value = null
  }
}

const roomTitle = computed(() => {
  if (props.chatRoom.type === 'DIRECT' && props.chatRoom.peerUser) {
    return props.chatRoom.peerUser.displayName ?? props.chatRoom.peerUser.username
  }
  return props.chatRoom.name
})

const roomSubtitle = computed(() => {
  if (props.chatRoom.type === 'DIRECT' && props.chatRoom.peerUser) {
    return `@${props.chatRoom.peerUser.username} · 1:1 채팅`
  }
  const mediaLabel = props.chatRoom.mediaMode === 'WEBRTC' ? 'WebRTC' : '일반 채팅'
  const visibilityLabel = props.chatRoom.isPrivate ? '비공개' : '공개'
  return `${mediaLabel} · ${visibilityLabel} · 멤버 ${props.chatRoom.memberCount}명 · ID: ${props.chatRoom.id}${
    props.chatRoom.description ? ` · ${props.chatRoom.description}` : ''
  }`
})

const canLeaveRoom = computed(() => props.chatRoom.type !== 'DIRECT')

const isWebRtcRoom = computed(() => props.chatRoom.mediaMode === 'WEBRTC')

const capacityMin = computed(() => {
  if (isWebRtcRoom.value) {
    return Math.max(2, members.value.length)
  }
  return Math.max(1, members.value.length)
})

const capacityMax = computed(() => (isWebRtcRoom.value ? 6 : 100))

const canManageRoom = computed(
  () => props.chatRoom.type !== 'DIRECT' && props.chatRoom.createdBy.id === props.currentUserId,
)

const formatTime = (dateString: string): string => {
  const date = new Date(dateString)
  return date.toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' })
}

watch(
  () => props.chatRoom.id,
  () => {
    showMembers.value = false
    members.value = []
    inviteQuery.value = ''
    inviteResults.value = []
    capacityInput.value = props.chatRoom.maxMembers
    settingsName.value = props.chatRoom.name
    settingsDescription.value = props.chatRoom.description ?? ''
    settingsPrivate.value = props.chatRoom.isPrivate ?? false
    settingsPassword.value = ''
    showSettings.value = false
    void loadMessages()
    if (props.chatRoom.mediaMode === 'WEBRTC') {
      void loadMembers()
    }
  },
  { immediate: true },
)

watch(
  () => props.chatRoom.maxMembers,
  (maxMembers) => {
    capacityInput.value = maxMembers
  },
)

watch(showMembers, (open) => {
  if (open && members.value.length === 0) {
    void loadMembers()
  }
})

let inviteSearchDebounce: ReturnType<typeof setTimeout> | undefined
watch(inviteQuery, () => {
  if (!showMembers.value || !canManageRoom.value) {
    return
  }
  clearTimeout(inviteSearchDebounce)
  inviteSearchDebounce = setTimeout(() => {
    void loadInviteCandidates()
  }, 300)
})

watch(wsError, (value) => {
  if (value) {
    emit('error', value)
  }
})
</script>

<template>
  <section class="chat-window">
    <header class="chat-window-header">
      <div>
        <h2>{{ roomTitle }}</h2>
        <p>{{ roomSubtitle }}</p>
      </div>
      <div class="chat-window-actions">
        <button
          v-if="canManageRoom"
          type="button"
          class="secondary compact"
          @click="showSettings = !showSettings"
        >
          설정
        </button>
        <button type="button" class="secondary compact" @click="showMembers = !showMembers">
          참여자
        </button>
        <button
          v-if="canLeaveRoom"
          type="button"
          class="secondary compact"
          :disabled="leaveLoading"
          @click="handleLeaveRoom"
        >
          {{ leaveLoading ? '처리 중...' : '나가기' }}
        </button>
        <span class="connection-badge" :class="{ online: isConnected }">
          {{ isConnected ? '연결됨' : '연결 중...' }}
        </span>
      </div>
    </header>

    <div v-if="showSettings && canManageRoom" class="member-panel room-settings-panel">
      <form class="profile-form" @submit.prevent="handleUpdateSettings">
        <label>
          채팅방 이름
          <input v-model="settingsName" type="text" maxlength="100" required />
        </label>
        <label>
          설명
          <input v-model="settingsDescription" type="text" />
        </label>
        <label>
          공개 설정
          <select v-model="settingsPrivate">
            <option :value="false">공개</option>
            <option :value="true">비공개</option>
          </select>
        </label>
        <label v-if="settingsPrivate">
          {{ chatRoom.isPrivate ? '새 비밀번호 (변경 시에만 입력)' : '비밀번호' }}
          <input
            v-model="settingsPassword"
            type="password"
            :required="!chatRoom.isPrivate"
            minlength="4"
            placeholder="4자 이상"
          />
        </label>
        <div class="modal-actions">
          <button type="submit" class="compact" :disabled="settingsLoading">
            {{ settingsLoading ? '저장 중...' : '설정 저장' }}
          </button>
        </div>
      </form>
    </div>

    <div v-if="showMembers" class="member-panel">
      <div v-if="canManageRoom" class="member-management">
        <form class="capacity-form" @submit.prevent="handleUpdateCapacity">
          <label>
            최대 인원
            <input
              v-model.number="capacityInput"
              type="number"
              :min="capacityMin"
              :max="capacityMax"
            />
          </label>
          <button type="submit" class="compact" :disabled="capacityLoading">
            {{ capacityLoading ? '저장 중...' : '저장' }}
          </button>
        </form>

        <div class="invite-user-panel">
          <input
            v-model="inviteQuery"
            type="search"
            placeholder="초대할 사용자 검색..."
            class="chat-search"
          />
          <div v-if="inviteQuery || inviteResults.length > 0" class="invite-result-list">
            <p v-if="inviteLoading" class="chat-empty slim">검색 중...</p>
            <p v-else-if="inviteResults.length === 0" class="chat-empty slim">초대할 사용자가 없습니다</p>
            <button
              v-for="user in inviteResults"
              :key="user.id"
              type="button"
              class="user-search-item"
              :disabled="actionUserId === user.id"
              @click="handleInviteUser(user)"
            >
              <strong>{{ user.displayName ?? user.username }}</strong>
              <span>@{{ user.username }}</span>
            </button>
          </div>
        </div>
      </div>

      <p v-if="membersLoading" class="chat-empty slim">참여자를 불러오는 중...</p>
      <div v-else class="member-list">
        <div v-for="member in members" :key="member.id" class="member-item">
          <div>
            <strong>{{ member.user.displayName ?? member.user.username }}</strong>
            <span>@{{ member.user.username }} · {{ member.role }}</span>
          </div>
          <div v-if="member.user.id !== currentUserId" class="member-actions">
            <button
              type="button"
              class="secondary compact"
              :disabled="actionUserId === member.user.id"
              @click="handleAddFriend(member.user.id)"
            >
              친구 추가
            </button>
            <button
              type="button"
              class="danger compact"
              :disabled="actionUserId === member.user.id"
              @click="handleBlockUser(member.user.id)"
            >
              차단
            </button>
            <button
              v-if="canManageRoom"
              type="button"
              class="danger compact"
              :disabled="actionUserId === member.user.id"
              @click="handleKickMember(member.user)"
            >
              내보내기
            </button>
          </div>
        </div>
      </div>
    </div>

    <WebRtcPanel
      v-if="isWebRtcRoom"
      :key="chatRoom.id"
      :token="token"
      :chat-room-id="chatRoom.id"
      :current-user-id="currentUserId"
      :members="members"
      @error="emit('error', $event)"
      @kicked="handleKicked"
    />

    <div class="chat-messages">
      <p v-if="isLoadingMessages" class="chat-empty">메시지를 불러오는 중...</p>
      <p v-else-if="messages.length === 0" class="chat-empty">첫 번째 메시지를 보내보세요</p>

      <div
        v-for="message in messages"
        :key="message.id"
        class="chat-message-row"
        :class="{ own: message.sender.id === currentUserId, system: message.type === 'SYSTEM' }"
      >
        <span v-if="message.sender.id !== currentUserId && message.type !== 'SYSTEM'" class="chat-message-author">
          {{ message.sender.displayName ?? message.sender.username }}
        </span>
        <div class="chat-message-bubble">
          <p>{{ message.content }}</p>
          <time>{{ formatTime(message.createdAt) }}</time>
        </div>
      </div>
      <div ref="messagesEndRef" />
    </div>

    <form class="chat-input-area" @submit.prevent="handleSendMessage">
      <textarea
        v-model="messageInput"
        :disabled="!isConnected"
        :placeholder="isConnected ? '메시지를 입력하세요...' : '연결 중...'"
        rows="2"
        @keydown.enter.exact.prevent="handleSendMessage"
      />
      <button type="submit" :disabled="!isConnected || !messageInput.trim()">전송</button>
    </form>
  </section>
</template>
