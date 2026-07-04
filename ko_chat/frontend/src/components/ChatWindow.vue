<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, ref, watch } from 'vue'
import {
  fetchLinkPreview,
  getChatRoom,
  getChatRoomMembers,
  getMessages,
  inviteToChatRoom,
  kickChatRoomMember,
  leaveChatRoom,
  markChatRoomRead,
  updateChatRoomCapacity,
  updateChatRoomSettings,
  uploadChatAttachment,
} from '../api/chatApi'
import { ApiError } from '../api/http'
import { addFriend, blockUser, searchUsers } from '../api/userApi'
import { useWebSocket } from '../composables/useWebSocket'
import WebRtcPanel from './WebRtcPanel.vue'
import ChatMessageContent from './ChatMessageContent.vue'
import SurveyPanel from './SurveyPanel.vue'
import PaginationBar from './PaginationBar.vue'
import { usePagination } from '../composables/usePagination'
import type {
  ChatRoom,
  ChatRoomMember,
  ChatUser,
  IncomingWebSocketMessage,
  Message,
  MessageMetadata,
  MessageType,
  OutgoingWebSocketMessage,
} from '../types/chat'

const props = defineProps<{
  token: string
  chatRoom: ChatRoom
  currentUserId: number
  isAdmin?: boolean
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
const showMemberOverlay = ref(false)
const showRoomMenu = ref(false)
const showSurveyPanel = ref(false)
const memberPagination = usePagination(20)
const invitePagination = usePagination(10)
const memberSearchQuery = ref('')
const actionUserId = ref<number | null>(null)
const inviteQuery = ref('')
const inviteResults = ref<ChatUser[]>([])
const inviteLoading = ref(false)
const capacityInput = ref(props.chatRoom.maxMembers)
const capacityLoading = ref(false)
const showSettings = ref(false)
const settingsLoading = ref(false)
const leaveLoading = ref(false)
const roomClosed = ref(false)
const settingsName = ref(props.chatRoom.name)
const settingsDescription = ref(props.chatRoom.description ?? '')
const settingsPrivate = ref(props.chatRoom.isPrivate ?? false)
const settingsPassword = ref('')
const messagesEndRef = ref<HTMLElement | null>(null)
const fileInputRef = ref<HTMLInputElement | null>(null)
const uploadLoading = ref(false)
const showInputSubmenu = ref(false)
const inputToolsRef = ref<HTMLElement | null>(null)

const COMING_SOON_MESSAGE = '추후 서비스할 예정입니다'

const inputSubmenuItems = [
  { id: 'capture', label: '캡처', hasSubmenu: true },
  { id: 'schedule-message', label: '메시지 예약' },
  { id: 'spell-translate', label: '맞춤법/번역' },
  { id: 'calendar', label: '일정' },
  { id: 'todo', label: '할 일' },
  { id: 'minigame', label: '미니게임' },
] as const

const URL_ONLY_REGEX = /^https?:\/\/[^\s<>"']+$/i

const compareMessagesBySequence = (a: Message, b: Message) => {
  if (a.sequenceNumber !== b.sequenceNumber) {
    return a.sequenceNumber - b.sequenceNumber
  }
  return a.id - b.id
}

const sortMessagesBySequence = (list: Message[]) =>
  [...list].sort(compareMessagesBySequence)

const upsertMessageBySequence = (list: Message[], incoming: Message): Message[] => {
  if (list.some((message) => message.id === incoming.id)) {
    return list
  }
  return sortMessagesBySequence([...list, incoming])
}

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
    messages.value = sortMessagesBySequence(response.content)
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
    const page = await getChatRoomMembers(
      props.token,
      props.chatRoom.id,
      memberPagination.page.value,
      memberPagination.size.value,
    )
    members.value = page.content
    memberPagination.applyPageResponse(page)
  } catch {
    members.value = []
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
    const page = await searchUsers(
      props.token,
      inviteQuery.value,
      invitePagination.page.value,
      invitePagination.size.value,
    )
    const activeMemberIds = new Set(members.value.map((member) => member.user.id))
    inviteResults.value = page.content.filter(
      (user) => user.id !== props.currentUserId && !activeMemberIds.has(user.id),
    )
    invitePagination.applyPageResponse(page)
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
  messageType?: MessageType
  metadata?: string | MessageMetadata | null
} => {
  return (
    typeof payload === 'object' &&
    payload !== null &&
    'senderId' in payload &&
    typeof payload.senderId === 'number' &&
    'chatRoomId' in payload &&
    typeof payload.chatRoomId === 'number' &&
    'id' in payload &&
    'sequenceNumber' in payload &&
    typeof payload.sequenceNumber === 'number'
  )
}

const parseMetadata = (raw: unknown): MessageMetadata | null => {
  if (!raw) {
    return null
  }
  if (typeof raw === 'object') {
    return raw as MessageMetadata
  }
  if (typeof raw === 'string') {
    try {
      return JSON.parse(raw) as MessageMetadata
    } catch {
      return null
    }
  }
  return null
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
    type: payload.messageType ?? 'TEXT',
    content: payload.content ?? null,
    metadata: parseMetadata(payload.metadata),
    sequenceNumber: payload.sequenceNumber,
    isEdited: false,
    isDeleted: false,
    createdAt: new Date(payload.timestamp).toISOString(),
  }

  messages.value = upsertMessageBySequence(messages.value, newMessage)
  void scrollToBottom()
  void markRead()

  if (newMessage.type === 'SYSTEM' && newMessage.content?.includes('종료되었습니다')) {
    roomClosed.value = true
  }
}

