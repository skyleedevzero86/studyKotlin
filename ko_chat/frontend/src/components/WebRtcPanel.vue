<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useWebMedia } from '../composables/useWebMedia'
import type { ChatRoomMember } from '../types/chat'

const props = defineProps<{
  token: string
  chatRoomId: number
  currentUserId: number
  members?: ChatRoomMember[]
}>()

const emit = defineEmits<{
  error: [message: string]
  kicked: [message: string]
}>()

const localVideoRef = ref<HTMLVideoElement | null>(null)
const peerVideoRefs = ref<Record<string, HTMLVideoElement | null>>({})
const showJoinChoice = ref(false)

const {
  isJoined,
  isPublishing,
  isConnecting,
  isMicMuted,
  isCameraOff,
  isScreenSharing,
  isAudioOnly,
  localStream,
  peerStreams,
  otherUsers,
  maxMembers,
  join,
  startPublish,
  stopPublish,
  toggleMic,
  toggleCamera,
  startScreenShare,
  stopScreenShare,
} = useWebMedia({
  token: props.token,
  chatRoomId: props.chatRoomId,
  currentUserId: props.currentUserId,
  onKicked: (message) => emit('kicked', message),
  onError: (message) => emit('error', message),
})

const memberNameById = computed(() => {
  const map = new Map<string, string>()
  for (const member of props.members ?? []) {
    map.set(String(member.user.id), member.user.displayName ?? member.user.username)
  }
  return map
})

const resolveName = (userId: string) => memberNameById.value.get(userId) ?? `참가자 ${userId}`

const connectionLabel = computed(() => {
  if (isConnecting.value) {
    return '연결 중...'
  }
  if (!isJoined.value) {
    return '시그널링 대기'
  }
  if (isPublishing.value) {
    return isAudioOnly.value && !isScreenSharing.value ? '음성 송출 중' : '송출 중'
  }
  return '대기 중'
})

const localPlaceholder = computed(() => {
  if (!isPublishing.value) {
    return '대기 중'
  }
  if (isAudioOnly.value && !isScreenSharing.value) {
    return '음성 참여 중'
  }
  if (isCameraOff.value && !isScreenSharing.value) {
    return '카메라 꺼짐'
  }
  return '대기 중'
})

const cameraButtonLabel = computed(() => {
  if (isScreenSharing.value) {
    return '카메라'
  }
  if (isCameraOff.value || isAudioOnly.value) {
    return '카메라 켜기'
  }
  return '카메라 끄기'
})

const setPeerVideoRef = (userId: string, element: unknown) => {
  peerVideoRefs.value[userId] = element instanceof HTMLVideoElement ? element : null
}

const openJoinChoice = () => {
  showJoinChoice.value = true
}

const closeJoinChoice = () => {
  showJoinChoice.value = false
}

const handleJoinWithAudio = async () => {
  closeJoinChoice()
  await startPublish(false)
}

const handleJoinWithVideo = async () => {
  closeJoinChoice()
  await startPublish(true)
}

watch(localStream, (stream) => {
  if (localVideoRef.value) {
    localVideoRef.value.srcObject = stream
  }
})

watch(
  peerStreams,
  (streams) => {
    Object.entries(streams).forEach(([userId, stream]) => {
      const element = peerVideoRefs.value[userId]
      if (element) {
        element.srcObject = stream
      }
    })
  },
  { deep: true },
)

watch(isPublishing, (publishing) => {
  if (publishing) {
    closeJoinChoice()
  }
})

onMounted(() => {
  void join()
})
</script>

