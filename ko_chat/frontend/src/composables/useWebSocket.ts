import { onBeforeUnmount, onMounted, ref } from 'vue'
import type { IncomingWebSocketMessage, OutgoingWebSocketMessage } from '../types/chat'

interface UseWebSocketOptions {
  token: string
  onMessage?: (message: IncomingWebSocketMessage) => void
  onConnect?: () => void
  onDisconnect?: () => void
  onError?: (error: string) => void
}

export const useWebSocket = (options: UseWebSocketOptions) => {
  const isConnected = ref(false)
  const lastMessage = ref<IncomingWebSocketMessage | null>(null)
  const error = ref<string | null>(null)

  let ws: WebSocket | null = null
  let reconnectAttempts = 0
  let reconnectTimeout: ReturnType<typeof setTimeout> | null = null
  const maxReconnectAttempts = 5

  const clearReconnectTimeout = () => {
    if (reconnectTimeout) {
      clearTimeout(reconnectTimeout)
      reconnectTimeout = null
    }
  }

  const connect = () => {
    if (!options.token) {
      return
    }

    if (ws?.readyState === WebSocket.OPEN) {
      return
    }

    if (ws) {
      ws.close()
      ws = null
    }

    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
    const wsUrl = `${protocol}//${window.location.host}/api/v1/ws/chat?token=${encodeURIComponent(options.token)}`

    try {
      ws = new WebSocket(wsUrl)

      ws.onopen = () => {
        isConnected.value = true
        error.value = null
        reconnectAttempts = 0
        options.onConnect?.()
      }

      ws.onmessage = (event) => {
        try {
          const message = JSON.parse(event.data) as IncomingWebSocketMessage
          lastMessage.value = message
          options.onMessage?.(message)
        } catch {
          error.value = '메시지 파싱 실패'
        }
      }

      ws.onclose = (event) => {
        isConnected.value = false
        ws = null
        options.onDisconnect?.()

        if (
          event.code !== 1000 &&
          event.code !== 1001 &&
          reconnectAttempts < maxReconnectAttempts
        ) {
          const delay = Math.min(1000 * 2 ** reconnectAttempts, 30000)
          clearReconnectTimeout()
          reconnectTimeout = setTimeout(() => {
            reconnectAttempts += 1
            connect()
          }, delay)
        } else if (reconnectAttempts >= maxReconnectAttempts) {
          error.value = '최대 재연결 시도 횟수를 초과했습니다.'
          options.onError?.(error.value)
        }
      }

      ws.onerror = () => {
        error.value = 'WebSocket 연결 오류'
        isConnected.value = false
        options.onError?.(error.value)
      }
    } catch {
      error.value = 'WebSocket 연결 생성 실패'
    }
  }

  const disconnect = () => {
    reconnectAttempts = 0
    clearReconnectTimeout()
    if (ws) {
      ws.close(1000, 'User disconnected')
      ws = null
    }
    isConnected.value = false
  }

  const sendMessage = (message: OutgoingWebSocketMessage): boolean => {
    if (ws?.readyState === WebSocket.OPEN) {
      try {
        ws.send(JSON.stringify(message))
        return true
      } catch {
        error.value = '메시지 전송 실패'
        return false
      }
    }
    error.value = 'WebSocket이 연결되지 않았습니다'
    return false
  }

  onMounted(() => {
    connect()
  })

  onBeforeUnmount(() => {
    disconnect()
  })

  return {
    isConnected,
    lastMessage,
    error,
    connect,
    disconnect,
    sendMessage,
  }
}
