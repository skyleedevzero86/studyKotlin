<script setup lang="ts">
defineProps<{
  currentPage: number
  totalPages: number
  totalElements: number
  size: number
  isFirst: boolean
  isLast: boolean
}>()

const emit = defineEmits<{
  'go-page': [page: number]
}>()

function toNum(v: number): number {
  return typeof v === 'number' && !Number.isNaN(v) ? v : 0
}

function range(start: number, end: number): number[] {
  const r: number[] = []
  for (let i = start; i <= end; i++) r.push(i)
  return r
}
</script>

<template>
  <div class="pagination">
    <span class="info">
      전체 {{ toNum(totalElements) }}건 ({{ toNum(currentPage) + 1 }} / {{ Math.max(1, toNum(totalPages)) }} 페이지)
    </span>
    <div class="nav">
      <button
        type="button"
        :disabled="isFirst"
        @click="emit('go-page', 0)"
        aria-label="첫 페이지"
      >
        «
      </button>
      <button
        type="button"
        :disabled="isFirst"
        @click="emit('go-page', toNum(currentPage) - 1)"
        aria-label="이전"
      >
        ‹
      </button>
      <template
        v-for="p in range(Math.max(0, toNum(currentPage) - 2), Math.min(Math.max(0, toNum(totalPages) - 1), toNum(currentPage) + 2))"
        :key="p"
      >
        <button
          type="button"
          :class="{ active: p === toNum(currentPage) }"
          @click="emit('go-page', p)"
        >
          {{ p + 1 }}
        </button>
      </template>
      <button
        type="button"
        :disabled="isLast"
        @click="emit('go-page', toNum(currentPage) + 1)"
        aria-label="다음"
      >
        ›
      </button>
      <button
        type="button"
        :disabled="isLast"
        @click="emit('go-page', Math.max(0, toNum(totalPages) - 1))"
        aria-label="마지막 페이지"
      >
        »
      </button>
    </div>
  </div>
</template>

<style scoped>
.pagination {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-top: 1rem;
}
.info {
  font-size: 0.875rem;
  color: #e4e4e7;
}
.nav {
  display: flex;
  gap: 0.25rem;
  align-items: center;
}
.nav button {
  min-width: 2rem;
  padding: 0.4rem 0.5rem;
  border: 1px solid var(--border);
  background: var(--hover);
  color: #e4e4e7;
  border-radius: 4px;
  cursor: pointer;
  font: inherit;
}
.nav button:hover:not(:disabled) {
  background: var(--border);
  border-color: var(--muted);
}
.nav button.active {
  background: var(--primary);
  color: white;
  border-color: var(--primary);
  font-weight: 600;
}
.nav button:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}
</style>
