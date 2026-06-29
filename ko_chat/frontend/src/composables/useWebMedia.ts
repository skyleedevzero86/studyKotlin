import { onBeforeUnmount, ref } from 'vue'
import { WebMediaClient } from '../lib/webmedia/WebMediaClient'
import { createWebMediaPublisher } from '../lib/webmedia/WebMediaPublisher'
import { createWebMediaSubscriber } from '../lib/webmedia/WebMediaSubscriber'
import type {
  WebMediaJoinResponse,
  WebMediaObjectMessageContainer,
  WebMediaUser,
} from '../types/webmedia'

interface UseWebMediaOptions {
  token: string
  chatRoomId: number
  currentUserId: number
  onError?: (message: string) => void
  onKicked?: (message: string) => void
}

type PeerSubscriber = ReturnType<typeof createWebMediaSubscriber>
type MediaPublisher = ReturnType<typeof createWebMediaPublisher>

const normalizeApiUrl = (url: string) => {
  if (import.meta.env.DEV && /localhost:1985|127\.0\.0\.1:1985/.test(url)) {
    return '/srs'
  }
  return url
}

export const useWebMedia = (options: UseWebMediaOptions) => {
  const isJoined = ref(false)
  const isPublishing = ref(false)
  const isConnecting = ref(false)
  const isMicMuted = ref(false)
  const isCameraOff = ref(false)
  const isScreenSharing = ref(false)
  const isAudioOnly = ref(false)
  const localStream = ref<MediaStream | null>(null)
  const peerStreams = ref<Record<string, MediaStream>>({})
  const otherUsers = ref<WebMediaUser[]>([])
  const maxMembers = ref(6)

  let client: WebMediaClient | null = null
  let publisher: MediaPublisher | null = null
  let apiUrl = ''
  let streamUrl = ''
  let mediaUserId = ''
  let micTrack: MediaStreamTrack | null = null
  let cameraTrack: MediaStreamTrack | null = null
  let screenTrack: MediaStreamTrack | null = null

  const peerSubscribers = new Map<string, PeerSubscriber>()

  const buildWsUrl = () => {
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
    return `${protocol}//${window.location.host}/api/v1/ws/webmedia?token=${encodeURIComponent(options.token)}`
  }

  const refreshLocalPreview = () => {
    const tracks: MediaStreamTrack[] = []
    if (micTrack) {
      tracks.push(micTrack)
    }
    const videoTrack = isScreenSharing.value ? screenTrack : cameraTrack
    if (videoTrack && !isCameraOff.value) {
      tracks.push(videoTrack)
    }
    if (tracks.length === 0) {
      localStream.value = null
      return
    }
    localStream.value = new MediaStream(tracks)
  }

  const stopTrack = (track: MediaStreamTrack | null) => {
    track?.stop()
  }

  const cleanupSubscriber = (peerUserId: string) => {
    const subscriber = peerSubscribers.get(peerUserId)
    if (subscriber) {
      subscriber.close()
      peerSubscribers.delete(peerUserId)
    }
    const next = { ...peerStreams.value }
    delete next[peerUserId]
    peerStreams.value = next
  }

  const subscribeToPeer = async (peerUserId: string) => {
    if (!client || peerSubscribers.has(peerUserId) || peerUserId === mediaUserId) {
      return
    }

    try {
      const subscriber = createWebMediaSubscriber(apiUrl, streamUrl)
      await subscriber.subscribe(String(options.chatRoomId), peerUserId)
      peerSubscribers.set(peerUserId, subscriber)
      peerStreams.value = { ...peerStreams.value, [peerUserId]: subscriber.stream }
    } catch {
      options.onError?.(`참가자(${peerUserId}) 영상 연결에 실패했습니다. SRS가 실행 중인지 확인하세요.`)
    }
  }

  const syncSubscriptions = async () => {
    for (const peer of otherUsers.value) {
      if (peer.published) {
        await subscribeToPeer(peer.userId)
      } else {
        cleanupSubscriber(peer.userId)
      }
    }
  }

  const handleEvent = async (container: WebMediaObjectMessageContainer) => {
    const { type, message } = container

    if (type === 'UserJoinedEvent') {
      const user = message.user as WebMediaUser
      if (user.userId === mediaUserId) {
        return
      }
      const exists = otherUsers.value.some((item) => item.userId === user.userId)
      otherUsers.value = exists
        ? otherUsers.value.map((item) => (item.userId === user.userId ? user : item))
        : [...otherUsers.value, user]
      if (user.published) {
        await subscribeToPeer(user.userId)
      }
      return
    }

    if (type === 'UserStateChangedEvent') {
      const userId = String(message.userId)
      const published = Boolean(message.published)
      otherUsers.value = otherUsers.value.map((item) =>
        item.userId === userId ? { ...item, published } : item,
      )
      if (published) {
        await subscribeToPeer(userId)
      } else {
        cleanupSubscriber(userId)
      }
      return
    }

    if (type === 'UserLeftEvent') {
      const userId = String(message.userId)
      otherUsers.value = otherUsers.value.filter((item) => item.userId !== userId)
      cleanupSubscriber(userId)
      return
    }

    if (type === 'UserKickedEvent') {
      const userId = String(message.userId)
      if (userId === mediaUserId) {
        disconnect()
        options.onKicked?.(String(message.message ?? '채팅방에서 내보졌습니다'))
      } else {
        otherUsers.value = otherUsers.value.filter((item) => item.userId !== userId)
        cleanupSubscriber(userId)
      }
    }
  }

  const acquireMedia = async (withVideo: boolean) => {
    const stream = await navigator.mediaDevices.getUserMedia({
      video: withVideo,
      audio: {
        echoCancellation: true,
        noiseSuppression: true,
      },
    })
    micTrack = stream.getAudioTracks()[0] ?? null
    if (withVideo) {
      cameraTrack = stream.getVideoTracks()[0] ?? null
    } else {
      stopTrack(cameraTrack)
      cameraTrack = null
    }
    if (micTrack) {
      micTrack.enabled = !isMicMuted.value
    }
    if (cameraTrack) {
      cameraTrack.enabled = !isCameraOff.value
    }
    return stream
  }

  const enableCamera = async () => {
    if (!isPublishing.value || !publisher || isScreenSharing.value) {
      return
    }
    if (cameraTrack) {
      isCameraOff.value = false
      cameraTrack.enabled = true
      isAudioOnly.value = false
      await publisher.replaceTrack('video', cameraTrack)
      refreshLocalPreview()
      return
    }

    try {
      const stream = await navigator.mediaDevices.getUserMedia({ video: true })
      cameraTrack = stream.getVideoTracks()[0] ?? null
      if (!cameraTrack) {
        throw new Error('카메라 트랙을 가져오지 못했습니다')
      }
      isCameraOff.value = false
      isAudioOnly.value = false
      await publisher.replaceTrack('video', cameraTrack)
      refreshLocalPreview()
    } catch {
      options.onError?.('카메라를 켜지 못했습니다. 브라우저 권한을 확인하세요.')
    }
  }

  const join = async () => {
    if (isConnecting.value || isJoined.value) {
      return
    }

    isConnecting.value = true
    try {
      client = new WebMediaClient(handleEvent)
      await client.connect(buildWsUrl(), String(options.chatRoomId))

      const response = (await client.sendMessage(
        { roomId: String(options.chatRoomId) },
        'JoinRequest',
        true,
      )) as WebMediaObjectMessageContainer

      if (response.type !== 'JoinResponse') {
        throw new Error('WebRTC 방 참여에 실패했습니다')
      }

      const payload = response.message as unknown as WebMediaJoinResponse
      apiUrl = normalizeApiUrl(payload.apiUrl)
      streamUrl = payload.streamUrl
      mediaUserId = payload.user.userId
      otherUsers.value = payload.otherUsers ?? []
      maxMembers.value = payload.maxMembers
      isJoined.value = true
      await syncSubscriptions()
    } catch (error) {
      disconnect()
      options.onError?.(error instanceof Error ? error.message : 'WebRTC 연결에 실패했습니다')
    } finally {
      isConnecting.value = false
    }
  }

  const startPublish = async (withVideo: boolean) => {
    if (!client || !isJoined.value || isPublishing.value) {
      return
    }

    try {
      publisher?.close()
      stopTrack(micTrack)
      stopTrack(cameraTrack)
      stopTrack(screenTrack)
      micTrack = null
      cameraTrack = null
      screenTrack = null
      isScreenSharing.value = false
      isAudioOnly.value = !withVideo
      isCameraOff.value = !withVideo

      const stream = await acquireMedia(withVideo)
      if (!stream.getAudioTracks()[0]) {
        throw new Error('마이크를 사용할 수 없습니다')
      }

      publisher = createWebMediaPublisher(apiUrl, streamUrl)
      await publisher.publish(stream, String(options.chatRoomId), mediaUserId)
      refreshLocalPreview()
      isPublishing.value = true

      await client.sendMessage({ published: true }, 'UserPublishedChangeReport', false)
      await syncSubscriptions()
    } catch {
      stopPublish()
      const message = withVideo
        ? '카메라/마이크를 시작하지 못했습니다. 브라우저 권한과 SRS 실행 여부를 확인하세요.'
        : '마이크를 시작하지 못했습니다. 브라우저 권한과 SRS 실행 여부를 확인하세요.'
      options.onError?.(message)
    }
  }

  const stopPublish = async () => {
    stopTrack(micTrack)
    stopTrack(cameraTrack)
    stopTrack(screenTrack)
    micTrack = null
    cameraTrack = null
    screenTrack = null
    isMicMuted.value = false
    isCameraOff.value = false
    isScreenSharing.value = false
    isAudioOnly.value = false
    localStream.value = null
    publisher?.close()
    publisher = null
    isPublishing.value = false

    if (client?.connected) {
      await client.sendMessage({ published: false }, 'UserPublishedChangeReport', false)
    }
  }

  const toggleMic = () => {
    if (!isPublishing.value || !micTrack) {
      return
    }
    isMicMuted.value = !isMicMuted.value
    micTrack.enabled = !isMicMuted.value
  }

  const toggleCamera = async () => {
    if (!isPublishing.value) {
      return
    }
    if (isScreenSharing.value) {
      options.onError?.('화면 공유 중에는 카메라를 끌 수 없습니다. 화면 공유를 먼저 종료하세요.')
      return
    }
    if (!cameraTrack || isCameraOff.value) {
      await enableCamera()
      return
    }
    isCameraOff.value = true
    cameraTrack.enabled = false
    await publisher?.replaceTrack('video', null)
    refreshLocalPreview()
  }

  const stopScreenShareInternal = async (restoreCamera: boolean) => {
    if (!isScreenSharing.value) {
      return
    }
    stopTrack(screenTrack)
    screenTrack = null
    isScreenSharing.value = false

    if (restoreCamera && isPublishing.value && cameraTrack) {
      try {
        if (!cameraTrack || cameraTrack.readyState === 'ended') {
          const stream = await navigator.mediaDevices.getUserMedia({ video: true })
          stopTrack(cameraTrack)
          cameraTrack = stream.getVideoTracks()[0] ?? null
        }
        if (cameraTrack) {
          cameraTrack.enabled = !isCameraOff.value
        }
        await publisher?.replaceTrack('video', isCameraOff.value ? null : cameraTrack)
      } catch {
        isCameraOff.value = true
        isAudioOnly.value = true
        await publisher?.replaceTrack('video', null)
        options.onError?.('카메라를 다시 켜지 못했습니다')
      }
    } else {
      await publisher?.replaceTrack('video', isCameraOff.value ? null : cameraTrack)
    }
    refreshLocalPreview()
  }

  const startScreenShare = async () => {
    if (!isPublishing.value || !publisher) {
      options.onError?.('화면 공유 전에 먼저 참여하세요')
      return
    }
    if (isScreenSharing.value) {
      return
    }

    try {
      const displayStream = await navigator.mediaDevices.getDisplayMedia({
        video: true,
        audio: false,
      })
      const nextScreenTrack = displayStream.getVideoTracks()[0]
      if (!nextScreenTrack) {
        throw new Error('화면 트랙을 가져오지 못했습니다')
      }

      screenTrack = nextScreenTrack
      isScreenSharing.value = true
      isCameraOff.value = false
      isAudioOnly.value = false
      screenTrack.onended = () => {
        void stopScreenShareInternal(true)
      }

      await publisher.replaceTrack('video', screenTrack)
      refreshLocalPreview()
    } catch (error) {
      if (error instanceof DOMException && error.name === 'NotAllowedError') {
        return
      }
      options.onError?.('화면 공유를 시작하지 못했습니다')
    }
  }

  const stopScreenShare = async () => {
    await stopScreenShareInternal(true)
  }

  const disconnect = () => {
    void stopPublish()
    peerSubscribers.forEach((subscriber) => subscriber.close())
    peerSubscribers.clear()
    peerStreams.value = {}
    otherUsers.value = []
    client?.close()
    client = null
    isJoined.value = false
    isConnecting.value = false
  }

  onBeforeUnmount(() => {
    disconnect()
  })

  return {
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
    enableCamera,
    toggleMic,
    toggleCamera,
    startScreenShare,
    stopScreenShare,
    disconnect,
  }
}
