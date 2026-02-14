<script setup lang="ts">
defineProps<{
  currentPage: number
  totalPages: number
  totalElements: number
  size: number
  isFirst: boolean
  isLast: boolean
}>()

const emit = defineEmits<{ 'go-page': [page: number] }>()

function range(start: number, end: number): number[] {
  const r: number[] = []
  for (let i = start; i <= end; i++) r.push(i)
  return r
}
</script>

<template>
  <div class="pagination">
    <span class="info">전체 {{ totalElements }}건 ({{ currentPage + 1 }} / {{ totalPages || 1 }} 페이지)</span>
    <div class="nav">
      <button type="button" :disabled="isFirst" @click="emit('go-page', 0)" aria-label="첫 페이지">«</button>
      <button type="button" :disabled="isFirst" @click="emit('go-page', currentPage - 1)" aria-label="이전">‹</button>
      <template v-for="p in range(Math.max(0, currentPage - 2), Math.min(totalPages - 1, currentPage + 2))" :key="p">
        <button type="button" :class="{ active: p === currentPage }" @click="emit('go-page', p)">{{ p + 1 }}</button>
      </template>
      <button type="button" :disabled="isLast" @click="emit('go-page', currentPage + 1)" aria-label="다음">›</button>
      <button type="button" :disabled="isLast" @click="emit('go-page', totalPages - 1)" aria-label="마지막">»</button>
    </div>
  </div>
</template>

<style scoped>
.pagination { display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 0.5rem; margin-top: 1rem; }
.info { font-size: 0.875rem; color: var(--muted); }
.nav { display: flex; gap: 0.25rem; }
.nav button { min-width: 2rem; padding: 0.4rem; border: 1px solid var(--border); background: var(--bg); border-radius: 4px; cursor: pointer; font: inherit; }
.nav button:hover:not(:disabled) { background: var(--hover); }
.nav button.active { background: var(--primary); color: white; border-color: var(--primary); }
.nav button:disabled { opacity: 0.5; cursor: not-allowed; }
</style>
