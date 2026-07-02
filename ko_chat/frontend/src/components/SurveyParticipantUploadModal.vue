<script setup lang="ts">
import { ref } from 'vue'
import type { ParticipantUploadResult } from '../types/survey'

const props = defineProps<{
  open: boolean
  uploading: boolean
  result: ParticipantUploadResult | null
}>()

const emit = defineEmits<{
  close: []
  upload: [file: File]
}>()

const fileInputRef = ref<HTMLInputElement | null>(null)
const selectedFile = ref<File | null>(null)

const onFileChange = (event: Event) => {
  const input = event.target as HTMLInputElement
  selectedFile.value = input.files?.[0] ?? null
}

const handleUpload = () => {
  if (!selectedFile.value) return
  emit('upload', selectedFile.value)
}

const handleClose = () => {
  selectedFile.value = null
  if (fileInputRef.value) fileInputRef.value.value = ''
  emit('close')
}
</script>

<template>
  <div v-if="open" class="modal-overlay" @click="handleClose">
    <div class="modal-card upload-modal" @click.stop>
      <h2>참여자 엑셀 업로드</h2>
      <p class="hint">
        첫 번째 열에 <strong>username</strong> 또는 <strong>userId</strong>를 입력하세요.
        첫 행이 헤더(username, 아이디)이면 자동으로 건너뜁니다.
      </p>
      <input
        ref="fileInputRef"
        type="file"
        accept=".xlsx,.xls"
        @change="onFileChange"
      />
      <div v-if="result" class="upload-result">
        <p>성공 {{ result.successCount }}건 / 실패 {{ result.failureCount }}건</p>
        <ul>
          <li v-for="row in result.rows" :key="`${row.row}-${row.identifier}`" :class="{ failed: !row.success }">
            {{ row.row }}행 {{ row.identifier }}: {{ row.message }}
          </li>
        </ul>
      </div>
      <div class="modal-actions">
        <button type="button" class="secondary" @click="handleClose">닫기</button>
        <button type="button" :disabled="!selectedFile || uploading" @click="handleUpload">
          {{ uploading ? '업로드 중...' : '업로드' }}
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped src="../styles/components/SurveyParticipantUploadModal.css"></style>