const { isConnected, sendMessage, error: wsError } = useWebSocket({
  token: props.token,
  onMessage: handleIncomingMessage,
  onError: (message) => emit('error', message),
})

const sendRichMessage = (
  messageType: MessageType,
  content: string | null,
  metadata?: MessageMetadata | null,
) => {
  const wsMessage: OutgoingWebSocketMessage = {
    type: 'SEND_MESSAGE',
    chatRoomId: props.chatRoom.id,
    messageType,
    content,
    metadata: metadata ? JSON.stringify(metadata) : null,
  }
  return sendMessage(wsMessage)
}

const handleSendMessage = async () => {
  const content = messageInput.value.trim()
  if (!content || !isConnected.value || uploadLoading.value) {
    return
  }

  if (URL_ONLY_REGEX.test(content)) {
    try {
      const preview = await fetchLinkPreview(props.token, content)
      if (sendRichMessage('LINK', preview.linkUrl ?? content, preview)) {
        messageInput.value = ''
      }
      return
    } catch (error) {
      emit('error', resolveError(error, '링크 미리보기를 가져오지 못했습니다'))
      return
    }
  }

  if (sendRichMessage('TEXT', content)) {
    messageInput.value = ''
  }
}

const openFilePicker = () => {
  showInputSubmenu.value = false
  fileInputRef.value?.click()
}

const toggleInputSubmenu = () => {
  showInputSubmenu.value = !showInputSubmenu.value
}

const handleComingSoon = () => {
  showInputSubmenu.value = false
  emit('notice', COMING_SOON_MESSAGE)
}

const onInputToolsOutsideClick = (event: MouseEvent) => {
  if (!showInputSubmenu.value) {
    return
  }
  const root = inputToolsRef.value
  if (root && !root.contains(event.target as Node)) {
    showInputSubmenu.value = false
  }
}

