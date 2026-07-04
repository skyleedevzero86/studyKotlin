<script setup lang="ts">
defineProps<{
  page: number
  totalPages: number
  totalElements: number
  hasPrev: boolean
  hasNext: boolean
  pageLabel: string
}>()

const emit = defineEmits<{
  prev: []
  next: []
}>()
</script>

<template>
  <nav v-if="totalElements > 0" class="pagination-bar" aria-label="페이지 네비게이션">
    <button type="button" class="pagination-btn" :disabled="!hasPrev" aria-label="이전 페이지" @click="emit('prev')">
      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="15 18 9 12 15 6" /></svg>
    </button>
    <span class="pagination-info">{{ pageLabel }}</span>
    <button type="button" class="pagination-btn" :disabled="!hasNext" aria-label="다음 페이지" @click="emit('next')">
      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="9 6 15 12 9 18" /></svg>
    </button>
  </nav>
</template>

<style scoped src="../styles/components/PaginationBar.css"></style>
