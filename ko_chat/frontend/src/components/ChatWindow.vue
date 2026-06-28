<script setup lang="ts">
import { nextTick, ref, watch } from 'vue'
import { getMessages } from '../api/chatApi'
import { ApiError } from '../api/http'
import { useWebSocket } from '../composables/useWebSocket'
import type {
  ChatRoom,
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
}>()

const messages = ref<Message[]>([])
const messageInput = ref('')
const isLoadingMessages = ref(false)
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

const loadMessages = async () => {
  isLoadingMessages.value = true
  try {
    const response = await getMessages(props.token, props.chatRoom.id, 0, 50)
    const sorted = [...response.content].sort(
      (a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime(),
    )
    messages.value = sorted
    await scrollToBottom()
  } catch (error) {
    emit('error', resolveError(error, '메시지를 불러올 수 없습니다'))
  } finally {
    isLoadingMessages.value = false
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

  if (!isChatMessage(payload)) {
    return
  }

  if (payload.chatRoomId !== props.chatRoom.id) {
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

const formatTime = (dateString: string): string => {
  const date = new Date(dateString)
  return date.toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' })
}

watch(
  () => props.chatRoom.id,
  () => {
    void loadMessages()
  },
  { immediate: true },
)

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
        <h2>{{ chatRoom.name }}</h2>
        <p>
          멤버 {{ chatRoom.memberCount }}명 · ID: {{ chatRoom.id }}
          <span v-if="chatRoom.description"> · {{ chatRoom.description }}</span>
        </p>
      </div>
      <span class="chat-connection" :class="{ online: isConnected }">
        {{ isConnected ? '연결됨' : '연결 중...' }}
      </span>
    </header>

    <div class="chat-messages">
      <p v-if="isLoadingMessages" class="chat-empty">메시지를 불러오는 중...</p>
      <p v-else-if="messages.length === 0" class="chat-empty">첫 번째 메시지를 보내보세요!</p>

      <div
        v-for="message in messages"
        :key="message.id"
        class="chat-message-row"
        :class="{ own: message.sender.id === currentUserId }"
      >
        <span v-if="message.sender.id !== currentUserId" class="chat-sender">
          {{ message.sender.displayName ?? message.sender.username }}
        </span>
        <div class="chat-bubble">
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