const handleFileSelected = async (event: Event) => {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  input.value = ''
  if (!file) {
    return
  }
  if (!isConnected.value) {
    emit('error', '서버에 연결되어 있지 않습니다. 잠시 후 다시 시도해 주세요.')
    return
  }

  uploadLoading.value = true
  try {
    const uploaded = await uploadChatAttachment(props.token, props.chatRoom.id, file)
    if (!sendRichMessage(uploaded.messageType, uploaded.content, uploaded.metadata)) {
      emit('error', '파일이 업로드되었지만 메시지 전송에 실패했습니다. 다시 시도해 주세요.')
    }
  } catch (error) {
    emit('error', resolveError(error, '파일 업로드에 실패했습니다'))
  } finally {
    uploadLoading.value = false
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

const canLeaveRoom = computed(() => true)

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

const canManageSurvey = computed(
  () => canManageRoom.value || props.isAdmin === true,
)

const formatTime = (dateString: string): string => {
  const date = new Date(dateString)
  return date.toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit', hour12: true })
}

const memberCount = computed(() => props.chatRoom.memberCount)

const filteredMembers = computed(() => {
  const query = memberSearchQuery.value.trim().toLowerCase()
  if (!query) {
    return members.value
  }
  return members.value.filter((member) => {
    const name = member.user.displayName ?? member.user.username
    return (
      name.toLowerCase().includes(query) ||
      member.user.username.toLowerCase().includes(query)
    )
  })
})

const avatarLabel = (user: ChatUser) => {
  const name = user.displayName ?? user.username
  return name.slice(0, 1).toUpperCase()
}

const avatarColor = (userId: number) => {
  const palette = ['#5b8def', '#f59e0b', '#10b981', '#ef4444', '#8b5cf6', '#ec4899']
  return palette[userId % palette.length]
}

const toggleMemberOverlay = async () => {
  showMemberOverlay.value = !showMemberOverlay.value
  showRoomMenu.value = false
  if (showMemberOverlay.value && members.value.length === 0) {
    await loadMembers()
  }
  if (!showMemberOverlay.value) {
    memberSearchQuery.value = ''
  }
}

const closeMemberOverlay = () => {
  showMemberOverlay.value = false
  memberSearchQuery.value = ''
}

const toggleRoomMenu = () => {
  showRoomMenu.value = !showRoomMenu.value
  showMemberOverlay.value = false
}

const openSurveyPanel = () => {
  showRoomMenu.value = false
  showSurveyPanel.value = true
}

const closeSurveyPanel = () => {
  showSurveyPanel.value = false
}

const openManagePanel = async () => {
  showRoomMenu.value = false
  showMembers.value = true
  if (members.value.length === 0) {
    await loadMembers()
  }
}

const copyRoomUrl = async () => {
  const url = `${window.location.origin}/?room=${props.chatRoom.id}`
  try {
    if (navigator.clipboard && window.isSecureContext) {
      await navigator.clipboard.writeText(url)
    } else {
      const textarea = document.createElement('textarea')
      textarea.value = url
      textarea.style.position = 'fixed'
      textarea.style.opacity = '0'
      document.body.appendChild(textarea)
      textarea.select()
      document.execCommand('copy')
      document.body.removeChild(textarea)
    }
    emit('notice', '채팅방 URL을 복사했습니다')
  } catch {
    emit('error', 'URL 복사에 실패했습니다')
  }
}

watch(
  () => props.chatRoom.id,
  () => {
    showMembers.value = false
    showMemberOverlay.value = false
    showRoomMenu.value = false
    showSurveyPanel.value = false
    memberPagination.resetPage()
    invitePagination.resetPage()
    showInputSubmenu.value = false
    memberSearchQuery.value = ''
    members.value = []
    inviteQuery.value = ''
    inviteResults.value = []
    capacityInput.value = props.chatRoom.maxMembers
    settingsName.value = props.chatRoom.name
    settingsDescription.value = props.chatRoom.description ?? ''
    settingsPrivate.value = props.chatRoom.isPrivate ?? false
    settingsPassword.value = ''
    showSettings.value = false
    roomClosed.value = false
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
  invitePagination.resetPage()
  clearTimeout(inviteSearchDebounce)
  inviteSearchDebounce = setTimeout(() => {
    void loadInviteCandidates()
  }, 300)
})

watch(() => memberPagination.page.value, () => {
  if (showMembers.value || showMemberOverlay.value) {
    void loadMembers()
  }
})

watch(() => invitePagination.page.value, () => {
  if (inviteQuery.value.trim() && showMembers.value && canManageRoom.value) {
    void loadInviteCandidates()
  }
})

watch(wsError, (value) => {
  if (value) {
    emit('error', value)
  }
})

watch(showInputSubmenu, (open) => {
  if (open) {
    document.addEventListener('click', onInputToolsOutsideClick, true)
  } else {
    document.removeEventListener('click', onInputToolsOutsideClick, true)
  }
})

onBeforeUnmount(() => {
  document.removeEventListener('click', onInputToolsOutsideClick, true)
})
</script>

<template>
  <div class="chat-window-layout">
  <section class="chat-window" :class="{ 'with-survey': showSurveyPanel }">
    <header class="sleekydz86-chat-header">
      <div class="sleekydz86-chat-header-main">
        <h2 class="sleekydz86-chat-title">{{ roomTitle }}</h2>
        <button type="button" class="sleekydz86-member-trigger" @click="toggleMemberOverlay">
          <span class="sleekydz86-member-icon" aria-hidden="true">👤</span>
          <span>{{ memberCount }}</span>
        </button>
      </div>
      <div class="sleekydz86-chat-header-actions">
        <span v-if="!showRoomMenu" class="sleekydz86-connection" :class="{ online: isConnected }" :title="isConnected ? '연결됨' : '연결 중'" />
        <button type="button" class="sleekydz86-header-btn" title="참여자" @click="toggleMemberOverlay">
          🔍
        </button>
        <button type="button" class="sleekydz86-header-btn" title="메뉴" @click="toggleRoomMenu">
          ☰
        </button>
        <button type="button" class="sleekydz86-header-btn" title="채팅방 나가기" @click="handleLeaveRoom">
          ✕
        </button>
      </div>
      <div v-if="showRoomMenu" class="sleekydz86-room-menu">
        <button v-if="canManageRoom" type="button" @click="openManagePanel">방 관리</button>
        <button v-if="canManageRoom" type="button" @click="showSettings = !showSettings; showRoomMenu = false">
          설정
        </button>
        <button type="button" @click="openSurveyPanel">설문조사</button>
        <button v-if="canLeaveRoom" type="button" class="danger" :disabled="leaveLoading" @click="handleLeaveRoom">
          {{ leaveLoading ? '처리 중...' : (canManageRoom ? '채팅방 종료' : '채팅방 나가기') }}
        </button>
      </div>
    </header>

    <div v-if="showMemberOverlay" class="sleekydz86-member-overlay">
      <div class="sleekydz86-member-backdrop" @click="closeMemberOverlay" />
      <div class="sleekydz86-member-sheet">
        <div class="sleekydz86-member-sheet-header">
          <input
            v-model="memberSearchQuery"
            type="search"
            class="sleekydz86-member-search"
            placeholder="이름으로 검색"
          />
          <button type="button" class="sleekydz86-member-close" @click="closeMemberOverlay">✕</button>
        </div>
        <p v-if="membersLoading" class="chat-empty slim">참여자를 불러오는 중...</p>
        <ul v-else class="sleekydz86-member-sheet-list">
          <li v-for="member in filteredMembers" :key="member.id" class="sleekydz86-member-sheet-item">
            <span
              class="sleekydz86-avatar"
              :style="{ backgroundColor: avatarColor(member.user.id) }"
            >
              {{ avatarLabel(member.user) }}
            </span>
            <div class="sleekydz86-member-sheet-info">
              <strong>{{ member.user.displayName ?? member.user.username }}</strong>
              <span>@{{ member.user.username }}</span>
            </div>
            <span v-if="member.role === 'OWNER'" class="sleekydz86-member-badge">방장</span>
          </li>
          <li v-if="filteredMembers.length === 0" class="sleekydz86-member-empty">검색 결과가 없습니다</li>
        </ul>
        <button type="button" class="sleekydz86-copy-url" @click="copyRoomUrl">
          🔗 채팅방 URL 복사
        </button>
      </div>
    </div>

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
          <PaginationBar
            v-if="inviteResults.length || invitePagination.totalElements.value > 0"
            :page="invitePagination.page.value"
            :total-pages="invitePagination.totalPages.value"
            :total-elements="invitePagination.totalElements.value"
            :has-prev="invitePagination.hasPrev.value"
            :has-next="invitePagination.hasNext.value"
            :page-label="invitePagination.pageLabel.value"
            @prev="invitePagination.goPrev()"
            @next="invitePagination.goNext()"
          />
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
        <PaginationBar
          v-if="members.length || memberPagination.totalElements.value > 0"
          :page="memberPagination.page.value"
          :total-pages="memberPagination.totalPages.value"
          :total-elements="memberPagination.totalElements.value"
          :has-prev="memberPagination.hasPrev.value"
          :has-next="memberPagination.hasNext.value"
          :page-label="memberPagination.pageLabel.value"
          @prev="memberPagination.goPrev()"
          @next="memberPagination.goNext()"
        />
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

    <div class="chat-messages sleekydz86-chat-messages">
      <p v-if="isLoadingMessages" class="chat-empty sleekydz86-chat-empty">메시지를 불러오는 중...</p>
      <p v-else-if="messages.length === 0" class="chat-empty sleekydz86-chat-empty">첫 번째 메시지를 보내보세요</p>

      <template v-for="message in messages" :key="message.id">
        <div v-if="message.type === 'SYSTEM'" class="chat-message-row system">
          <div class="chat-message-bubble system">
            <ChatMessageContent :message="message" :token="token" />
          </div>
        </div>

        <div v-else-if="message.sender.id === currentUserId" class="chat-message-row own">
          <div class="chat-message-meta own-meta">
            <time>{{ formatTime(message.createdAt) }}</time>
          </div>
          <div class="chat-message-bubble" :class="message.type.toLowerCase()">
            <ChatMessageContent :message="message" :token="token" />
          </div>
        </div>

        <div v-else class="chat-message-row other">
          <span
            class="sleekydz86-avatar message-avatar"
            :style="{ backgroundColor: avatarColor(message.sender.id) }"
          >
            {{ avatarLabel(message.sender) }}
          </span>
          <div class="chat-message-other-wrap">
            <span class="chat-message-author">
              {{ message.sender.displayName ?? message.sender.username }}
            </span>
            <div class="chat-message-line">
              <div class="chat-message-bubble" :class="message.type.toLowerCase()">
                <ChatMessageContent :message="message" :token="token" />
              </div>
              <div class="chat-message-meta other-meta">
                <time>{{ formatTime(message.createdAt) }}</time>
              </div>
            </div>
          </div>
        </div>
      </template>
      <div ref="messagesEndRef" />
    </div>

    <div v-if="!chatRoom.isActive || roomClosed" class="sleekydz86-room-closed">
      <p>이 채팅방은 종료되었습니다.</p>
      <button type="button" class="sleekydz86-room-closed-btn" @click="handleLeaveRoom" :disabled="leaveLoading">
        {{ leaveLoading ? '처리 중...' : '채팅방 나가기' }}
      </button>
    </div>

    <form v-else class="sleekydz86-input-area" @submit.prevent="handleSendMessage">
      <textarea
        v-model="messageInput"
        class="sleekydz86-input-textarea"
        :disabled="!isConnected || uploadLoading"
        :placeholder="uploadLoading ? '업로드 중...' : isConnected ? '메시지 입력' : '연결 중...'"
        rows="3"
        @keydown.enter.exact.prevent="handleSendMessage"
      />
      <div class="sleekydz86-input-footer">
        <div ref="inputToolsRef" class="sleekydz86-input-tools">
          <div class="sleekydz86-input-tool-wrap">
            <button
              type="button"
              class="sleekydz86-input-tool"
              :class="{ active: showInputSubmenu }"
              :disabled="!isConnected || uploadLoading"
              @click.stop="toggleInputSubmenu"
            >
              <svg class="sleekydz86-tool-icon" viewBox="0 0 24 24" aria-hidden="true">
                <path d="M12 5v14M5 12h14" stroke="currentColor" stroke-width="2" stroke-linecap="round" />
              </svg>
            </button>
            <div v-if="showInputSubmenu" class="sleekydz86-input-submenu" @click.stop>
              <button
                v-for="item in inputSubmenuItems"
                :key="item.id"
                type="button"
                class="sleekydz86-input-submenu-item"
                @click="handleComingSoon"
              >
                <span class="sleekydz86-submenu-icon" :data-icon="item.id" aria-hidden="true" />
                <span class="sleekydz86-submenu-label">{{ item.label }}</span>
                <span v-if="'hasSubmenu' in item && item.hasSubmenu" class="sleekydz86-submenu-arrow">›</span>
              </button>
            </div>
          </div>
          <button
            type="button"
            class="sleekydz86-input-tool"
            :disabled="!isConnected || uploadLoading"
            @click="handleComingSoon"
          >
            <svg class="sleekydz86-tool-icon" viewBox="0 0 24 24" aria-hidden="true">
              <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="1.8" fill="none" />
              <path d="M8.5 10.5a3.5 3.5 0 0 1 6.3 1.5c0 2-2 2.5-3.15 3.2-.6.4-.65.9-.65 1.3" stroke="currentColor" stroke-width="1.6" fill="none" stroke-linecap="round" />
              <circle cx="12" cy="17.2" r="0.8" fill="currentColor" />
            </svg>
          </button>
          <button
            type="button"
            class="sleekydz86-input-tool"
            :disabled="!isConnected || uploadLoading"
            @click="openFilePicker"
          >
            <svg class="sleekydz86-tool-icon" viewBox="0 0 24 24" aria-hidden="true">
              <path d="M8 4h8l2 4v12a1 1 0 0 1-1 1H7a1 1 0 0 1-1-1V5a1 1 0 0 1 1-1z" stroke="currentColor" stroke-width="1.8" fill="none" />
              <path d="M9 4v4h8" stroke="currentColor" stroke-width="1.8" fill="none" />
            </svg>
          </button>
          <input ref="fileInputRef" type="file" class="chat-file-input" @change="handleFileSelected" />
        </div>
        <button
          type="submit"
          class="sleekydz86-send-btn"
          :disabled="!isConnected || uploadLoading || !messageInput.trim()"
        >
          전송
        </button>
      </div>
    </form>
  </section>
  <aside v-if="showSurveyPanel" class="chat-survey-aside">
    <SurveyPanel
      :token="token"
      :room-id="chatRoom.id"
      :can-manage="canManageSurvey"
      :current-user-id="currentUserId"
      @close="closeSurveyPanel"
      @notice="emit('notice', $event)"
      @error="emit('error', $event)"
    />
  </aside>
  </div>
</template>

<style scoped src="../styles/components/ChatWindow.css"></style>
