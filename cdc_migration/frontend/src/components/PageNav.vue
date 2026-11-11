<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps<{ page: number; totalPages: number; total: number }>();
const emit = defineEmits<{ go: [page: number] }>();

const pages = computed(() => {
  const start = Math.max(1, Math.min(props.page - 2, props.totalPages - 4));
  const end = Math.min(props.totalPages, start + 4);
  return Array.from({ length: end - start + 1 }, (_, i) => start + i);
});

function go(target: number) {
  if (target >= 1 && target <= props.totalPages && target !== props.page) emit('go', target);
}
</script>

<template>
  <nav class="pager" aria-label="쪽 이동">
    <span class="pager-total">전체 {{ total }}건 · {{ page }} / {{ totalPages }}쪽</span>
    <div class="pager-buttons">
      <button type="button" :disabled="page <= 1" aria-label="첫 쪽" @click="go(1)">«</button>
      <button type="button" :disabled="page <= 1" aria-label="이전 쪽" @click="go(page - 1)">‹</button>
      <button v-for="n in pages" :key="n" type="button" :class="{ current: n === page }" :aria-current="n === page ? 'page' : undefined" @click="go(n)">{{ n }}</button>
      <button type="button" :disabled="page >= totalPages" aria-label="다음 쪽" @click="go(page + 1)">›</button>
      <button type="button" :disabled="page >= totalPages" aria-label="마지막 쪽" @click="go(totalPages)">»</button>
    </div>
  </nav>
</template>
