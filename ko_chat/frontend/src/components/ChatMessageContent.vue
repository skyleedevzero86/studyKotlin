<script setup lang="ts">
import { computed } from 'vue'
import type { Message } from '../types/chat'

const props = defineProps<{
  message: Message
  token: string
}>()

const metadata = computed(() => props.message.metadata)

const isAttachment = computed(
  () =>
    (props.message.type === 'IMAGE' || props.message.type === 'FILE') &&
    Boolean(metadata.value),
)

const formatFileSize = (size?: number | null) => {
  if (!size) {
    return '-'
  }
  if (size < 1024) {
    return `${size}B`
  }
  if (size < 1024 * 1024) {
    return `${(size / 1024).toFixed(1)}KB`
  }
  return `${(size / (1024 * 1024)).toFixed(2)}MB`
}

const formatMessageDate = (value?: string | null) => {
  if (!value) {
    return '-'
  }
  return new Date(value).toLocaleString('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

const fileIcon = computed(() => {
  const mime = metadata.value?.mimeType ?? ''
  if (mime.startsWith('image/')) return '🖼️'
  if (mime.includes('pdf')) return '📕'
  if (mime.includes('zip') || mime.includes('compressed')) return '🗜️'
  if (mime.includes('sheet') || mime.includes('excel')) return '📊'
  if (mime.includes('word') || mime.includes('document')) return '📝'
  return '📄'
})

const downloadAttachment = () => {
  const url = metadata.value?.url
  if (!url) return

  const fileName = metadata.value?.fileName ?? 'download'
  const anchor = document.createElement('a')
  anchor.href = url
  anchor.download = fileName
  anchor.target = '_blank'
  anchor.rel = 'noopener noreferrer'
  document.body.appendChild(anchor)
  anchor.click()
  document.body.removeChild(anchor)
}

const openPreview = () => {
  if (metadata.value?.url) {
    window.open(metadata.value.url, '_blank', 'noopener,noreferrer')
  }
}
</script>

<template>
  <div class="chat-message-body">
    <template v-if="isAttachment">
      <div class="chat-attachment-bubble">
        <button
          v-if="message.type === 'IMAGE' && metadata?.url"
          type="button"
          class="chat-attachment-preview-btn"
          title="원본 보기"
          @click="openPreview"
        >
          <img
            :src="metadata.url"
            :alt="metadata.fileName ?? '이미지'"
            class="chat-attachment-image"
          />
        </button>

        <div v-else class="chat-attachment-file-preview">
          <span class="chat-attachment-file-icon" aria-hidden="true">{{ fileIcon }}</span>
        </div>

        <div class="chat-attachment-meta">
          <strong class="chat-attachment-name">{{ metadata?.fileName ?? '파일' }}</strong>
          <span class="chat-attachment-details">
            {{ formatMessageDate(message.createdAt) }} · {{ formatFileSize(metadata?.size) }}
          </span>
          <button
            type="button"
            class="chat-attachment-download-btn"
            :disabled="!metadata?.url"
            @click="downloadAttachment"
          >
            ↓ 다운로드
          </button>
        </div>
      </div>
    </template>

    <template v-else-if="message.type === 'LINK' && metadata">
      <a
        :href="metadata.linkUrl ?? message.content ?? '#'"
        target="_blank"
        rel="noopener noreferrer"
        class="chat-link-card"
      >
        <img
          v-if="metadata.imageUrl"
          :src="metadata.imageUrl"
          :alt="metadata.title ?? '링크 미리보기'"
          class="chat-link-thumb"
        />
        <div class="chat-link-body">
          <strong>{{ metadata.title ?? metadata.linkUrl }}</strong>
          <p v-if="metadata.description">{{ metadata.description }}</p>
          <span class="chat-link-domain">{{ metadata.domain ?? metadata.siteName }}</span>
        </div>
      </a>
    </template>

    <p v-else-if="message.content" class="chat-text-content">{{ message.content }}</p>
  </div>
</template>
