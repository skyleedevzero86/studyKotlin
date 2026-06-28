<script setup lang="ts">
import { computed, nextTick, ref, watch } from 'vue'
import { getChatRoomMembers, getMessages, markChatRoomRead } from '../api/chatApi'
import { ApiError } from '../api/http'
import { addFriend, blockUser } from '../api/userApi'
import { useWebSocket } from '../composables/useWebSocket'
import type {
  ChatRoom,
  ChatRoomMember,
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
  read: [room: ChatRoom]
  relationshipChanged: []
}>()

const messages = ref<Message[]>([])
const members = ref<ChatRoomMember[]>([])
const messageInput = ref('')
const isLoadingMessages = ref(false)
const membersLoading = ref(false)
const showMembers = ref(false)
const actionUserId = ref<number | null>(null)
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
    emit('relationshipChanged')
  } catch (error) {
    emit('error', resolveError(error, '친구 추가에 실패했습니다'))
  } finally {
    actionUserId.value = null
  }
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
  return `멤버 ${props.chatRoom.memberCount}명 · ID: ${props.chatRoom.id}${
    props.chatRoom.description ? ` · ${props.chatRoom.description}` : ''
  }`
})

const formatTime = (dateString: string): string => {
  const date = new Date(dateString)
  return date.toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' })
}

watch(
  () => props.chatRoom.id,
  () => {
    showMembers.value = false
    members.value = []
    void loadMessages()
  },
  { immediate: true },
)

watch(showMembers, (open) => {
  if (open && members.value.length === 0) {
    void loadMembers()
  }
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
        <button type="button" class="secondary compact" @click="showMembers = !showMembers">
          참여자
        </button>
        <span class="connection-badge" :class="{ online: isConnected }">
          {{ isConnected ? '연결됨' : '연결 중...' }}
        </span>
      </div>
    </header>

    <div v-if="showMembers" class="member-panel">
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
          </div>
        </div>
      </div>
    </div>

    <div class="chat-messages">
      <p v-if="isLoadingMessages" class="chat-empty">메시지를 불러오는 중...</p>
      <p v-else-if="messages.length === 0" class="chat-empty">첫 번째 메시지를 보내보세요</p>

      <div
        v-for="message in messages"
        :key="message.id"
        class="chat-message-row"
        :class="{ own: message.sender.id === currentUserId }"
      >
        <span v-if="message.sender.id !== currentUserId" class="chat-message-author">
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
