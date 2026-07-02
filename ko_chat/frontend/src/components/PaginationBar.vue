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
    <button type="button" :disabled="!hasPrev" @click="emit('prev')">이전</button>
    <span class="pagination-info">{{ pageLabel }}</span>
    <button type="button" :disabled="!hasNext" @click="emit('next')">다음</button>
  </nav>
</template>

<style scoped src="../styles/components/PaginationBar.css"></style>