<template>
  <section class="webrtc-panel">
    <div class="webrtc-panel-header">
      <div class="webrtc-title-block">
        <h3>WebRTC 화상</h3>
        <span class="webrtc-meta">
          참여 {{ otherUsers.length + (isPublishing ? 1 : 0) }} / {{ maxMembers }}명 · {{ connectionLabel }}
        </span>
      </div>
    </div>

    <div class="webrtc-grid">
      <div
        class="webrtc-tile"
        :class="{
          muted: isMicMuted,
          'camera-off': (isCameraOff || isAudioOnly) && !isScreenSharing,
          'audio-only': isAudioOnly && !isScreenSharing,
        }"
      >
        <video
          v-show="localStream && (!isCameraOff || isScreenSharing) && !isAudioOnly"
          ref="localVideoRef"
          autoplay
          playsinline
          muted
          class="webrtc-video"
        />
        <div
          v-if="!localStream || ((isCameraOff || isAudioOnly) && !isScreenSharing)"
          class="webrtc-placeholder"
        >
          <span>{{ localPlaceholder }}</span>
        </div>
        <div class="webrtc-badges">
          <span v-if="isAudioOnly && !isScreenSharing" class="webrtc-badge accent">음성만</span>
          <span v-if="isMicMuted" class="webrtc-badge">음소거</span>
          <span v-if="isScreenSharing" class="webrtc-badge accent">화면 공유</span>
        </div>
        <span class="webrtc-label">나</span>
      </div>

      <div
        v-for="peer in otherUsers"
        :key="peer.userId"
        class="webrtc-tile"
        :class="{ waiting: !peer.published }"
      >
        <video
          v-show="peer.published && peerStreams[peer.userId]"
          :ref="(el) => setPeerVideoRef(peer.userId, el)"
          autoplay
          playsinline
          class="webrtc-video"
        />
        <div v-if="!peer.published || !peerStreams[peer.userId]" class="webrtc-placeholder">
          <span>{{ peer.published ? '연결 중...' : '대기 중' }}</span>
        </div>
        <span class="webrtc-label">{{ resolveName(peer.userId) }}</span>
      </div>
    </div>

    <div class="webrtc-toolbar">
      <template v-if="!isPublishing">
        <button
          type="button"
          class="webrtc-tool primary"
          :disabled="!isJoined || isConnecting"
          @click="openJoinChoice"
        >
          {{ isConnecting ? '연결 중...' : '참여하기' }}
        </button>
      </template>
      <template v-else>
        <button
          type="button"
          class="webrtc-tool"
          :class="{ active: isMicMuted }"
          :title="isMicMuted ? '마이크 켜기' : '마이크 끄기'"
          @click="toggleMic"
        >
          {{ isMicMuted ? '마이크 꺼짐' : '마이크' }}
        </button>
        <button
          type="button"
          class="webrtc-tool"
          :class="{ active: (isCameraOff || isAudioOnly) && !isScreenSharing }"
          :title="cameraButtonLabel"
          :disabled="isScreenSharing"
          @click="toggleCamera"
        >
          {{ cameraButtonLabel }}
        </button>
        <button
          v-if="!isScreenSharing"
          type="button"
          class="webrtc-tool"
          title="화면 공유"
          @click="startScreenShare"
        >
          화면 공유
        </button>
        <button
          v-else
          type="button"
          class="webrtc-tool active"
          title="화면 공유 종료"
          @click="stopScreenShare"
        >
          공유 종료
        </button>
        <button type="button" class="webrtc-tool danger" @click="stopPublish">영상 종료</button>
      </template>
    </div>

    <div v-if="showJoinChoice" class="webrtc-join-choice">
      <p class="webrtc-join-choice-title">어떻게 참여할까요?</p>
      <div class="webrtc-join-choice-actions">
        <button type="button" class="webrtc-join-option" @click="handleJoinWithAudio">
          <strong>음성만</strong>
          <span>마이크만 사용 (카메라 권한 없음)</span>
        </button>
        <button type="button" class="webrtc-join-option primary" @click="handleJoinWithVideo">
          <strong>카메라 + 음성</strong>
          <span>화상 통화로 참여</span>
        </button>
      </div>
      <button type="button" class="webrtc-join-cancel" @click="closeJoinChoice">취소</button>
    </div>

  </section>
</template>
