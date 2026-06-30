<script setup lang="ts">
import { computed } from 'vue'
import type { Message } from '../types/chat'

const props = defineProps<{
  message: Message
  token: string
}>()

const metadata = computed(() => props.message.metadata)

const formatFileSize = (size?: number | null) => {
  if (!size) {
    return '-'
  }
  if (size < 1024 * 1024) {
    return `${(size / 1024).toFixed(1)}KB`
  }
  return `${(size / (1024 * 1024)).toFixed(2)}MB`
}

const formatExpiry = (value?: string | null) => {
  if (!value) {
    return null
  }
  return new Date(value).toLocaleDateString('ko-KR')
}

const openFile = () => {
  if (metadata.value?.url) {
    window.open(metadata.value.url, '_blank', 'noopener,noreferrer')
  }
}
</script>

<template>
  <div class="chat-message-body">
    <template v-if="message.type === 'IMAGE' && metadata?.url">
      <a :href="metadata.url" target="_blank" rel="noopener noreferrer" class="chat-image-link">
        <img :src="metadata.url" :alt="metadata.fileName ?? '이미지'" class="chat-image-preview" />
      </a>
    </template>

    <template v-else-if="message.type === 'FILE' && metadata">
      <div class="chat-file-card">
        <div class="chat-file-card-top">
          <div class="chat-file-icon">📄</div>
          <div class="chat-file-info">
            <strong>{{ metadata.fileName }}</strong>
            <span v-if="formatExpiry(metadata.expiresAt)">
              유효기간: ~{{ formatExpiry(metadata.expiresAt) }}
            </span>
            <span>용량: {{ formatFileSize(metadata.size) }}</span>
          </div>
          <button type="button" class="chat-file-download" title="다운로드" @click="openFile">↓</button>
        </div>
        <div class="chat-file-actions">
          <button type="button" @click="openFile">저장</button>
          <button type="button" @click="openFile">다른 이름으로 저장</button>
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
